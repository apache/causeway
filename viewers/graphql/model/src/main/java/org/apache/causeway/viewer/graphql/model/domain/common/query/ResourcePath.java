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
package org.apache.causeway.viewer.graphql.model.domain.common.query;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.core.config.CausewayConfiguration;

/**
 * Builds opaque, origin-relative references to the GraphQL resource controller.
 */
public final class ResourcePath {

    private final String graphqlResourceRoot;

    private ResourcePath(final String graphqlResourceRoot) {
        this.graphqlResourceRoot = graphqlResourceRoot;
    }

    public static ResourcePath from(final CausewayConfiguration configuration) {
        var graphqlConfiguration = configuration.viewer().graphql();
        return of(
                graphqlConfiguration.resources().externalPathPrefix(),
                configuration.valueOf("server.servlet.context-path").orElse(""),
                configuration.valueOf("spring.graphql.http.path")
                        .or(() -> configuration.valueOf("spring.graphql.path"))
                        .orElse("/graphql"));
    }

    static ResourcePath of(
            final String externalPathPrefix,
            final String servletContextPath,
            final String graphqlPath) {
        return new ResourcePath(joinPath(
                externalPathPrefix,
                servletContextPath,
                graphqlPath));
    }

    public String metadata(
            final Bookmark bookmark,
            final String metadataFieldName,
            final String resourceName) {
        return append(
                "object",
                bookmarkSegment(bookmark),
                requireSegment(metadataFieldName),
                requireSegment(resourceName));
    }

    public String property(
            final Bookmark bookmark,
            final String propertyId,
            final String resourceName) {
        return append(
                "object",
                bookmarkSegment(bookmark),
                requireSegment(propertyId),
                requireSegment(resourceName));
    }

    public String application(final String resourceName) {
        return append("application", requireSegment(resourceName));
    }

    String controllerPath() {
        return append("object");
    }

    private String append(final String... segments) {
        return graphqlResourceRoot + "/" + String.join("/", segments);
    }

    private static String bookmarkSegment(final Bookmark bookmark) {
        Objects.requireNonNull(bookmark);
        return requireSegment(bookmark.logicalTypeName()) + ":" + requireSegment(bookmark.identifier());
    }

    private static String requireSegment(final String segment) {
        if (segment == null
                || segment.isBlank()
                || segment.indexOf('/') >= 0
                || segment.indexOf('\\') >= 0
                || segment.indexOf('?') >= 0
                || segment.indexOf('#') >= 0) {
            throw new IllegalArgumentException("Resource identity is not a valid opaque path segment");
        }
        return segment;
    }

    private static String joinPath(final String... pathComponents) {
        var path = Arrays.stream(pathComponents)
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(component -> !component.isEmpty())
                .peek(ResourcePath::requireApplicationPath)
                .flatMap(component -> Arrays.stream(component.split("/+")))
                .filter(component -> !component.isEmpty())
                .collect(Collectors.joining("/"));
        return "/" + path;
    }

    private static void requireApplicationPath(final String path) {
        if (path.contains("://") || path.indexOf('?') >= 0 || path.indexOf('#') >= 0) {
            throw new IllegalArgumentException("Resource application path must not contain an authority, query, or fragment");
        }
    }
}
