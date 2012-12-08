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
package org.apache.isis.viewer.restfulobjects.viewer;

import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.Before;
import org.junit.Test;

import org.apache.isis.applib.annotation.Where;
import org.apache.isis.viewer.restfulobjects.applib.RepresentationType;
import org.apache.isis.viewer.restfulobjects.viewer.RestfulObjectsApplicationException;
import org.apache.isis.viewer.restfulobjects.viewer.ResourceContext;

public class ResourceContextTest_ensureCompatibleAcceptHeader {

    private HttpHeaders httpHeaders;
    private HttpServletRequest httpServletRequest;

    private final Mockery context = new JUnit4Mockery();

    @Before
    public void setUp() throws Exception {
        httpHeaders = context.mock(HttpHeaders.class);
        httpServletRequest = context.mock(HttpServletRequest.class);
        context.checking(new Expectations() {
            {
                allowing(httpServletRequest).getQueryString();
                will(returnValue(""));
            }
        });
    }

    @Test
    public void noop() throws Exception {
        final RepresentationType representationType = RepresentationType.HOME_PAGE;
        givenHttpHeadersGetAcceptableMediaTypesReturns(Arrays.<MediaType> asList(representationType.getMediaType()));

        instantiateResourceContext(representationType);
    }

    @Test
    public void happyCase() throws Exception {
        final RepresentationType representationType = RepresentationType.HOME_PAGE;
        givenHttpHeadersGetAcceptableMediaTypesReturns(Arrays.<MediaType> asList(representationType.getMediaType()));

        instantiateResourceContext(representationType);
    }

    @Test
    public void acceptGenericAndProduceGeneric() throws Exception {
        final RepresentationType representationType = RepresentationType.GENERIC;
        givenHttpHeadersGetAcceptableMediaTypesReturns(Arrays.<MediaType> asList(MediaType.APPLICATION_JSON_TYPE));

        instantiateResourceContext(representationType);
    }

    @Test
    public void acceptGenericAndProduceSpecific() throws Exception {
        final RepresentationType representationType = RepresentationType.HOME_PAGE;
        givenHttpHeadersGetAcceptableMediaTypesReturns(Arrays.<MediaType> asList(MediaType.APPLICATION_JSON_TYPE));

        instantiateResourceContext(representationType);
    }

    @Test(expected = RestfulObjectsApplicationException.class)
    public void nonMatching() throws Exception {
        final RepresentationType representationType = RepresentationType.HOME_PAGE;
        givenHttpHeadersGetAcceptableMediaTypesReturns(Arrays.<MediaType> asList(MediaType.APPLICATION_ATOM_XML_TYPE));

        instantiateResourceContext(representationType);
    }

    @Test(expected = RestfulObjectsApplicationException.class)
    public void nonMatchingProfile() throws Exception {
        final RepresentationType representationType = RepresentationType.HOME_PAGE;
        givenHttpHeadersGetAcceptableMediaTypesReturns(Arrays.<MediaType> asList(RepresentationType.USER.getMediaType()));

        instantiateResourceContext(representationType);
    }

    @Test(expected = RestfulObjectsApplicationException.class)
    public void nonMatchingProfile_ignoreGeneric() throws Exception {
        final RepresentationType representationType = RepresentationType.HOME_PAGE;
        givenHttpHeadersGetAcceptableMediaTypesReturns(Arrays.<MediaType> asList(RepresentationType.USER.getMediaType(), MediaType.APPLICATION_JSON_TYPE));

        instantiateResourceContext(representationType);
    }

    @Test(expected = RestfulObjectsApplicationException.class)
    public void emptyList() throws Exception {
        final RepresentationType representationType = RepresentationType.HOME_PAGE;
        givenHttpHeadersGetAcceptableMediaTypesReturns(Arrays.<MediaType> asList());

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
        return new ResourceContext(representationType, httpHeaders, null, null, httpServletRequest, null, null, null, null, null, null, null, null, null);
    }

}
