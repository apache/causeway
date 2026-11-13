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
@RequestMapping("${causeway.viewer.restfulobjects.base-path}/objects")
public interface DomainObjectResource {

    @PostMapping(path = "/{domainType}", produces = {
            MediaType.APPLICATION_JSON_VALUE, RestfulMediaType.APPLICATION_JSON_OBJECT, RestfulMediaType.APPLICATION_JSON_ERROR,
            MediaType.APPLICATION_XML_VALUE, RestfulMediaType.APPLICATION_XML_OBJECT, RestfulMediaType.APPLICATION_XML_ERROR
    })
    public ResponseEntity<Object> persist(@PathVariable String domainType, InputStream object);

    // -- DOMAIN OBJECT

    @GetMapping(path = "/{domainType}/{instanceId}", produces = {
            MediaType.APPLICATION_JSON_VALUE, RestfulMediaType.APPLICATION_JSON_OBJECT, RestfulMediaType.APPLICATION_JSON_ERROR,
            MediaType.APPLICATION_XML_VALUE, RestfulMediaType.APPLICATION_XML_OBJECT, RestfulMediaType.APPLICATION_XML_ERROR
        })
    public ResponseEntity<Object> object(@PathVariable String domainType, @PathVariable String instanceId);

    @PutMapping(path = "/{domainType}/{instanceId}", produces = {
            MediaType.APPLICATION_JSON_VALUE, RestfulMediaType.APPLICATION_JSON_OBJECT, RestfulMediaType.APPLICATION_JSON_ERROR,
            MediaType.APPLICATION_XML_VALUE, RestfulMediaType.APPLICATION_XML_OBJECT, RestfulMediaType.APPLICATION_XML_ERROR
    })
    public ResponseEntity<Object> object(@PathVariable String domainType, @PathVariable String instanceId, InputStream arguments);

    @DeleteMapping(path = "/{domainType}/{instanceId}")
    public ResponseEntity<Object> deleteMethodNotSupported(@PathVariable String domainType, @PathVariable String instanceId);

    @PostMapping(path = "/{domainType}/{instanceId}")
    public ResponseEntity<Object> postMethodNotAllowed(@PathVariable String domainType, @PathVariable String instanceId);

    // -- DOMAIN OBJECT IMAGE

    @GetMapping(path = "/{domainType}/{instanceId}/image", produces = {
            "image/png",
            "image/gif",
            "image/jpeg",
            "image/jpg",
            "image/svg+xml"
    })
    public ResponseEntity<Object> image(
            @PathVariable
            String domainType,
            @PathVariable
            String instanceId);

    // -- DOMAIN OBJECT LAYOUT

    @GetMapping(path = "/{domainType}/{instanceId}/object-layout", produces = {
            MediaType.APPLICATION_JSON_VALUE, RestfulMediaType.APPLICATION_JSON_OBJECT_LAYOUT_BS,
            MediaType.APPLICATION_XML_VALUE, RestfulMediaType.APPLICATION_XML_OBJECT_LAYOUT_BS
    })
    public ResponseEntity<Object> layout(
            @PathVariable
            String domainType,
            @PathVariable
            String instanceId);

    // -- DOMAIN OBJECT PROPERTY

    @GetMapping(path = "/{domainType}/{instanceId}/properties/{propertyId}", produces = {
            MediaType.APPLICATION_JSON_VALUE, RestfulMediaType.APPLICATION_JSON_OBJECT_PROPERTY, RestfulMediaType.APPLICATION_JSON_ERROR,
            MediaType.APPLICATION_XML_VALUE, RestfulMediaType.APPLICATION_XML_OBJECT_PROPERTY, RestfulMediaType.APPLICATION_XML_ERROR
    })
    public ResponseEntity<Object> propertyDetails(@PathVariable String domainType, @PathVariable String instanceId, @PathVariable String propertyId);

    @PutMapping(path = "/{domainType}/{instanceId}/properties/{propertyId}", produces = {
            MediaType.APPLICATION_JSON_VALUE, RestfulMediaType.APPLICATION_JSON_OBJECT_PROPERTY, RestfulMediaType.APPLICATION_JSON_ERROR,
            MediaType.APPLICATION_XML_VALUE, RestfulMediaType.APPLICATION_XML_OBJECT_PROPERTY, RestfulMediaType.APPLICATION_XML_ERROR
    })
    public ResponseEntity<Object> modifyProperty(@PathVariable String domainType, @PathVariable String instanceId, @PathVariable String propertyId, InputStream arguments);

    @DeleteMapping(path = "/{domainType}/{instanceId}/properties/{propertyId}", produces = {
            MediaType.APPLICATION_JSON_VALUE, RestfulMediaType.APPLICATION_JSON_OBJECT_PROPERTY, RestfulMediaType.APPLICATION_JSON_ERROR,
            MediaType.APPLICATION_XML_VALUE, RestfulMediaType.APPLICATION_XML_OBJECT_PROPERTY, RestfulMediaType.APPLICATION_XML_ERROR
    })
    public ResponseEntity<Object> clearProperty(@PathVariable String domainType, @PathVariable String instanceId, @PathVariable String propertyId);

    @PostMapping(path = "/{domainType}/{instanceId}/properties/{propertyId}")
    public ResponseEntity<Object> postPropertyNotAllowed(@PathVariable String domainType, @PathVariable String instanceId, @PathVariable String propertyId);

    // -- DOMAIN OBJECT COLLECTION

    @GetMapping(path = "/{domainType}/{instanceId}/collections/{collectionId}", produces = {
            MediaType.APPLICATION_JSON_VALUE, RestfulMediaType.APPLICATION_JSON_OBJECT_COLLECTION, RestfulMediaType.APPLICATION_JSON_ERROR,
            MediaType.APPLICATION_XML_VALUE, RestfulMediaType.APPLICATION_XML_OBJECT_COLLECTION, RestfulMediaType.APPLICATION_XML_ERROR
    })
    public ResponseEntity<Object> accessCollection(@PathVariable String domainType, @PathVariable String instanceId, @PathVariable String collectionId);

    // -- DOMAIN OBJECT ACTION

    @GetMapping(path = "/{domainType}/{instanceId}/actions/{actionId}", produces = {
            MediaType.APPLICATION_JSON_VALUE, RestfulMediaType.APPLICATION_JSON_OBJECT_ACTION, RestfulMediaType.APPLICATION_JSON_ERROR,
            MediaType.APPLICATION_XML_VALUE, RestfulMediaType.APPLICATION_XML_OBJECT_ACTION, RestfulMediaType.APPLICATION_XML_ERROR
    })
    public ResponseEntity<Object> actionPrompt(@PathVariable String domainType, @PathVariable String instanceId, @PathVariable String actionId);

    @DeleteMapping(path = "/{domainType}/{instanceId}/actions/{actionId}")
    public ResponseEntity<Object> deleteActionPromptNotAllowed(@PathVariable String domainType, @PathVariable String instanceId, @PathVariable String actionId);

    @PutMapping(path = "/{domainType}/{instanceId}/actions/{actionId}")
    public ResponseEntity<Object> putActionPromptNotAllowed(@PathVariable String domainType, @PathVariable String instanceId, @PathVariable String actionId);

    @PostMapping(path = "/{domainType}/{instanceId}/actions/{actionId}")
    public ResponseEntity<Object> postActionPromptNotAllowed(@PathVariable String domainType, @PathVariable String instanceId, @PathVariable String actionId);

    // -- DOMAIN OBJECT ACTION INVOKE

    @GetMapping(path = "/{domainType}/{instanceId}/actions/{actionId}/invoke", produces = {
            MediaType.APPLICATION_JSON_VALUE, RestfulMediaType.APPLICATION_JSON_ACTION_RESULT, RestfulMediaType.APPLICATION_JSON_ERROR,
            MediaType.APPLICATION_XML_VALUE, RestfulMediaType.APPLICATION_XML_ACTION_RESULT, RestfulMediaType.APPLICATION_XML_ERROR
    })
    public ResponseEntity<Object> invokeActionQueryOnly(@PathVariable String domainType, @PathVariable String instanceId, @PathVariable String actionId, @RequestParam("x-causeway-querystring") String xCausewayQueryString);

    @PutMapping(path = "/{domainType}/{instanceId}/actions/{actionId}/invoke", produces = {
            MediaType.APPLICATION_JSON_VALUE, RestfulMediaType.APPLICATION_JSON_ACTION_RESULT, RestfulMediaType.APPLICATION_JSON_ERROR,
            MediaType.APPLICATION_XML_VALUE, RestfulMediaType.APPLICATION_XML_ACTION_RESULT, RestfulMediaType.APPLICATION_XML_ERROR
    })
    public ResponseEntity<Object> invokeActionIdempotent(@PathVariable String domainType, @PathVariable String instanceId, @PathVariable String actionId, InputStream arguments);

    @PostMapping(path = "/{domainType}/{instanceId}/actions/{actionId}/invoke", produces = {
            MediaType.APPLICATION_JSON_VALUE, RestfulMediaType.APPLICATION_JSON_ACTION_RESULT, RestfulMediaType.APPLICATION_JSON_ERROR,
            MediaType.APPLICATION_XML_VALUE, RestfulMediaType.APPLICATION_XML_ACTION_RESULT, RestfulMediaType.APPLICATION_XML_ERROR
    })
    public ResponseEntity<Object> invokeAction(@PathVariable String domainType, @PathVariable String instanceId, @PathVariable String actionId, InputStream arguments);

    @DeleteMapping(path = "/{domainType}/{instanceId}/actions/{actionId}/invoke")
    public ResponseEntity<Object> deleteInvokeActionNotAllowed(@PathVariable String domainType, @PathVariable String instanceId, @PathVariable String actionId);

}
