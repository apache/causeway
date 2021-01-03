package demoapp.dom.ui.custom.vm;

import lombok.Data;
import lombok.Getter;

import demoapp.dom.ui.custom.latlng.LatLng;

@Data
public class BoundingBox {
    @Getter
    private final LatLng minimum;
    @Getter
    private final LatLng maximum;

    public String getMinimumLatitude() {
        return minimum.getLatitude();
    }

    public String getMinimumLongitude() {
        return minimum.getLongitude();
    }

    public String getMaximumLatitude() {
        return maximum.getLatitude();
    }

    public String getMaximumLongitude() {
        return maximum.getLongitude();
    }

    public final String toUrl(String divider) {
        return getMinimumLongitude() + divider + this.getMinimumLatitude() + divider + getMaximumLongitude() + divider + getMaximumLatitude();
    }

    public final String toUrl() {
        return toUrl("%2C");
    }
}
