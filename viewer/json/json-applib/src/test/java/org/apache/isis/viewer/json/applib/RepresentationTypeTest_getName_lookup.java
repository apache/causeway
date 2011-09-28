package org.apache.isis.viewer.json.applib;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;

import javax.ws.rs.core.MediaType;

import org.junit.Test;

public class RepresentationTypeTest_getName_lookup {

    @Test
    public void selectedValues() {
        assertThat(RepresentationType.CAPABILITIES.getName(), is("capabilities"));
        assertThat(RepresentationType.HOME_PAGE.getName(), is("homePage"));
        assertThat(RepresentationType.TYPE_ACTION_PARAMETER.getName(), is("typeActionParameter"));
    }

    @Test
    public void roundtrip() {
        for (RepresentationType repType : RepresentationType.values()) {
            final String name = repType.getName();
            final RepresentationType lookup = RepresentationType.lookup(name);
            assertSame(repType, lookup);
        }
    }

    @Test
    public void lookup_whenUnknown() {
        assertThat(RepresentationType.lookup("foobar"), is(RepresentationType.GENERIC));
    }

    @Test
    public void lookup_whenNull() {
        assertThat(RepresentationType.lookup((String)null), is(RepresentationType.GENERIC));
    }

}
