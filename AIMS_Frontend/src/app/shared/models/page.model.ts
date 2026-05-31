// models/page.model.ts
export interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;       // current page (0-based)
  size: number;
  first: boolean;
  last: boolean;
}