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
package org.apache.isis.viewer.restfulobjects.server;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;

import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import org.apache.isis.commons.internal.collections._Maps;
import org.apache.isis.commons.internal.context._Context;
import org.apache.isis.config.IsisConfiguration;
import org.apache.isis.core.metamodel.services.ServicesInjector;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;
import org.apache.isis.core.runtime.system.persistence.PersistenceSession;
import org.apache.isis.core.runtime.system.session.IsisSession;
import org.apache.isis.core.runtime.system.session.IsisSessionFactory;
import org.apache.isis.core.security.authentication.AuthenticationSession;
import org.apache.isis.core.unittestsupport.jmocking.JUnitRuleMockery2;
import org.apache.isis.viewer.restfulobjects.applib.RepresentationType;
import org.apache.isis.viewer.restfulobjects.applib.client.RestfulResponse.HttpStatusCode;
import org.apache.isis.viewer.restfulobjects.rendering.RestfulObjectsApplicationException;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public abstract class ResourceContext_ensureCompatibleAcceptHeader_ContractTest {

    @Rule
    public JUnitRuleMockery2 context = JUnitRuleMockery2.createFor(JUnitRuleMockery2.Mode.INTERFACES_AND_CLASSES);

    @Mock HttpHeaders mockHttpHeaders;
    @Mock HttpServletRequest mockHttpServletRequest;
    @Mock ServletContext mockServletContext;
    @Mock IsisSessionFactory mockIsisSessionFactory;
    @Mock ServicesInjector mockServicesInjector;
    @Mock IsisConfiguration mockConfiguration;
    @Mock IsisSession mockIsisSession;
    @Mock AuthenticationSession mockAuthenticationSession;
    @Mock PersistenceSession mockPersistenceSession;
    @Mock SpecificationLoader mockSpecificationLoader;

    @Before
    public void setUp() throws Exception {
            
        // PRODUCTION
        
        _Context.put(IsisSessionFactory.class, mockIsisSessionFactory, false);

        context.checking(new Expectations() {
            {
                allowing(mockHttpServletRequest).getQueryString();
                will(returnValue(""));
                //[ISIS-1976] IsisSessionFactory does no longer live on the ServletContext
//              allowing(mockHttpServletRequest).getServletContext();
//              will(returnValue(mockServletContext));
//              allowing(mockServletContext).getAttribute("org.apache.isis.core.webapp.isisSessionFactory");
//              will(returnValue(mockIsisSessionFactory));
                allowing(mockIsisSessionFactory).getServicesInjector();
                will(returnValue(mockServicesInjector));
                allowing(mockIsisSessionFactory).getConfiguration();
                will(returnValue(mockConfiguration));
                allowing(mockIsisSessionFactory).getCurrentSession();
                will(returnValue(mockIsisSession));
                allowing(mockIsisSession).getAuthenticationSession();
                will(returnValue(mockAuthenticationSession));
                allowing(mockIsisSessionFactory).getSpecificationLoader();
                will(returnValue(mockSpecificationLoader));
                allowing(mockIsisSession).getPersistenceSession();
                will(returnValue(mockPersistenceSession));
            }
        });
    }

    @Test
    public void noop() throws Exception {
        final RepresentationType representationType = RepresentationType.HOME_PAGE;
        givenHttpHeadersGetAcceptableMediaTypesReturns(Arrays.<MediaType> asList(representationType.getMediaType()));
        givenServletRequestParameterMapEmpty();

        instantiateResourceContext(representationType);
    }

    @Test
    public void happyCase() throws Exception {
        final RepresentationType representationType = RepresentationType.HOME_PAGE;
        givenHttpHeadersGetAcceptableMediaTypesReturns(Arrays.<MediaType> asList(representationType.getMediaType()));
        givenServletRequestParameterMapEmpty();

        instantiateResourceContext(representationType);
    }

    @Test
    public void acceptGenericAndProduceGeneric() throws Exception {
        final RepresentationType representationType = RepresentationType.GENERIC;
        givenHttpHeadersGetAcceptableMediaTypesReturns(Arrays.<MediaType> asList(MediaType.APPLICATION_JSON_TYPE));
        givenServletRequestParameterMapEmpty();

        instantiateResourceContext(representationType);
    }

    @Test
    public void acceptGenericAndProduceSpecific() throws Exception {
        final RepresentationType representationType = RepresentationType.HOME_PAGE;
        givenHttpHeadersGetAcceptableMediaTypesReturns(Arrays.<MediaType> asList(MediaType.APPLICATION_JSON_TYPE));
        givenServletRequestParameterMapEmpty();

        instantiateResourceContext(representationType);
    }

    @Test
    public void nonMatching() throws Exception {
        final RepresentationType representationType = RepresentationType.HOME_PAGE;
        givenHttpHeadersGetAcceptableMediaTypesReturns(Arrays.<MediaType> asList(MediaType.APPLICATION_ATOM_XML_TYPE));
        givenServletRequestParameterMapEmpty();

        try {
            instantiateResourceContext(representationType);
        } catch(RestfulObjectsApplicationException ex ) {
            assertThat(ex.getHttpStatusCode(), is(HttpStatusCode.NOT_ACCEPTABLE));
        }
    }

    @Test
    public void nonMatchingProfile() throws Exception {
        final RepresentationType representationType = RepresentationType.HOME_PAGE;
        givenHttpHeadersGetAcceptableMediaTypesReturns(Arrays.<MediaType> asList(RepresentationType.USER.getMediaType()));
        givenServletRequestParameterMapEmpty();

        try {
            instantiateResourceContext(representationType);
        } catch(RestfulObjectsApplicationException ex ) {
            assertThat(ex.getHttpStatusCode(), is(HttpStatusCode.NOT_ACCEPTABLE));
        }
    }

    @Test
    public void nonMatchingProfile_ignoreGeneric() throws Exception {
        final RepresentationType representationType = RepresentationType.HOME_PAGE;
        givenHttpHeadersGetAcceptableMediaTypesReturns(Arrays.<MediaType> asList(RepresentationType.USER.getMediaType(), MediaType.APPLICATION_JSON_TYPE));
        givenServletRequestParameterMapEmpty();

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
        givenServletRequestParameterMapEmpty();

        instantiateResourceContext(representationType);
    }

    private void givenHttpHeadersGetAcceptableMediaTypesReturns(final List<MediaType> mediaTypes) {
        context.checking(new Expectations() {
            {
                allowing(mockHttpHeaders).getAcceptableMediaTypes();
                will(returnValue(mediaTypes));
            }
        });
    }

    private void givenServletRequestParameterMapEmpty() {
        final HashMap<Object, Object> parameterMap = _Maps.newHashMap();
        context.checking(new Expectations() {
            {
                oneOf(mockHttpServletRequest).getParameterMap();
                will(returnValue(parameterMap));
            }
        });
    }

    private ResourceContext instantiateResourceContext(
            final RepresentationType representationType) {
        return new ResourceContext(representationType, mockHttpHeaders, null, null, null, null, null, null,
                mockHttpServletRequest, null, null,
                null);
    }

}
