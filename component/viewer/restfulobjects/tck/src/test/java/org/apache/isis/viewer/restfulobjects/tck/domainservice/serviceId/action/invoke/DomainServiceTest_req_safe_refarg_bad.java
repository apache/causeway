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
package org.apache.isis.viewer.restfulobjects.tck.domainservice.serviceId.action.invoke;

import static org.apache.isis.viewer.restfulobjects.tck.RestfulMatchers.isLink;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.isis.viewer.restfulobjects.applib.JsonRepresentation;
import org.apache.isis.viewer.restfulobjects.applib.LinkRepresentation;
import org.apache.isis.viewer.restfulobjects.applib.Rel;
import org.apache.isis.viewer.restfulobjects.applib.RestfulMediaType;
import org.apache.isis.viewer.restfulobjects.applib.client.RestfulClient;
import org.apache.isis.viewer.restfulobjects.applib.client.RestfulResponse;
import org.apache.isis.viewer.restfulobjects.applib.client.RestfulResponse.Header;
import org.apache.isis.viewer.restfulobjects.applib.client.RestfulResponse.HttpStatusCode;
import org.apache.isis.viewer.restfulobjects.applib.domainobjects.ActionResultRepresentation;
import org.apache.isis.viewer.restfulobjects.applib.domainobjects.ActionResultRepresentation.ResultType;
import org.apache.isis.viewer.restfulobjects.applib.domainobjects.DomainServiceResource;
import org.apache.isis.viewer.restfulobjects.applib.domainobjects.ListRepresentation;
import org.apache.isis.viewer.restfulobjects.applib.domainobjects.ObjectActionRepresentation;
import org.apache.isis.viewer.restfulobjects.applib.domainobjects.ScalarValueRepresentation;
import org.apache.isis.viewer.restfulobjects.applib.errors.ErrorRepresentation;
import org.apache.isis.viewer.restfulobjects.applib.util.UrlEncodingUtils;
import org.apache.isis.viewer.restfulobjects.tck.IsisWebServerRule;
import org.apache.isis.viewer.restfulobjects.tck.RestfulMatchers;

import static org.apache.isis.viewer.restfulobjects.tck.RestfulMatchers.*;
import org.apache.isis.viewer.restfulobjects.tck.Util;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

public class DomainServiceTest_req_safe_refarg_bad {

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
        args.mapPut("searchFor", nonExistentEntityLink);
        args.mapPut("from", 0);
        args.mapPut("to", 1);
        
        RestfulResponse<ActionResultRepresentation> restfulResponse = client.followT(invokeLink, args);

        // then the response is an error
        assertThat(restfulResponse, hasStatus(HttpStatusCode.VALIDATION_FAILED));

        assertThat(restfulResponse.getHeader(Header.WARNING), is("199 Argument 'searchFor' href does not reference a known entity"));

        // hmmm... what is the media type, though?  the spec doesn't say.  just assuming generic for now.
        assertThat(restfulResponse.getHeader(Header.CONTENT_TYPE), hasProfile(MediaType.APPLICATION_JSON));

        RestfulResponse<JsonRepresentation> restfulResponseOfError = restfulResponse.wraps(JsonRepresentation.class);
        JsonRepresentation repr = restfulResponseOfError.getEntity();
        

        
        
    }

    @Ignore("still to update according to above test...")
    @Test
    public void usingResourceProxy() throws Exception {

        // given a reference to the first entity
        final ListRepresentation subListRepr = givenSublistActionInvoked(0, 1);
        LinkRepresentation firstEntityLink = subListRepr.getValue().arrayGet(0).asLink();

        // when query the 'contains' action passing in the entity 
        // (for a range where the entity is contained in the range)
        JsonRepresentation args = JsonRepresentation.newMap();
        args.mapPut("searchFor", firstEntityLink);
        args.mapPut("from", 0);
        args.mapPut("to", 3);
        Response response = serviceResource.invokeActionQueryOnly("ActionsEntities", "contains", UrlEncodingUtils.urlEncode(args));
        RestfulResponse<ActionResultRepresentation> restfulResponse = RestfulResponse.ofT(response);
        
        // then
        final ActionResultRepresentation actionResultRepr = restfulResponse.getEntity();
        JsonRepresentation resultRepr = actionResultRepr.getResult();
        
        assertThat(actionResultRepr.getResultType(), is(ResultType.SCALAR_VALUE));
        ScalarValueRepresentation scalarValueRepr = resultRepr.as(ScalarValueRepresentation.class);

        assertThat(scalarValueRepr.getValue().asBoolean(), is(true));
    }

    

    private ListRepresentation givenSublistActionInvoked(int from, int to) throws Exception {
        final JsonRepresentation givenSubListAction = Util.givenAction(client, "ActionsEntities", "subList");
        final ObjectActionRepresentation actionRepr = givenSubListAction.as(ObjectActionRepresentation.class);
        
        final LinkRepresentation invokeLink = actionRepr.getInvoke();
        final JsonRepresentation args = invokeLink.getArguments();
        
        // when
        args.mapPut("from", from);
        args.mapPut("to", to);
        
        final RestfulResponse<ActionResultRepresentation> restfulResponse = client.followT(invokeLink, args);
        
        final ActionResultRepresentation actionResultRepr = restfulResponse.getEntity();
        return actionResultRepr.getResult().as(ListRepresentation.class);
    }

}
