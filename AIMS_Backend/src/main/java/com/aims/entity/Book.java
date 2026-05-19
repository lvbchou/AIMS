package com.aims.entity;
import jakarta.persistence.*; import lombok.*; import java.time.LocalDate;
@Entity @Table(name="book") @PrimaryKeyJoinColumn(name="product_id")
@Getter @Setter @NoArgsConstructor
public class Book extends PrintableProduct {
    @Column(name="author") private String author;
    @Column(name="cover_type") private String coverType;
    @Column(name="pages") private int pages;
    @Column(name="genre") private String genre;
    public Book(String title,String category,String barcode,String image,
                long originalValue,long sellingPrice,double weight,
                String description,String dimensions,int quantityInStock,
                String publisher,LocalDate publicationDate,String language,
                String author,String coverType,int pages,String genre){
        super(title,category,barcode,image,originalValue,sellingPrice,weight,description,dimensions,quantityInStock,publisher,publicationDate,language);
        this.author=author; this.coverType=coverType; this.pages=pages; this.genre=genre;
    }
}
