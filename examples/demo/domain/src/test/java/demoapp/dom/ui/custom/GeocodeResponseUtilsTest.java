package demoapp.dom.ui.custom;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import lombok.val;

import demoapp.dom.ui.custom.latlng.LatLngUtils;

class GeocodeResponseUtilsTest {

    @Test
    void add() {
        val add = LatLngUtils.add("51.753500", 1);
        Assertions.assertThat(add).isEqualTo("51.763500");
    }
}
