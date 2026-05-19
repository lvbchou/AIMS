package com.aims.entity;
import jakarta.persistence.*; import lombok.*; import java.time.LocalDate;
@MappedSuperclass @Getter @Setter @NoArgsConstructor
public abstract class PrintableProduct extends Product {
    @Column(name="publisher") private String publisher;
    @Column(name="publication_date") private LocalDate publicationDate;
    @Column(name="language") private String language;
    public PrintableProduct(String title,String category,String barcode,String image,
                            long originalValue,long sellingPrice,double weight,
                            String description,String dimensions,int quantityInStock,
                            String publisher,LocalDate publicationDate,String language){
        super(title,category,barcode,image,originalValue,sellingPrice,weight,description,dimensions,quantityInStock);
        this.publisher=publisher; this.publicationDate=publicationDate; this.language=language;
    }
}
