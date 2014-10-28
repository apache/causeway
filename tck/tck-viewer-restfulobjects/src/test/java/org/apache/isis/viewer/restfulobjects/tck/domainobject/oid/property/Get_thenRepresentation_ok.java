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
package org.apache.isis.viewer.restfulobjects.tck.domainobject.oid.property;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status.Family;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.apache.isis.core.webserver.WebServer;
import org.apache.isis.viewer.restfulobjects.applib.LinkRepresentation;
import org.apache.isis.viewer.restfulobjects.applib.Rel;
import org.apache.isis.viewer.restfulobjects.applib.RepresentationType;
import org.apache.isis.viewer.restfulobjects.applib.RestfulHttpMethod;
import org.apache.isis.viewer.restfulobjects.applib.client.RestfulClient;
import org.apache.isis.viewer.restfulobjects.applib.client.RestfulResponse;
import org.apache.isis.viewer.restfulobjects.applib.client.RestfulResponse.HttpStatusCode;
import org.apache.isis.viewer.restfulobjects.applib.domainobjects.DomainObjectResource;
import org.apache.isis.viewer.restfulobjects.applib.domainobjects.ObjectPropertyRepresentation;
import org.apache.isis.viewer.restfulobjects.tck.IsisWebServerRule;

import static org.apache.isis.viewer.restfulobjects.tck.RestfulMatchers.*;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

public class Get_thenRepresentation_ok {


    @Rule
    public IsisWebServerRule webServerRule = new IsisWebServerRule();

    protected RestfulClient client;
    private DomainObjectResource domainObjectResource;

    @Before
    public void setUp() throws Exception {
        final WebServer webServer = webServerRule.getWebServer();
        client = new RestfulClient(webServer.getBase());
        domainObjectResource = client.getDomainObjectResource();
    }


    @Test
    public void representation() throws Exception {

        // when
        final Response idPropertyResp = domainObjectResource.propertyDetails("org.apache.isis.core.tck.dom.defaults.WithDefaultsEntity","68", "anInt");
        final RestfulResponse<ObjectPropertyRepresentation> idPropertyJsonResp = RestfulResponse.ofT(idPropertyResp);
        assertThat(idPropertyJsonResp.getStatus().getFamily(), is(Family.SUCCESSFUL));

        // then
        final ObjectPropertyRepresentation propertyRepr = idPropertyJsonResp.getEntity();

        assertThat(propertyRepr.getString("memberType"), is("property"));

        // self link
        final LinkRepresentation selfLink = propertyRepr.getLinkWithRel(Rel.SELF);
        assertThat(selfLink, isLink(client)
                                .httpMethod(RestfulHttpMethod.GET)
                                .href(endsWith("/objects/org.apache.isis.core.tck.dom.defaults.WithDefaultsEntity/68/properties/anInt"))
                                .returning(HttpStatusCode.OK)
                                .build());

        // up link
        final LinkRepresentation upLink = propertyRepr.getLinkWithRel(Rel.UP);
        assertThat(upLink, isLink(client)
                                .httpMethod(RestfulHttpMethod.GET)
                                .href(endsWith("/objects/org.apache.isis.core.tck.dom.defaults.WithDefaultsEntity/68"))
                                .returning(HttpStatusCode.OK)
                                .type(RepresentationType.DOMAIN_OBJECT.getMediaType())
                                .title("default-name")
                                .build());

        //modify link
        final LinkRepresentation modifyLink = propertyRepr.getLinkWithRel(Rel.MODIFY);
        assertThat(modifyLink, isLink(client)
                                .httpMethod(RestfulHttpMethod.PUT)
                                .type(RepresentationType.OBJECT_PROPERTY.getMediaType())
                                .href(endsWith("/objects/org.apache.isis.core.tck.dom.defaults.WithDefaultsEntity/68/properties/anInt"))
                                .build());

        assertThat(modifyLink.getArguments(), is(not(nullValue())));
        assertThat(modifyLink.getArguments().isArray(), is(false));
        assertThat(modifyLink.getArguments().size(), is(1));

        //clear link
        final LinkRepresentation clearLink = propertyRepr.getLinkWithRel(Rel.CLEAR);
        assertThat(clearLink, isLink(client)
                                .httpMethod(RestfulHttpMethod.DELETE)
                                .type(RepresentationType.OBJECT_PROPERTY.getMediaType())
                                .href(endsWith("/objects/org.apache.isis.core.tck.dom.defaults.WithDefaultsEntity/68/properties/anInt"))
                                .build());

        // described by link
        final LinkRepresentation describedByLink = propertyRepr.getLinkWithRel(Rel.DESCRIBEDBY);
        assertThat(describedByLink, isLink(client)
                                .returning(HttpStatusCode.OK)
                                .responseEntityWithSelfHref(describedByLink.getHref())
                                .build());

        assertThat(propertyRepr.getInt("value"), is(42));
        assertThat(propertyRepr.getString("format"),is("int"));
        assertThat(propertyRepr.getString("extensions.x-isis-format"), is("int"));
        assertThat(propertyRepr.getExtensions(), isMap());
        assertThat(propertyRepr.getExtensions().getArray("changed").isArray(), is(true));
        assertThat(propertyRepr.getExtensions().getArray("disposed").isArray(), is(true));
    }
}
