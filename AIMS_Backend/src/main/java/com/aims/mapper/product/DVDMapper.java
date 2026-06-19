package com.aims.mapper.product;

import com.aims.dto.product.DVDInfoDTO;
import com.aims.entity.product.DVD;
import org.springframework.stereotype.Component;

@Component
public class DVDMapper extends ProductMapper<DVDInfoDTO, DVD> {

    public DVDMapper(ProductCommonMapper commonMapper){
        super(commonMapper);
    }

    @Override
    public Class<DVD> supportedType() {
        return DVD.class;
    }

    @Override
    protected void mapTypeFields(DVDInfoDTO dto, DVD product) {
        dto.setProductType("DVD");
        dto.setGenre(product.getGenre());
        dto.setDirector(product.getDirector());
        dto.setRuntime(product.getRuntime());
        dto.setStudio(product.getStudio());
        dto.setDiscType(product.getDiscType());
        dto.setLanguage(product.getLanguage());
        dto.setSubtitles(product.getSubtitles());
        dto.setReleaseDate(product.getReleaseDate());
    }

    @Override
    protected DVDInfoDTO createDTO() {
        return new DVDInfoDTO();
    }
}
