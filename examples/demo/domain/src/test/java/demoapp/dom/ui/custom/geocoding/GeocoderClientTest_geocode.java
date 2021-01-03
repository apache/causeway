package demoapp.dom.ui.custom.geocoding;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;

import lombok.val;

class GeocoderClientTest_geocode {

    @Test
    void happy_case() {

        val client = new GeocoderClient();
        val latLng = client.geocode("OX1 3DP,UK");

        val softly = new SoftAssertions();
        softly.assertThat(latLng.getLatitude()).isEqualTo("51.753769");
        softly.assertThat(latLng.getLongitude()).isEqualTo("-1.256271");

        softly.assertAll();
    }
}
