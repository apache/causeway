package org.apache.isis.viewer.json.applib;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class RepresentationTypeTest {

    @Test
    public void converts() {
        assertThat(RepresentationType.CAPABILITIES.getName(), is("capabilities"));
        assertThat(RepresentationType.HOME_PAGE.getName(), is("homePage"));
        assertThat(RepresentationType.TYPE_ACTION_PARAMETER.getName(), is("typeActionParameter"));
    }


    @Test
    public void parser_roundtrips() {
        final Parser<RepresentationType> parser = RepresentationType.parser();
        for (RepresentationType repType : RepresentationType.values()) {
            final String asString = parser.asString(repType);
            final RepresentationType roundtripped = parser.valueOf(asString);
            assertSame(roundtripped, repType);
        }
    }

}
