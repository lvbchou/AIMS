package com.aims.service.validator;

import com.aims.dto.product.NewspaperInfoDTO;
import com.aims.dto.product.ProductInfoDTO;
import com.aims.exception.InvalidProductInfoException;
import org.springframework.stereotype.Component;

@Component
public class NewspaperValidator extends ProductValidator {

    @Override
    protected void validateTypeFields(ProductInfoDTO dto) {
        NewspaperInfoDTO newspaper = (NewspaperInfoDTO) dto;

        if (newspaper.getEditorInChief() == null || newspaper.getEditorInChief().isBlank())
            throw new InvalidProductInfoException("Editor in chief is required for Newspaper");

        if (newspaper.getPublisher() == null || newspaper.getPublisher().isBlank())
            throw new InvalidProductInfoException("Publisher is required for Newspaper");

        if (newspaper.getPublicationDate() == null)
            throw new InvalidProductInfoException("Publication date is required for Newspaper");
    }
}