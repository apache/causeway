package org.apache.isis.viewer.json.viewer.resources;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertThat;

import org.apache.isis.viewer.json.applib.JsonRepresentation;
import org.apache.isis.viewer.json.applib.util.JsonMapper;
import org.junit.Test;

public class ResourceAbstractTest_jsonFor {

    @Test
    public void simpleNoMessage() throws Exception {
        // given
        Exception ex = new Exception();

        // when
        String jsonFor = ResourceAbstract.jsonFor(ex);
        assertThat(jsonFor, is(not(nullValue())));
        JsonRepresentation jsonRepr = JsonMapper.instance().read(jsonFor, JsonRepresentation.class);
        
        // then
        assertThat(jsonRepr.getString("message"), is(nullValue()));
        assertThat(jsonRepr.getArray("stackTrace"), is(not(nullValue())));
        assertThat(jsonRepr.getArray("stackTrace").arraySize(), is(greaterThan(0)));
        assertThat(jsonRepr.getRepresentation("causedBy"), is(nullValue()));
    }

    @Test
    public void withMessage() throws Exception {
        // given
        Exception ex = new Exception("foobar");
        
        // when
        String jsonFor = ResourceAbstract.jsonFor(ex);
        assertThat(jsonFor, is(not(nullValue())));
        JsonRepresentation jsonRepr = JsonMapper.instance().read(jsonFor, JsonRepresentation.class);
        
        // then
        assertThat(jsonRepr.getString("message"), is(ex.getMessage()));
    }

    @Test
    public void withCause() throws Exception {
        // given
        Exception cause = new Exception("barfoo");
        Exception ex = new Exception("foobar", cause);
        
        // when
        String jsonFor = ResourceAbstract.jsonFor(ex);
        assertThat(jsonFor, is(not(nullValue())));
        JsonRepresentation jsonRepr = JsonMapper.instance().read(jsonFor, JsonRepresentation.class);
        
        // then
        assertThat(jsonRepr.getString("message"), is(ex.getMessage()));
        JsonRepresentation causedByRepr = jsonRepr.getRepresentation("causedBy");
        assertThat(causedByRepr, is(not(nullValue())));
        assertThat(causedByRepr.getString("message"), is(cause.getMessage()));
    }


}
