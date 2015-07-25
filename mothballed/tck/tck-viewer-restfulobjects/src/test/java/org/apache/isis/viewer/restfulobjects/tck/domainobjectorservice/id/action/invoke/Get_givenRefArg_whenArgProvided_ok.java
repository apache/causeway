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
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
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
import org.apache.isis.viewer.restfulobjects.applib.util.UrlEncodingUtils;
import org.apache.isis.viewer.restfulobjects.tck.IsisWebServerRule;
import org.apache.isis.viewer.restfulobjects.tck.Util;

import static org.apache.isis.viewer.restfulobjects.tck.RestfulMatchers.hasMediaTypeProfile;
import static org.apache.isis.viewer.restfulobjects.tck.RestfulMatchers.isLink;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

public class Get_givenRefArg_whenArgProvided_ok {

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

        // given a reference to the first entity
        final ListRepresentation subListRepr = givenSublistActionInvoked(0, 1);
        assertThat(subListRepr.getValue().size(), is(1));
        
        LinkRepresentation firstEntityLink = subListRepr.getValue().arrayGet(0).asLink();

        // and given a representation of the 'contains' action accepting a entity href
        final JsonRepresentation containsAction = Util.givenAction(client, "ActionsEntities", "contains");
        final ObjectActionRepresentation containsActionRepr = containsAction.as(ObjectActionRepresentation.class);
        
        final LinkRepresentation invokeLink = containsActionRepr.getInvoke();
        final JsonRepresentation args = invokeLink.getArguments();
        assertThat(args.size(), is(3));
        
        // when query the 'contains' action passing in the entity 
        // (for a range where the entity is contained in the range)
        args.mapPut("searchFor.value", firstEntityLink);
        args.mapPut("from.value", 0);
        args.mapPut("to.value", 3);
        
        RestfulResponse<ActionResultRepresentation> restfulResponse = client.followT(invokeLink, args);

        // then 
        thenResponseIsScalarValueOf(restfulResponse, true);
        
        
        // and when query the 'contains' action for a different range which does not
        // contain the entity
        args.mapPut("searchFor.value", firstEntityLink);
        args.mapPut("from.value", 3);
        args.mapPut("to.values", 5);
        
        restfulResponse = client.followT(invokeLink, args);

        // then 
        thenResponseIsScalarValueOf(restfulResponse, false);
    }


    
    @Test
    public void usingResourceProxy() throws Exception {

        // given a reference to the first entity
        final ListRepresentation subListRepr = givenSublistActionInvoked(0, 1);
        LinkRepresentation firstEntityLink = subListRepr.getValue().arrayGet(0).asLink();

        // when query the 'contains' action passing in the entity 
        // (for a range where the entity is contained in the range)
        JsonRepresentation args = JsonRepresentation.newMap();
        args.mapPut("searchFor.value", firstEntityLink);
        args.mapPut("from.value", 0);
        args.mapPut("to.value", 3);
        Response response = serviceResource.invokeActionQueryOnly("ActionsEntities", "contains", UrlEncodingUtils.urlEncode(args));
        RestfulResponse<ActionResultRepresentation> restfulResponse = RestfulResponse.ofT(response);
        
        // then
        thenResponseIsScalarValueOf(restfulResponse, true);
    }

    private void thenResponseIsScalarValueOf(RestfulResponse<ActionResultRepresentation> restfulResponse, boolean value) throws IOException {
        assertThat(restfulResponse.getHeader(Header.CONTENT_TYPE), hasMediaTypeProfile(RestfulMediaType.APPLICATION_JSON_ACTION_RESULT));
        ActionResultRepresentation actionResultRepr = restfulResponse.getEntity();
        assertThat(actionResultRepr.getResultType(), is(ResultType.SCALAR_VALUE));
        JsonRepresentation resultRepr = actionResultRepr.getResult();
        assertThat(resultRepr, is(not(nullValue())));
        
        ScalarValueRepresentation scalarValueRepr = resultRepr.as(ScalarValueRepresentation.class);
        
        LinkRepresentation returnTypeLink = scalarValueRepr.getLinkWithRel(Rel.RETURN_TYPE);
        assertThat(returnTypeLink, is(not(nullValue())));
        assertThat(returnTypeLink, isLink(client)
                                        .rel(Rel.RETURN_TYPE)
                                        .href(Matchers.endsWith(":39393/domain-types/boolean"))
                                        .returning(HttpStatusCode.OK)
                                        .build());
        
        assertThat(scalarValueRepr.getValue().asBoolean(), is(value));
    }


    private ListRepresentation givenSublistActionInvoked(int from, int to) throws Exception {
        final JsonRepresentation givenSubListAction = Util.givenAction(client, "ActionsEntities", "subList");
        final ObjectActionRepresentation actionRepr = givenSubListAction.as(ObjectActionRepresentation.class);
        
        final LinkRepresentation invokeLink = actionRepr.getInvoke();
        final JsonRepresentation args = invokeLink.getArguments();
        
        // when
        args.mapPut("from.value", from);
        args.mapPut("to.value", to);
        
        final RestfulResponse<ActionResultRepresentation> restfulResponse = client.followT(invokeLink, args);
        
        final ActionResultRepresentation actionResultRepr = restfulResponse.getEntity();
        return actionResultRepr.getResult().as(ListRepresentation.class);
    }

}
