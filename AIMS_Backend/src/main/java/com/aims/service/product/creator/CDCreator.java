package com.aims.service.product.creator;

import com.aims.dto.product.CDInfoDTO;
import com.aims.entity.product.CD;
import com.aims.entity.product.Product;
import com.aims.entity.product.Track;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CDCreator extends ProductCreator<CDInfoDTO> {

    @Override
    public String getSupportedType() {
        return "CD";
    }

    @Override
    protected Product buildProduct(CDInfoDTO cdDto) {
        CD cd = new CD(
                cdDto.getTitle(), cdDto.getCategory(), cdDto.getBarcode(), cdDto.getImage(),
                cdDto.getOriginalValue(), cdDto.getSellingPrice(), cdDto.getWeight(),
                cdDto.getDescription(), cdDto.getDimensions(), 0,
                cdDto.getGenre(), cdDto.getReleaseDate(),
                cdDto.getArtists(), cdDto.getRecordLabel()
        );

        if (cdDto.getTracks() != null) {
            List<Track> tracks = cdDto.getTracks().stream()
                    .map(t -> new Track(t.getTrackTitle(), t.getTrackLength(), cd))
                    .toList();
            cd.setTracks(tracks);
        }

        return cd;
    }
}