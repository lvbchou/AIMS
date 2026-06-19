package com.aims.service.product.updater;

import com.aims.dto.product.CDInfoDTO;
import com.aims.entity.product.CD;
import com.aims.entity.product.Track;
import org.springframework.stereotype.Component;

@Component
public class CDUpdater extends ProductUpdater<CD, CDInfoDTO> {

    public CDUpdater(ProductCommonUpdater productCommonUpdater){
        super(productCommonUpdater);
    }

    @Override
    public String getSupportedType(){
        return "CD";
    }

    @Override
    protected void updateTypeFields(CD cd, CDInfoDTO dto) {
        cd.setRecordLabel(dto.getRecordLabel());
        cd.setGenre(dto.getGenre());
        cd.setReleaseDate(dto.getReleaseDate());

        if (dto.getArtists() != null) {
            cd.getArtists().clear();
            cd.getArtists().addAll(dto.getArtists());
        }

        if (dto.getTracks() != null) {
            cd.getTracks().clear();
            dto.getTracks().forEach(t ->
                    cd.getTracks().add(new Track(t.getTrackTitle(), t.getTrackLength(), cd))
            );
        }
    }
}
