package org.apache.isis.viewer.json.applib;

import static org.junit.Assert.assertSame;

import org.junit.Test;

public class RepresentationTypeTest_parser {

    @Test
    public void roundtrips() {
        final Parser<RepresentationType> parser = RepresentationType.parser();
        for (RepresentationType repType : RepresentationType.values()) {
            final String asString = parser.asString(repType);
            final RepresentationType roundtripped = parser.valueOf(asString);
            assertSame(roundtripped, repType);
        }
    }

}
