package org.apache.isis.viewer.json.viewer.resources;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertThat;

import javax.ws.rs.core.Response;

import org.apache.isis.viewer.json.applib.JsonRepresentation;
import org.apache.isis.viewer.json.applib.RestfulResponse.HttpStatusCode;
import org.apache.isis.viewer.json.applib.util.JsonMapper;
import org.apache.isis.viewer.json.viewer.JsonApplicationException;
import org.apache.isis.viewer.json.viewer.JsonApplicationExceptionMapper;
import org.junit.Before;
import org.junit.Test;

public class JsonApplicationExceptionMapperTest {

    private JsonApplicationExceptionMapper exceptionMapper;

    @Before
    public void setUp() throws Exception {
        exceptionMapper = new JsonApplicationExceptionMapper();
    }
    
    @Test
    public void simpleNoMessage() throws Exception {
        
        // given
        HttpStatusCode status = HttpStatusCode.BAD_REQUEST;
        JsonApplicationException ex = JsonApplicationException.create(status);

        // when
        Response response = exceptionMapper.toResponse(ex);
        
        // then
        assertThat(HttpStatusCode.lookup(response.getStatus()), is(status));
        assertThat(response.getMetadata().get("Warning"), is(nullValue()));
        
        // and then
        String entity = (String) response.getEntity();
        assertThat(entity, is(not(nullValue())));
        JsonRepresentation jsonRepr = JsonMapper.instance().read(entity, JsonRepresentation.class);
        
        // then
        assertThat(jsonRepr.getString("message"), is(nullValue()));
        assertThat(jsonRepr.getArray("stackTrace"), is(not(nullValue())));
        assertThat(jsonRepr.getArray("stackTrace").size(), is(greaterThan(0)));
        assertThat(jsonRepr.getRepresentation("causedBy"), is(nullValue()));
    }

    @Test
    public void entity_withMessage() throws Exception {
        
        // givens
        JsonApplicationException ex = JsonApplicationException.create(HttpStatusCode.BAD_REQUEST, "foobar");
        
        // when
        Response response = exceptionMapper.toResponse(ex);
        
        // then
        assertThat((String)response.getMetadata().get("Warning").get(0), is(ex.getMessage()));
        
        // and then
        String entity = (String) response.getEntity();
        assertThat(entity, is(not(nullValue())));
        JsonRepresentation jsonRepr = JsonMapper.instance().read(entity, JsonRepresentation.class);
        
        // then
        assertThat(jsonRepr.getString("message"), is(ex.getMessage()));
    }

    @Test
    public void entity_withCause() throws Exception {
        // given
        Exception cause = new Exception("barfoo");
        JsonApplicationException ex = JsonApplicationException.create(HttpStatusCode.BAD_REQUEST, cause, "foobar");
        
        // when
        Response response = exceptionMapper.toResponse(ex);
        String entity = (String) response.getEntity();
        assertThat(entity, is(not(nullValue())));
        JsonRepresentation jsonRepr = JsonMapper.instance().read(entity, JsonRepresentation.class);
        
        // then
        assertThat(jsonRepr.getString("message"), is(ex.getMessage()));
        JsonRepresentation causedByRepr = jsonRepr.getRepresentation("causedBy");
        assertThat(causedByRepr, is(not(nullValue())));
        assertThat(causedByRepr.getString("message"), is(cause.getMessage()));
    }


}
