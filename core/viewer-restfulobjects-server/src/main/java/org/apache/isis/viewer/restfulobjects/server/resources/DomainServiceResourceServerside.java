/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.isis.viewer.restfulobjects.server.resources;

import java.io.InputStream;
import java.util.List;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.annotation.Where;
import org.apache.isis.core.commons.url.UrlEncodingUtils;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.facets.object.domainservice.DomainServiceFacet;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.viewer.restfulobjects.applib.JsonRepresentation;
import org.apache.isis.viewer.restfulobjects.applib.RepresentationType;
import org.apache.isis.viewer.restfulobjects.applib.RestfulMediaType;
import org.apache.isis.viewer.restfulobjects.applib.client.RestfulResponse;
import org.apache.isis.viewer.restfulobjects.applib.domainobjects.DomainServiceResource;
import org.apache.isis.viewer.restfulobjects.rendering.Caching;
import org.apache.isis.viewer.restfulobjects.rendering.Responses;
import org.apache.isis.viewer.restfulobjects.rendering.domainobjects.DomainObjectReprRenderer;
import org.apache.isis.viewer.restfulobjects.rendering.domainobjects.DomainServiceLinkTo;
import org.apache.isis.viewer.restfulobjects.rendering.RestfulObjectsApplicationException;

@Path("/services")
public class DomainServiceResourceServerside extends ResourceAbstract implements DomainServiceResource {

    private final static Predicate<ObjectAdapter> NATURE_OF_MENU = new Predicate<ObjectAdapter>() {
        @Override
        public boolean apply(final ObjectAdapter input) {
            final ObjectSpecification specification = input.getSpecification();
            final DomainServiceFacet facet = specification.getFacet(DomainServiceFacet.class);
            if (facet == null) {
                // not expected, because we know these are domain services.
                return false;
            }
            final NatureOfService natureOfService = facet.getNatureOfService();
            return  natureOfService == NatureOfService.VIEW ||
                    natureOfService == NatureOfService.VIEW_MENU_ONLY;
        }
    };

    @Override
    @GET
    @Path("/")
    @Produces({ MediaType.APPLICATION_JSON, RestfulMediaType.APPLICATION_JSON_LIST, RestfulMediaType.APPLICATION_JSON_ERROR })
    public Response services() {
        init(RepresentationType.LIST, Where.STANDALONE_TABLES);

        final List<ObjectAdapter> serviceAdapters =
                Lists.newArrayList(
                        Iterables.filter(
                                getResourceContext().getServiceAdapters(), NATURE_OF_MENU));

        final DomainServicesListReprRenderer renderer = new DomainServicesListReprRenderer(getResourceContext(), null, JsonRepresentation.newMap());
        renderer.usingLinkToBuilder(new DomainServiceLinkTo())
            .includesSelf()
            .with(serviceAdapters);

        return Responses.ofOk(renderer, Caching.ONE_DAY).build();
    }

    @Override
    public Response deleteServicesNotAllowed() {
        throw RestfulObjectsApplicationException.createWithMessage(RestfulResponse.HttpStatusCode.METHOD_NOT_ALLOWED, "Deleting the services resource is not allowed.");
    }

    @Override
    public Response putServicesNotAllowed() {
        throw RestfulObjectsApplicationException.createWithMessage(RestfulResponse.HttpStatusCode.METHOD_NOT_ALLOWED, "Putting to the services resource is not allowed.");
    }

    @Override
    public Response postServicesNotAllowed() {
        throw RestfulObjectsApplicationException.createWithMessage(RestfulResponse.HttpStatusCode.METHOD_NOT_ALLOWED, "Posting to the services resource is not allowed.");
    }

    // //////////////////////////////////////////////////////////
    // domain service
    // //////////////////////////////////////////////////////////

    @Override
    @GET
    @Path("/{serviceId}")
    @Produces({ MediaType.APPLICATION_JSON, RestfulMediaType.APPLICATION_JSON_OBJECT, RestfulMediaType.APPLICATION_JSON_ERROR })
    public Response service(@PathParam("serviceId") final String serviceId) {
        init(RepresentationType.DOMAIN_OBJECT, Where.OBJECT_FORMS);

        final ObjectAdapter serviceAdapter = getServiceAdapter(serviceId);

        final DomainObjectReprRenderer renderer = new DomainObjectReprRenderer(getResourceContext(), null, JsonRepresentation.newMap());
        renderer.usingLinkToBuilder(new DomainServiceLinkTo())
            .with(serviceAdapter)
            .includesSelf();

        return Responses.ofOk(renderer, Caching.ONE_DAY).build();
    }

    @Override
    public Response deleteServiceNotAllowed(@PathParam("serviceId") String serviceId) {
        throw RestfulObjectsApplicationException.createWithMessage(RestfulResponse.HttpStatusCode.METHOD_NOT_ALLOWED, "Deleting a service resource is not allowed.");
    }

    @Override
    public Response putServiceNotAllowed(@PathParam("serviceId") String serviceId) {
        throw RestfulObjectsApplicationException.createWithMessage(RestfulResponse.HttpStatusCode.METHOD_NOT_ALLOWED, "Putting to a service resource is not allowed.");
    }

    @Override
    public Response postServiceNotAllowed(@PathParam("serviceId") String serviceId) {
        throw RestfulObjectsApplicationException.createWithMessage(RestfulResponse.HttpStatusCode.METHOD_NOT_ALLOWED, "Posting to a service resource is not allowed.");
    }


    // //////////////////////////////////////////////////////////
    // domain service action
    // //////////////////////////////////////////////////////////

    @Override
    @GET
    @Path("/{serviceId}/actions/{actionId}")
    @Produces({ MediaType.APPLICATION_JSON, RestfulMediaType.APPLICATION_JSON_OBJECT_ACTION, RestfulMediaType.APPLICATION_JSON_ERROR })
    public Response actionPrompt(@PathParam("serviceId") final String serviceId, @PathParam("actionId") final String actionId) {
        init(RepresentationType.OBJECT_ACTION, Where.OBJECT_FORMS);

        final ObjectAdapter serviceAdapter = getServiceAdapter(serviceId);
        final DomainResourceHelper helper = new DomainResourceHelper(getResourceContext(), serviceAdapter).using(new DomainServiceLinkTo());

        return helper.actionPrompt(actionId);
    }

    @Override
    public Response deleteActionPromptNotAllowed(@PathParam("serviceId") String serviceId, @PathParam("actionId") String actionId) {
        throw RestfulObjectsApplicationException.createWithMessage(RestfulResponse.HttpStatusCode.METHOD_NOT_ALLOWED, "Deleting action prompt resource is not allowed.");
    }

    @Override
    public Response putActionPromptNotAllowed(@PathParam("serviceId") String serviceId, @PathParam("actionId") String actionId) {
        throw RestfulObjectsApplicationException.createWithMessage(RestfulResponse.HttpStatusCode.METHOD_NOT_ALLOWED, "Putting to an action prompt resource is not allowed.");
    }

    @Override
    public Response postActionPromptNotAllowed(@PathParam("serviceId") String serviceId, @PathParam("actionId") String actionId) {
        throw RestfulObjectsApplicationException.createWithMessage(RestfulResponse.HttpStatusCode.METHOD_NOT_ALLOWED, "Posting to an action prompt resource is not allowed.");
    }

    // //////////////////////////////////////////////////////////
    // domain service action invoke
    // //////////////////////////////////////////////////////////

    @Override
    @GET
    @Path("/{serviceId}/actions/{actionId}/invoke")
    @Produces({ MediaType.APPLICATION_JSON, RestfulMediaType.APPLICATION_JSON_ACTION_RESULT, RestfulMediaType.APPLICATION_JSON_ERROR })
    public Response invokeActionQueryOnly(
            final @PathParam("serviceId") String serviceId,
            final @PathParam("actionId") String actionId,
            final @QueryParam("x-isis-querystring") String xIsisUrlEncodedQueryString) {


        final String urlUnencodedQueryString = UrlEncodingUtils.urlDecodeNullSafe(xIsisUrlEncodedQueryString != null? xIsisUrlEncodedQueryString: httpServletRequest.getQueryString());
        init(RepresentationType.ACTION_RESULT, Where.STANDALONE_TABLES, urlUnencodedQueryString);


        final JsonRepresentation arguments = getResourceContext().getQueryStringAsJsonRepr();
        
        final ObjectAdapter serviceAdapter = getServiceAdapter(serviceId);
        final DomainResourceHelper helper = new DomainResourceHelper(getResourceContext(), serviceAdapter).using(new DomainServiceLinkTo());

        return helper.invokeActionQueryOnly(actionId, arguments);
    }


    @Override
    @PUT
    @Path("/{serviceId}/actions/{actionId}/invoke")
    @Consumes({ MediaType.WILDCARD })
    // to save the client having to specify a Content-Type: application/json
    @Produces({ MediaType.APPLICATION_JSON, RestfulMediaType.APPLICATION_JSON_ACTION_RESULT, RestfulMediaType.APPLICATION_JSON_ERROR })
    public Response invokeActionIdempotent(
            final @PathParam("serviceId") String serviceId,
            final @PathParam("actionId") String actionId,
            final InputStream body) {
        init(RepresentationType.ACTION_RESULT, Where.STANDALONE_TABLES, body);

        final JsonRepresentation arguments = getResourceContext().getQueryStringAsJsonRepr();
        
        final ObjectAdapter serviceAdapter = getServiceAdapter(serviceId);
        final DomainResourceHelper helper = new DomainResourceHelper(getResourceContext(), serviceAdapter).using(new DomainServiceLinkTo());

        return helper.invokeActionIdempotent(actionId, arguments);
    }


    @Override
    @POST
    @Path("/{serviceId}/actions/{actionId}/invoke")
    @Consumes({ MediaType.WILDCARD })
    // to save the client having to specify a Content-Type: application/json
    @Produces({ MediaType.APPLICATION_JSON, RestfulMediaType.APPLICATION_JSON_ACTION_RESULT, RestfulMediaType.APPLICATION_JSON_ERROR })
    public Response invokeAction(@PathParam("serviceId") final String serviceId, @PathParam("actionId") final String actionId, final InputStream body) {
        init(RepresentationType.ACTION_RESULT, Where.STANDALONE_TABLES, body);

        final JsonRepresentation arguments = getResourceContext().getQueryStringAsJsonRepr();
        
        final ObjectAdapter serviceAdapter = getServiceAdapter(serviceId);
        final DomainResourceHelper helper = new DomainResourceHelper(getResourceContext(), serviceAdapter).using(new DomainServiceLinkTo());

        return helper.invokeAction(actionId, arguments);
    }

    @Override
    public Response deleteInvokeActionNotAllowed(@PathParam("serviceId") String serviceId, @PathParam("actionId") String actionId) {
        throw RestfulObjectsApplicationException.createWithMessage(RestfulResponse.HttpStatusCode.METHOD_NOT_ALLOWED, "Deleting an action invocation resource is not allowed.");
    }

}
