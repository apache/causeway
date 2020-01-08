package org.wicketstuff.pdfjs;

import org.apache.wicket.util.lang.Objects;

public enum Scale {
    AUTOMATIC("auto"),
    ACTUAL_SIZE("page-actual"),
    PAGE_FIT("page-fit"),
    PAGE_WIDTH("page-width"),
    _0_50("0.50"),
    _0_75("0.75"),
    _1_00("1.00"),
    _1_25("1.25"),
    _1_50("1.50"),
    _2_00("2.00"),
    _3_00("3.00"),
    _4_00("4.00"),;

    private final String value;

    private Scale(final String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static Scale forValue(final String scaleValue) {
        if (scaleValue == null) {
            return null;
        }
        for (Scale scale : Scale.values()) {
            if (Objects.equal(scale.value, scaleValue)) {
                return scale;
            }
        }
        return null;
    }
}
