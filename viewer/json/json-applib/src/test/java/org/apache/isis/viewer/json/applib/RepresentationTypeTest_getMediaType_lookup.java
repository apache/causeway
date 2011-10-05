package org.apache.isis.viewer.json.applib;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;

import javax.ws.rs.core.MediaType;

import org.junit.Test;

public class RepresentationTypeTest_getMediaType_lookup {

    @Test
    public void roundtrip() {
        for (RepresentationType repType : RepresentationType.values()) {
            final MediaType mediaType = repType.getMediaType();
            final RepresentationType lookup = RepresentationType.lookup(mediaType);
            assertSame(repType, lookup);
        }
    }

    @Test
    public void whenUnknown() {
        assertThat(RepresentationType.lookup(MediaType.APPLICATION_ATOM_XML_TYPE), is(RepresentationType.GENERIC));
    }

    @Test
    public void whenNull() {
        assertThat(RepresentationType.lookup((MediaType)null), is(RepresentationType.GENERIC));
    }

    @Test
    public void getMediaTypeProfile() {
        assertThat(RepresentationType.CAPABILITIES.getMediaTypeWithProfile(), is("http://restfulobjects.org/profiles/capabilities"));
        assertThat(RepresentationType.GENERIC.getMediaTypeWithProfile(), is(nullValue()));
    }

}
