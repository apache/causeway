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

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import org.apache.causeway.viewer.restfulobjects.applib.RestfulMediaType;

/**
 * @since 1.x {@index}
 */
@RequestMapping("${causeway.viewer.restfulobjects.base-path}/services")
public interface DomainServiceResource {

    @GetMapping(produces = { MediaType.APPLICATION_JSON_VALUE, RestfulMediaType.APPLICATION_JSON_LIST, RestfulMediaType.APPLICATION_JSON_ERROR })
    public ResponseEntity<Object> services();

    @DeleteMapping
    public ResponseEntity<Object> deleteServicesNotAllowed();

    @PutMapping
    public ResponseEntity<Object> putServicesNotAllowed();

    @PostMapping
    public ResponseEntity<Object> postServicesNotAllowed();

    // -- DOMAIN SERVICE

    @GetMapping(
        path = "/{serviceId}",
        produces = {
            MediaType.APPLICATION_JSON_VALUE, RestfulMediaType.APPLICATION_JSON_OBJECT, RestfulMediaType.APPLICATION_JSON_ERROR,
            MediaType.APPLICATION_XML_VALUE, RestfulMediaType.APPLICATION_XML_OBJECT, RestfulMediaType.APPLICATION_XML_ERROR
    })
    public ResponseEntity<Object> service(@PathVariable String serviceId);

    @DeleteMapping(path = "/{serviceId}")
    public ResponseEntity<Object> deleteServiceNotAllowed(@PathVariable String serviceId);

    @PutMapping(path = "/{serviceId}")
    public ResponseEntity<Object> putServiceNotAllowed(@PathVariable String serviceId);

    @PostMapping(path = "/{serviceId}")
    public ResponseEntity<Object> postServiceNotAllowed(@PathVariable String serviceId);

    // -- DOMAIN SERVICE ACTION

    @GetMapping(
        path = "/{serviceId}/actions/{actionId}",
        produces = {
            MediaType.APPLICATION_JSON_VALUE, RestfulMediaType.APPLICATION_JSON_OBJECT_ACTION, RestfulMediaType.APPLICATION_JSON_ERROR,
            MediaType.APPLICATION_XML_VALUE, RestfulMediaType.APPLICATION_XML_OBJECT_ACTION, RestfulMediaType.APPLICATION_XML_ERROR
    })
    public ResponseEntity<Object> actionPrompt(@PathVariable String serviceId, @PathVariable String actionId);

    @DeleteMapping(path = "/{serviceId}/actions/{actionId}")
    public ResponseEntity<Object> deleteActionPromptNotAllowed(@PathVariable String serviceId, @PathVariable String actionId);

    @PutMapping(path = "/{serviceId}/actions/{actionId}")
    public ResponseEntity<Object> putActionPromptNotAllowed(@PathVariable String serviceId, @PathVariable String actionId);

    @PostMapping(path = "/{serviceId}/actions/{actionId}")
    public ResponseEntity<Object> postActionPromptNotAllowed(@PathVariable String serviceId, @PathVariable String actionId);

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
    public ResponseEntity<Object> invokeActionQueryOnly(@PathVariable String serviceId, @PathVariable String actionId, @RequestParam("x-causeway-querystring") String xCausewayQueryString);

    @PutMapping(
        path = "/{serviceId}/actions/{actionId}/invoke",
        produces = {
            MediaType.APPLICATION_JSON_VALUE, RestfulMediaType.APPLICATION_JSON_ACTION_RESULT, RestfulMediaType.APPLICATION_JSON_ERROR,
            MediaType.APPLICATION_XML_VALUE, RestfulMediaType.APPLICATION_XML_ACTION_RESULT, RestfulMediaType.APPLICATION_XML_ERROR
    })
    public ResponseEntity<Object> invokeActionIdempotent(@PathVariable String serviceId, @PathVariable String actionId, InputStream arguments);

    @PostMapping(path = "/{serviceId}/actions/{actionId}/invoke",
        produces = {
            MediaType.APPLICATION_JSON_VALUE, RestfulMediaType.APPLICATION_JSON_ACTION_RESULT, RestfulMediaType.APPLICATION_JSON_ERROR,
            MediaType.APPLICATION_XML_VALUE, RestfulMediaType.APPLICATION_XML_ACTION_RESULT, RestfulMediaType.APPLICATION_XML_ERROR
    })
    public ResponseEntity<Object> invokeAction(@PathVariable String serviceId, @PathVariable String actionId, InputStream arguments);

    @DeleteMapping(path = "/{serviceId}/actions/{actionId}/invoke")
    public ResponseEntity<Object> deleteInvokeActionNotAllowed(@PathVariable String serviceId, @PathVariable String actionId);
}
