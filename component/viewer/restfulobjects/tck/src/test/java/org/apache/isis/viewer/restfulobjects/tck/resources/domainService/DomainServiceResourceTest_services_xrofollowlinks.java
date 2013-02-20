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
package org.apache.isis.viewer.restfulobjects.tck.resources.domainService;

import static org.apache.isis.viewer.restfulobjects.tck.RepresentationMatchers.assertThat;
import static org.apache.isis.viewer.restfulobjects.tck.RepresentationMatchers.isArray;
import static org.apache.isis.viewer.restfulobjects.tck.RepresentationMatchers.isLink;
import static org.apache.isis.viewer.restfulobjects.tck.RepresentationMatchers.isMap;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertThat;

import org.apache.isis.viewer.restfulobjects.applib.JsonRepresentation;
import org.apache.isis.viewer.restfulobjects.applib.Rel;
import org.apache.isis.viewer.restfulobjects.applib.RestfulHttpMethod;
import org.apache.isis.viewer.restfulobjects.applib.client.RestfulClient;
import org.apache.isis.viewer.restfulobjects.applib.client.RestfulRequest;
import org.apache.isis.viewer.restfulobjects.applib.client.RestfulRequest.RequestParameter;
import org.apache.isis.viewer.restfulobjects.applib.client.RestfulResponse;
import org.apache.isis.viewer.restfulobjects.applib.domainobjects.ListRepresentation;
import org.apache.isis.viewer.restfulobjects.applib.user.UserRepresentation;
import org.apache.isis.viewer.restfulobjects.tck.IsisWebServerRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class DomainServiceResourceTest_services_xrofollowlinks {

    @Rule
    public IsisWebServerRule webServerRule = new IsisWebServerRule();

    private RestfulClient client;

    private RestfulRequest request;
    private RestfulResponse<UserRepresentation> restfulResponse;
    private UserRepresentation repr;

    @Before
    public void setUp() throws Exception {
        client = webServerRule.getClient();

    }

    @Test
    public void noFollow() throws Exception {

        request = client.createRequest(RestfulHttpMethod.GET, "user");
        restfulResponse = request.executeT();
        repr = restfulResponse.getEntity();

        assertThat(repr.getSelf().getValue(), is(nullValue()));
        assertThat(repr.getUp().getValue(), is(nullValue()));
    }

    @Test
    public void self() throws Exception {

        request = client.createRequest(RestfulHttpMethod.GET, "user")
                    .withArg(RequestParameter.FOLLOW_LINKS, "links[rel=" + Rel.SELF.getName() + "]");
        restfulResponse = request.executeT();
        repr = restfulResponse.getEntity();

        assertThat(repr.getSelf().getValue(), is(not(nullValue())));
    }

    @Test
    public void up() throws Exception {

        request = client.createRequest(RestfulHttpMethod.GET, "user")
                    .withArg(RequestParameter.FOLLOW_LINKS, "links[rel=" + Rel.UP.getName() + "]");
        restfulResponse = request.executeT();
        repr = restfulResponse.getEntity();

        assertThat(repr.getUp().getValue(), is(not(nullValue())));
    }

    
    // TODO: split up this test?
    @Test
    public void services_withFollowLinks() throws Exception {

        RestfulRequest request;
        RestfulResponse<ListRepresentation> restfulResponse;
        ListRepresentation repr;

        request = client.createRequest(RestfulHttpMethod.GET, "services");
        restfulResponse = request.executeT();
        repr = restfulResponse.getEntity();

        assertThat(repr.getValue(), isArray());
        assertThat(repr.getValue().size(), is(greaterThan(0)));
        assertThat(repr.getValue().arrayGet(0), isLink().novalue());

        request = client.createRequest(RestfulHttpMethod.GET, "services")
                .withArg(RequestParameter.FOLLOW_LINKS, "value");
        restfulResponse = request.executeT();
        repr = restfulResponse.getEntity();

        assertThat(repr.getValue().arrayGet(0), isLink().value(is(not(nullValue(JsonRepresentation.class)))));
        assertThat(repr.getValue().arrayGet(0).getRepresentation("value"), isMap());
    }

    
}
