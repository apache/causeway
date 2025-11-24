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
package org.apache.causeway.viewer.restfulobjects.rendering;

import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import org.jspecify.annotations.Nullable;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import org.apache.causeway.applib.services.clock.ClockService;
import org.apache.causeway.core.config.environment.CausewaySystemEnvironment;
import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.viewer.restfulobjects.applib.JsonRepresentation;
import org.apache.causeway.viewer.restfulobjects.applib.util.JsonMapperUtil;
import org.apache.causeway.viewer.restfulobjects.rendering.util.JsonWriterUtil;

public record ResponseFactory(
    @Nullable ClockService clockService,
    JsonMapperUtil.PrettyPrinting prettyPrinting) {

    // non-canonical constructor
    public ResponseFactory(MetaModelContext mmc) {
        this(
            mmc.getServiceRegistry().lookupService(ClockService.class).orElse(null),
            prettyPrinting(mmc.getSystemEnvironment()));
    }

    public static ResponseEntity<Object> noContent() {
        return ResponseEntity.status(HttpStatus.NO_CONTENT)
            .body(null);
    }

    public static ResponseEntity<Object> notFound() {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(null);
    }

    public static ResponseEntity<Object> notImplemented() {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED)
            .body(null);
    }

    public static ResponseEntity<Object> temporaryRedirect(URI location) {
        return ResponseEntity.status(HttpStatus.TEMPORARY_REDIRECT)
            .location(location)
            .body(null);
    }

    /**
     * @param rootRepresentation - if present, is used for entity;
     *      otherwise the renderer is used.
     *      The idea is that the renderer will be set up to render to some sub-node of root representation
     */
    public ResponseEntity<Object> of(
            final HttpStatus httpStatus,
            final ReprRenderer<?> renderer,
            final Caching caching,
            final @Nullable JsonRepresentation rootRepresentation,
            final @Nullable MediaType mediaTypeOverride) {

        // if a rootRepresentation is provided, then the assumption is that the rendered
        // will be rendering to some submap of the rootRepresentation
        final JsonRepresentation entityRepresentation = rootRepresentation != null
            ? rootRepresentation
            : renderer.render();

        return ResponseEntity
            .status(httpStatus)
            .header("Date", now())
            .contentType(mediaTypeOverride!=null ? mediaTypeOverride : renderer.getMediaType())
            .cacheControl(caching.getCacheControl())
            .body((Object)JsonWriterUtil.jsonFor(entityRepresentation, prettyPrinting()));
    }

    /**
     * @param rootRepresentation - if present, is used for entity;
     *      otherwise the renderer is used.
     *      The idea is that the renderer will be set up to render to some sub-node of root representation
     */
    public ResponseEntity<Object> ok(
            final ReprRenderer<?> renderer,
            final Caching caching,
            final @Nullable JsonRepresentation rootRepresentation,
            final @Nullable MediaType mediaTypeOverride) {
        return of(HttpStatus.OK, renderer, caching, rootRepresentation, mediaTypeOverride);
    }

    public ResponseEntity<Object> ok(
        final ReprRenderer<?> renderer,
        final Caching caching) {
        return of(HttpStatus.OK, renderer, caching, null, null);
    }

    public ResponseEntity<Object> ok(Object mappedDomainObject, MediaType mediaType) {
        return ResponseEntity.ok()
            .contentType(mediaType)
            .body(mappedDomainObject);
    }

    // -- HELPER

    final static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
    static {
        DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("GMT"));
    }

    private String now() {
        var now =  clockService!=null
            ? clockService.getClock().nowAsJavaUtilDate()
            : new Date();
        return DATE_FORMAT.format(now);
    }

    private static JsonMapperUtil.PrettyPrinting prettyPrinting(@Nullable CausewaySystemEnvironment env) {
        return env!=null
                && env.isPrototyping()
            ? JsonMapperUtil.PrettyPrinting.ENABLE
            : JsonMapperUtil.PrettyPrinting.DISABLE;
    }

}
