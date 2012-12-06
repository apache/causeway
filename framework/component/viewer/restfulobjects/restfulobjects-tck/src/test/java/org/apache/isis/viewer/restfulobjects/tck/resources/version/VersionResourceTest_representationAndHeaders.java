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
package org.apache.isis.viewer.restfulobjects.tck.resources.version;

import static org.apache.isis.viewer.restfulobjects.tck.RepresentationMatchers.assertThat;
import static org.apache.isis.viewer.restfulobjects.tck.RepresentationMatchers.hasMaxAge;
import static org.apache.isis.viewer.restfulobjects.tck.RepresentationMatchers.hasParameter;
import static org.apache.isis.viewer.restfulobjects.tck.RepresentationMatchers.hasSubType;
import static org.apache.isis.viewer.restfulobjects.tck.RepresentationMatchers.hasType;
import static org.apache.isis.viewer.restfulobjects.tck.RepresentationMatchers.isArray;
import static org.apache.isis.viewer.restfulobjects.tck.RepresentationMatchers.isFollowableLinkToSelf;
import static org.apache.isis.viewer.restfulobjects.tck.RepresentationMatchers.isLink;
import static org.apache.isis.viewer.restfulobjects.tck.RepresentationMatchers.isMap;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import java.io.IOException;

import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status.Family;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import org.apache.isis.runtimes.dflt.webserver.WebServer;
import org.apache.isis.viewer.restfulobjects.applib.HttpMethod;
import org.apache.isis.viewer.restfulobjects.applib.JsonRepresentation;
import org.apache.isis.viewer.restfulobjects.applib.RepresentationType;
import org.apache.isis.viewer.restfulobjects.applib.RestfulClient;
import org.apache.isis.viewer.restfulobjects.applib.RestfulResponse;
import org.apache.isis.viewer.restfulobjects.applib.RestfulResponse.Header;
import org.apache.isis.viewer.restfulobjects.applib.RestfulResponse.HttpStatusCode;
import org.apache.isis.viewer.restfulobjects.applib.version.VersionRepresentation;
import org.apache.isis.viewer.restfulobjects.applib.version.VersionResource;
import org.apache.isis.viewer.restfulobjects.tck.IsisWebServerRule;

public class VersionResourceTest_representationAndHeaders {

    @Rule
    public IsisWebServerRule webServerRule = new IsisWebServerRule();

    private RestfulClient client;
    private VersionResource resource;

    @Before
    public void setUp() throws Exception {
        final WebServer webServer = webServerRule.getWebServer();
        client = new RestfulClient(webServer.getBase());

        resource = client.getVersionResource();
    }

    @Test
    public void representation() throws Exception {

        // given
        final Response servicesResp = resource.version();

        // when
        final RestfulResponse<VersionRepresentation> restfulResponse = RestfulResponse.ofT(servicesResp);
        assertThat(restfulResponse.getStatus().getFamily(), is(Family.SUCCESSFUL));

        // then
        assertThat(restfulResponse.getStatus(), is(HttpStatusCode.OK));

        final VersionRepresentation repr = restfulResponse.getEntity();
        assertThat(repr, is(not(nullValue())));
        assertThat(repr, isMap());

        assertThat(repr.getSelf(), isLink().httpMethod(HttpMethod.GET));

        assertThat(repr.getString("specVersion"), is("0.52"));
        assertThat(repr.getString("implVersion"), is(not(nullValue())));

        final JsonRepresentation optionalCapbilitiesRepr = repr.getOptionalCapabilities();
        assertThat(optionalCapbilitiesRepr, isMap());

        assertThat(optionalCapbilitiesRepr.getString("concurrencyChecking"), is("no"));
        assertThat(optionalCapbilitiesRepr.getString("transientObjects"), is("yes"));
        assertThat(optionalCapbilitiesRepr.getString("deleteObjects"), is("no"));
        assertThat(optionalCapbilitiesRepr.getString("simpleArguments"), is("no"));
        assertThat(optionalCapbilitiesRepr.getString("partialArguments"), is("no"));
        assertThat(optionalCapbilitiesRepr.getString("followLinks"), is("yes"));
        assertThat(optionalCapbilitiesRepr.getString("validateOnly"), is("no"));
        assertThat(optionalCapbilitiesRepr.getString("pagination"), is("no"));
        assertThat(optionalCapbilitiesRepr.getString("sorting"), is("no"));
        assertThat(optionalCapbilitiesRepr.getString("domainModel"), is("rich"));

        assertThat(repr.getLinks(), isArray());
        assertThat(repr.getExtensions(), is(not(nullValue())));
    }

    @Test
    public void headers() throws Exception {
        // given
        final Response resp = resource.version();

        // when
        final RestfulResponse<VersionRepresentation> restfulResponse = RestfulResponse.ofT(resp);

        // then
        final MediaType contentType = restfulResponse.getHeader(Header.CONTENT_TYPE);
        assertThat(contentType, hasType("application"));
        assertThat(contentType, hasSubType("json"));
        assertThat(contentType, hasParameter("profile", "urn:org.restfulobjects/version"));
        assertThat(contentType, is(RepresentationType.VERSION.getMediaType()));

        // then
        final CacheControl cacheControl = restfulResponse.getHeader(Header.CACHE_CONTROL);
        assertThat(cacheControl, hasMaxAge(24 * 60 * 60));
        assertThat(cacheControl.getMaxAge(), is(24 * 60 * 60));
    }

    @Test
    public void selfIsFollowable() throws Exception {
        // given
        final VersionRepresentation repr = givenRepresentation();

        // when, then
        assertThat(repr, isFollowableLinkToSelf(client));
    }

    private VersionRepresentation givenRepresentation() throws JsonParseException, JsonMappingException, IOException {
        final RestfulResponse<VersionRepresentation> jsonResp = RestfulResponse.ofT(resource.version());
        return jsonResp.getEntity();
    }

}
