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
package org.apache.causeway.viewer.graphql.viewer.controller;

import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;

import jakarta.inject.Inject;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.apache.causeway.applib.services.iactn.InteractionContext;
import org.apache.causeway.applib.services.iactn.InteractionService;
import org.apache.causeway.applib.services.user.RoleMemento;
import org.apache.causeway.applib.services.user.UserMemento;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.core.config.CausewayConfiguration;
import org.apache.causeway.viewer.graphql.model.application.ApplicationEntryService;

@RestController
@RequestMapping("${spring.graphql.http.path:${spring.graphql.path:/graphql}}/application")
public record ApplicationResourceController(
        ApplicationEntryService applicationEntryService,
        InteractionService interactionService,
        CausewayConfiguration.Viewer.Graphql graphqlConfiguration) {

    private static final String NOSNIFF = "nosniff";

    @Inject
    public ApplicationResourceController(
            final ApplicationEntryService applicationEntryService,
            final InteractionService interactionService,
            final CausewayConfiguration causewayConfiguration) {
        this(applicationEntryService, interactionService, causewayConfiguration.viewer().graphql());
    }

    @GetMapping(value = "/menu-bars", produces = MediaType.APPLICATION_XML_VALUE)
    public ResponseEntity<byte[]> menuBars(final Principal principal) {
        return callWithinInteraction(principal, this::menuBarsWithinInteraction);
    }

    private ResponseEntity<byte[]> menuBarsWithinInteraction() {
        var responseType = graphqlConfiguration.resources().effectiveStructuralMetadataResponseType();
        if (responseType == CausewayConfiguration.Viewer.Graphql.ResponseType.FORBIDDEN) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .header(HttpHeaders.CACHE_CONTROL, ApplicationEntryService.PRIVATE_NO_STORE)
                    .build();
        }
        var resource = applicationEntryService.menuBarsResource();
        if (resource.xml() == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .header(HttpHeaders.CACHE_CONTROL, ApplicationEntryService.PRIVATE_NO_STORE)
                    .build();
        }
        var content = resource.xml().getBytes(StandardCharsets.UTF_8);
        var response = ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_XML)
                .contentLength(content.length)
                .header(HttpHeaders.CACHE_CONTROL, ApplicationEntryService.PRIVATE_NO_STORE)
                .header(HttpHeaders.ETAG, '"' + resource.generation() + '"')
                .header("X-Content-Type-Options", NOSNIFF);
        if (responseType == CausewayConfiguration.Viewer.Graphql.ResponseType.ATTACHMENT) {
            response.header(
                    HttpHeaders.CONTENT_DISPOSITION,
                    ContentDisposition.attachment().filename("menubars.layout.xml").build().toString());
        }
        return response.body(content);
    }

    private <T> T callWithinInteraction(
            final Principal principal,
            final Callable<T> callable) {
        var userMemento = userMemento(principal);
        return userMemento != null
                ? interactionService.call(
                        InteractionContext.builder().user(userMemento).build(),
                        callable)
                : interactionService.callAnonymous(callable);
    }

    private UserMemento userMemento(final Principal principal) {
        if (principal != null) {
            return UserMemento.builder(principal.getName()).build();
        }
        var fallback = graphqlConfiguration.authentication().fallback();
        if (fallback == null || fallback.username() == null) {
            return null;
        }
        var roleNames = Optional.ofNullable(fallback.roles()).orElseGet(List::of);
        var roles = Can.ofStream(roleNames.stream()
                .map(roleName -> RoleMemento.builder().name(roleName).build()));
        return UserMemento.builder(fallback.username())
                .roles(roles)
                .build();
    }
}
