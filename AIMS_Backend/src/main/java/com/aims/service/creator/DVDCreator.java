package com.aims.service.creator;

import com.aims.dto.product.DVDInfoDTO;
import com.aims.dto.product.ProductInfoDTO;
import com.aims.entity.product.DVD;
import com.aims.entity.product.Product;
import org.springframework.stereotype.Component;

@Component
public class DVDCreator extends ProductCreator {

    @Override
    protected Product buildProduct(ProductInfoDTO dto) {
        DVDInfoDTO dvd = (DVDInfoDTO) dto;
        return new DVD(
                dvd.getTitle(), dvd.getCategory(), dvd.getBarcode(), dvd.getImage(),
                dvd.getOriginalValue(), dvd.getSellingPrice(), dvd.getWeight(),
                dvd.getDescription(), dvd.getDimensions(), 0,
                dvd.getGenre(), dvd.getReleaseDate(),
                dvd.getDiscType(), dvd.getDirector(), dvd.getRuntime(),
                dvd.getStudio(), dvd.getLanguage(), dvd.getSubtitles()
        );
    }
}