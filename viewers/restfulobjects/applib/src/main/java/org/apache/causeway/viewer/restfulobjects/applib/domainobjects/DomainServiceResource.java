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
package org.apache.causeway.viewer.restfulobjects.applib.domainobjects;

import java.io.InputStream;

import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.ws.rs.core.Response;

import org.apache.causeway.viewer.restfulobjects.applib.RestfulMediaType;

/**
 * @since 1.x {@index}
 */
@RequestMapping("${roPrefix}/services")
public interface DomainServiceResource {

    @GetMapping(produces = { MediaType.APPLICATION_JSON_VALUE, RestfulMediaType.APPLICATION_JSON_LIST, RestfulMediaType.APPLICATION_JSON_ERROR })
    public Response services();

    @DeleteMapping
    public Response deleteServicesNotAllowed();

    @PutMapping
    public Response putServicesNotAllowed();

    @PostMapping
    public Response postServicesNotAllowed();

    // -- DOMAIN SERVICE

    @GetMapping(
        path = "/{serviceId}",
        produces = {
            MediaType.APPLICATION_JSON_VALUE, RestfulMediaType.APPLICATION_JSON_OBJECT, RestfulMediaType.APPLICATION_JSON_ERROR,
            MediaType.APPLICATION_XML_VALUE, RestfulMediaType.APPLICATION_XML_OBJECT, RestfulMediaType.APPLICATION_XML_ERROR
    })
    public Response service(@PathParam("serviceId") final String serviceId);

    @DeleteMapping(path = "/{serviceId}")
    public Response deleteServiceNotAllowed(@PathParam("serviceId") final String serviceId);

    @PutMapping(path = "/{serviceId}")
    public Response putServiceNotAllowed(@PathParam("serviceId") final String serviceId);

    @PostMapping(path = "/{serviceId}")
    public Response postServiceNotAllowed(@PathParam("serviceId") final String serviceId);

    // -- DOMAIN SERVICE ACTION

    @GetMapping(
        path = "/{serviceId}/actions/{actionId}",
        produces = {
            MediaType.APPLICATION_JSON_VALUE, RestfulMediaType.APPLICATION_JSON_OBJECT_ACTION, RestfulMediaType.APPLICATION_JSON_ERROR,
            MediaType.APPLICATION_XML_VALUE, RestfulMediaType.APPLICATION_XML_OBJECT_ACTION, RestfulMediaType.APPLICATION_XML_ERROR
    })
    public Response actionPrompt(@PathParam("serviceId") final String serviceId, @PathParam("actionId") final String actionId);

    @DeleteMapping(path = "/{serviceId}/actions/{actionId}")
    public Response deleteActionPromptNotAllowed(@PathParam("serviceId") final String serviceId, @PathParam("actionId") final String actionId);

    @PutMapping(path = "/{serviceId}/actions/{actionId}")
    public Response putActionPromptNotAllowed(@PathParam("serviceId") final String serviceId, @PathParam("actionId") final String actionId);

    @PostMapping(path = "/{serviceId}/actions/{actionId}")
    public Response postActionPromptNotAllowed(@PathParam("serviceId") final String serviceId, @PathParam("actionId") final String actionId);

    // -- DOMAIN SERVICE ACTION INVOKE

    /**
     * Because it isn't possible with the RestEasy client-side framework to specify a query string nor to pass arbitrary query params; instead
     * we provide an additional syntax of passing an Causeway-defined query param <tt>x-causeway-querystring</tt>.
     *
     * <p>The content of this is taken to be the URL encoded map of arguments.
     */
    @GetMapping(
        path = "/{serviceId}/actions/{actionId}/invoke",
        produces = {
            MediaType.APPLICATION_JSON_VALUE, RestfulMediaType.APPLICATION_JSON_ACTION_RESULT, RestfulMediaType.APPLICATION_JSON_ERROR,
            MediaType.APPLICATION_XML_VALUE, RestfulMediaType.APPLICATION_XML_ACTION_RESULT, RestfulMediaType.APPLICATION_XML_ERROR
    })
    public Response invokeActionQueryOnly(@PathParam("serviceId") final String serviceId, @PathParam("actionId") final String actionId, @QueryParam("x-causeway-querystring") final String xCausewayQueryString);

    @PutMapping(
        path = "/{serviceId}/actions/{actionId}/invoke",
        produces = {
            MediaType.APPLICATION_JSON_VALUE, RestfulMediaType.APPLICATION_JSON_ACTION_RESULT, RestfulMediaType.APPLICATION_JSON_ERROR,
            MediaType.APPLICATION_XML_VALUE, RestfulMediaType.APPLICATION_XML_ACTION_RESULT, RestfulMediaType.APPLICATION_XML_ERROR
    })
    public Response invokeActionIdempotent(@PathParam("serviceId") final String serviceId, @PathParam("actionId") final String actionId, final InputStream arguments);

    @PostMapping(path = "/{serviceId}/actions/{actionId}/invoke",
        produces = {
            MediaType.APPLICATION_JSON_VALUE, RestfulMediaType.APPLICATION_JSON_ACTION_RESULT, RestfulMediaType.APPLICATION_JSON_ERROR,
            MediaType.APPLICATION_XML_VALUE, RestfulMediaType.APPLICATION_XML_ACTION_RESULT, RestfulMediaType.APPLICATION_XML_ERROR
    })
    public Response invokeAction(@PathParam("serviceId") final String serviceId, @PathParam("actionId") final String actionId, final InputStream arguments);

    @DeleteMapping(path = "/{serviceId}/actions/{actionId}/invoke")
    public Response deleteInvokeActionNotAllowed(@PathParam("serviceId") final String serviceId, @PathParam("actionId") final String actionId);
}
