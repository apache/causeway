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
package org.apache.causeway.viewer.restfulobjects.applib.domaintypes;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import org.apache.causeway.viewer.restfulobjects.applib.RestfulMediaType;

/**
 * @since 1.x {@index}
 */
@RequestMapping("${causeway.viewer.restfulobjects.base-path}/domain-types")
public interface DomainTypeResource {

    @GetMapping(path = "/", produces = {MediaType.APPLICATION_JSON_VALUE, RestfulMediaType.APPLICATION_JSON_TYPE_LIST})
    ResponseEntity<Object> domainTypes();

    @GetMapping(path = "/{domainType}", produces = {MediaType.APPLICATION_JSON_VALUE, RestfulMediaType.APPLICATION_JSON_DOMAIN_TYPE})
    ResponseEntity<Object> domainType(@PathVariable String domainType);

    @GetMapping(path = "/{domainType}/layout", produces = {
            MediaType.APPLICATION_JSON_VALUE, RestfulMediaType.APPLICATION_JSON_LAYOUT_BS,
            MediaType.APPLICATION_XML_VALUE, RestfulMediaType.APPLICATION_XML_LAYOUT_BS
    })
    ResponseEntity<Object> layout(@PathVariable String domainType);

    @GetMapping(path = "/{domainType}/properties/{propertyId}", produces = {
            MediaType.APPLICATION_JSON_VALUE, RestfulMediaType.APPLICATION_JSON_PROPERTY_DESCRIPTION
    })
    ResponseEntity<Object> typeProperty(@PathVariable String domainType, @PathVariable String propertyId);

    @GetMapping(path = "/{domainType}/collections/{collectionId}", produces = {
            MediaType.APPLICATION_JSON_VALUE, RestfulMediaType.APPLICATION_JSON_COLLECTION_DESCRIPTION
    })
    ResponseEntity<Object> typeCollection(@PathVariable String domainType, @PathVariable String collectionId);

    @GetMapping(path = "/{domainType}/actions/{actionId}", produces = {
            MediaType.APPLICATION_JSON_VALUE, RestfulMediaType.APPLICATION_JSON_ACTION_DESCRIPTION
    })
    ResponseEntity<Object> typeAction(@PathVariable String domainType, @PathVariable String actionId);

    @GetMapping(path = "/{domainType}/actions/{actionId}/params/{paramId}", produces = {
            MediaType.APPLICATION_JSON_VALUE, RestfulMediaType.APPLICATION_JSON_ACTION_PARAMETER_DESCRIPTION
    })
    ResponseEntity<Object> typeActionParam(@PathVariable String domainType, @PathVariable String actionId, @PathVariable String paramId);

    @GetMapping(path = "/{domainType}/isSubtypeOf/invoke", produces = {
            MediaType.APPLICATION_JSON_VALUE, RestfulMediaType.APPLICATION_JSON_TYPE_ACTION_RESULT, RestfulMediaType.APPLICATION_JSON_ERROR
    })
    ResponseEntity<Object> domainTypeIsSubtypeOf(
            @PathVariable String domainType,
            @RequestParam("supertype") String superType, // simple style
            @RequestParam("args") String argumentsQueryString // formal style
            );

    @GetMapping(path = "/{domainType}/isSupertypeOf/invoke", produces = {
            MediaType.APPLICATION_JSON_VALUE, RestfulMediaType.APPLICATION_JSON_TYPE_ACTION_RESULT, RestfulMediaType.APPLICATION_JSON_ERROR
    })
    ResponseEntity<Object> domainTypeIsSupertypeOf(
            @PathVariable String domainType,
            @RequestParam("supertype") String superType, // simple style
            @RequestParam("args") String argumentsQueryString // formal style
            );
}
