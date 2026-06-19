package com.aims.service.product.creator;

import com.aims.dto.product.NewspaperInfoDTO;
import com.aims.entity.product.Newspaper;
import com.aims.entity.product.Product;
import org.springframework.stereotype.Component;

@Component
public class NewspaperCreator extends ProductCreator<NewspaperInfoDTO> {

    @Override
    public String getSupportedType() {
        return "NEWSPAPER";
    }

    @Override
    protected Product buildProduct(NewspaperInfoDTO newspaper) {
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