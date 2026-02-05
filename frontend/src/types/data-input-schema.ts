/* ----------------------------------
   Data Input Schema
----------------------------------- */

export type InputFieldType =
  | "text"
  | "number"
  | "textarea"
  | "select"
  | "file";

export type DataInputField = {
  id: string;
  label: string;
  type: InputFieldType;
  required: boolean;

  // Optional constraints
  placeholder?: string;
  min?: number;
  max?: number;
  options?: string[]; // for select
  fileTypes?: string[]; // for file
};