package org.apache.isis.viewer.json.viewer.resources.domainobjects;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import javax.ws.rs.WebApplicationException;

import org.apache.isis.viewer.json.applib.JsonRepresentation;
import org.apache.isis.viewer.json.viewer.JsonApplicationException;
import org.junit.Test;

public class DomainResourceHelperTest_readBodyAsMap {

    private JsonRepresentation representation;

    @Test
    public void whenNull() throws Exception {
        representation = DomainResourceHelper.readBodyAsMap(null);
        
        assertThat(representation.isMap(), is(true));
        assertThat(representation.size(), is(0));
    }

    @Test
    public void whenEmptyString() throws Exception {
        representation = DomainResourceHelper.readBodyAsMap("");
        
        assertThat(representation.isMap(), is(true));
        assertThat(representation.size(), is(0));
    }

    @Test
    public void whenWhitespaceOnlyString() throws Exception {
        representation = DomainResourceHelper.readBodyAsMap(" \t ");
        
        assertThat(representation.isMap(), is(true));
        assertThat(representation.size(), is(0));
    }

    @Test
    public void emptyMap() throws Exception {
        representation = DomainResourceHelper.readBodyAsMap("{}");
        
        assertThat(representation.isMap(), is(true));
        assertThat(representation.size(), is(0));
    }

    @Test
    public void map() throws Exception {
        representation = DomainResourceHelper.readBodyAsMap("{\"foo\":\"bar\"}");
        
        assertThat(representation.isMap(), is(true));
        assertThat(representation.size(), is(1));
    }

    @Test(expected=JsonApplicationException.class)
    public void whenArray() throws Exception {
        DomainResourceHelper.readBodyAsMap("[]");
    }

}
