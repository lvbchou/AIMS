package com.aims.service.validator;

import com.aims.dto.product.DVDInfoDTO;
import com.aims.dto.product.ProductInfoDTO;
import com.aims.exception.InvalidProductInfoException;
import org.springframework.stereotype.Component;

@Component
public class DVDValidator extends ProductValidator {

    @Override
    protected void validateTypeFields(ProductInfoDTO dto) {
        DVDInfoDTO dvd = (DVDInfoDTO) dto;

        if (dvd.getDiscType() == null || dvd.getDiscType().isBlank())
            throw new InvalidProductInfoException("Disc type is required for DVD");
        if (!dvd.getDiscType().equalsIgnoreCase("BLU-RAY") &&
                !dvd.getDiscType().equalsIgnoreCase("HD-DVD"))
            throw new InvalidProductInfoException("Disc type must be Blu-ray or HD-DVD");

        if (dvd.getDirector() == null || dvd.getDirector().isBlank())
            throw new InvalidProductInfoException("Director is required for DVD");

        if (dvd.getRuntime() == null)
            throw new InvalidProductInfoException("Runtime is required for DVD");
        if (dvd.getRuntime() <= 0)
            throw new InvalidProductInfoException("Runtime must be positive");

        if (dvd.getStudio() == null || dvd.getStudio().isBlank())
            throw new InvalidProductInfoException("Studio is required for DVD");

        if (dvd.getLanguage() == null || dvd.getLanguage().isBlank())
            throw new InvalidProductInfoException("Language is required for DVD");

        if (dvd.getSubtitles() == null || dvd.getSubtitles().isBlank())
            throw new InvalidProductInfoException("Subtitles is required for DVD");
    }
}