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
package org.apache.isis.viewer.restfulobjects.viewer.context;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import org.apache.isis.applib.services.iactnlayer.InteractionService;
import org.apache.isis.commons.internal.codec._UrlDecoderUtil;
import org.apache.isis.core.interaction.session.InteractionTracker;
import org.apache.isis.core.interaction.session.IsisInteraction;
import org.apache.isis.core.internaltestsupport.jmocking.JUnitRuleMockery2;
import org.apache.isis.core.internaltestsupport.jmocking.JUnitRuleMockery2.Mode;
import org.apache.isis.core.metamodel._testing.MetaModelContext_forTesting;
import org.apache.isis.core.metamodel.context.MetaModelContext;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;
import org.apache.isis.core.security.authentication.manager.AuthenticationManager;
import org.apache.isis.viewer.restfulobjects.applib.JsonRepresentation;
import org.apache.isis.viewer.restfulobjects.applib.RepresentationType;
import org.apache.isis.viewer.restfulobjects.applib.RestfulRequest.RequestParameter;
import org.apache.isis.viewer.restfulobjects.applib.util.UrlEncodingUtils;
import org.apache.isis.viewer.restfulobjects.viewer.resources.ResourceDescriptor;

public class ResourceContext_getArg_Test {

    @Rule public JUnitRuleMockery2 context = JUnitRuleMockery2.createFor(Mode.INTERFACES_AND_CLASSES);

    @Mock private HttpServletRequest mockHttpServletRequest;
    @Mock private ServletContext mockServletContext;
    @Mock private IsisInteraction mockIsisInteraction;
    @Mock private InteractionService mockInteractionService;
    @Mock private InteractionTracker mockIsisInteractionTracker;
    @Mock private AuthenticationManager mockAuthenticationManager;
    @Mock private SpecificationLoader mockSpecificationLoader;
    @Mock private WebApplicationContext webApplicationContext;

    private ResourceContext resourceContext;
    private MetaModelContext metaModelContext;

    @Before
    public void setUp() throws Exception {

        // PRODUCTION;

        metaModelContext = MetaModelContext_forTesting.builder()
                .specificationLoader(mockSpecificationLoader)
                .singleton(mockInteractionService)
                .singleton(mockAuthenticationManager)
                .singleton(mockIsisInteractionTracker)
                //                .serviceInjector(mockServiceInjector)
                //                .serviceRegistry(mockServiceRegistry)
                //                .translationService(mockTranslationService)
                //                .objectAdapterProvider(mockPersistenceSessionServiceInternal)
                //                .authenticationProvider(mockAuthenticationProvider)
                .build();


        context.checking(new Expectations() {{

                allowing(webApplicationContext).getBean(MetaModelContext.class);
                will(returnValue(metaModelContext));

                allowing(mockServletContext).getAttribute("org.springframework.web.context.WebApplicationContext.ROOT");
                will(returnValue(webApplicationContext));

                allowing(mockHttpServletRequest).getServletContext();
                will(returnValue(mockServletContext));

                allowing(mockHttpServletRequest).getQueryString();
                will(returnValue(""));

        }});
    }

    @Test
    public void whenArgExists() throws Exception {
        final String queryString = UrlEncodingUtils.urlEncode(JsonRepresentation.newMap("x-ro-page", "123").asJsonNode());

        resourceContext = new ResourceContext(ResourceDescriptor.empty(), null, null, null, null,
                _UrlDecoderUtil.urlDecodeNullSafe(queryString),
                mockHttpServletRequest, null, null,
                metaModelContext, null, null) {
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

        resourceContext = new ResourceContext(ResourceDescriptor.empty(), null, null, null, null,
                _UrlDecoderUtil.urlDecodeNullSafe(queryString),
                mockHttpServletRequest, null, null,
                metaModelContext, null, null) {
            @Override
            void init(final RepresentationType representationType) {
                //
            }
        };
        final Integer arg = resourceContext.getArg(RequestParameter.PAGE);
        assertThat(arg, equalTo(RequestParameter.PAGE.getDefault()));
    }


}
