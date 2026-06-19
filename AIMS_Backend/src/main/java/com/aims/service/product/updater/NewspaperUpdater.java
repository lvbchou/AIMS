package com.aims.service.product.updater;

import com.aims.dto.product.NewspaperInfoDTO;
import com.aims.entity.product.Newspaper;
import org.springframework.stereotype.Component;

@Component
public class NewspaperUpdater extends ProductUpdater<Newspaper, NewspaperInfoDTO> {

    public NewspaperUpdater(ProductCommonUpdater productCommonUpdater){
        super(productCommonUpdater);
    }

    @Override
    public String getSupportedType(){
        return "NEWSPAPER";
    }

    @Override
    protected void updateTypeFields(Newspaper newspaper, NewspaperInfoDTO dto){
        newspaper.setEditorInChief(dto.getEditorInChief());
        newspaper.setIssueNumber(dto.getIssueNumber());
        newspaper.setPublicationFrequency(dto.getPublicationFrequency());
        newspaper.setISSN(newspaper.getISSN());
        newspaper.setPublisher(dto.getPublisher());
        newspaper.setPublicationDate(dto.getPublicationDate());
        newspaper.setLanguage(dto.getLanguage());

        if (dto.getSections() != null) {
            newspaper.getSections().clear();
            newspaper.getSections().addAll(dto.getSections());
        }
    }
}
