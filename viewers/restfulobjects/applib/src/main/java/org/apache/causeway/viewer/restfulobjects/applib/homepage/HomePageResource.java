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
package org.apache.causeway.viewer.restfulobjects.applib.homepage;

import jakarta.ws.rs.core.Response;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import org.apache.causeway.viewer.restfulobjects.applib.RestfulMediaType;

/**
 * @since 1.x {@index}
 */
@RequestMapping("${roPrefix}")
public interface HomePageResource {

    @GetMapping(produces = { MediaType.APPLICATION_JSON_VALUE, RestfulMediaType.APPLICATION_JSON_HOME_PAGE })
    public Response homePage(/*@RequestHeader HttpHeaders headers*/);

    @DeleteMapping
    public Response deleteHomePageNotAllowed();

    @PutMapping
    public Response putHomePageNotAllowed();

    @PostMapping
    public Response postHomePageNotAllowed();

    /**
     * Not part of the RO spec; this resource always returns 401, and is
     * intended to be redirected to if credentials cannot be obtained.
     */
    @GetMapping(
        path = "/notAuthenticated",
        produces = { MediaType.APPLICATION_JSON_VALUE })
    public Response notAuthenticated();

}
