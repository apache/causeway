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

public class Get_givenRefArg_whenArgIsHrefAndLinksToNonExistentEntity_bad {

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

        // given a reference to a non-existent entity
        LinkRepresentation nonExistentEntityLink = new LinkRepresentation()
            .withHref("http://localhost:39393/objects/NONEXISTENT/123");
        
        // and given a representation of the 'contains' action accepting a entity href
        final JsonRepresentation containsAction = Util.givenAction(client, "ActionsEntities", "contains");
        final ObjectActionRepresentation containsActionRepr = containsAction.as(ObjectActionRepresentation.class);
        
        final LinkRepresentation invokeLink = containsActionRepr.getInvoke();
        final JsonRepresentation args = invokeLink.getArguments();
        
        // when query the 'contains' action passing in the reference to the non-existent entity 
        args.mapPut("searchFor.value", nonExistentEntityLink);
        args.mapPut("from.value", 0);
        args.mapPut("to.value", 1);
        
        RestfulResponse<ActionResultRepresentation> restfulResponse = client.followT(invokeLink, args);

        // then
        then(args, restfulResponse);
    }

    @Test
    public void usingResourceProxy() throws Exception {

        // given a reference to a non-existent entity
        LinkRepresentation nonExistentEntityLink = new LinkRepresentation()
            .withHref("http://localhost:39393/objects/NONEXISTENT/123");

        // when query the 'contains' action passing in the reference to the non-existent entity 
        JsonRepresentation args = JsonRepresentation.newMap();
        args.mapPut("searchFor.value", nonExistentEntityLink);
        args.mapPut("from.value", 0);
        args.mapPut("to.value", 3);
        Response response = serviceResource.invokeActionQueryOnly("ActionsEntities", "contains", UrlEncodingUtils.urlEncode(args));
        RestfulResponse<ActionResultRepresentation> restfulResponse = RestfulResponse.ofT(response);
        
        // then
        then(args, restfulResponse);
    }

    private static void then(final JsonRepresentation args, RestfulResponse<ActionResultRepresentation> restfulResponse) throws IOException {
        // then the response is an error
        assertThat(restfulResponse, hasStatus(HttpStatusCode.VALIDATION_FAILED));
        assertThat(restfulResponse.getHeader(Header.WARNING), is("Validation failed, see body for details"));

        // hmmm... what is the media type, though?  the spec doesn't say.  testing for a generic one.
        assertThat(restfulResponse.getHeader(Header.CONTENT_TYPE), hasMediaTypeProfile(MediaType.APPLICATION_JSON));

        RestfulResponse<JsonRepresentation> restfulResponseOfError = restfulResponse.wraps(JsonRepresentation.class);
        JsonRepresentation repr = restfulResponseOfError.getEntity();
        
        assertThat(repr.getString("searchFor.value.href"), is(args.getString("searchFor.value.href")));
        assertThat(repr.getString("searchFor.invalidReason"), is("'href' does not reference a known entity"));
    }


    
}
