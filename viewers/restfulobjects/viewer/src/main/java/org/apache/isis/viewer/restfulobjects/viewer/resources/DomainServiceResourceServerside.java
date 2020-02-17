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
package org.apache.isis.viewer.restfulobjects.viewer.resources;

import java.io.InputStream;
import java.util.function.Predicate;
import java.util.stream.Stream;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.springframework.stereotype.Component;

import org.apache.isis.applib.annotation.Where;
import org.apache.isis.applib.services.command.Command;
import org.apache.isis.core.commons.internal.codec._UrlDecoderUtil;
import org.apache.isis.core.config.IsisConfiguration;
import org.apache.isis.core.metamodel.context.MetaModelContext;
import org.apache.isis.core.metamodel.facets.object.domainservice.DomainServiceFacet;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.viewer.restfulobjects.applib.JsonRepresentation;
import org.apache.isis.viewer.restfulobjects.applib.RepresentationType;
import org.apache.isis.viewer.restfulobjects.applib.RestfulMediaType;
import org.apache.isis.viewer.restfulobjects.applib.RestfulResponse;
import org.apache.isis.viewer.restfulobjects.applib.domainobjects.DomainServiceResource;
import org.apache.isis.viewer.restfulobjects.rendering.Caching;
import org.apache.isis.viewer.restfulobjects.rendering.Responses;
import org.apache.isis.viewer.restfulobjects.rendering.RestfulObjectsApplicationException;
import org.apache.isis.viewer.restfulobjects.rendering.domainobjects.DomainObjectReprRenderer;
import org.apache.isis.viewer.restfulobjects.rendering.domainobjects.DomainServiceLinkTo;
import org.apache.isis.viewer.restfulobjects.rendering.service.RepresentationService;

import lombok.val;

@Component
@Path("/services")
public class DomainServiceResourceServerside extends ResourceAbstract implements DomainServiceResource {

    private static final Predicate<ManagedObject> NATURE_REST_ALSO = (final ManagedObject input) -> {
        final ObjectSpecification specification = input.getSpecification();
        final DomainServiceFacet facet = specification.getFacet(DomainServiceFacet.class);
        if (facet == null) {
            // not expected, because we know these are domain services.
            return false;
        }
        val natureOfService = facet.getNatureOfService();
        return natureOfService.isRestAlso();
    };

    @Inject
    public DomainServiceResourceServerside(
            final MetaModelContext metaModelContext,
            final IsisConfiguration isisConfiguration) {
        super(metaModelContext, isisConfiguration);
    }

    @Override
    @GET
    @Path("/")
    @Produces({ MediaType.APPLICATION_JSON, RestfulMediaType.APPLICATION_JSON_LIST, RestfulMediaType.APPLICATION_JSON_ERROR })
    public Response services() {
        
        val resourceContext = createResourceContext(
                RepresentationType.LIST, Where.STANDALONE_TABLES, RepresentationService.Intent.NOT_APPLICABLE);

        val metaModelContext = resourceContext.getMetaModelContext();
        
        final Stream<ManagedObject> serviceAdapters = metaModelContext.streamServiceAdapters()
                .filter(NATURE_REST_ALSO);

        final DomainServicesListReprRenderer renderer = new DomainServicesListReprRenderer(resourceContext, null, JsonRepresentation.newMap());
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
    @Produces({
        MediaType.APPLICATION_JSON, RestfulMediaType.APPLICATION_JSON_OBJECT, RestfulMediaType.APPLICATION_JSON_ERROR,
        MediaType.APPLICATION_XML, RestfulMediaType.APPLICATION_XML_OBJECT, RestfulMediaType.APPLICATION_XML_ERROR
    })
    public Response service(@PathParam("serviceId") final String serviceId) {
        
        val resourceContext = createResourceContext(
                RepresentationType.DOMAIN_OBJECT, Where.OBJECT_FORMS, RepresentationService.Intent.ALREADY_PERSISTENT);

        val serviceAdapter = getServiceAdapter(serviceId);

        final DomainObjectReprRenderer renderer = new DomainObjectReprRenderer(resourceContext, null, JsonRepresentation.newMap());
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
    @Produces({
        MediaType.APPLICATION_JSON, RestfulMediaType.APPLICATION_JSON_OBJECT_ACTION, RestfulMediaType.APPLICATION_JSON_ERROR,
        MediaType.APPLICATION_XML, RestfulMediaType.APPLICATION_XML_OBJECT_ACTION, RestfulMediaType.APPLICATION_XML_ERROR
    })
    public Response actionPrompt(@PathParam("serviceId") final String serviceId, @PathParam("actionId") final String actionId) {
        
        val resourceContext = createResourceContext(
                RepresentationType.OBJECT_ACTION, Where.OBJECT_FORMS, RepresentationService.Intent.ALREADY_PERSISTENT);

        val serviceAdapter = getServiceAdapter(serviceId);
        val domainResourceHelper = DomainResourceHelper.ofServiceResource(resourceContext, serviceAdapter);

        return domainResourceHelper.actionPrompt(actionId);
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
    @Produces({
        MediaType.APPLICATION_JSON, RestfulMediaType.APPLICATION_JSON_ACTION_RESULT, RestfulMediaType.APPLICATION_JSON_ERROR,
        MediaType.APPLICATION_XML, RestfulMediaType.APPLICATION_XML_ACTION_RESULT, RestfulMediaType.APPLICATION_XML_ERROR
    })
    public Response invokeActionQueryOnly(
            final @PathParam("serviceId") String serviceId,
            final @PathParam("actionId") String actionId,
            final @QueryParam("x-isis-querystring") String xIsisUrlEncodedQueryString) {

        final String urlUnencodedQueryString = _UrlDecoderUtil
                .urlDecodeNullSafe(xIsisUrlEncodedQueryString != null? xIsisUrlEncodedQueryString: httpServletRequest.getQueryString());
        val resourceContext = createResourceContext(
                ResourceDescriptor.of(RepresentationType.ACTION_RESULT, Where.STANDALONE_TABLES, RepresentationService.Intent.NOT_APPLICABLE),
                urlUnencodedQueryString);

        setCommandExecutor(Command.Executor.USER);

        final JsonRepresentation arguments = resourceContext.getQueryStringAsJsonRepr();

        val serviceAdapter = getServiceAdapter(serviceId);
        val domainResourceHelper = DomainResourceHelper.ofServiceResource(resourceContext, serviceAdapter);

        return domainResourceHelper.invokeActionQueryOnly(actionId, arguments);
    }


    @Override
    @PUT
    @Path("/{serviceId}/actions/{actionId}/invoke")
    @Consumes({ MediaType.WILDCARD }) // to save the client having to specify a Content-Type: application/json
    @Produces({
        MediaType.APPLICATION_JSON, RestfulMediaType.APPLICATION_JSON_ACTION_RESULT, RestfulMediaType.APPLICATION_JSON_ERROR,
        MediaType.APPLICATION_XML, RestfulMediaType.APPLICATION_XML_ACTION_RESULT, RestfulMediaType.APPLICATION_XML_ERROR
    })
    public Response invokeActionIdempotent(
            final @PathParam("serviceId") String serviceId,
            final @PathParam("actionId") String actionId,
            final InputStream body) {
        
        val resourceContext = createResourceContext(
                ResourceDescriptor.of(RepresentationType.ACTION_RESULT, Where.STANDALONE_TABLES, RepresentationService.Intent.NOT_APPLICABLE),
                body);

        setCommandExecutor(Command.Executor.USER);

        final JsonRepresentation arguments = resourceContext.getQueryStringAsJsonRepr();

        val serviceAdapter = getServiceAdapter(serviceId);
        val domainResourceHelper = DomainResourceHelper.ofServiceResource(resourceContext, serviceAdapter);

        return domainResourceHelper.invokeActionIdempotent(actionId, arguments);
    }

    @Override
    @POST
    @Path("/{serviceId}/actions/{actionId}/invoke")
    @Consumes({ MediaType.WILDCARD }) // to save the client having to specify a Content-Type: application/json
    @Produces({
        MediaType.APPLICATION_JSON, RestfulMediaType.APPLICATION_JSON_ACTION_RESULT, RestfulMediaType.APPLICATION_JSON_ERROR,
        MediaType.APPLICATION_XML, RestfulMediaType.APPLICATION_XML_ACTION_RESULT, RestfulMediaType.APPLICATION_XML_ERROR
    })
    public Response invokeAction(@PathParam("serviceId") final String serviceId, @PathParam("actionId") final String actionId, final InputStream body) {
        
        val resourceContext = createResourceContext(
                ResourceDescriptor.of(RepresentationType.ACTION_RESULT, Where.STANDALONE_TABLES, RepresentationService.Intent.NOT_APPLICABLE),
                body);

        setCommandExecutor(Command.Executor.USER);

        final JsonRepresentation arguments = resourceContext.getQueryStringAsJsonRepr();

        val serviceAdapter = getServiceAdapter(serviceId);
        val domainResourceHelper = DomainResourceHelper.ofServiceResource(resourceContext, serviceAdapter);

        return domainResourceHelper.invokeAction(actionId, arguments);
    }

    @Override
    public Response deleteInvokeActionNotAllowed(@PathParam("serviceId") String serviceId, @PathParam("actionId") String actionId) {
        throw RestfulObjectsApplicationException.createWithMessage(RestfulResponse.HttpStatusCode.METHOD_NOT_ALLOWED, "Deleting an action invocation resource is not allowed.");
    }



}
