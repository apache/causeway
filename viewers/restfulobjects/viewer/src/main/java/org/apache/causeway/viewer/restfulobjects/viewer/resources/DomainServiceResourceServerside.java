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
package org.apache.causeway.viewer.restfulobjects.viewer.resources;

import java.io.InputStream;
import java.util.stream.Stream;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.springframework.stereotype.Component;

import org.apache.causeway.applib.annotation.Where;
import org.apache.causeway.commons.io.UrlUtils;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.viewer.restfulobjects.applib.JsonRepresentation;
import org.apache.causeway.viewer.restfulobjects.applib.RepresentationType;
import org.apache.causeway.viewer.restfulobjects.applib.RestfulMediaType;
import org.apache.causeway.viewer.restfulobjects.applib.RestfulResponse;
import org.apache.causeway.viewer.restfulobjects.applib.domainobjects.DomainServiceResource;
import org.apache.causeway.viewer.restfulobjects.rendering.Caching;
import org.apache.causeway.viewer.restfulobjects.rendering.Responses;
import org.apache.causeway.viewer.restfulobjects.rendering.RestfulObjectsApplicationException;
import org.apache.causeway.viewer.restfulobjects.rendering.domainobjects.DomainObjectReprRenderer;
import org.apache.causeway.viewer.restfulobjects.rendering.domainobjects.DomainServiceLinkTo;
import org.apache.causeway.viewer.restfulobjects.rendering.service.RepresentationService;
import org.apache.causeway.viewer.restfulobjects.rendering.util.RequestParams;

import lombok.extern.log4j.Log4j2;

@Component
@Path("/services")
@Log4j2
public class DomainServiceResourceServerside
extends ResourceAbstract
implements DomainServiceResource {

    public DomainServiceResourceServerside() {
        super();
        log.debug("<init>");
    }

    @Override
    @GET
    @Path("/")
    @Produces({
        MediaType.APPLICATION_JSON,
        RestfulMediaType.APPLICATION_JSON_LIST,
        RestfulMediaType.APPLICATION_JSON_ERROR })
    public Response services() {

        var resourceContext = createResourceContext(
                RepresentationType.LIST, Where.STANDALONE_TABLES, RepresentationService.Intent.NOT_APPLICABLE);

        final Stream<ManagedObject> serviceAdapters = resourceContext.streamServiceAdapters();

        final DomainServicesListReprRenderer renderer = new DomainServicesListReprRenderer(
                resourceContext, null, JsonRepresentation.newMap());
        renderer.usingLinkToBuilder(new DomainServiceLinkTo())
        .includesSelf()
        .with(serviceAdapters);

        return _EndpointLogging.response(log, "GET /services/",
                Responses.ofOk(renderer, Caching.ONE_DAY).build());
    }

    @Override
    public Response deleteServicesNotAllowed() {
        throw _EndpointLogging.error(log, "DELETE /services",
                RestfulObjectsApplicationException
                .createWithMessage(
                        RestfulResponse.HttpStatusCode.METHOD_NOT_ALLOWED,
                        "Deleting the services resource is not allowed."));
    }

    @Override
    public Response putServicesNotAllowed() {
        throw _EndpointLogging.error(log, "PUT /services",
                RestfulObjectsApplicationException
                .createWithMessage(
                        RestfulResponse.HttpStatusCode.METHOD_NOT_ALLOWED,
                        "Putting to the services resource is not allowed."));
    }

    @Override
    public Response postServicesNotAllowed() {
        throw _EndpointLogging.error(log, "POST /services",
                RestfulObjectsApplicationException
                .createWithMessage(
                        RestfulResponse.HttpStatusCode.METHOD_NOT_ALLOWED,
                        "Posting to the services resource is not allowed."));
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
    public Response service(
            @PathParam("serviceId") final String serviceId) {

        var resourceContext = createResourceContext(
                RepresentationType.DOMAIN_OBJECT, Where.OBJECT_FORMS, RepresentationService.Intent.ALREADY_PERSISTENT);

        var serviceAdapter = _DomainResourceHelper.getServiceAdapter(resourceContext, serviceId);

        var renderer = new DomainObjectReprRenderer(resourceContext, null, JsonRepresentation.newMap());
        renderer.usingLinkToBuilder(new DomainServiceLinkTo())
        .with(serviceAdapter)
        .includesSelf();

        return _EndpointLogging.response(log, "GET /services/{}", serviceId,
                Responses.ofOk(renderer, Caching.ONE_DAY).build());
    }

    @DELETE
    @Path("/{serviceId}")
    @Override
    public Response deleteServiceNotAllowed(
            @PathParam("serviceId") final String serviceId) {
        throw _EndpointLogging.error(log, "DELETE /services/{}", serviceId,
                RestfulObjectsApplicationException
                .createWithMessage(
                        RestfulResponse.HttpStatusCode.METHOD_NOT_ALLOWED,
                        "Deleting a service resource is not allowed."));
    }

    @PUT
    @Path("/{serviceId}")
    @Override
    public Response putServiceNotAllowed(
            @PathParam("serviceId") final String serviceId) {
        throw _EndpointLogging.error(log, "PUT /services/{}", serviceId,
                RestfulObjectsApplicationException
                .createWithMessage(
                        RestfulResponse.HttpStatusCode.METHOD_NOT_ALLOWED,
                        "Putting to a service resource is not allowed."));
    }

    @POST
    @Path("/{serviceId}")
    @Override
    public Response postServiceNotAllowed(
            @PathParam("serviceId") final String serviceId) {
        throw _EndpointLogging.error(log, "POST /services/{}", serviceId,
                RestfulObjectsApplicationException
                .createWithMessage(
                        RestfulResponse.HttpStatusCode.METHOD_NOT_ALLOWED,
                        "Posting to a service resource is not allowed."));
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
    public Response actionPrompt(
            @PathParam("serviceId") final String serviceId,
            @PathParam("actionId") final String actionId) {

        var resourceContext = createResourceContext(
                RepresentationType.OBJECT_ACTION, Where.OBJECT_FORMS, RepresentationService.Intent.ALREADY_PERSISTENT);

        var domainResourceHelper = _DomainResourceHelper.ofServiceResource(resourceContext, serviceId);

        return _EndpointLogging.response(log, "GET /services/{}/actions/{}", serviceId, actionId,
                domainResourceHelper.actionPrompt(actionId));
    }

    @DELETE
    @Path("/{serviceId}/actions/{actionId}")
    @Override
    public Response deleteActionPromptNotAllowed(
            @PathParam("serviceId") final String serviceId,
            @PathParam("actionId") final String actionId) {
        throw _EndpointLogging.error(log, "DELETE /services/{}/actions/{}", serviceId, actionId,
                RestfulObjectsApplicationException
                .createWithMessage(
                        RestfulResponse.HttpStatusCode.METHOD_NOT_ALLOWED,
                        "Deleting action prompt resource is not allowed."));
    }

    @PUT
    @Path("/{serviceId}/actions/{actionId}")
    @Override
    public Response putActionPromptNotAllowed(
            @PathParam("serviceId") final String serviceId,
            @PathParam("actionId") final String actionId) {
        throw _EndpointLogging.error(log, "PUT /services/{}/actions/{}", serviceId, actionId,
                RestfulObjectsApplicationException
                .createWithMessage(
                        RestfulResponse.HttpStatusCode.METHOD_NOT_ALLOWED,
                        "Putting to an action prompt resource is not allowed."));
    }

    @POST
    @Path("/{serviceId}/actions/{actionId}")
    @Override
    public Response postActionPromptNotAllowed(
            @PathParam("serviceId") final String serviceId,
            @PathParam("actionId") final String actionId) {
        throw _EndpointLogging.error(log, "POST /services/{}/actions/{}", serviceId, actionId,
                RestfulObjectsApplicationException
                .createWithMessage(
                        RestfulResponse.HttpStatusCode.METHOD_NOT_ALLOWED,
                        "Posting to an action prompt resource is not allowed."));
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
            final @QueryParam("x-causeway-querystring") String xCausewayUrlEncodedQueryString) {

        final String urlUnencodedQueryString = UrlUtils.urlDecodeUtf8(
                xCausewayUrlEncodedQueryString != null
                    ? xCausewayUrlEncodedQueryString
                    : httpServletRequest.getQueryString());
        var resourceContext = createResourceContext(
                new ResourceDescriptor(RepresentationType.ACTION_RESULT, Where.STANDALONE_TABLES, RepresentationService.Intent.NOT_APPLICABLE),
                RequestParams.ofQueryString(urlUnencodedQueryString));

        final JsonRepresentation arguments = resourceContext.getQueryStringAsJsonRepr();

        var domainResourceHelper = _DomainResourceHelper.ofServiceResource(resourceContext, serviceId);

        return _EndpointLogging.response(log, "GET /services/{}/actions/{}/invoke", serviceId, actionId,
                domainResourceHelper.invokeActionQueryOnly(actionId, arguments));
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

        var resourceContext = createResourceContext(
                new ResourceDescriptor(RepresentationType.ACTION_RESULT, Where.STANDALONE_TABLES, RepresentationService.Intent.NOT_APPLICABLE),
                body);

        final JsonRepresentation arguments = resourceContext.getQueryStringAsJsonRepr();

        var domainResourceHelper = _DomainResourceHelper.ofServiceResource(resourceContext, serviceId);

        return _EndpointLogging.response(log, "PUT /services/{}/actions/{}/invoke", serviceId, actionId,
                domainResourceHelper.invokeActionIdempotent(actionId, arguments));
    }

    @Override
    @POST
    @Path("/{serviceId}/actions/{actionId}/invoke")
    @Consumes({ MediaType.WILDCARD }) // to save the client having to specify a Content-Type: application/json
    @Produces({
        MediaType.APPLICATION_JSON, RestfulMediaType.APPLICATION_JSON_ACTION_RESULT, RestfulMediaType.APPLICATION_JSON_ERROR,
        MediaType.APPLICATION_XML, RestfulMediaType.APPLICATION_XML_ACTION_RESULT, RestfulMediaType.APPLICATION_XML_ERROR
    })
    public Response invokeAction(
            @PathParam("serviceId") final String serviceId,
            @PathParam("actionId") final String actionId,
            final InputStream body) {

        var resourceContext = createResourceContext(
                new ResourceDescriptor(RepresentationType.ACTION_RESULT, Where.STANDALONE_TABLES, RepresentationService.Intent.NOT_APPLICABLE),
                body);

        final JsonRepresentation arguments = resourceContext.getQueryStringAsJsonRepr();

        var domainResourceHelper = _DomainResourceHelper.ofServiceResource(resourceContext, serviceId);

        return _EndpointLogging.response(log, "POST /services/{}/actions/{}/invoke", serviceId, actionId,
                domainResourceHelper.invokeAction(actionId, arguments));
    }

    @DELETE
    @Path("/{serviceId}/actions/{actionId}/invoke")
    @Override
    public Response deleteInvokeActionNotAllowed(
            @PathParam("serviceId") final String serviceId,
            @PathParam("actionId") final String actionId) {
        throw _EndpointLogging.error(log, "DELETE /services/{}/actions/{}/invoke", serviceId, actionId,
                RestfulObjectsApplicationException
                .createWithMessage(
                        RestfulResponse.HttpStatusCode.METHOD_NOT_ALLOWED,
                        "Deleting an action invocation resource is not allowed."));
    }

}
