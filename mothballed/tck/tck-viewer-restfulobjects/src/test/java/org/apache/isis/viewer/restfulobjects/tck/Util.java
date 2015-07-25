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
package org.apache.isis.viewer.restfulobjects.tck;

import java.io.IOException;
import javax.ws.rs.core.Response;
import org.apache.isis.core.commons.matchers.IsisMatchers;
import org.apache.isis.viewer.restfulobjects.applib.JsonRepresentation;
import org.apache.isis.viewer.restfulobjects.applib.LinkRepresentation;
import org.apache.isis.viewer.restfulobjects.applib.Rel;
import org.apache.isis.viewer.restfulobjects.applib.RepresentationType;
import org.apache.isis.viewer.restfulobjects.applib.RestfulHttpMethod;
import org.apache.isis.viewer.restfulobjects.applib.client.RestfulClient;
import org.apache.isis.viewer.restfulobjects.applib.client.RestfulRequest;
import org.apache.isis.viewer.restfulobjects.applib.client.RestfulRequest.RequestParameter;
import org.apache.isis.viewer.restfulobjects.applib.client.RestfulResponse;
import org.apache.isis.viewer.restfulobjects.applib.client.RestfulResponse.HttpStatusCode;
import org.apache.isis.viewer.restfulobjects.applib.domainobjects.ActionResultRepresentation;
import org.apache.isis.viewer.restfulobjects.applib.domainobjects.ActionResultRepresentation.ResultType;
import org.apache.isis.viewer.restfulobjects.applib.domainobjects.DomainObjectRepresentation;
import org.apache.isis.viewer.restfulobjects.applib.domainobjects.DomainServiceResource;
import org.apache.isis.viewer.restfulobjects.applib.domainobjects.ListRepresentation;

import static org.apache.isis.viewer.restfulobjects.tck.RestfulMatchers.isLink;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

public class Util {

    private Util() {
    }

    public static String givenLinkToService(RestfulClient restfulClient, String serviceId) throws IOException {

        final DomainServiceResource resource = restfulClient.getDomainServiceResource();
        final Response response = resource.services();
        final ListRepresentation services = RestfulResponse.<ListRepresentation> ofT(response).getEntity();

        final String href = services.getRepresentation("value[rel=" + Rel.SERVICE.getName() + ";serviceId=\"" + serviceId + "\"]").asLink().getHref();
        return href;
    }

    public static String givenHrefToService(RestfulClient client, final String serviceId) throws IOException {
        final DomainServiceResource resource = client.getDomainServiceResource();
        final Response response = resource.services();
        final ListRepresentation services = RestfulResponse.<ListRepresentation> ofT(response).getEntity();

        return services.getRepresentation("value[rel=urn:org.restfulobjects:rels/service;serviceId=\"%s\"]", serviceId).asLink().getHref();
    }

    public static JsonRepresentation givenAction(RestfulClient client, final String serviceId, final String actionId) throws IOException {
        final String href = givenHrefToService(client, serviceId);
        final String detailRel = Rel.DETAILS.andParam("action", actionId);

        final RestfulRequest request = client.createRequest(RestfulHttpMethod.GET, href).withArg(RequestParameter.FOLLOW_LINKS, "members[%s].links[rel=%s]", actionId, detailRel);
        final RestfulResponse<DomainObjectRepresentation> restfulResponse = request.executeT();

        assertThat(restfulResponse.getStatus(), is(HttpStatusCode.OK));
        final DomainObjectRepresentation repr = restfulResponse.getEntity();

        final JsonRepresentation actionLinkRepr = repr.getAction(actionId);
        return actionLinkRepr.getRepresentation("links[rel=%s].value", detailRel);
    }

    /**
     * For clientFollow tests; returns a link to the first entity in the list returned by invoking the 'list' action of the specified repo
     */
    public static LinkRepresentation serviceActionListInvokeFirstReference(RestfulClient client, String repoName) throws Exception {
        return serviceActionListInvokeFirstReference(client, repoName, "list");
    }

    /**
     * For clientFollow tests; returns a link to the first entity in the list returned by invoking the specified repo and action
     */
    public static LinkRepresentation serviceActionListInvokeFirstReference(RestfulClient client, String repoName, String actionName) throws Exception {
        return serviceActionListInvokeFirstReference(client, repoName, actionName, 0);
    }

    /**
     * For clientFollow tests; returns a link to the Nth entity in the list returned by invoking the specified repo and action
     */
    public static LinkRepresentation serviceActionListInvokeFirstReference(RestfulClient client, String repoName, String actionName, int idx) throws Exception {

        final DomainServiceResource serviceResource = client.getDomainServiceResource();

        Response response = serviceResource.invokeActionQueryOnly(repoName, actionName, null);
        RestfulResponse<ActionResultRepresentation> restfulResponse = RestfulResponse.ofT(response);

        assertThat(restfulResponse.getStatus(), is(HttpStatusCode.OK));
        final ActionResultRepresentation actionResultRepr = restfulResponse.getEntity();

        assertThat(actionResultRepr.getResultType(), is(ResultType.LIST));
        final ListRepresentation listRepr = actionResultRepr.getResult().as(ListRepresentation.class);

        assertThat(listRepr.getValue(), is(not(nullValue())));
        assertThat(listRepr.getValue().size(), is(IsisMatchers.greaterThan(idx + 1)));

        final LinkRepresentation domainObjectLinkRepr = listRepr.getValue().arrayGet(idx).as(LinkRepresentation.class);

        assertThat(domainObjectLinkRepr, is(not(nullValue())));
        assertThat(domainObjectLinkRepr, isLink().rel(Rel.ELEMENT).httpMethod(RestfulHttpMethod.GET).type(RepresentationType.DOMAIN_OBJECT.getMediaType()).arguments(JsonRepresentation.newMap()).build());

        return domainObjectLinkRepr;
    }

    
    
    /**
     * For resourceProxy tests; returns the first entity in the list returned by invoking the 'list' action on the specified repo
     */
    public static RestfulResponse<DomainObjectRepresentation> domainObjectJaxrsResponse(RestfulClient client, String repoName) throws Exception {
        return domainObjectJaxrsResponse(client, repoName, "list");
    }

    /**
     * For resourceProxy tests; returns the first entity in the list returned by invoking the specified repo and action
     */
    public static RestfulResponse<DomainObjectRepresentation> domainObjectJaxrsResponse(RestfulClient client, String repoName, String actionName) throws Exception {
        return domainObjectJaxrsResponse(client, repoName, actionName, 0);
    }

    /**
     * For resourceProxy tests; returns the Nth entity in the list returned by invoking the specified repo and action
     */
    public static RestfulResponse<DomainObjectRepresentation> domainObjectJaxrsResponse(RestfulClient client, String repoName, String actionName, int idx) throws Exception {
        final LinkRepresentation link = Util.serviceActionListInvokeFirstReference(client, repoName, actionName, idx);
        DomainObjectRepresentation domainObjectRepr = client.follow(link).getEntity().as(DomainObjectRepresentation.class);

        final Response jaxrsResponse = client.getDomainObjectResource().object(domainObjectRepr.getDomainType(), domainObjectRepr.getInstanceId());
        final RestfulResponse<DomainObjectRepresentation> restfulResponse = RestfulResponse.ofT(jaxrsResponse);
        return restfulResponse;
    }

}
