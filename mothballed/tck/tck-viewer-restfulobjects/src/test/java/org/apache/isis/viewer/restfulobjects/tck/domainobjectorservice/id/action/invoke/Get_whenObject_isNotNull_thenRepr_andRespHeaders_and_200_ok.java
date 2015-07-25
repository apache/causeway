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

import java.io.IOException;
import javax.ws.rs.core.Response;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.apache.isis.core.commons.matchers.IsisMatchers;
import org.apache.isis.viewer.restfulobjects.applib.JsonRepresentation;
import org.apache.isis.viewer.restfulobjects.applib.LinkRepresentation;
import org.apache.isis.viewer.restfulobjects.applib.RestfulMediaType;
import org.apache.isis.viewer.restfulobjects.applib.client.RestfulClient;
import org.apache.isis.viewer.restfulobjects.applib.client.RestfulResponse;
import org.apache.isis.viewer.restfulobjects.applib.client.RestfulResponse.Header;
import org.apache.isis.viewer.restfulobjects.applib.client.RestfulResponse.HttpStatusCode;
import org.apache.isis.viewer.restfulobjects.applib.domainobjects.ActionResultRepresentation;
import org.apache.isis.viewer.restfulobjects.applib.domainobjects.ActionResultRepresentation.ResultType;
import org.apache.isis.viewer.restfulobjects.applib.domainobjects.DomainObjectRepresentation;
import org.apache.isis.viewer.restfulobjects.applib.domainobjects.DomainServiceResource;
import org.apache.isis.viewer.restfulobjects.applib.domainobjects.ObjectActionRepresentation;
import org.apache.isis.viewer.restfulobjects.applib.util.UrlEncodingUtils;
import org.apache.isis.viewer.restfulobjects.tck.IsisWebServerRule;
import org.apache.isis.viewer.restfulobjects.tck.Util;

import static org.apache.isis.viewer.restfulobjects.tck.RestfulMatchers.hasMediaTypeProfile;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

// Get_whenObject_isNotNull_thenRepr_ok_andRespHeaders_ContentType_andContentLength__andRespCode_200_ok
public class Get_whenObject_isNotNull_thenRepr_andRespHeaders_and_200_ok {

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
    public void usingClientFollow() throws Exception {

        // given
        final JsonRepresentation givenAction = Util.givenAction(client, "ActionsEntities", "findById");
        final ObjectActionRepresentation actionRepr = givenAction.as(ObjectActionRepresentation.class);

        final LinkRepresentation invokeLink = actionRepr.getInvoke();
        final JsonRepresentation args =invokeLink.getArguments();
        
        // when
        args.mapPut("id.value", 1);

        // when
        final RestfulResponse<ActionResultRepresentation> restfulResponse = client.followT(invokeLink, args);
        
        // then
        then(restfulResponse);
    }

    
    @Test
    public void usingResourceProxy() throws Exception {

        // given, when
        
        JsonRepresentation args = JsonRepresentation.newMap();
        args.mapPut("id.value", 1);

        Response response = serviceResource.invokeActionQueryOnly("ActionsEntities", "findById", UrlEncodingUtils.urlEncode(args));
        RestfulResponse<ActionResultRepresentation> restfulResponse = RestfulResponse.ofT(response);
        
        then(restfulResponse);
    }

    private static void then(final RestfulResponse<ActionResultRepresentation> restfulResponse) throws IOException {
        
        assertThat(restfulResponse.getStatus(), is(HttpStatusCode.OK));
        assertThat(restfulResponse.getHeader(Header.CONTENT_TYPE), hasMediaTypeProfile(RestfulMediaType.APPLICATION_JSON_ACTION_RESULT));
        assertThat(restfulResponse.getHeader(Header.CONTENT_LENGTH), is(IsisMatchers.greaterThan(1000)));
        
        final ActionResultRepresentation actionResultRepr = restfulResponse.getEntity();

        assertThat(actionResultRepr.getResultType(), is(ResultType.DOMAIN_OBJECT));
        final DomainObjectRepresentation objRepr = actionResultRepr.getResult().as(DomainObjectRepresentation.class);
        
        assertThat(objRepr.getMembers(), is(not(nullValue())));
        assertThat(objRepr.getMembers().size(), is(IsisMatchers.greaterThan(0)));
        
        assertThat(objRepr.getTitle(), is(not(nullValue())));
        
        assertThat(objRepr.getLinks(), is(not(nullValue())));
        assertThat(objRepr.getMembers().size(), is(IsisMatchers.greaterThan(0)));
        
        assertThat(objRepr.getExtensions(), is(not(nullValue())));
    }

    
}
