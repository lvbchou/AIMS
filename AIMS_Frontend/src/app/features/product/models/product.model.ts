import { ProductType } from './product-type.enum';

export interface ProductSummary {
  productId: number;
  title: string;
  productType: ProductType;
  sellingPrice: number;
  image?: string;
  imageUrl?: string;
}

// ── Base ──────────────────────────────────────────────────────────────────────
export interface ProductBase {
  productId: number;
  productType: ProductType;
  title: string;
  category: string;
  barcode: string;
  image: string;
  imageUrl: string;
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
  tracklist: Track[];
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
  ISSN?: string;
  issn?: string;
  publisher: string;
  publicationDate: string;
  language?: string;
  sections?: string[];
}

// ── Full product union ────────────────────────────────────────────────────────
export interface DvdProduct extends ProductBase {
  type: ProductType.DVD;
  typeDetails: DvdDetails;
}

export interface CdProduct extends ProductBase {
  type: ProductType.CD;
  typeDetails: CdDetails;
}

export interface BookProduct extends ProductBase {
  type: ProductType.BOOK;
  typeDetails: BookDetails;
}

export interface NewspaperProduct extends ProductBase {
  type: ProductType.NEWSPAPER;
  typeDetails: NewspaperDetails;
}

export type Product = DvdProduct | CdProduct | BookProduct | NewspaperProduct;