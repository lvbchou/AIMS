package com.aims.mapper;

import com.aims.dto.ProductInfoDTO;
import com.aims.entity.*;

import java.util.List;

/**
 * ProductMapper – static utility class that converts Product entity → ProductInfoDTO.
 *
 * Used by ProductService.viewProduct() to map the retrieved entity before returning to controller.
 *
 * Coupling  : Data coupling – receives a Product object, returns a ProductInfoDTO.
 *             No shared state, no side effects.
 * Cohesion  : Functional – single responsibility: entity → DTO conversion.
 */
public class ProductMapper {

    private ProductMapper() {
        // Utility class – no instantiation
    }

    /**
     * Convert any Product subtype (Book, DVD, CD, Newspaper) to ProductInfoDTO.
     * Common fields are mapped first, then subtype-specific fields.
     */
    public static ProductInfoDTO toDTO(Product product) {
        if (product == null) return null;

        ProductInfoDTO dto = new ProductInfoDTO();

        // ── Common fields ─────────────────────────────────────────────
        dto.setProductId(String.valueOf(product.getProductId()));
        dto.setTitle(product.getTitle());
        dto.setCategory(product.getCategory());
        dto.setBarcode(product.getBarcode());
        dto.setImage(product.getImage());
        dto.setStatus(product.getStatus());
        dto.setOriginalValue(product.getOriginalValue());
        dto.setSellingPrice(product.getSellingPrice());
        dto.setWeight(product.getWeight());
        dto.setDescription(product.getDescription());
        dto.setDimensions(product.getDimensions());
        dto.setQuantityInStock(product.getQuantityInStock());

        // ── Subtype-specific fields ───────────────────────────────────
        if (product instanceof Book book) {
            dto.setProductType("BOOK");
            dto.setAuthor(book.getAuthor());
            dto.setPublisher(book.getPublisher());
            dto.setPublicationDate(book.getPublicationDate());
            dto.setLanguage(book.getLanguage());
            dto.setCoverType(book.getCoverType());
            dto.setPages(book.getPages());
            dto.setGenre(book.getGenre());

        } else if (product instanceof DVD dvd) {
            dto.setProductType("DVD");
            dto.setDiscType(dvd.getDiscType());
            dto.setDirector(dvd.getDirector());
            dto.setRuntime(dvd.getRuntime());
            dto.setStudio(dvd.getStudio());
            dto.setLanguage(dvd.getLanguage());
            dto.setSubtitles(dvd.getSubtitles());
            dto.setGenre(dvd.getGenre());
            dto.setReleaseDate(dvd.getReleaseDate());

        } else if (product instanceof CD cd) {
            dto.setProductType("CD");
            dto.setArtists(cd.getArtists());
            dto.setRecordLabel(cd.getRecordLabel());
            dto.setGenre(cd.getGenre());
            dto.setReleaseDate(cd.getReleaseDate());

            // Map tracks
            if (cd.getTracks() != null) {
                List<ProductInfoDTO.TrackDTO> trackDTOs = cd.getTracks().stream()
                        .map(t -> new ProductInfoDTO.TrackDTO(t.getTrackTitle(), t.getTrackLength()))
                        .toList();
                dto.setTracks(trackDTOs);
            }

        } else if (product instanceof Newspaper np) {
            dto.setProductType("NEWSPAPER");
            dto.setEditorInChief(np.getEditorInChief());
            dto.setIssueNumber(np.getIssueNumber());
            dto.setPublicationFrequency(np.getPublicationFrequency());
            dto.setPublisher(np.getPublisher());
            dto.setPublicationDate(np.getPublicationDate());
            dto.setLanguage(np.getLanguage());
            dto.setSections(np.getSections());

        } else {
            // Fallback for unknown subtypes
            dto.setProductType(product.getClass().getSimpleName().toUpperCase());
        }

        return dto;
    }
}