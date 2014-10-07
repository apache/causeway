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
package org.apache.isis.viewer.restfulobjects.tck.domainservice.serviceId;

import javax.ws.rs.core.Response;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.apache.isis.viewer.restfulobjects.applib.LinkRepresentation;
import org.apache.isis.viewer.restfulobjects.applib.Rel;
import org.apache.isis.viewer.restfulobjects.applib.RestfulHttpMethod;
import org.apache.isis.viewer.restfulobjects.applib.client.RestfulClient;
import org.apache.isis.viewer.restfulobjects.applib.client.RestfulResponse;
import org.apache.isis.viewer.restfulobjects.applib.client.RestfulResponse.HttpStatusCode;
import org.apache.isis.viewer.restfulobjects.applib.domainobjects.DomainObjectMemberRepresentation;
import org.apache.isis.viewer.restfulobjects.applib.domainobjects.DomainObjectRepresentation;
import org.apache.isis.viewer.restfulobjects.applib.domainobjects.DomainServiceResource;
import org.apache.isis.viewer.restfulobjects.tck.IsisWebServerRule;

import static org.apache.isis.core.commons.matchers.IsisMatchers.matches;
import static org.apache.isis.viewer.restfulobjects.tck.RestfulMatchers.*;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

public class Get_thenRepresentation_ok {

    @Rule
    public IsisWebServerRule webServerRule = new IsisWebServerRule();

    private RestfulClient client;
    private DomainServiceResource resource;

    @Before
    public void setUp() throws Exception {
        client = webServerRule.getClient();

        resource = client.getDomainServiceResource();
    }

    @Test
    public void representation() throws Exception {

        // given
        final Response resp = resource.service("JdkValuedEntities");

        // when
        final RestfulResponse<DomainObjectRepresentation> jsonResp = RestfulResponse.ofT(resp);

        // then
        assertThat(jsonResp.getStatus(), is(HttpStatusCode.OK));

        final DomainObjectRepresentation repr = jsonResp.getEntity();

        assertThat(repr, isMap());

        assertThat(repr.getTitle(), matches("JdkValuedEntities"));
        
        assertThat(repr.getDomainType(), is(nullValue()));
        assertThat(repr.getInstanceId(), is(nullValue()));
        
        assertThat(repr.getServiceId(), is("JdkValuedEntities"));
        
        assertThat(repr.getSelf(), isLink().httpMethod(RestfulHttpMethod.GET).build());
        
        assertThat(repr.getMembers(), isMap());
        assertThat(repr.getMembers().size(), is(2));
        DomainObjectMemberRepresentation listMemberRepr = repr.getAction("list");
        
        assertThat(listMemberRepr.getMemberType(), is("action"));
        assertThat(listMemberRepr.getDisabledReason(), is(nullValue()));
        assertThat(listMemberRepr.getLinks(), isArray());
        assertThat(listMemberRepr.getLinks().size(), is(1));
        
        LinkRepresentation listMemberReprDetailsLink = listMemberRepr.getLinkWithRel(Rel.DETAILS);
        assertThat(listMemberReprDetailsLink, isLink(client)
                                       .httpMethod(RestfulHttpMethod.GET)
                                       .href(endsWith("/services/JdkValuedEntities/actions/list"))
                                       .returning(HttpStatusCode.OK)
                                       .responseEntityWithSelfHref(listMemberReprDetailsLink.getHref()).build());
        
        
        assertThat(repr.getLinks(), isArray());
        assertThat(repr.getLinks().size(), is(3));
        
        // link to self (see above)
        // link to describedby
        LinkRepresentation describedByLink = repr.getLinkWithRel(Rel.DESCRIBEDBY);
        assertThat(describedByLink, isLink(client)
                                       .httpMethod(RestfulHttpMethod.GET)
                                       .href(endsWith("/domain-types/JdkValuedEntities")).build()
                                       );
        assertThat(describedByLink, isLink(client)
                .returning(HttpStatusCode.OK)
                .responseEntityWithSelfHref(describedByLink.getHref()).build());
        
        assertThat(repr.getLinkWithRel(Rel.PERSIST), is(nullValue()));
        assertThat(repr.getLinkWithRel(Rel.UPDATE), is(nullValue()));
        assertThat(repr.getLinkWithRel(Rel.DELETE), is(nullValue()));
        
        assertThat(repr.getExtensions(), isMap());
        assertThat(repr.getOid(), matches("JdkValuedEntities:28"));
    }



    @Test
    public void visibleAndInvocableAction() throws Exception {
        
        // given
        final Response resp = resource.service("BusinessRulesEntities");

        // when
        final RestfulResponse<DomainObjectRepresentation> jsonResp = RestfulResponse.ofT(resp);

        // then
        assertThat(jsonResp.getStatus(), is(HttpStatusCode.OK));
        final DomainObjectRepresentation repr = jsonResp.getEntity();

        assertThat(repr, isMap());

        final DomainObjectMemberRepresentation actionRepr = repr.getAction("visibleAndInvocableAction");
        assertThat(actionRepr, isMap());

        assertThat(actionRepr.getDisabledReason(), is(nullValue()));

        final LinkRepresentation actionDetailsLink = actionRepr.getLinkWithRel(Rel.DETAILS);
        assertThat(actionDetailsLink, isLink(this.client)
                                        .rel(Rel.DETAILS)
                                        .httpMethod(RestfulHttpMethod.GET)
                                        .href(Matchers.endsWith(":39393/services/BusinessRulesEntities/actions/visibleAndInvocableAction"))
                                        .returning(HttpStatusCode.OK).build());
    }


    @Test
    public void visibleButNotInvocableAction() throws Exception {
        
        // given
        final Response resp = resource.service("BusinessRulesEntities");

        // when
        final RestfulResponse<DomainObjectRepresentation> jsonResp = RestfulResponse.ofT(resp);

        // then
        assertThat(jsonResp.getStatus(), is(HttpStatusCode.OK));
        final DomainObjectRepresentation repr = jsonResp.getEntity();

        final DomainObjectMemberRepresentation actionRepr = repr.getAction("visibleButNotInvocableAction");
        assertThat(actionRepr, isMap());

        assertThat(actionRepr.getDisabledReason(), is("Always disabled"));

        final LinkRepresentation actionDetailsLink = actionRepr.getLinkWithRel(Rel.DETAILS);

        // even though not invocable, still can traverse to its details page
        assertThat(actionDetailsLink, isLink(this.client)
                                        .href(Matchers.endsWith(":39393/services/BusinessRulesEntities/actions/visibleButNotInvocableAction"))
                                        .returning(HttpStatusCode.OK).build());
    }

    @Test
    public void invisibleAction() throws Exception {
        
        // given
        final Response resp = resource.service("BusinessRulesEntities");

        // when
        final RestfulResponse<DomainObjectRepresentation> jsonResp = RestfulResponse.ofT(resp);

        // then
        assertThat(jsonResp.getStatus(), is(HttpStatusCode.OK));
        final DomainObjectRepresentation repr = jsonResp.getEntity();

        assertThat(repr.getAction("invisibleAction"), is(nullValue()));
    }


}
