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
package org.apache.isis.viewer.restfulobjects.tck.domainobject.oid.collection;

import static org.apache.isis.viewer.restfulobjects.tck.RestfulMatchers.assertThat;
import static org.apache.isis.viewer.restfulobjects.tck.RestfulMatchers.isLink;
import static org.apache.isis.viewer.restfulobjects.tck.RestfulMatchers.isMap;
import static org.hamcrest.CoreMatchers.endsWith;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

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
import org.apache.isis.viewer.restfulobjects.applib.domainobjects.ObjectCollectionRepresentation;
import org.apache.isis.viewer.restfulobjects.tck.IsisWebServerRule;

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
        final Response collectionResp = domainObjectResource.accessCollection("BSRL", "64", "invisibleCollection");
        final RestfulResponse<ObjectCollectionRepresentation> collectionJsonResp = RestfulResponse.ofT(collectionResp);
        assertThat(collectionJsonResp.getStatus().getFamily(), is(Family.SUCCESSFUL));

        // then
        final ObjectCollectionRepresentation collectionRepr = collectionJsonResp.getEntity();

        assertThat(collectionRepr.getString("memberType"), is("collection"));

        // self link
        final LinkRepresentation selfLink = collectionRepr.getLinkWithRel(Rel.SELF);
        assertThat(selfLink, isLink(client)
                                .httpMethod(RestfulHttpMethod.GET)
                                .href(endsWith("/objects/BSRL/64/collections/invisibleCollection"))
                                .returning(HttpStatusCode.OK));

        // up link
        final LinkRepresentation upLink = collectionRepr.getLinkWithRel(Rel.UP);
        assertThat(upLink, isLink(client)
                                .httpMethod(RestfulHttpMethod.GET)
                                .href(endsWith("http://localhost:39393/objects/BSRL/64"))
                                .returning(HttpStatusCode.OK)
                                .type(RepresentationType.DOMAIN_OBJECT.getMediaType())
                                .title("Untitled Bus Rules Entity"));

        //addto link
        final LinkRepresentation addtoLink = collectionRepr.getLinkWithRel(Rel.ADD_TO);
        assertThat(addtoLink, isLink(client)
                                .httpMethod(RestfulHttpMethod.POST)
                                .type(RepresentationType.OBJECT_COLLECTION.getMediaType())
                                .href(endsWith("/objects/BSRL/64/collections/invisibleCollection")));

        assertThat(addtoLink.getArguments(), is(not(nullValue())));
        assertThat(addtoLink.getArguments().isArray(), is(false));
        assertThat(addtoLink.getArguments().size(), is(1));

       //remove-from link
        final LinkRepresentation removeFromLink = collectionRepr.getLinkWithRel(Rel.REMOVE_FROM);
        assertThat(removeFromLink, isLink(client)
                                .httpMethod(RestfulHttpMethod.DELETE)
                                .type(RepresentationType.OBJECT_COLLECTION.getMediaType())
                                .href(endsWith("/objects/BSRL/64/collections/invisibleCollection")));

        assertThat(removeFromLink.getArguments(), is(not(nullValue())));
        assertThat(removeFromLink.getArguments().isArray(), is(false));
        assertThat(removeFromLink.getArguments().size(), is(1));

        // described by link
        final LinkRepresentation describedByLink = collectionRepr.getLinkWithRel(Rel.DESCRIBEDBY);
        assertThat(describedByLink, isLink(client)
                                .returning(HttpStatusCode.OK)
                                .responseEntityWithSelfHref(describedByLink.getHref()));

        assertThat(collectionRepr.getArray("value").isArray(),is(true));

        assertThat(collectionRepr.getExtensions(), isMap());
        assertThat(collectionRepr.getExtensions().getString("collectionSemantics"), is("list"));
        assertThat(collectionRepr.getExtensions().getArray("changed").isArray(), is(true));
        assertThat(collectionRepr.getExtensions().getArray("disposed").isArray(), is(true));
    }
}
