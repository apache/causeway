package demoapp.dom.ui.custom;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import lombok.val;

class LatLngTest {

    @Test
    void add() {
        val add = LatLng.add("51.753500", 1);
        Assertions.assertThat(add).isEqualTo("51.763500");
    }
}
