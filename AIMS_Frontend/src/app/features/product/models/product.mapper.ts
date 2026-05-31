import {
  Product, ProductBase,
  DvdProduct, CdProduct, BookProduct, NewspaperProduct,
  Track
} from './product.model';
import { ProductType } from './product-type.enum';

// ── flat (backend) → nested (frontend) ───────────────────────────────────────
export function mapToProduct(raw: any): Product {
  const base: ProductBase = {
    productId:       raw.productId,
    productType:     raw.productType,
    title:           raw.title,
    category:        raw.category,
    barcode:         raw.barcode,
    image:           raw.image ?? raw.imageUrl ?? '',
    originalValue:   raw.originalValue,
    sellingPrice:    raw.sellingPrice,
    weight:          raw.weight,
    status:          raw.status,
    dimensions:      raw.dimensions,
    description:     raw.description,
    quantityInStock: raw.quantityInStock,
  };

  switch (raw.productType as ProductType) {

    case ProductType.DVD:
      return {
        ...base,
        productType: ProductType.DVD,
        typeDetails: {
          discType:    raw.discType    ?? '',
          director:    raw.director    ?? '',
          runtime:     raw.runtime     ?? 0,
          studio:      raw.studio      ?? '',
          language:    raw.language    ?? '',
          subtitles:   raw.subtitles   ?? '',
          genre:       raw.genre,
          releaseDate: raw.releaseDate,
        },
      } as DvdProduct;

    case ProductType.CD:
      return {
        ...base,
        productType: ProductType.CD,
        typeDetails: {
          artists:     raw.artists     ?? [],
          recordLabel: raw.recordLabel ?? '',
          genre:       raw.genre       ?? '',
          releaseDate: raw.releaseDate,
          tracks:      (raw.tracks ?? []).map((t: any): Track => ({
            title:  t.trackTitle  ?? t.title  ?? '',
            length: t.trackLength ?? t.length ?? '',
          })),
        },
      } as CdProduct;

    case ProductType.BOOK:
      return {
        ...base,
        productType: ProductType.BOOK,
        typeDetails: {
          author:          raw.author          ?? '',
          coverType:       raw.coverType       ?? '',
          pages:           raw.pages,
          genre:           raw.genre,
          publisher:       raw.publisher       ?? '',
          publicationDate: raw.publicationDate ?? '',
          language:        raw.language,
        },
      } as BookProduct;

    case ProductType.NEWSPAPER:
      return {
        ...base,
        productType: ProductType.NEWSPAPER,
        typeDetails: {
          editorInChief:        raw.editorInChief        ?? '',
          issueNumber:          raw.issueNumber,
          publicationFrequency: raw.publicationFrequency,
          issn:                 raw.ISSN ?? raw.issn,     // normalize ISSN
          publisher:            raw.publisher             ?? '',
          publicationDate:      raw.publicationDate       ?? '',
          language:             raw.language,
          sections:             raw.sections,
        },
      } as NewspaperProduct;

    default:
      throw new Error(`Unknown product type: ${raw.productType}`);
  }
}

// ── nested (frontend) → flat (backend payload) ────────────────────────────────
export function mapToPayload(product: Product): any {
  const { typeDetails, ...base } = product as any;

  const payload: any = {
    ...base,
    ...typeDetails,
  };

  // Normalize CD tracks: frontend dùng title/length → backend dùng trackTitle/trackLength
  if (product.productType === ProductType.CD && typeDetails?.tracks) {
    payload.tracks = typeDetails.tracks.map((t: Track) => ({
      trackTitle:  t.title,
      trackLength: t.length,
    }));
  }

  // Normalize Newspaper ISSN: frontend dùng issn → backend dùng ISSN
  if (product.productType === ProductType.NEWSPAPER) {
    payload.ISSN = typeDetails?.issn ?? null;
    delete payload.issn;
  }

  return payload;
}