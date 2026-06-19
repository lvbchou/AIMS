package com.aims.mapper.product;

import com.aims.dto.product.CDInfoDTO;
import com.aims.entity.product.CD;
import org.springframework.stereotype.Component;

@Component
public class CDMapper extends ProductMapper<CDInfoDTO, CD> {

    public CDMapper(ProductCommonMapper commonMapper){
        super(commonMapper);
    }

    @Override
    public Class<CD> supportedType() {
        return CD.class;
    }

    @Override
    protected void mapTypeFields(CDInfoDTO dto, CD product) {
        dto.setProductType("CD");
        dto.setGenre(product.getGenre());
        dto.setReleaseDate(product.getReleaseDate());
        dto.setRecordLabel(product.getRecordLabel());
        dto.setArtists(product.getArtists());

        dto.setTracks(
                product.getTracks().stream()
                        .map(track -> new CDInfoDTO.TrackDTO(
                                track.getTrackTitle(),
                                track.getTrackLength()
                        ))
                        .toList()
        );
    }

    @Override
    protected CDInfoDTO createDTO() {
        return new CDInfoDTO();
    }
}
