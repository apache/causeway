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

import java.util.Objects;
import java.util.Optional;

import javax.inject.Inject;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.util.MimeType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.apache.causeway.applib.layout.grid.Grid;
import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.applib.services.bookmark.BookmarkService;
import org.apache.causeway.applib.value.Blob;
import org.apache.causeway.applib.value.Clob;
import org.apache.causeway.commons.io.JaxbUtils;
import org.apache.causeway.core.config.CausewayConfiguration;
import org.apache.causeway.core.metamodel.facets.object.grid.GridFacet;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.objectmanager.ObjectManager;
import org.apache.causeway.core.metamodel.spec.feature.OneToOneAssociation;

import lombok.Value;

@RestController()
@RequestMapping("/graphql/object")
public class ResourceController {

    private final BookmarkService bookmarkService;
    private final ObjectManager objectManager;
    private final CausewayConfiguration.Viewer.Graphql graphqlConfiguration;

    @Inject
    public ResourceController(
            final BookmarkService bookmarkService,
            final ObjectManager objectManager,
            final CausewayConfiguration causewayConfiguration) {
        this.bookmarkService = bookmarkService;
        this.objectManager = objectManager;
        this.graphqlConfiguration = causewayConfiguration.getViewer().getGraphql();
    }

    @GetMapping(value = "/{logicalTypeName}:{id}/{propertyId}/blobBytes")
    public ResponseEntity<byte[]> propertyBlobBytes(
            @PathVariable final String logicalTypeName,
            @PathVariable final String id,
            @PathVariable final String propertyId
    ) {
        var responseType = graphqlConfiguration.getResources().getResponseType();

        // TODO: perhaps a filter would factor this check out?
        if (responseType == CausewayConfiguration.Viewer.Graphql.ResponseType.FORBIDDEN) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return valueOfProperty(logicalTypeName, id, propertyId)
                .filter(Blob.class::isInstance)
                .map(Blob.class::cast)
                .map(blob -> {
                    var bodyBuilder = ResponseEntity.ok()
                            .contentType(MediaType.asMediaType(MimeType.valueOf(blob.getMimeType().toString())));
                    if (responseType == CausewayConfiguration.Viewer.Graphql.ResponseType.ATTACHMENT) {
                        bodyBuilder
                                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename(blob.getName()).build().toString())
                                .contentLength(blob.getBytes().length);
                    }
                    return bodyBuilder.body(blob.getBytes());
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping(value = "/{logicalTypeName}:{id}/{propertyId}/clobChars")
    public ResponseEntity<CharSequence> propertyClobChars(
            @PathVariable final String logicalTypeName,
            @PathVariable final String id,
            @PathVariable final String propertyId
    ) {
        var responseType = graphqlConfiguration.getResources().getResponseType();

        // TODO: perhaps a filter would factor this check out?
        if (responseType == CausewayConfiguration.Viewer.Graphql.ResponseType.FORBIDDEN) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return valueOfProperty(logicalTypeName, id, propertyId)
                .filter(Clob.class::isInstance)
                .map(Clob.class::cast)
                .map(clob -> {
                    var bodyBuilder = ResponseEntity.ok()
                            .contentType(MediaType.asMediaType(MimeType.valueOf(clob.getMimeType().toString())));
                    if (responseType == CausewayConfiguration.Viewer.Graphql.ResponseType.ATTACHMENT) {
                        bodyBuilder.header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename(clob.getName()).build().toString())
                                .contentLength(clob.getChars().length());
                    }
                    return bodyBuilder.body(clob.getChars());
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping(value = "/{logicalTypeName}:{id}/{_meta}/grid")
    public ResponseEntity<String> grid(
            @PathVariable final String logicalTypeName,
            @PathVariable final String id,
            @PathVariable final String _meta
    ) {
        var responseType = graphqlConfiguration.getResources().getResponseType();

        // TODO: perhaps a filter would factor this check out?
        if (responseType == CausewayConfiguration.Viewer.Graphql.ResponseType.FORBIDDEN) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        if (!_meta.equals(graphqlConfiguration.getMetaData().getFieldName())) {
            return ResponseEntity.notFound().build();
        }

        return lookup(logicalTypeName, id)
                .map(ResourceController::gridOf)
                .filter(Objects::nonNull)
                .map(JaxbUtils::toStringUtf8)
                .map(x -> x.replaceAll("(\r\n)", "\n"))
                .map(gridText -> {
                    var bodyBuilder = ResponseEntity.ok()
                            .contentType(MediaType.APPLICATION_XML);
                    if (responseType == CausewayConfiguration.Viewer.Graphql.ResponseType.ATTACHMENT) {
                        bodyBuilder
                                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename(logicalTypeName + ".layout.xml").build().toString())
                                .contentLength(gridText.length());
                    }
                    return bodyBuilder.body(gridText);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping(value = "/{logicalTypeName}:{id}/{_meta}/icon")
    public ResponseEntity<byte[]> icon(
            @PathVariable final String logicalTypeName,
            @PathVariable final String id,
            @PathVariable final String _meta
    ) {
        // TODO: perhaps a filter would factor this check out?
        var responseType = graphqlConfiguration.getResources().getResponseType();
        if (responseType == CausewayConfiguration.Viewer.Graphql.ResponseType.FORBIDDEN) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        if (!_meta.equals(graphqlConfiguration.getMetaData().getFieldName())) {
            return ResponseEntity.notFound().build();
        }

        return lookup(logicalTypeName, id)
                .map(ManagedObject::getIcon)
                .filter(Objects::nonNull)
                .map(objectIcon -> {
                    var bytes = objectIcon.asBytes();
                    var bodyBuilder = ResponseEntity.ok()
                            .contentType(MediaType.parseMediaType(objectIcon.getMimeType().getMimeType().toString()));
                    if (responseType == CausewayConfiguration.Viewer.Graphql.ResponseType.ATTACHMENT) {
                        bodyBuilder
                                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename(logicalTypeName + ".png").build().toString())
                                .contentLength(bytes.length);
                    }
                    return bodyBuilder.body(bytes);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @Nullable
    private static Grid gridOf(ManagedObject managedObject) {
        var facet = managedObject.getSpecification().getFacet(GridFacet.class);
        return facet != null ? facet.getGrid(managedObject) : null;
    }

    private Optional<Object> valueOfProperty(String logicalTypeName, String id, String propertyId) {
        return lookup(logicalTypeName, id)
                .map(managedObject -> ManagedObjectAndPropertyIfAny.of(managedObject, managedObject.getSpecification().getProperty(propertyId)))
                .filter(ManagedObjectAndPropertyIfAny::isPropertyPresent)
                .map(ManagedObjectAndProperty::of)
                .map(ManagedObjectAndProperty::value)
                .map(ManagedObject::getPojo);
    }

    private Optional<ManagedObject> lookup(String logicalTypeName, String id) {
        return bookmarkService.lookup(Bookmark.forLogicalTypeNameAndIdentifier(logicalTypeName, id))
                .map(objectManager::adapt);
    }

    @Value(staticConstructor = "of")
    private static class ManagedObjectAndPropertyIfAny {
        ManagedObject owningObject;
        Optional<OneToOneAssociation> propertyIfAny;
        boolean isPropertyPresent() {
            return propertyIfAny.isPresent();
        }
    }

    private static class ManagedObjectAndProperty {
        private static ManagedObjectAndProperty of(ManagedObjectAndPropertyIfAny tuple) {
            return new ManagedObjectAndProperty(tuple);
        }
        private ManagedObjectAndProperty(ManagedObjectAndPropertyIfAny tuple) {
            this.owningObject = tuple.owningObject;
            this.property = tuple.propertyIfAny.orElse(null);
        }
        ManagedObject owningObject;
        OneToOneAssociation property;

        ManagedObject value() {
            return property.get(owningObject);
        }
    }
}
