package demoapp.dom.featured.customui;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import lombok.val;

import demoapp.dom.featured.customui.latlng.LatLngUtils;

class GeocodeResponseUtilsTest {

    @Test
    void add() {
        val add = LatLngUtils.add("51.753500", 1);
        Assertions.assertThat(add).isEqualTo("51.763500");
    }
}
