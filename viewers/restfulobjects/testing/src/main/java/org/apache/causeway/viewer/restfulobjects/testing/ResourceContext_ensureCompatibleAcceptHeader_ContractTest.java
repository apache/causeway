/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.apache.causeway.viewer.restfulobjects.testing;

import java.util.Arrays;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.apache.causeway.applib.services.iactn.Interaction;
import org.apache.causeway.applib.services.iactnlayer.InteractionContext;
import org.apache.causeway.applib.services.iactnlayer.InteractionLayerTracker;
import org.apache.causeway.applib.services.iactnlayer.InteractionService;
import org.apache.causeway.core.metamodel._testing.MetaModelContext_forTesting;
import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.core.metamodel.specloader.SpecificationLoader;
import org.apache.causeway.core.security.authentication.InteractionContextFactory;
import org.apache.causeway.core.security.authentication.manager.AuthenticationManager;
import org.apache.causeway.viewer.restfulobjects.applib.RepresentationType;
import org.apache.causeway.viewer.restfulobjects.applib.RestfulResponse.HttpStatusCode;
import org.apache.causeway.viewer.restfulobjects.rendering.RestfulObjectsApplicationException;
import org.apache.causeway.viewer.restfulobjects.viewer.context.ResourceContext;
import org.apache.causeway.viewer.restfulobjects.viewer.resources.ResourceDescriptor;

import lombok.val;

public abstract class ResourceContext_ensureCompatibleAcceptHeader_ContractTest {

    /*sonar-ignore-on*/

    protected final InteractionContext iaContext = InteractionContextFactory.testing();

    HttpHeaders mockHttpHeaders = Mockito.mock(HttpHeaders.class);
    HttpServletRequest mockHttpServletRequest = Mockito.mock(HttpServletRequest.class);
    ServletContext mockServletContext = Mockito.mock(ServletContext.class);
    InteractionService mockInteractionService = Mockito.mock(InteractionService.class);
    Interaction mockInteraction = Mockito.mock(Interaction.class);
    SpecificationLoader mockSpecificationLoader = Mockito.mock(SpecificationLoader.class);
    WebApplicationContext webApplicationContext = Mockito.mock(WebApplicationContext.class);
    InteractionLayerTracker mockInteractionLayerTracker = Mockito.mock(InteractionLayerTracker.class);
    AuthenticationManager mockAuthenticationManager = Mockito.mock(AuthenticationManager.class);

    MetaModelContext metaModelContext;

    @BeforeEach
    public void setUp() throws Exception {

        metaModelContext = MetaModelContext_forTesting.builder()
                .specificationLoader(mockSpecificationLoader)
                .authentication(iaContext)
                .singleton(mockAuthenticationManager)
                .singleton(mockInteractionLayerTracker)
                .singleton(mockInteractionService)
                .build();

        Mockito
        .when(webApplicationContext.getBean(MetaModelContext.class))
        .thenReturn(metaModelContext);

        Mockito
        .when(mockServletContext.getAttribute("org.springframework.web.context.WebApplicationContext.ROOT"))
        .thenReturn(webApplicationContext);

        Mockito
        .when(mockHttpServletRequest.getServletContext())
        .thenReturn(mockServletContext);

        Mockito
        .when(mockHttpServletRequest.getQueryString())
        .thenReturn("");

    }

    @Test
    public void noop() throws Exception {
        final RepresentationType representationType = RepresentationType.HOME_PAGE;
        givenHttpHeadersGetAcceptableMediaTypesReturns(List.of(representationType.getJsonMediaType()));

        instantiateResourceContext(representationType);
    }

    @Test
    public void happyCase() throws Exception {
        final RepresentationType representationType = RepresentationType.HOME_PAGE;
        givenHttpHeadersGetAcceptableMediaTypesReturns(List.of(representationType.getJsonMediaType()));

        instantiateResourceContext(representationType);
    }

    @Test
    public void acceptGenericAndProduceGeneric() throws Exception {
        final RepresentationType representationType = RepresentationType.GENERIC;
        givenHttpHeadersGetAcceptableMediaTypesReturns(List.of(MediaType.APPLICATION_JSON_TYPE));

        instantiateResourceContext(representationType);
    }

    @Test
    public void acceptGenericAndProduceSpecific() throws Exception {
        final RepresentationType representationType = RepresentationType.HOME_PAGE;
        givenHttpHeadersGetAcceptableMediaTypesReturns(List.of(MediaType.APPLICATION_JSON_TYPE));

        instantiateResourceContext(representationType);
    }

    @Test
    public void nonMatching() throws Exception {
        final RepresentationType representationType = RepresentationType.HOME_PAGE;
        givenHttpHeadersGetAcceptableMediaTypesReturns(Arrays.<MediaType> asList(MediaType.APPLICATION_ATOM_XML_TYPE));

        try {
            instantiateResourceContext(representationType);
        } catch(RestfulObjectsApplicationException ex ) {
            assertThat(ex.getHttpStatusCode(), is(HttpStatusCode.NOT_ACCEPTABLE));
        }
    }

    @Test
    public void nonMatchingProfile() throws Exception {
        final RepresentationType representationType = RepresentationType.HOME_PAGE;
        givenHttpHeadersGetAcceptableMediaTypesReturns(List.of(RepresentationType.USER.getJsonMediaType()));

        try {
            instantiateResourceContext(representationType);
        } catch(RestfulObjectsApplicationException ex ) {
            assertThat(ex.getHttpStatusCode(), is(HttpStatusCode.NOT_ACCEPTABLE));
        }
    }

    @Test
    public void nonMatchingProfile_ignoreGeneric() throws Exception {
        final RepresentationType representationType = RepresentationType.HOME_PAGE;
        givenHttpHeadersGetAcceptableMediaTypesReturns(List.of(RepresentationType.USER.getJsonMediaType(), MediaType.APPLICATION_JSON_TYPE));

        try {
            instantiateResourceContext(representationType);
        } catch(RestfulObjectsApplicationException ex ) {
            assertThat(ex.getHttpStatusCode(), is(HttpStatusCode.NOT_ACCEPTABLE));
        }
    }

    @Test
    public void emptyList_isOK() throws Exception {
        final RepresentationType representationType = RepresentationType.HOME_PAGE;
        givenHttpHeadersGetAcceptableMediaTypesReturns(Arrays.<MediaType> asList());

        instantiateResourceContext(representationType);
    }

    private void givenHttpHeadersGetAcceptableMediaTypesReturns(final List<MediaType> mediaTypes) {
        Mockito
        .when(mockHttpHeaders.getAcceptableMediaTypes())
        .thenReturn(mediaTypes);
    }

    private ResourceContext instantiateResourceContext(
            final RepresentationType representationType) {

        val resourceDescriptor = ResourceDescriptor.of(representationType, null, null);

        return new ResourceContext(resourceDescriptor, mockHttpHeaders, null, null, null, null, null,
                mockHttpServletRequest, null, null,
                metaModelContext, null, null);
    }

    /*sonar-ignore-off*/
}
