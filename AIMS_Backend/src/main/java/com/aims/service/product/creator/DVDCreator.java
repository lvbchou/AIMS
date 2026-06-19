package com.aims.service.product.creator;

import com.aims.dto.product.DVDInfoDTO;
import com.aims.entity.product.DVD;
import com.aims.entity.product.Product;
import org.springframework.stereotype.Component;

@Component
public class DVDCreator extends ProductCreator<DVDInfoDTO> {

    @Override
    public String getSupportedType() {
        return "DVD";
    }

    @Override
    protected Product buildProduct(DVDInfoDTO dvd) {
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