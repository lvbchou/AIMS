package com.aims.entity;
import jakarta.persistence.*; import lombok.*; import java.time.LocalDate;
@MappedSuperclass @Getter @Setter @NoArgsConstructor
public abstract class DiscProduct extends Product {
    @Column(name="genre") private String genre;
    @Column(name="release_date") private LocalDate releaseDate;
    public DiscProduct(String title,String category,String barcode,String image,
                       long originalValue,long sellingPrice,double weight,
                       String description,String dimensions,int quantityInStock,
                       String genre,LocalDate releaseDate){
        super(title,category,barcode,image,originalValue,sellingPrice,weight,description,dimensions,quantityInStock);
        this.genre=genre; this.releaseDate=releaseDate;
    }
}
