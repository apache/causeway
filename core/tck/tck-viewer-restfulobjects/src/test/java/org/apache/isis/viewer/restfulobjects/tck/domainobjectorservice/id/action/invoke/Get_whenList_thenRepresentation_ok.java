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
package org.apache.isis.viewer.restfulobjects.tck.domainobjectorservice.id.action.invoke;

import javax.ws.rs.core.Response;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.apache.isis.viewer.restfulobjects.applib.*;
import org.apache.isis.viewer.restfulobjects.applib.client.RestfulClient;
import org.apache.isis.viewer.restfulobjects.applib.client.RestfulResponse;
import org.apache.isis.viewer.restfulobjects.applib.client.RestfulResponse.HttpStatusCode;
import org.apache.isis.viewer.restfulobjects.applib.domainobjects.ActionResultRepresentation;
import org.apache.isis.viewer.restfulobjects.applib.domainobjects.ActionResultRepresentation.ResultType;
import org.apache.isis.viewer.restfulobjects.applib.domainobjects.DomainServiceResource;
import org.apache.isis.viewer.restfulobjects.applib.domainobjects.ListRepresentation;
import org.apache.isis.viewer.restfulobjects.tck.IsisWebServerRule;

import static org.apache.isis.core.commons.matchers.IsisMatchers.matches;
import static org.apache.isis.viewer.restfulobjects.tck.RestfulMatchers.isLink;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

public class Get_whenList_thenRepresentation_ok {

    @Rule
    public IsisWebServerRule webServerRule = new IsisWebServerRule();

    private RestfulClient client;

    private DomainServiceResource serviceResource;

    @Before
    public void setUp() throws Exception {
        client = webServerRule.getClient();

        serviceResource = client.getDomainServiceResource();
    }

    @Test
    public void usingResourceProxy() throws Exception {

        // given, when
        Response response = serviceResource.invokeActionQueryOnly("ActionsEntities", "list", null);
        RestfulResponse<ActionResultRepresentation> restfulResponse = RestfulResponse.ofT(response);
        
        // then
        assertThat(restfulResponse.getStatus(), is(HttpStatusCode.OK));
        
        final ActionResultRepresentation actionResultRepr = restfulResponse.getEntity();
        
        assertThat(actionResultRepr.getResultType(), is(ResultType.LIST));
        final ListRepresentation listRepr = actionResultRepr.getResult().as(ListRepresentation.class);

        // then list value
        assertThat(listRepr.getValue(), is(not(nullValue())));
        assertThat(listRepr.getValue().size(), is(5));

        final JsonRepresentation domainObjectLinkRepr = listRepr.getValue().arrayGet(0);

        assertThat(domainObjectLinkRepr, is(not(nullValue())));
        assertThat(domainObjectLinkRepr, isLink()
                .rel(Rel.ELEMENT)
                .title("Untitled Actions Entity")
                .httpMethod(RestfulHttpMethod.GET)
                .href(matches("http://localhost:\\d+/objects/RTNE/79"))
                .type(RepresentationType.DOMAIN_OBJECT.getMediaType())
                .arguments(JsonRepresentation.newMap())
                .build());

        // then list link element-type
        assertThat(listRepr.getLinks(), is(not(nullValue())));
        final LinkRepresentation elementTypeLink = listRepr.getLinkWithRel(Rel.ELEMENT_TYPE);
        assertThat(elementTypeLink, is(not(nullValue())));
    }
}
