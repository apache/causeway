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

import java.util.HashMap;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.HttpHeaders;

import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import org.apache.isis.applib.services.inject.ServiceInjector;
import org.apache.isis.commons.internal.collections._Maps;
import org.apache.isis.commons.internal.context._Context;
import org.apache.isis.commons.internal.url.UrlDecoderUtil;
import org.apache.isis.config.IsisConfiguration;
import org.apache.isis.metamodel.MetaModelContext;
import org.apache.isis.metamodel.specloader.SpecificationLoader;
import org.apache.isis.runtime.system.persistence.PersistenceSession;
import org.apache.isis.runtime.system.session.IsisSession;
import org.apache.isis.runtime.system.session.IsisSessionFactory;
import org.apache.isis.security.authentication.AuthenticationSession;
import org.apache.isis.unittestsupport.jmocking.JUnitRuleMockery2;
import org.apache.isis.unittestsupport.jmocking.JUnitRuleMockery2.Mode;
import org.apache.isis.viewer.restfulobjects.applib.JsonRepresentation;
import org.apache.isis.viewer.restfulobjects.applib.RepresentationType;
import org.apache.isis.viewer.restfulobjects.applib.client.RestfulRequest.RequestParameter;
import org.apache.isis.viewer.restfulobjects.applib.util.UrlEncodingUtils;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class ResourceContextTest_getArg {

    @Rule
    public JUnitRuleMockery2 context = JUnitRuleMockery2.createFor(Mode.INTERFACES_AND_CLASSES);

    @Mock HttpHeaders mockHttpHeaders;
    @Mock HttpServletRequest mockHttpServletRequest;
    @Mock ServletContext mockServletContext;
    @Mock IsisSessionFactory mockIsisSessionFactory;
    @Mock ServiceInjector mockServicesInjector;
    @Mock IsisConfiguration mockConfiguration;
    @Mock IsisSession mockIsisSession;
    @Mock AuthenticationSession mockAuthenticationSession;
    @Mock PersistenceSession mockPersistenceSession;
    @Mock SpecificationLoader mockSpecificationLoader;

    private ResourceContext resourceContext;


    @Before
    public void setUp() throws Exception {

        // PRODUCTION;

        MetaModelContext.preset(MetaModelContext.builder()
                .specificationLoader(mockSpecificationLoader)
                //                .serviceInjector(mockServiceInjector)
                //                .serviceRegistry(mockServiceRegistry)
                //                .translationService(mockTranslationService)
                //                .objectAdapterProvider(mockPersistenceSessionServiceInternal)
                //                .authenticationSessionProvider(mockAuthenticationSessionProvider)
                .build());

        _Context.put(IsisSessionFactory.class, mockIsisSessionFactory, false);

        context.checking(new Expectations() {
            {
                allowing(mockHttpServletRequest).getQueryString();
                will(returnValue(""));
                //[ISIS-1976] IsisSessionFactory does no longer live on the ServletContext
                //                allowing(mockHttpServletRequest).getServletContext();
                //                will(returnValue(mockServletContext));
                //                allowing(mockServletContext).getAttribute("org.apache.isis.webapp.isisSessionFactory");
                //                will(returnValue(mockIsisSessionFactory));
                //                allowing(mockIsisSessionFactory).getServiceInjector();
                //                will(returnValue(mockServicesInjector));
                //                allowing(mockIsisSessionFactory).getConfiguration();
                //                will(returnValue(mockConfiguration));
                allowing(mockIsisSessionFactory).getCurrentSession();
                will(returnValue(mockIsisSession));
                allowing(mockIsisSession).getAuthenticationSession();
                will(returnValue(mockAuthenticationSession));
                allowing(mockIsisSessionFactory).getSpecificationLoader();
                will(returnValue(mockSpecificationLoader));
                //                allowing(mockIsisSession).getPersistenceSession();
                //                will(returnValue(mockPersistenceSession));
            }
        });
    }

    @Test
    public void whenArgExists() throws Exception {
        final String queryString = UrlEncodingUtils.urlEncode(JsonRepresentation.newMap("x-ro-page", "123").asJsonNode());
        //givenServletRequestQueryString(queryString);
        givenServletRequestParameterMapEmpty();

        resourceContext = new ResourceContext(null, null, null, null, null, null, null,
                UrlDecoderUtil.urlDecodeNullSafe(queryString),
                mockHttpServletRequest, null, null,
                null) {
            @Override
            void init(final RepresentationType representationType) {
                //
            }
        };
        final Integer arg = resourceContext.getArg(RequestParameter.PAGE);
        assertThat(arg, equalTo(123));
    }

    @Test
    public void whenArgDoesNotExist() throws Exception {
        final String queryString = UrlEncodingUtils.urlEncode(JsonRepresentation.newMap("xxx", "123").asJsonNode());
        //givenServletRequestQueryString(queryString);
        givenServletRequestParameterMapEmpty();

        resourceContext = new ResourceContext(null, null, null, null, null, null, null,
                UrlDecoderUtil.urlDecodeNullSafe(queryString),
                mockHttpServletRequest, null, null,
                null) {
            @Override
            void init(final RepresentationType representationType) {
                //
            }
        };
        final Integer arg = resourceContext.getArg(RequestParameter.PAGE);
        assertThat(arg, equalTo(RequestParameter.PAGE.getDefault()));
    }

    private void givenServletRequestQueryString(final String queryString) {
        context.checking(new Expectations() {
            {
                oneOf(mockHttpServletRequest).getQueryString();
                will(returnValue(queryString));
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

}
