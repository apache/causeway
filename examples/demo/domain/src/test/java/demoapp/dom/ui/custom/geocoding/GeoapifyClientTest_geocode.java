package demoapp.dom.ui.custom.geocoding;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;

import lombok.val;

import demoapp.dom.AppConfiguration;

class GeoapifyClientTest_geocode {

    @Test
    void happy_case() {

        // given
        val appConfiguration = new AppConfiguration();
        val client = new GeoapifyClient(appConfiguration);

        // when
        val latLng = client.geocode("38 Upper Montagu Street, Westminster W1H 1LJ, United Kingdom");

        // then
        val softly = new SoftAssertions();
        softly.assertThat(latLng.getLatitude()).isEqualTo("51.52016005");
        softly.assertThat(latLng.getLongitude()).isEqualTo("-0.16030636023550826");

        softly.assertAll();
    }
}
