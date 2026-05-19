package com.aims.entity;
import jakarta.persistence.*; import lombok.*; import java.time.LocalDate;
@Entity @Table(name="dvd") @PrimaryKeyJoinColumn(name="product_id")
@Getter @Setter @NoArgsConstructor
public class DVD extends DiscProduct {
    @Column(name="disc_type") private String discType;
    @Column(name="director") private String director;
    @Column(name="runtime") private int runtime;
    @Column(name="studio") private String studio;
    @Column(name="language") private String language;
    @Column(name="subtitles") private String subtitles;
    public DVD(String title,String category,String barcode,String image,
               long originalValue,long sellingPrice,double weight,
               String description,String dimensions,int quantityInStock,
               String genre,LocalDate releaseDate,
               String discType,String director,int runtime,
               String studio,String language,String subtitles){
        super(title,category,barcode,image,originalValue,sellingPrice,weight,description,dimensions,quantityInStock,genre,releaseDate);
        this.discType=discType; this.director=director; this.runtime=runtime;
        this.studio=studio; this.language=language; this.subtitles=subtitles;
    }
}
