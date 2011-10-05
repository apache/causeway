package org.apache.isis.viewer.json.viewer;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;

import org.apache.isis.viewer.json.applib.RepresentationType;
import org.apache.isis.viewer.json.viewer.util.MapUtils;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.Before;
import org.junit.Test;

public class ResourceContextTest_ensureCompatibleAcceptHeader {

    private HttpHeaders httpHeaders;
    private ResourceContext resourceContext;

    private Mockery context = new JUnit4Mockery();

    @Before
    public void setUp() throws Exception {
        httpHeaders = context.mock(HttpHeaders.class);
        resourceContext = new ResourceContext(httpHeaders, null, null, null, null, null, null, null, null, null, null);
    }

    @Test
    public void noop() throws Exception {
        givenHttpHeadersGetAcceptableMediaTypesReturns(Arrays.<MediaType>asList(RepresentationType.HOME_PAGE.getMediaType()));
        resourceContext.ensureCompatibleAcceptHeader(null);
    }

    @Test
    public void happyCase() throws Exception {
        final RepresentationType homePage = RepresentationType.HOME_PAGE;
        givenHttpHeadersGetAcceptableMediaTypesReturns(Arrays.<MediaType>asList(homePage.getMediaType()));
        resourceContext.ensureCompatibleAcceptHeader(homePage);
    }

    @Test
    public void acceptGenericAndProduceGeneric() throws Exception {
        givenHttpHeadersGetAcceptableMediaTypesReturns(Arrays.<MediaType>asList(MediaType.APPLICATION_JSON_TYPE));
        resourceContext.ensureCompatibleAcceptHeader(RepresentationType.GENERIC);
    }

    @Test
    public void acceptGenericAndProduceSpecific() throws Exception {
        final RepresentationType homePage = RepresentationType.HOME_PAGE;
        givenHttpHeadersGetAcceptableMediaTypesReturns(Arrays.<MediaType>asList(MediaType.APPLICATION_JSON_TYPE));
        resourceContext.ensureCompatibleAcceptHeader(homePage);
    }

    @Test(expected = JsonApplicationException.class)
    public void nonMatching() throws Exception {
        givenHttpHeadersGetAcceptableMediaTypesReturns(Arrays.<MediaType>asList(MediaType.APPLICATION_ATOM_XML_TYPE));
        resourceContext.ensureCompatibleAcceptHeader(RepresentationType.HOME_PAGE);
    }

    @Test(expected = JsonApplicationException.class)
    public void nonMatchingProfile() throws Exception {
        givenHttpHeadersGetAcceptableMediaTypesReturns(Arrays.<MediaType>asList(RepresentationType.USER.getMediaType()));
        resourceContext.ensureCompatibleAcceptHeader(RepresentationType.HOME_PAGE);
    }

    @Test(expected = JsonApplicationException.class)
    public void nonMatchingProfile_ignoreGeneric() throws Exception {
        givenHttpHeadersGetAcceptableMediaTypesReturns(Arrays.<MediaType>asList(RepresentationType.USER.getMediaType(), MediaType.APPLICATION_JSON_TYPE));
        resourceContext.ensureCompatibleAcceptHeader(RepresentationType.HOME_PAGE);
    }

    @Test(expected = JsonApplicationException.class)
    public void emptyList() throws Exception {
        givenHttpHeadersGetAcceptableMediaTypesReturns(Arrays.<MediaType>asList());
        resourceContext.ensureCompatibleAcceptHeader(RepresentationType.HOME_PAGE);
    }

    private void givenHttpHeadersGetAcceptableMediaTypesReturns(final List<MediaType> mediaTypes) {
        context.checking(new Expectations() {
            {
                one(httpHeaders).getAcceptableMediaTypes();
                will(returnValue(mediaTypes));
            }
        });
    }
}
