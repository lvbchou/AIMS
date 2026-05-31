package com.aims.service.creator;

import com.aims.dto.product.NewspaperInfoDTO;
import com.aims.dto.product.ProductInfoDTO;
import com.aims.entity.product.Newspaper;
import com.aims.entity.product.Product;
import org.springframework.stereotype.Component;

@Component
public class NewspaperCreator extends ProductCreator {

    @Override
    protected Product buildProduct(ProductInfoDTO dto) {
        NewspaperInfoDTO newspaper = (NewspaperInfoDTO) dto;
        return new Newspaper(
                newspaper.getTitle(), newspaper.getCategory(), newspaper.getBarcode(), newspaper.getImage(),
                newspaper.getOriginalValue(), newspaper.getSellingPrice(), newspaper.getWeight(),
                newspaper.getDescription(), newspaper.getDimensions(), 0,
                newspaper.getPublisher(), newspaper.getPublicationDate(), newspaper.getLanguage(),
                newspaper.getEditorInChief(), newspaper.getIssueNumber(),
                newspaper.getPublicationFrequency(), newspaper.getISSN(), newspaper.getSections()
        );
    }
}