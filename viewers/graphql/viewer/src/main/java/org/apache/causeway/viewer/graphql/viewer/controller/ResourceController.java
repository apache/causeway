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
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Callable;

import jakarta.inject.Inject;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MimeType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.apache.causeway.applib.annotation.ObjectSupport.IconSize;
import org.apache.causeway.applib.annotation.Where;
import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.applib.services.bookmark.BookmarkService;
import org.apache.causeway.applib.services.grid.GridService;
import org.apache.causeway.applib.services.iactn.InteractionContext;
import org.apache.causeway.applib.services.iactn.InteractionService;
import org.apache.causeway.applib.services.user.RoleMemento;
import org.apache.causeway.applib.services.user.UserMemento;
import org.apache.causeway.applib.value.Blob;
import org.apache.causeway.applib.value.Clob;
import org.apache.causeway.applib.value.NamedWithMimeType.CommonMimeType;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.core.config.CausewayConfiguration;
import org.apache.causeway.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.causeway.core.metamodel.facets.object.grid.GridFacet;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.object.MmVisibilityUtils;
import org.apache.causeway.core.metamodel.objectmanager.ObjectManager;
import org.apache.causeway.core.metamodel.spec.feature.OneToOneAssociation;

@RestController
@RequestMapping("${spring.graphql.http.path:${spring.graphql.path:/graphql}}/object")
public record ResourceController(
    BookmarkService bookmarkService,
    ObjectManager objectManager,
    GridService gridService,
    InteractionService interactionService,
    CausewayConfiguration.Viewer.Graphql graphqlConfiguration) {

    private static final String PRIVATE_NO_STORE = "private, no-store";
    private static final String NOSNIFF = "nosniff";

    @Inject
    public ResourceController(
            final BookmarkService bookmarkService,
            final ObjectManager objectManager,
            final GridService gridService,
            final InteractionService interactionService,
            final CausewayConfiguration causewayConfiguration) {
        this(
                bookmarkService,
                objectManager,
                gridService,
                interactionService,
                causewayConfiguration.viewer().graphql());
    }

    @GetMapping(value = "/{logicalTypeName}:{id}/{propertyId}/blobBytes")
    public ResponseEntity<byte[]> propertyBlobBytes(
            @PathVariable final String logicalTypeName,
            @PathVariable final String id,
            @PathVariable final String propertyId,
            final Principal principal) {
        return callWithinInteraction(
                principal,
                () -> propertyBlobBytesWithinInteraction(logicalTypeName, id, propertyId));
    }

    private ResponseEntity<byte[]> propertyBlobBytesWithinInteraction(
            final String logicalTypeName,
            final String id,
            final String propertyId) {
        var responseType = graphqlConfiguration.resources().effectiveValueContentResponseType();
        if (responseType == CausewayConfiguration.Viewer.Graphql.ResponseType.FORBIDDEN) {
            return forbidden();
        }

        return valueOfVisibleProperty(logicalTypeName, id, propertyId, Blob.class)
                .map(blob -> resourceResponse(
                        blob.bytes(),
                        MediaType.asMediaType(MimeType.valueOf(blob.mimeType().toString())),
                        blob.name(),
                        responseType))
                .orElseGet(ResourceController::notFound);
    }

    @GetMapping(value = "/{logicalTypeName}:{id}/{propertyId}/clobChars")
    public ResponseEntity<byte[]> propertyClobChars(
            @PathVariable final String logicalTypeName,
            @PathVariable final String id,
            @PathVariable final String propertyId,
            final Principal principal) {
        return callWithinInteraction(
                principal,
                () -> propertyClobCharsWithinInteraction(logicalTypeName, id, propertyId));
    }

    private ResponseEntity<byte[]> propertyClobCharsWithinInteraction(
            final String logicalTypeName,
            final String id,
            final String propertyId) {
        var responseType = graphqlConfiguration.resources().effectiveValueContentResponseType();
        if (responseType == CausewayConfiguration.Viewer.Graphql.ResponseType.FORBIDDEN) {
            return forbidden();
        }

        return valueOfVisibleProperty(logicalTypeName, id, propertyId, Clob.class)
                .map(clob -> resourceResponse(
                        clob.chars().toString().getBytes(StandardCharsets.UTF_8),
                        MediaType.asMediaType(MimeType.valueOf(clob.mimeType().toString())),
                        clob.name(),
                        responseType))
                .orElseGet(ResourceController::notFound);
    }

    @GetMapping(value = "/{logicalTypeName}:{id}/{_meta}/grid")
    public ResponseEntity<byte[]> grid(
            @PathVariable final String logicalTypeName,
            @PathVariable final String id,
            @PathVariable final String _meta,
            final Principal principal) {
        return callWithinInteraction(
                principal,
                () -> gridWithinInteraction(logicalTypeName, id, _meta));
    }

    private ResponseEntity<byte[]> gridWithinInteraction(
            final String logicalTypeName,
            final String id,
            final String _meta) {
        var responseType = graphqlConfiguration.resources().effectiveStructuralMetadataResponseType();
        if (responseType == CausewayConfiguration.Viewer.Graphql.ResponseType.FORBIDDEN) {
            return forbidden();
        }
        if (!_meta.equals(graphqlConfiguration.metaData().fieldName())) {
            return notFound();
        }

        return lookupVisible(logicalTypeName, id)
                .map(managedObject -> gridAsXml(managedObject).orElse(null))
                .filter(Objects::nonNull)
                .map(gridText -> resourceResponse(
                        gridText.getBytes(StandardCharsets.UTF_8),
                        MediaType.APPLICATION_XML,
                        logicalTypeName + ".layout.xml",
                        responseType))
                .orElseGet(ResourceController::notFound);
    }

    @GetMapping(value = "/{logicalTypeName}:{id}/{_meta}/icon")
    public ResponseEntity<byte[]> icon(
            @PathVariable final String logicalTypeName,
            @PathVariable final String id,
            @PathVariable final String _meta,
            final Principal principal) {
        return callWithinInteraction(
                principal,
                () -> iconWithinInteraction(logicalTypeName, id, _meta));
    }

    private ResponseEntity<byte[]> iconWithinInteraction(
            final String logicalTypeName,
            final String id,
            final String _meta) {
        var responseType = graphqlConfiguration.resources().effectiveStructuralMetadataResponseType();
        if (responseType == CausewayConfiguration.Viewer.Graphql.ResponseType.FORBIDDEN) {
            return forbidden();
        }
        if (!_meta.equals(graphqlConfiguration.metaData().fieldName())) {
            return notFound();
        }

        return lookupVisible(logicalTypeName, id)
                .map(managedObject -> managedObject.getIcon(IconSize.MEDIUM))
                .filter(Objects::nonNull)
                .map(objectIcon -> resourceResponse(
                        objectIcon.iconData(),
                        MediaType.parseMediaType(objectIcon.mediaType()),
                        logicalTypeName + ".png",
                        responseType))
                .orElseGet(ResourceController::notFound);
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

    private Optional<String> gridAsXml(final ManagedObject managedObject) {
        return managedObject.objSpec().lookupFacet(GridFacet.class)
            .map(facet -> facet.getGrid(managedObject))
            .flatMap(grid -> gridService().marshaller(CommonMimeType.XML)
                    .map(marshaller -> marshaller.marshal(grid, CommonMimeType.XML)))
            .map(xml -> xml.replace("\r\n", "\n"));
    }

    private <T> Optional<T> valueOfVisibleProperty(
            final String logicalTypeName,
            final String id,
            final String propertyId,
            final Class<T> requiredType) {
        return lookupVisible(logicalTypeName, id)
                .flatMap(managedObject -> managedObject.objSpec().getProperty(propertyId)
                        .filter(property -> isVisible(property, managedObject))
                        .map(property -> property.get(managedObject)))
                .map(ManagedObject::getPojo)
                .filter(requiredType::isInstance)
                .map(requiredType::cast);
    }

    private static boolean isVisible(
            final OneToOneAssociation property,
            final ManagedObject owningObject) {
        return property.isVisible(owningObject, InteractionInitiatedBy.FRAMEWORK, Where.ANYWHERE).isAllowed()
                && property.isVisible(owningObject, InteractionInitiatedBy.USER, Where.ANYWHERE).isAllowed();
    }

    private Optional<ManagedObject> lookupVisible(final String logicalTypeName, final String id) {
        try {
            return bookmarkService.lookup(Bookmark.forLogicalTypeNameAndIdentifier(logicalTypeName, id))
                    .map(objectManager::adapt)
                    .filter(managedObject -> MmVisibilityUtils.isVisible(
                            managedObject,
                            InteractionInitiatedBy.FRAMEWORK))
                    .filter(managedObject -> MmVisibilityUtils.isVisible(
                            managedObject,
                            InteractionInitiatedBy.USER));
        } catch (RuntimeException ex) {
            return Optional.empty();
        }
    }

    private static ResponseEntity<byte[]> resourceResponse(
            final byte[] content,
            final MediaType mediaType,
            final String fileName,
            final CausewayConfiguration.Viewer.Graphql.ResponseType responseType) {
        var bodyBuilder = ResponseEntity.ok()
                .contentType(mediaType)
                .contentLength(content.length)
                .header(HttpHeaders.CACHE_CONTROL, PRIVATE_NO_STORE)
                .header("X-Content-Type-Options", NOSNIFF);
        if (responseType == CausewayConfiguration.Viewer.Graphql.ResponseType.ATTACHMENT) {
            bodyBuilder.header(
                    HttpHeaders.CONTENT_DISPOSITION,
                    ContentDisposition.attachment().filename(fileName).build().toString());
        }
        return bodyBuilder.body(content);
    }

    private static <T> ResponseEntity<T> forbidden() {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .header(HttpHeaders.CACHE_CONTROL, PRIVATE_NO_STORE)
                .build();
    }

    private static <T> ResponseEntity<T> notFound() {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .header(HttpHeaders.CACHE_CONTROL, PRIVATE_NO_STORE)
                .build();
    }
}
