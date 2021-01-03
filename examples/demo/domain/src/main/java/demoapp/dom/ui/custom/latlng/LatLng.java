package demoapp.dom.ui.custom.latlng;

import lombok.Data;
import lombok.Getter;

@Data
public class LatLng {
    @Getter
    private final String latitude;
    @Getter
    private final String longitude;
}
