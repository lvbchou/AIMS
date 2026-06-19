import { ProductType } from './product-type.enum';

export interface ProductSummary {
  productId: number;
  title: string;
  productType: ProductType;
  sellingPrice: number;
  image?: string;
  quantityInStock?: number;
}

// ── Base ──────────────────────────────────────────────────────────────────────
export interface ProductBase {
  productId: number;
  productType: ProductType;
  title: string;
  category: string;
  barcode: string;
  image: string;
  originalValue: number;
  sellingPrice: number;
  weight: number;
  status: string;
  dimensions: string;
  description: string;
  quantityInStock?: number;
}

export interface Track {
  title: string;
  length: string; // format "mm:ss"
}

// ── Category details ──────────────────────────────────────────────────────────
export interface DvdDetails {
  discType: string;
  director: string;
  runtime: number;
  studio: string;
  language: string;
  subtitles: string;
  genre?: string;
  releaseDate?: string;
}

export interface CdDetails {
  artists: string[];
  recordLabel: string;
  genre: string;
  releaseDate?: string;
  tracks: Track[];
}

export interface BookDetails {
  author: string;
  coverType: string;
  pages?: number;
  genre?: string;
  publisher: string;
  publicationDate: string;
  language?: string;
}

export interface NewspaperDetails {
  editorInChief: string;
  issueNumber?: string;
  publicationFrequency?: string;
  issn?: string;
  publisher: string;
  publicationDate: string;
  language?: string;
  sections?: string[];
}

// ── Full product union ────────────────────────────────────────────────────────
export interface DvdProduct extends ProductBase {
  productType: ProductType.DVD;
  typeDetails: DvdDetails;
}

export interface CdProduct extends ProductBase {
  productType: ProductType.CD;
  typeDetails: CdDetails;
}

export interface BookProduct extends ProductBase {
  productType: ProductType.BOOK;
  typeDetails: BookDetails;
}

export interface NewspaperProduct extends ProductBase {
  productType: ProductType.NEWSPAPER;
  typeDetails: NewspaperDetails;
}

export type Product = DvdProduct | CdProduct | BookProduct | NewspaperProduct;
