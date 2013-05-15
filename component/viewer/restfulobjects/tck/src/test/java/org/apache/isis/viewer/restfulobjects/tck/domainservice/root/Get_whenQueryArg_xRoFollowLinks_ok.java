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
import static org.apache.isis.viewer.restfulobjects.tck.RestfulMatchers.isLink;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import org.apache.isis.viewer.restfulobjects.applib.JsonRepresentation;
import org.apache.isis.viewer.restfulobjects.applib.LinkRepresentation;
import org.apache.isis.viewer.restfulobjects.applib.Rel;
import org.apache.isis.viewer.restfulobjects.applib.RepresentationType;
import org.apache.isis.viewer.restfulobjects.applib.RestfulHttpMethod;
import org.apache.isis.viewer.restfulobjects.applib.client.RestfulClient;
import org.apache.isis.viewer.restfulobjects.applib.client.RestfulRequest;
import org.apache.isis.viewer.restfulobjects.applib.client.RestfulRequest.RequestParameter;
import org.apache.isis.viewer.restfulobjects.applib.client.RestfulResponse;
import org.apache.isis.viewer.restfulobjects.applib.domainobjects.ListRepresentation;
import org.apache.isis.viewer.restfulobjects.tck.IsisWebServerRule;

public class Get_whenQueryArg_xRoFollowLinks_ok {

    @Rule
    public IsisWebServerRule webServerRule = new IsisWebServerRule();

    private RestfulClient client;

    private RestfulRequest request;
    private RestfulResponse<ListRepresentation> restfulResponse;
    private ListRepresentation repr;

    @Before
    public void setUp() throws Exception {
        client = webServerRule.getClient();

    }

    @Test
    public void noFollow() throws Exception {

        request = client.createRequest(RestfulHttpMethod.GET, "services");
        restfulResponse = request.executeT();
        repr = restfulResponse.getEntity();

        assertThat(repr.getSelf().getValue(), is(nullValue()));
        assertThat(repr.getUp().getValue(), is(nullValue()));
    }

    @Test
    public void self() throws Exception {

        request = client.createRequest(RestfulHttpMethod.GET, "services")
                    .withArg(RequestParameter.FOLLOW_LINKS, "links[rel=" + Rel.SELF.getName() + "]");
        restfulResponse = request.executeT();
        repr = restfulResponse.getEntity();

        assertThat(repr.getSelf().getValue(), is(not(nullValue())));
    }

    @Test
    public void up() throws Exception {

        request = client.createRequest(RestfulHttpMethod.GET, "services")
                    .withArg(RequestParameter.FOLLOW_LINKS, "links[rel=" + Rel.UP.getName() + "]");
        restfulResponse = request.executeT();
        repr = restfulResponse.getEntity();

        assertThat(repr.getUp().getValue(), is(not(nullValue())));
    }

    @Test
    public void value_noQualifications_andSoAllOfThemEagerlyReturned() throws Exception {

        request = client.createRequest(RestfulHttpMethod.GET, "services")
                .withArg(RequestParameter.FOLLOW_LINKS, "value[rel=" + Rel.SERVICE.getName() + "]");
        restfulResponse = request.executeT();
        repr = restfulResponse.getEntity();

        request = client.createRequest(RestfulHttpMethod.GET, "services")
                .withArg(RequestParameter.FOLLOW_LINKS, "value");
        restfulResponse = request.executeT();
        repr = restfulResponse.getEntity();

        // then
        for (final LinkRepresentation link : repr.getValue().arrayIterable(LinkRepresentation.class)) {

            assertThat(link, isLink(client)
                    .rel(containsString(Rel.SERVICE.getName()))
                    .href(containsString(":39393/"))
                    .httpMethod(RestfulHttpMethod.GET)
                    .type(RepresentationType.DOMAIN_OBJECT.getMediaType())
                    .value(is(not(nullValue(JsonRepresentation.class))))
                    );
        }

    }

    @Test
    public void value_withQualification_andSoSingleServiceEagerlyReturned() throws Exception {

        request = client.createRequest(RestfulHttpMethod.GET, "services")
                .withArg(RequestParameter.FOLLOW_LINKS, "value[rel=" + Rel.SERVICE.getName() + ";serviceId=\"JdkValuedEntities\"]");
        restfulResponse = request.executeT();
        repr = restfulResponse.getEntity();

        // then
        int numWithValue = 0;
        int numWithoutValue = 0;
        for (final LinkRepresentation link : repr.getValue().arrayIterable(LinkRepresentation.class)) {
            
            if(isLink(client).value(is(not(nullValue(JsonRepresentation.class)))).build().matches(link)) {
                numWithValue++;
            } else {
                numWithoutValue++;
            }
        }
        
        assertThat(numWithValue, is(1));
        assertThat(numWithoutValue, is(greaterThan(0)));
    }

}
