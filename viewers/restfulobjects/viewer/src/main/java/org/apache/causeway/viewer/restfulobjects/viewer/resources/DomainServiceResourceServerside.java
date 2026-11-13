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

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import org.apache.causeway.applib.annotation.Where;
import org.apache.causeway.commons.io.UrlUtils;
import org.apache.causeway.viewer.restfulobjects.applib.JsonRepresentation;
import org.apache.causeway.viewer.restfulobjects.applib.RepresentationType;
import org.apache.causeway.viewer.restfulobjects.applib.domainobjects.DomainServiceResource;
import org.apache.causeway.viewer.restfulobjects.rendering.Caching;
import org.apache.causeway.viewer.restfulobjects.rendering.RestfulObjectsApplicationException;
import org.apache.causeway.viewer.restfulobjects.rendering.context.ResourceDescriptor;
import org.apache.causeway.viewer.restfulobjects.rendering.context.ResourceDescriptor.ResourceLink;
import org.apache.causeway.viewer.restfulobjects.rendering.domainobjects.DomainObjectReprRenderer;
import org.apache.causeway.viewer.restfulobjects.rendering.domainobjects.DomainServiceLinkTo;
import org.apache.causeway.viewer.restfulobjects.rendering.service.RepresentationService;
import org.apache.causeway.viewer.restfulobjects.rendering.util.RequestParams;

import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
public class DomainServiceResourceServerside
extends ResourceAbstract
implements DomainServiceResource {

    public DomainServiceResourceServerside() {
        super();
        log.debug("<init>");
    }

    @Override
    public ResponseEntity<Object> services() {

        var resourceContext = createResourceContext(
                RepresentationType.LIST, Where.STANDALONE_TABLES, RepresentationService.Intent.NOT_APPLICABLE);

        var renderer = new DomainServicesListReprRenderer(
                resourceContext, null, JsonRepresentation.newMap());
        renderer
            .usingLinkToBuilder(new DomainServiceLinkTo())
            .includesSelf()
            .with(resourceContext.streamServiceAdapters());

        return _EndpointLogging.response(log, "GET /services/",
                responseFactory.ok(renderer, Caching.ONE_DAY));
    }

    @Override
    public ResponseEntity<Object> deleteServicesNotAllowed() {
        throw _EndpointLogging.error(log, "DELETE /services",
                RestfulObjectsApplicationException
                .createWithMessage(
                        HttpStatus.METHOD_NOT_ALLOWED,
                        "Deleting the services resource is not allowed."));
    }

    @Override
    public ResponseEntity<Object> putServicesNotAllowed() {
        throw _EndpointLogging.error(log, "PUT /services",
                RestfulObjectsApplicationException
                .createWithMessage(
                        HttpStatus.METHOD_NOT_ALLOWED,
                        "Putting to the services resource is not allowed."));
    }

    @Override
    public ResponseEntity<Object> postServicesNotAllowed() {
        throw _EndpointLogging.error(log, "POST /services",
                RestfulObjectsApplicationException
                .createWithMessage(
                        HttpStatus.METHOD_NOT_ALLOWED,
                        "Posting to the services resource is not allowed."));
    }

    // DOMAIN SERVICE

    @Override
    public ResponseEntity<Object> service(
            final String serviceId) {

        var resourceContext = createResourceContext(new ResourceDescriptor(
                RepresentationType.DOMAIN_OBJECT, Where.OBJECT_FORMS,
                RepresentationService.Intent.ALREADY_PERSISTENT, ResourceLink.SERVICE));

        var serviceAdapter = resourceContext.lookupServiceAdapterElseFail(serviceId);

        var renderer = new DomainObjectReprRenderer(resourceContext, null, JsonRepresentation.newMap());
        renderer
            .usingLinkToBuilder(new DomainServiceLinkTo())
            .with(serviceAdapter)
            .includesSelf();

        return _EndpointLogging.response(log, "GET /services/{}", serviceId,
                responseFactory.ok(renderer, Caching.ONE_DAY));
    }

    @Override
    public ResponseEntity<Object> deleteServiceNotAllowed(
            final String serviceId) {
        throw _EndpointLogging.error(log, "DELETE /services/{}", serviceId,
                RestfulObjectsApplicationException
                .createWithMessage(
                        HttpStatus.METHOD_NOT_ALLOWED,
                        "Deleting a service resource is not allowed."));
    }

    @Override
    public ResponseEntity<Object> putServiceNotAllowed(
            final String serviceId) {
        throw _EndpointLogging.error(log, "PUT /services/{}", serviceId,
                RestfulObjectsApplicationException
                .createWithMessage(
                        HttpStatus.METHOD_NOT_ALLOWED,
                        "Putting to a service resource is not allowed."));
    }

    @Override
    public ResponseEntity<Object> postServiceNotAllowed(
            final String serviceId) {
        throw _EndpointLogging.error(log, "POST /services/{}", serviceId,
                RestfulObjectsApplicationException
                .createWithMessage(
                        HttpStatus.METHOD_NOT_ALLOWED,
                        "Posting to a service resource is not allowed."));
    }

    // DOMAIN SERVICE ACTION

    @Override
    public ResponseEntity<Object> actionPrompt(
            final String serviceId,
            final String actionId) {

        var resourceContext = createResourceContext(new ResourceDescriptor(
                RepresentationType.OBJECT_ACTION, Where.OBJECT_FORMS,
                RepresentationService.Intent.ALREADY_PERSISTENT, ResourceLink.SERVICE));

        var domainResourceHelper = _DomainResourceHelper.ofServiceResource(resourceContext, serviceId);

        return _EndpointLogging.response(log, "GET /services/{}/actions/{}", serviceId, actionId,
                domainResourceHelper.actionPrompt(actionId));
    }

    @Override
    public ResponseEntity<Object> deleteActionPromptNotAllowed(
            final String serviceId,
            final String actionId) {
        throw _EndpointLogging.error(log, "DELETE /services/{}/actions/{}", serviceId, actionId,
                RestfulObjectsApplicationException
                .createWithMessage(
                        HttpStatus.METHOD_NOT_ALLOWED,
                        "Deleting action prompt resource is not allowed."));
    }

    @Override
    public ResponseEntity<Object> putActionPromptNotAllowed(
            final String serviceId,
            final String actionId) {
        throw _EndpointLogging.error(log, "PUT /services/{}/actions/{}", serviceId, actionId,
                RestfulObjectsApplicationException
                .createWithMessage(
                        HttpStatus.METHOD_NOT_ALLOWED,
                        "Putting to an action prompt resource is not allowed."));
    }

    @Override
    public ResponseEntity<Object> postActionPromptNotAllowed(
            final String serviceId,
            final String actionId) {
        throw _EndpointLogging.error(log, "POST /services/{}/actions/{}", serviceId, actionId,
                RestfulObjectsApplicationException
                .createWithMessage(
                        HttpStatus.METHOD_NOT_ALLOWED,
                        "Posting to an action prompt resource is not allowed."));
    }

    // DOMAIN SERVICE ACTION INVOKE

    @Override
    public ResponseEntity<Object> invokeActionQueryOnly(
            final String serviceId,
            final String actionId,
            final String xCausewayUrlEncodedQueryString) {

        final String urlUnencodedQueryString = UrlUtils.urlDecodeUtf8(
                xCausewayUrlEncodedQueryString != null
                    ? xCausewayUrlEncodedQueryString
                    : httpServletRequest.getQueryString());
        var resourceContext = createResourceContext(
                new ResourceDescriptor(RepresentationType.ACTION_RESULT, Where.STANDALONE_TABLES,
                    RepresentationService.Intent.NOT_APPLICABLE, ResourceLink.SERVICE),
                RequestParams.ofQueryString(urlUnencodedQueryString));

        final JsonRepresentation arguments = resourceContext.queryStringAsJsonRepr();

        var domainResourceHelper = _DomainResourceHelper.ofServiceResource(resourceContext, serviceId);

        return _EndpointLogging.response(log, "GET /services/{}/actions/{}/invoke", serviceId, actionId,
                domainResourceHelper.invokeActionQueryOnly(actionId, arguments));
    }

    @Override
    public ResponseEntity<Object> invokeActionIdempotent(
            final String serviceId,
            final String actionId,
            final InputStream body) {

        var resourceContext = createResourceContext(
                new ResourceDescriptor(RepresentationType.ACTION_RESULT, Where.STANDALONE_TABLES,
                    RepresentationService.Intent.NOT_APPLICABLE, ResourceLink.SERVICE),
                body);

        final JsonRepresentation arguments = resourceContext.queryStringAsJsonRepr();

        var domainResourceHelper = _DomainResourceHelper.ofServiceResource(resourceContext, serviceId);

        return _EndpointLogging.response(log, "PUT /services/{}/actions/{}/invoke", serviceId, actionId,
                domainResourceHelper.invokeActionIdempotent(actionId, arguments));
    }

    @Override
    public ResponseEntity<Object> invokeAction(
            final String serviceId,
            final String actionId,
            final InputStream body) {

        var resourceContext = createResourceContext(
                new ResourceDescriptor(RepresentationType.ACTION_RESULT, Where.STANDALONE_TABLES,
                    RepresentationService.Intent.NOT_APPLICABLE, ResourceLink.SERVICE),
                body);

        final JsonRepresentation arguments = resourceContext.queryStringAsJsonRepr();

        var domainResourceHelper = _DomainResourceHelper.ofServiceResource(resourceContext, serviceId);

        return _EndpointLogging.response(log, "POST /services/{}/actions/{}/invoke", serviceId, actionId,
                domainResourceHelper.invokeAction(actionId, arguments));
    }

    @Override
    public ResponseEntity<Object> deleteInvokeActionNotAllowed(
            final String serviceId,
            final String actionId) {
        throw _EndpointLogging.error(log, "DELETE /services/{}/actions/{}/invoke", serviceId, actionId,
                RestfulObjectsApplicationException
                .createWithMessage(
                    HttpStatus.METHOD_NOT_ALLOWED,
                        "Deleting an action invocation resource is not allowed."));
    }

}
