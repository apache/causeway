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
package org.apache.isis.viewer.restfulobjects.tck.domainservice.root;

import static org.apache.isis.viewer.restfulobjects.tck.RestfulMatchers.assertThat;
import static org.apache.isis.viewer.restfulobjects.tck.RestfulMatchers.isArray;
import static org.apache.isis.viewer.restfulobjects.tck.RestfulMatchers.isLink;
import static org.apache.isis.viewer.restfulobjects.tck.RestfulMatchers.isMap;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.endsWith;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import javax.ws.rs.core.Response;

import org.apache.isis.core.webserver.WebServer;
import org.apache.isis.viewer.restfulobjects.applib.JsonRepresentation;
import org.apache.isis.viewer.restfulobjects.applib.LinkRepresentation;
import org.apache.isis.viewer.restfulobjects.applib.Rel;
import org.apache.isis.viewer.restfulobjects.applib.RepresentationType;
import org.apache.isis.viewer.restfulobjects.applib.RestfulHttpMethod;
import org.apache.isis.viewer.restfulobjects.applib.client.RestfulClient;
import org.apache.isis.viewer.restfulobjects.applib.client.RestfulResponse;
import org.apache.isis.viewer.restfulobjects.applib.client.RestfulResponse.HttpStatusCode;
import org.apache.isis.viewer.restfulobjects.applib.domainobjects.DomainServiceResource;
import org.apache.isis.viewer.restfulobjects.applib.domainobjects.ListRepresentation;
import org.apache.isis.viewer.restfulobjects.tck.IsisWebServerRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class Get_thenRepresentation_ok {

    @Rule
    public IsisWebServerRule webServerRule = new IsisWebServerRule();

    private RestfulClient client;
    private DomainServiceResource resource;

    @Before
    public void setUp() throws Exception {
        final WebServer webServer = webServerRule.getWebServer();
        client = new RestfulClient(webServer.getBase());

        resource = client.getDomainServiceResource();
    }

    @Test
    public void representation() throws Exception {

        // when
        final Response response = resource.services();
        final RestfulResponse<ListRepresentation> restfulResponse = RestfulResponse.ofT(response);

        // then
        final ListRepresentation repr = restfulResponse.getEntity();

        assertThat(repr, isMap());

        assertThat(repr.getSelf(), isLink(client)
                                    .rel(Rel.SELF)
                                    .href(endsWith(":39393/services"))
                                    .httpMethod(RestfulHttpMethod.GET)
                                    .type(RepresentationType.LIST.getMediaType())
                                    .returning(HttpStatusCode.OK)
                                    );
        assertThat(repr.getUp(), isLink(client)
                                    .rel(Rel.UP)
                                    .href(endsWith(":39393/"))
                                    .httpMethod(RestfulHttpMethod.GET)
                                    .type(RepresentationType.HOME_PAGE.getMediaType())
                                    .returning(HttpStatusCode.OK)
                                    );

        assertThat(repr.getValue(), isArray());

        assertThat(repr.getLinks(), isArray());
        assertThat(repr.getExtensions(), isMap());
    }

    @Test
    public void linksToDomainServiceResources() throws Exception {

        // given
        final RestfulResponse<ListRepresentation> jsonResp = RestfulResponse.ofT(resource.services());
        final ListRepresentation repr = jsonResp.getEntity();

        // when
        final JsonRepresentation values = repr.getValue();

        // then
        for (final LinkRepresentation link : values.arrayIterable(LinkRepresentation.class)) {
            assertThat("HiddenRepository should not show up in services list", false, is(link.getHref().endsWith("HiddenRepository")));
        }
        
        // and also
        for (final LinkRepresentation link : values.arrayIterable(LinkRepresentation.class)) {

            assertThat(link, isLink(client)
                    .rel(containsString(Rel.SERVICE.getName()))
                    .href(containsString(":39393/"))
                    .httpMethod(RestfulHttpMethod.GET)
                    .type(RepresentationType.DOMAIN_OBJECT.getMediaType())
                    );
            assertThat(link, isLink(client)
                    .returning(HttpStatusCode.OK)
                    .responseEntityWithSelfHref(link.getHref())
                    );
        }
    }

}
