import React, { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { motion, AnimatePresence } from "framer-motion";
import { Search, Sparkles } from "lucide-react";
import { Button } from "@/components/ui/button";
import type { Tool } from "@/types/report";
import { toolsApi } from "@/services";

/* ----------------------------------
   Types
----------------------------------- */

type ToolCardProps = {
  tool: Tool;
  onClick: () => void;
};

/* ----------------------------------
   Tool Card
----------------------------------- */

const ToolCard: React.FC<ToolCardProps> = ({ tool, onClick }) => {
  return (
    <motion.div
      initial={{ opacity: 0, y: 20 }}
      animate={{ opacity: 1, y: 0 }}
      whileHover={{ y: -8, scale: 1.02 }}
      whileTap={{ scale: 0.98 }}
      onClick={onClick}
      className="bg-card/80 backdrop-blur-xl rounded-2xl border border-border p-6 cursor-pointer shadow-soft hover:shadow-hover transition-all group"
      data-testid={`tool-card-${tool.id}`}
    >
      <div className="flex items-start justify-between mb-4">
        <div className="text-3xl">{getCategoryIcon(tool.category)}</div>
        <span className="text-xs font-medium px-3 py-1 rounded-full bg-primary/10">
          {tool.category}
        </span>
      </div>

      <h3 className="text-lg font-semibold mb-2 group-hover:text-primary">
        {tool.title}
      </h3>

      <p className="text-sm text-muted-foreground line-clamp-2">
        {tool.description}
      </p>

      <div className="mt-4 flex items-center gap-2 text-primary text-sm font-medium">
        <Sparkles size={16} />
        Generate Report
      </div>
    </motion.div>
  );
};

function getCategoryIcon(category: string): string {
  const icons: Record<string, string> = {
    Finance: "💰",
    Investment: "📈",
    Tax: "📋",
    Startup: "🚀",
    Banking: "🏦",
    Insurance: "🛡️",
  };
  return icons[category] || "🧮";
}

/* ----------------------------------
   Tool Selection Page
----------------------------------- */

const ToolSelection: React.FC = () => {
  const navigate = useNavigate();

  const [tools, setTools] = useState<Tool[]>([]);
  const [filteredTools, setFilteredTools] = useState<Tool[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [selectedCategory, setSelectedCategory] = useState<string>("All");
  const [searchQuery, setSearchQuery] = useState<string>("");

  useEffect(() => {
    fetchTools();
  }, []);

  useEffect(() => {
    filterTools();
  }, [tools, selectedCategory, searchQuery]);

  /* ----------------------------------
     Data
  ----------------------------------- */

  const fetchTools = async (): Promise<void> => {
    try {
      const data = await toolsApi.getAll(); 
      setTools(data);
      setFilteredTools(data);
    } catch (err) {
      console.error("Failed to fetch tools", err);
    } finally {
      setLoading(false);
    }
  };

  const filterTools = (): void => {
    let filtered = [...tools];

    if (selectedCategory !== "All") {
      filtered = filtered.filter(
        (tool) => tool.category === selectedCategory
      );
    }

    if (searchQuery) {
      const q = searchQuery.toLowerCase();
      filtered = filtered.filter(
        (tool) =>
          tool.title.toLowerCase().includes(q) ||
          tool.description.toLowerCase().includes(q)
      );
    }

    setFilteredTools(filtered);
  };

  const categories: string[] = [
    "All",
    ...Array.from(new Set(tools.map((t) => t.category))),
  ];

  const handleToolSelect = (tool: Tool): void => {
    navigate(`/dashboard/create-report/wizard/${tool.id}`);
  };

  /* ----------------------------------
     UI
  ----------------------------------- */

  if (loading) {
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

  return (
    <div data-testid="tool-selection-page">
      <h1 className="text-4xl font-bold mb-2">Choose Your Calculator</h1>
      <p className="text-muted-foreground mb-8">
        Select a tool to generate your AI-powered report
      </p>

      {/* Search */}
      <div className="relative mb-6">
        <Search className="absolute left-4 top-1/2 -translate-y-1/2 text-muted-foreground" />
        <input
          value={searchQuery}
          onChange={(e) => setSearchQuery(e.target.value)}
          placeholder="Search calculators..."
          className="w-full pl-12 pr-4 py-3 rounded-lg border"
        />
      </div>

      {/* Categories */}
      <div className="flex gap-2 mb-8 flex-wrap">
        {categories.map((cat) => (
          <Button
            key={cat}
            onClick={() => setSelectedCategory(cat)}
            variant={selectedCategory === cat ? "default" : "secondary"}
            size="sm"
          >
            {cat}
          </Button>
        ))}
      </div>

      {/* Grid */}
      <AnimatePresence mode="wait">
        {filteredTools.length === 0 ? (
          <p className="text-center text-muted-foreground">
            No calculators found
          </p>
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {filteredTools.map((tool) => (
              <ToolCard
                key={tool.id}
                tool={tool}
                onClick={() => handleToolSelect(tool)}
              />
            ))}
          </div>
        )}
      </AnimatePresence>
    </div>
  );
};

export default ToolSelection;