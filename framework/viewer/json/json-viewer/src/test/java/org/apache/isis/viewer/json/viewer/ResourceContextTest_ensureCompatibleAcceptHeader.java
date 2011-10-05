package org.apache.isis.viewer.json.viewer;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;

import org.apache.isis.viewer.json.applib.RepresentationType;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.Before;
import org.junit.Test;

public class ResourceContextTest_ensureCompatibleAcceptHeader {

    private HttpHeaders httpHeaders;
    private HttpServletRequest httpServletRequest;

    private Mockery context = new JUnit4Mockery();

    @Before
    public void setUp() throws Exception {
        httpHeaders = context.mock(HttpHeaders.class);
        httpServletRequest = context.mock(HttpServletRequest.class);
        context.checking(new Expectations() {
            {
                allowing(httpServletRequest).getParameterMap();
                will(returnValue(Collections.emptyMap()));
            }
        });
    }

    @Test
    public void noop() throws Exception {
        final RepresentationType representationType = RepresentationType.HOME_PAGE;
        givenHttpHeadersGetAcceptableMediaTypesReturns(Arrays.<MediaType>asList(representationType.getMediaType()));
        
        instantiateResourceContext(representationType);
    }

    @Test
    public void happyCase() throws Exception {
        final RepresentationType representationType = RepresentationType.HOME_PAGE;
        givenHttpHeadersGetAcceptableMediaTypesReturns(Arrays.<MediaType>asList(representationType.getMediaType()));
        
        instantiateResourceContext(representationType);
    }

    @Test
    public void acceptGenericAndProduceGeneric() throws Exception {
        RepresentationType representationType = RepresentationType.GENERIC;
        givenHttpHeadersGetAcceptableMediaTypesReturns(Arrays.<MediaType>asList(MediaType.APPLICATION_JSON_TYPE));
        
        instantiateResourceContext(representationType);
    }

    @Test
    public void acceptGenericAndProduceSpecific() throws Exception {
        final RepresentationType representationType = RepresentationType.HOME_PAGE;
        givenHttpHeadersGetAcceptableMediaTypesReturns(Arrays.<MediaType>asList(MediaType.APPLICATION_JSON_TYPE));
        
        instantiateResourceContext(representationType);
    }

    @Test(expected = JsonApplicationException.class)
    public void nonMatching() throws Exception {
        RepresentationType representationType = RepresentationType.HOME_PAGE;
        givenHttpHeadersGetAcceptableMediaTypesReturns(Arrays.<MediaType>asList(MediaType.APPLICATION_ATOM_XML_TYPE));
        
        instantiateResourceContext(representationType);
    }

    @Test(expected = JsonApplicationException.class)
    public void nonMatchingProfile() throws Exception {
        RepresentationType representationType = RepresentationType.HOME_PAGE;
        givenHttpHeadersGetAcceptableMediaTypesReturns(Arrays.<MediaType>asList(RepresentationType.USER.getMediaType()));
        
        instantiateResourceContext(representationType);
    }

    @Test(expected = JsonApplicationException.class)
    public void nonMatchingProfile_ignoreGeneric() throws Exception {
        RepresentationType representationType = RepresentationType.HOME_PAGE;
        givenHttpHeadersGetAcceptableMediaTypesReturns(Arrays.<MediaType>asList(RepresentationType.USER.getMediaType(), MediaType.APPLICATION_JSON_TYPE));

        instantiateResourceContext(representationType);
    }

    @Test(expected = JsonApplicationException.class)
    public void emptyList() throws Exception {
        RepresentationType representationType = RepresentationType.HOME_PAGE;
        givenHttpHeadersGetAcceptableMediaTypesReturns(Arrays.<MediaType>asList());

        instantiateResourceContext(representationType);
    }

    private void givenHttpHeadersGetAcceptableMediaTypesReturns(final List<MediaType> mediaTypes) {
        context.checking(new Expectations() {
            {
                one(httpHeaders).getAcceptableMediaTypes();
                will(returnValue(mediaTypes));
            }
        });
    }

    private ResourceContext instantiateResourceContext(final RepresentationType representationType) {
        return new ResourceContext(representationType, httpHeaders, null, null, httpServletRequest, null, null, null, null, null, null, null);
    }


}
