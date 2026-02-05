import React, { useEffect, useState, useRef } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { motion, AnimatePresence } from "framer-motion";
import { Check, ChevronLeft, ChevronRight, Loader2, Upload, X, FileText } from "lucide-react";
import { reportsApi, toolsApi, filesApi, type FileUploadResponse } from "@/services/apiClient";
import { toast } from "react-toastify";

import {
  REPORT_STEPS,
  REPORT_TYPES,
  AUDIENCES,
  PURPOSES,
  TONES,
  DEPTHS,
} from "@/config/create-report.config";
import { Button } from "@/components/ui/button";
import type { Tool } from "@/types/report";

/* ---------------------------------- */
/* Types */
/* ---------------------------------- */

type FormDataState = {
  industry: string;
  reportType: string;
  audience: string;
  purpose: string;
  tone: string;
  depth: string;
  wizardData: WizardData;
  inputs: Record<string, string>;
  uploadedFiles: FileUploadResponse[];
};

type StepIndicatorProps = {
  steps: readonly string[];
  currentStep: number;
};

type WizardData = {
  notes?: string;
};

/* ---------------------------------- */
/* Step Indicator */
/* ---------------------------------- */

const StepIndicator: React.FC<StepIndicatorProps> = ({
  steps,
  currentStep,
}) => (
  <div className="flex items-center justify-between mb-8">
    {steps.map((step, index) => {
      const isCompleted = index < currentStep;
      const isActive = index === currentStep;

      return (
        <React.Fragment key={step}>
          <div className="flex flex-col items-center">
            <motion.div
              className={`w-10 h-10 rounded-full flex items-center justify-center font-medium transition-all
                ${
                  isCompleted || isActive
                    ? "text-white bg-[linear-gradient(135deg,#7ddcff_0%,#b6c9ff_35%,#f7c4d7_65%,#fde2b8_100%)]"
                    : "bg-muted text-muted-foreground"
                }`}
            >
              {isCompleted ? <Check size={18} /> : index + 1}
            </motion.div>
            <span className="text-xs mt-2 text-center">{step}</span>
          </div>

          {index < steps.length - 1 && (
            <div
              className={`flex-1 h-0.5 mx-2 transition-all ${
                isCompleted
                  ? "bg-[linear-gradient(135deg,#7ddcff_0%,#b6c9ff_35%,#f7c4d7_65%,#fde2b8_100%)]"
                  : "bg-border"
              }`}
            />
          )}
        </React.Fragment>
      );
    })}
  </div>
);

/* ---------------------------------- */
/* Main Component */
/* ---------------------------------- */

const CreateReport: React.FC = () => {
  const navigate = useNavigate();
  const { toolId } = useParams<{ toolId: string }>();
  const fileInputRef = useRef<HTMLInputElement>(null);

  const [tool, setTool] = useState<Tool | null>(null);
  const [loadingTool, setLoadingTool] = useState(true);
  const [currentStep, setCurrentStep] = useState(0);
  const [loading, setLoading] = useState(false);
  const [uploading, setUploading] = useState(false);

  const [formData, setFormData] = useState<FormDataState>({
    industry: "",
    reportType: "",
    audience: "",
    purpose: "",
    tone: "Professional",
    depth: "Comprehensive",
    wizardData: {},
    inputs: {},
    uploadedFiles: [],
  });

  useEffect(() => {
    if (!toolId) return;

    async function loadTool(id: string) {
      try {
        const fetchedTool = await toolsApi.getTool(id);
        setTool(fetchedTool);
        if (fetchedTool.industry) {
          setFormData(prev => ({ ...prev, industry: fetchedTool.industry || "" }));
        }
      } catch (err) {
        console.error(err);
        toast.error("Failed to load tool");
        setTool(null);
      } finally {
        setLoadingTool(false);
      }
    }

    loadTool(toolId);
  }, [toolId]);

  const update = <K extends keyof FormDataState>(
    key: K,
    value: FormDataState[K]
  ) => setFormData((prev) => ({ ...prev, [key]: value }));

  const handleFileUpload = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const files = e.target.files;
    if (!files || files.length === 0) return;

    setUploading(true);
    try {
      const uploaded = await Promise.all(
        Array.from(files).map((file) => filesApi.upload(file))
      );
      setFormData((prev) => ({
        ...prev,
        uploadedFiles: [...prev.uploadedFiles, ...uploaded],
      }));
      toast.success(`${uploaded.length} file(s) uploaded successfully!`);
    } catch (err: any) {
      toast.error(err.message || "Failed to upload files");
    } finally {
      setUploading(false);
      if (fileInputRef.current) {
        fileInputRef.current.value = "";
      }
    }
  };

  const removeUploadedFile = (fileId: string) => {
    setFormData((prev) => ({
      ...prev,
      uploadedFiles: prev.uploadedFiles.filter((f) => f.id !== fileId),
    }));
  };

  const canProceed = (): boolean => {
    switch (currentStep) {
      case 0:
        return Boolean(formData.industry);
      case 1:
        return Boolean(formData.reportType);
      case 2:
        return Boolean(formData.audience && formData.purpose);
      case 3:
        return Boolean(formData.tone && formData.depth);
      case 4:
        return true;
      default:
        return false;
    }
  };

  const handleGenerate = async () => {
    if (!tool) return;
    setLoading(true);

    try {
      // Create the report
      const report = await reportsApi.create({
        tool_id: tool.id,
        title: `${tool.title} Report`,
        industry: formData.industry,
        report_type: formData.reportType,
        audience: formData.audience,
        purpose: formData.purpose,
        tone: formData.tone,
        depth: formData.depth,
        wizard_data: {
          ...formData.wizardData,
          uploadedFileIds: formData.uploadedFiles.map((f) => f.id),
        },
        inputs: formData.inputs,
      });

      // Generate the report with AI
      await reportsApi.generate(report.id);
      
      toast.success("Report generated successfully!");
      navigate(`/dashboard/my-reports`, { replace: true });
    } catch (err: any) {
      console.error("Report generation failed:", err);
      toast.error(err.message || "Failed to generate report");
    } finally {
      setLoading(false);
    }
  };

  if (loadingTool) {
    return (
      <div className="flex items-center justify-center h-96">
        <motion.div
          animate={{ rotate: 360 }}
          transition={{ duration: 1, repeat: Infinity, ease: "linear" }}
          className="w-12 h-12 border-4 border-primary border-t-transparent rounded-full"
        />
      </div>
    );
  }

  if (!tool) {
    return (
      <div className="max-w-2xl mx-auto py-20 text-center">
        <p className="text-muted-foreground mb-4">
          No tool selected. Please choose a tool to create a report.
        </p>
        <Button onClick={() => navigate("/dashboard/create-report")}>
          Go to Create Report
        </Button>
      </div>
    );
  }

  // TO do task : Refactor each step into separate components for better readability

  return (
    <div className="max-w-4xl mx-auto" data-testid="create-report">
      <h1 className="text-3xl font-bold mb-2">{tool.title}</h1>
      <p className="text-muted-foreground mb-8">{tool.description}</p>

      <div className="bg-card rounded-2xl border p-8">
        <StepIndicator steps={REPORT_STEPS} currentStep={currentStep} />

        <AnimatePresence mode="wait">
          <motion.div
            key={currentStep}
            initial={{ opacity: 0, x: 20 }}
            animate={{ opacity: 1, x: 0 }}
            exit={{ opacity: 0, x: -20 }}
            className="min-h-[300px]"
          >
            {/* STEP 0 – Industry */}
            {currentStep === 0 && (
              <div className="grid grid-cols-2 gap-4">
                {Object.keys(REPORT_TYPES).map((industry) => (
                  <Button
                    key={industry}
                    onClick={() => update("industry", industry)}
                    variant={
                      formData.industry === industry ? "selected" : "outline"
                    }
                    className="p-4"
                  >
                    {industry}
                  </Button>
                ))}
              </div>
            )}

            {/* STEP 1 – Report Type */}
            {currentStep === 1 && (
              <div className="grid grid-cols-2 gap-4">
                {(REPORT_TYPES[formData.industry] ?? []).map((type) => (
                  <Button
                    key={type}
                    onClick={() => update("reportType", type)}
                    variant={
                      formData.reportType === type ? "selected" : "outline"
                    }
                    className="p-4"
                  >
                    {type}
                  </Button>
                ))}
              </div>
            )}

            {/* STEP 2 – Audience & Purpose */}
            {currentStep === 2 && (
              <>
                <h3 className="text-sm font-semibold text-muted-foreground mb-3">
                  Select Audience
                </h3>
                <div className="grid grid-cols-3 gap-3 mb-6">
                  {AUDIENCES.map((a) => (
                    <Button
                      key={a}
                      onClick={() => update("audience", a)}
                      variant={
                        formData.audience === a ? "selected" : "outline"
                      }
                      className="p-3"
                    >
                      {a}
                    </Button>
                  ))}
                </div>

                <h3 className="text-sm font-semibold text-muted-foreground mb-3">
                  Select Purpose
                </h3>
                <div className="grid grid-cols-3 gap-3">
                  {PURPOSES.map((p) => (
                    <Button
                      key={p}
                      onClick={() => update("purpose", p)}
                      variant={
                        formData.purpose === p ? "selected" : "outline"
                      }
                      className="p-3"
                    >
                      {p}
                    </Button>
                  ))}
                </div>
              </>
            )}

            {/* STEP 3 – Tone & Depth */}
            {currentStep === 3 && (
              <>
                <div className="mb-8">
                  <h3 className="text-sm font-semibold text-muted-foreground mb-3">
                    Select Tone
                  </h3>
                  <div className="grid grid-cols-2 gap-4">
                    {TONES.map((t) => (
                      <Button
                        key={t}
                        onClick={() => update("tone", t)}
                        variant={formData.tone === t ? "selected" : "outline"}
                        className="p-4"
                      >
                        {t}
                      </Button>
                    ))}
                  </div>
                </div>

                <div>
                  <h3 className="text-sm font-semibold text-muted-foreground mb-3">
                    Select Depth
                  </h3>
                  <div className="grid grid-cols-2 gap-4">
                    {DEPTHS.map((d) => (
                      <Button
                        key={d}
                        onClick={() => update("depth", d)}
                        variant={formData.depth === d ? "selected" : "outline"}
                        className="p-4"
                      >
                        {d}
                      </Button>
                    ))}
                  </div>
                </div>
              </>
            )}

            {/* STEP 4 – Data Inputs */}
            {currentStep === 4 && (
              <div className="space-y-6">
                <div>
                  <h2 className="text-2xl font-bold mb-2">Data Inputs</h2>
                  <p className="text-muted-foreground">
                    Upload files or add notes for your {tool?.title} report
                  </p>
                </div>

                {/* File Upload Area */}
                <div className="space-y-4">
                  <h3 className="text-sm font-semibold text-muted-foreground">
                    Upload Data Files
                  </h3>
                  <p className="text-xs text-muted-foreground">
                    Supported formats: PDF, Excel (.xlsx, .xls), Word (.docx), CSV, JSON, TXT
                  </p>

                  <div
                    onClick={() => fileInputRef.current?.click()}
                    className="border-2 border-dashed border-border rounded-xl p-8 text-center cursor-pointer hover:border-primary/50 hover:bg-primary/5 transition-all"
                  >
                    {uploading ? (
                      <div className="flex flex-col items-center gap-2">
                        <Loader2 className="h-8 w-8 animate-spin text-primary" />
                        <span className="text-sm text-muted-foreground">Uploading...</span>
                      </div>
                    ) : (
                      <div className="flex flex-col items-center gap-2">
                        <Upload className="h-8 w-8 text-muted-foreground" />
                        <span className="font-medium">Click to upload files</span>
                        <span className="text-xs text-muted-foreground">or drag and drop</span>
                      </div>
                    )}
                  </div>

                  <input
                    ref={fileInputRef}
                    type="file"
                    accept=".csv,.xlsx,.xls,.pdf,.txt,.json,.docx"
                    multiple
                    onChange={handleFileUpload}
                    className="hidden"
                  />

                  {/* Uploaded Files List */}
                  {formData.uploadedFiles.length > 0 && (
                    <div className="space-y-2">
                      <h4 className="text-sm font-medium">Uploaded Files:</h4>
                      {formData.uploadedFiles.map((file) => (
                        <div
                          key={file.id}
                          className="flex items-center justify-between p-3 bg-muted/50 rounded-lg"
                        >
                          <div className="flex items-center gap-3">
                            <FileText className="h-5 w-5 text-primary" />
                            <div>
                              <p className="font-medium text-sm">{file.filename}</p>
                              <p className="text-xs text-muted-foreground">
                                {file.dataSummary} • {Math.round(file.fileSize / 1024)}KB
                              </p>
                            </div>
                          </div>
                          <Button
                            variant="ghost"
                            size="sm"
                            onClick={() => removeUploadedFile(file.id)}
                          >
                            <X className="h-4 w-4" />
                          </Button>
                        </div>
                      ))}
                    </div>
                  )}
                </div>

                {/* Notes Section */}
                <div className="space-y-2">
                  <h3 className="text-sm font-semibold text-muted-foreground">
                    Additional Notes (Optional)
                  </h3>
                  <textarea
                    value={formData.wizardData.notes ?? ""}
                    onChange={(e) =>
                      update("wizardData", {
                        ...formData.wizardData,
                        notes: e.target.value,
                      })
                    }
                    className="w-full h-40 px-4 py-3 rounded-lg border font-sans text-sm resize-none"
                    placeholder="Add any notes, assumptions, or context for the report..."
                  />
                </div>
              </div>
            )}

            {/* STEP 5 – Review & Generate */}
            {currentStep === 5 && (
              <motion.div
                initial={{ opacity: 0, x: 20 }}
                animate={{ opacity: 1, x: 0 }}
                exit={{ opacity: 0, x: -20 }}
                className="space-y-6"
              >
                <h2 className="text-2xl font-bold text-card-foreground mb-4">
                  Review & Generate
                </h2>

                <div className="bg-muted/50 rounded-lg p-6 space-y-4">
                  <div>
                    <p className="text-sm text-muted-foreground">Tool</p>
                    <p className="text-lg font-semibold text-card-foreground">
                      {tool?.title}
                    </p>
                  </div>

                  <div className="grid grid-cols-2 gap-4">
                    <div>
                      <p className="text-sm text-muted-foreground">Industry</p>
                      <p className="font-medium text-card-foreground">
                        {formData.industry}
                      </p>
                    </div>
                    <div>
                      <p className="text-sm text-muted-foreground">Report Type</p>
                      <p className="font-medium text-card-foreground">
                        {formData.reportType}
                      </p>
                    </div>
                    <div>
                      <p className="text-sm text-muted-foreground">Audience</p>
                      <p className="font-medium text-card-foreground">
                        {formData.audience}
                      </p>
                    </div>
                    <div>
                      <p className="text-sm text-muted-foreground">Purpose</p>
                      <p className="font-medium text-card-foreground">
                        {formData.purpose}
                      </p>
                    </div>
                    <div>
                      <p className="text-sm text-muted-foreground">Tone</p>
                      <p className="font-medium text-card-foreground">
                        {formData.tone}
                      </p>
                    </div>
                    <div>
                      <p className="text-sm text-muted-foreground">Depth</p>
                      <p className="font-medium text-card-foreground">
                        {formData.depth}
                      </p>
                    </div>
                  </div>
                </div>

                <div className="bg-amber-50 border border-amber-200 rounded-lg p-4">
                  <p className="text-sm text-amber-800">
                    ⚡ This will use 1 credit to generate your report
                  </p>
                </div>
              </motion.div>
            )}
          </motion.div>
        </AnimatePresence>

        {/* Footer */}
        <div className="flex justify-between mt-8 pt-6 border-t">
          <Button
            variant="secondary"
            onClick={() => setCurrentStep((s) => Math.max(0, s - 1))}
            disabled={currentStep === 0}
          >
            <ChevronLeft className="h-4 w-4 mr-2" />
            Back
          </Button>

          <Button
            onClick={() =>
              currentStep === REPORT_STEPS.length - 1
                ? handleGenerate()
                : setCurrentStep((s) => s + 1)
            }
            // disabled={!canProceed() || loading}
            size="lg"
            className="gap-2"
          >
            {loading ? (
              <>
                <Loader2 className="h-4 w-4 animate-spin" />
                Generating
              </>
            ) : (
              <>
                {currentStep === REPORT_STEPS.length - 1
                  ? "Generate Report"
                  : "Next"}
                <ChevronRight className="h-4 w-4" />
              </>
            )}
          </Button>
        </div>
      </div>
    </div>
  );
};

export default CreateReport;