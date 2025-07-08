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
package org.apache.causeway.viewer.restfulobjects.rendering.context;

import java.util.Collections;
import java.util.List;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import org.springframework.web.context.WebApplicationContext;

import org.apache.causeway.applib.services.iactnlayer.InteractionLayerTracker;
import org.apache.causeway.applib.services.iactnlayer.InteractionService;
import org.apache.causeway.core.metamodel._testing.MetaModelContext_forTesting;
import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.core.metamodel.specloader.SpecificationLoader;
//import org.apache.causeway.core.security.authentication.manager.AuthenticationManager;
import org.apache.causeway.viewer.restfulobjects.applib.JsonRepresentation;
import org.apache.causeway.viewer.restfulobjects.applib.RestfulRequest.RequestParameter;

class ResourceContext_getArg_Test {

    private HttpServletRequest mockHttpServletRequest;
    private ServletContext mockServletContext;
    private InteractionService mockInteractionService;
    private InteractionLayerTracker mockInteractionLayerTracker;
    //private AuthenticationManager mockAuthenticationManager;
    private SpecificationLoader mockSpecificationLoader;
    private WebApplicationContext webApplicationContext;

    private ResourceContext resourceContext;
    private MetaModelContext metaModelContext;

    @BeforeEach
    void setUp() throws Exception {

        metaModelContext = MetaModelContext_forTesting.builder()
                .specificationLoader(mockSpecificationLoader)
                .singleton(mockInteractionService)
      //          .singleton(mockAuthenticationManager)
                .singleton(mockInteractionLayerTracker)
                .build();

        mockHttpServletRequest = Mockito.mock(HttpServletRequest.class);
        mockServletContext = Mockito.mock(ServletContext.class);
        mockInteractionService = Mockito.mock(InteractionService.class);
        mockInteractionLayerTracker = Mockito.mock(InteractionLayerTracker.class);
        //mockAuthenticationManager = Mockito.mock(AuthenticationManager.class);
        mockSpecificationLoader = Mockito.mock(SpecificationLoader.class);
        webApplicationContext = Mockito.mock(WebApplicationContext.class);

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

        Mockito
        .when(mockHttpServletRequest.getHeaderNames())
        .thenReturn(Collections.enumeration(List.of()));

    }

    @Test
    void whenArgExists() throws Exception {
        final String queryString = JsonRepresentation.newMap("x-ro-page", "123").asUrlEncoded();
        resourceContext = ResourceContext.forTesting(queryString, mockHttpServletRequest);
        final Integer arg = ResourceContext.arg(resourceContext.queryStringAsJsonRepr(), RequestParameter.PAGE);
        assertThat(arg, equalTo(123));
    }

    @Test
    void whenArgDoesNotExist() throws Exception {
        final String queryString = JsonRepresentation.newMap("xxx", "123").asUrlEncoded();
        resourceContext = ResourceContext.forTesting(queryString, mockHttpServletRequest);
        final Integer arg = ResourceContext.arg(resourceContext.queryStringAsJsonRepr(), RequestParameter.PAGE);
        assertThat(arg, equalTo(RequestParameter.PAGE.getDefault()));
    }

}
