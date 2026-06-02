/**
 * LSP VIOLATION (applyUpdate method):
 * applyUpdate(ProductInfoDTO dto) accepts the abstract parent type but
 * immediately casts to a concrete subtype
 * (e.g., BookInfoDTO book = (BookInfoDTO) dto).
 *
 * Impact: Passing the wrong DTO subtype causes a ClassCastException at runtime.
 * A caller holding a Product reference cannot safely invoke applyUpdate()
 * with any arbitrary ProductInfoDTO.
 *
 * Improvement: Apply the same generic solution as validators:
 * abstract class Product<T extends ProductInfoDTO> with abstract void applyUpdate(T dto).
 * Each entity subtype declares its own T. Alternatively, add an explicit type-check
 * that throws a descriptive IllegalArgumentException instead of an opaque ClassCastException.
 */
package com.aims.entity.product;

import com.aims.dto.product.BookInfoDTO;
import com.aims.dto.product.ProductInfoDTO;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "book")
@PrimaryKeyJoinColumn(name = "product_id")
@Getter
@Setter
@NoArgsConstructor
public class Book extends PrintableProduct {

    @Column(name = "author")
    private String author;

    @Column(name = "cover_type")
    private String coverType;

    @Column(name = "pages")
    private Integer pages;

    @Column(name = "genre")
    private String genre;

    public Book(String title, String category, String barcode, String image,
                Long originalValue, Long sellingPrice, Double weight,
                String description, String dimensions, Integer quantityInStock,
                String publisher, LocalDate publicationDate, String language,
                String author, String coverType, Integer pages, String genre) {
        super(title, category, barcode, image, originalValue, sellingPrice,
                weight, description, dimensions, quantityInStock,
                publisher, publicationDate, language);
        this.author = author;
        this.coverType = coverType;
        this.pages = pages;
        this.genre = genre;
    }

    @Override
    public void applyUpdate(ProductInfoDTO dto) {
        BookInfoDTO book = (BookInfoDTO) dto;
        this.author = book.getAuthor();
        this.coverType = book.getCoverType();
        this.pages = book.getPages();
        this.genre = book.getGenre();
        this.setPublisher(book.getPublisher());
        this.setPublicationDate(book.getPublicationDate());
        this.setLanguage(book.getLanguage());
    }

    @Override
    public ProductInfoDTO toDTO() {
        BookInfoDTO dto = new BookInfoDTO();

        baseDTO(dto);

        dto.setProductType("BOOK");
        dto.setAuthor(author);
        dto.setPublisher(getPublisher());
        dto.setPublicationDate(getPublicationDate());
        dto.setPages(pages);
        dto.setCoverType(coverType);
        dto.setLanguage(getLanguage());
        dto.setGenre(genre);

        return dto;
    }
}