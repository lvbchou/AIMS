package com.aims.mapper.product;

import com.aims.dto.product.NewspaperInfoDTO;
import com.aims.entity.product.Newspaper;
import org.springframework.stereotype.Component;

@Component
public class NewspaperMapper extends ProductMapper<NewspaperInfoDTO, Newspaper> {

    public NewspaperMapper(ProductCommonMapper commonMapper){
        super(commonMapper);
    }

    @Override
    public Class<Newspaper> supportedType() {
        return Newspaper.class;
    }

    @Override
    protected void mapTypeFields(NewspaperInfoDTO dto, Newspaper product) {
        dto.setProductType("NEWSPAPER");
        dto.setPublisher(product.getPublisher());
        dto.setPublicationDate(product.getPublicationDate());
        dto.setLanguage(product.getLanguage());
        dto.setEditorInChief(product.getEditorInChief());
        dto.setIssueNumber(product.getIssueNumber());
        dto.setPublicationFrequency(product.getPublicationFrequency());
        dto.setISSN(product.getISSN());
        dto.setSections(product.getSections());
    }

    @Override
    protected NewspaperInfoDTO createDTO() {
        return new NewspaperInfoDTO();
    }
}
