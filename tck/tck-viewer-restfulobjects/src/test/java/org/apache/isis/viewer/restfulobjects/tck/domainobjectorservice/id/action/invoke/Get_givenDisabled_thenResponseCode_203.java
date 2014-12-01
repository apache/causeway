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
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.apache.isis.viewer.restfulobjects.applib.JsonRepresentation;
import org.apache.isis.viewer.restfulobjects.applib.LinkRepresentation;
import org.apache.isis.viewer.restfulobjects.applib.Rel;
import org.apache.isis.viewer.restfulobjects.applib.client.RestfulClient;
import org.apache.isis.viewer.restfulobjects.applib.client.RestfulResponse;
import org.apache.isis.viewer.restfulobjects.applib.client.RestfulResponse.Header;
import org.apache.isis.viewer.restfulobjects.applib.client.RestfulResponse.HttpStatusCode;
import org.apache.isis.viewer.restfulobjects.applib.domainobjects.ActionResultRepresentation;
import org.apache.isis.viewer.restfulobjects.applib.domainobjects.DomainServiceResource;
import org.apache.isis.viewer.restfulobjects.applib.domainobjects.ObjectActionRepresentation;
import org.apache.isis.viewer.restfulobjects.applib.util.UrlEncodingUtils;
import org.apache.isis.viewer.restfulobjects.tck.IsisWebServerRule;
import org.apache.isis.viewer.restfulobjects.tck.Util;

import static org.apache.isis.viewer.restfulobjects.tck.RestfulMatchers.hasMediaTypeProfile;
import static org.apache.isis.viewer.restfulobjects.tck.RestfulMatchers.hasStatus;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class Get_givenDisabled_thenResponseCode_203 {

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

        // given, when
        final JsonRepresentation givenAction = Util.givenAction(client, "BusinessRulesEntities", "visibleButNotInvocableAction");
        final ObjectActionRepresentation actionRepr = givenAction.as(ObjectActionRepresentation.class);
        
        // then
        final String disabledReason = actionRepr.getDisabledReason();
        assertThat(disabledReason, is("Always disabled"));
        

        final LinkRepresentation invokeLink = new LinkRepresentation()
            .withRel(Rel.INVOKE)
            .withHref("http://localhost:39393/services/BusinessRulesEntities/actions/visibleButNotInvocableAction/invoke");
        
        // when
        JsonRepresentation args = JsonRepresentation.newMap();
        args = JsonRepresentation.newMap();
        args.mapPut("id.value", 123);

        final RestfulResponse<ActionResultRepresentation> restfulResponse = client.followT(invokeLink, args);
        
        // then
        thenResponseIsErrorWithInvalidReason(restfulResponse, disabledReason);
    }


    @Test
    public void usingResourceProxy() throws Exception {

        // given, when
        JsonRepresentation args = JsonRepresentation.newMap();
        args.mapPut("id.value", 123);

        Response response = serviceResource.invokeActionQueryOnly("BusinessRulesEntities", "visibleButNotInvocableAction", UrlEncodingUtils.urlEncode(args));
        RestfulResponse<ActionResultRepresentation> restfulResponse = RestfulResponse.ofT(response);
        
        // then
        thenResponseIsErrorWithInvalidReason(restfulResponse, "Always disabled");
    }

    private static void thenResponseIsErrorWithInvalidReason(final RestfulResponse<ActionResultRepresentation> restfulResponse, String disabledReason) throws IOException {
        assertThat(restfulResponse, hasStatus(HttpStatusCode.FORBIDDEN));
        assertThat(restfulResponse.getHeader(Header.WARNING), is(disabledReason));

        // hmmm... what is the media type, though?  the spec doesn't say.  testing for a generic one.
        assertThat(restfulResponse.getHeader(Header.CONTENT_TYPE), hasMediaTypeProfile(MediaType.APPLICATION_JSON));
    }

}
