package com.aims.service.validator;

import com.aims.dto.product.CDInfoDTO;
import com.aims.dto.product.ProductInfoDTO;
import com.aims.exception.InvalidProductInfoException;
import org.springframework.stereotype.Component;

@Component
public class CDValidator extends ProductValidator {

    @Override
    protected void validateTypeFields(ProductInfoDTO dto) {
        CDInfoDTO cd = (CDInfoDTO) dto;

        if (cd.getArtists() == null || cd.getArtists().isEmpty())
            throw new InvalidProductInfoException("Artists is required for CD");

        if (cd.getRecordLabel() == null || cd.getRecordLabel().isBlank())
            throw new InvalidProductInfoException("Record label is required for CD");

        if (cd.getGenre() == null || cd.getGenre().isBlank())
            throw new InvalidProductInfoException("Genre is required for CD");

        if (cd.getTracks() == null || cd.getTracks().isEmpty())
            throw new InvalidProductInfoException("CD must have at least one track");

        for (CDInfoDTO.TrackDTO track : cd.getTracks()) {
            if (track.getTrackTitle() == null || track.getTrackTitle().isBlank())
                throw new InvalidProductInfoException("Track title is required");
            if (track.getTrackLength() == null || track.getTrackLength().isBlank())
                throw new InvalidProductInfoException("Track length is required");
        }
    }
}