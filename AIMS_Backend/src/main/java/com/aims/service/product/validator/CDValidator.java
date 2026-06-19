/**
 * LSP VIOLATION:
 * validateTypeFields(ProductInfoDTO dto) declares it accepts any ProductInfoDTO
 * (the parent type), but immediately downcasts to a specific subtype
 * (e.g., BookInfoDTO book = (BookInfoDTO) dto).
 *
 * Impact: If a caller substitutes a different ProductInfoDTO subtype, a
 * ClassCastException is thrown at runtime. The subclass cannot safely substitute
 * the base ProductValidator for all valid inputs of the declared type.
 *
 * Improvement: Use generics — abstract class ProductValidator<T extends ProductInfoDTO>
 * with abstract void validateTypeFields(T dto). Each subtype validator is typed as
 * ProductValidator<BookInfoDTO>, ProductValidator<CDInfoDTO>, etc.
 * Type correctness is enforced at compile time; no runtime downcast is needed.
 */
package com.aims.service.product.validator;

import com.aims.dto.product.CDInfoDTO;
import com.aims.dto.product.ProductInfoDTO;
import com.aims.exception.InvalidProductInfoException;
import org.springframework.stereotype.Component;

@Component
public class CDValidator extends ProductValidator<CDInfoDTO> {

    public CDValidator(ProductCommonValidator commonValidator) {
        super(commonValidator);
    }

    @Override
    public String getSupportedType() { return "CD"; }

    @Override
    protected void validateTypeFields(CDInfoDTO cd) {
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