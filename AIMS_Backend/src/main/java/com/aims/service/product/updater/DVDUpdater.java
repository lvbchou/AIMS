package com.aims.service.product.updater;

import com.aims.dto.product.DVDInfoDTO;
import com.aims.entity.product.DVD;
import org.springframework.stereotype.Component;

@Component
public class DVDUpdater extends ProductUpdater<DVD, DVDInfoDTO> {

    public DVDUpdater(ProductCommonUpdater productCommonUpdater){
        super(productCommonUpdater);
    }

    @Override
    public String getSupportedType(){
        return "DVD";
    }

    @Override
    protected void updateTypeFields(DVD dvd, DVDInfoDTO dto) {
        dvd.setDiscType(dto.getDiscType());
        dvd.setDirector(dto.getDirector());
        dvd.setRuntime(dto.getRuntime());
        dvd.setStudio(dto.getStudio());
        dvd.setLanguage(dto.getLanguage());
        dvd.setSubtitles(dto.getSubtitles());
        dvd.setGenre(dto.getGenre());
        dvd.setReleaseDate(dto.getReleaseDate());
    }
}
