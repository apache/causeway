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
package org.apache.causeway.core.metamodel.objectmanager.memento;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

import org.jspecify.annotations.Nullable;

import org.springframework.util.StringUtils;

import org.apache.causeway.applib.annotation.ObjectSupport.IconSize;
import org.apache.causeway.applib.id.LogicalType;
import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.applib.services.i18n.TranslationContext;
import org.apache.causeway.applib.services.render.PlaceholderRenderService;
import org.apache.causeway.applib.services.render.PlaceholderRenderService.PlaceholderLiteral;
import org.apache.causeway.commons.internal.assertions._Assert;
import org.apache.causeway.commons.internal.collections._Lists;
import org.apache.causeway.commons.internal.exceptions._Exceptions;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.object.ManagedObjects;
import org.apache.causeway.core.metamodel.object.MmAssertionUtils;
import org.apache.causeway.core.metamodel.object.MmHintUtils;
import org.apache.causeway.core.metamodel.object.MmTitleUtils;

/**
 * @since 2.0
 */
public sealed interface ObjectMemento
extends Serializable
permits ObjectMementoEmpty, ObjectMementoSingular, ObjectMementoPacked {

    LogicalType logicalType();
    Bookmark bookmark();

    /**
     * The object's title for rendering (before translation).
     * Corresponds to {@link ManagedObject#getTitle()}.
     *
     * <p>Directly support choice rendering, without the need to (re-)fetch entire object graphs.
     * (pre-translated)
     */
    String title();

    // -- FACTORIES

    static ObjectMemento empty(final LogicalType logicalType) {
        return new ObjectMementoEmpty(
            logicalType,
            PlaceholderRenderService.fallback().asText(PlaceholderLiteral.NULL_REPRESENTATION));
    }

    static Optional<ObjectMemento> singular(
            final @Nullable ManagedObject adapter) {
        if(ManagedObjects.isNullOrUnspecifiedOrEmpty(adapter))
            return Optional.empty();

        var spec = adapter.objSpec();

        _Assert.assertTrue(spec.isIdentifiable()
                || spec.isParented()
                || spec.isValue(), ()->"Don't know how to create an ObjectMemento for a type "
                        + "with ObjectSpecification %s. "
                        + "All other strategies failed. Type is neither "
                        + "identifiable (isManagedBean() || isViewModel() || isEntity()), "
                        + "nor is a 'parented' Collection, "
                        + "nor has 'encodable' semantics, nor is (Serializable || Externalizable)"
                        .formatted(spec));

        return Optional.ofNullable(new ObjectMementoSingular(
                adapter.logicalType(),
                MmHintUtils.bookmarkElseFail(adapter),
                adapter.getTranslationService().translate(TranslationContext.empty(), MmTitleUtils.titleOf(adapter)),
                adapter.getObjectRenderService().iconToHtml(adapter.getIcon(IconSize.SMALL), IconSize.SMALL)));
    }
    /**
     * returns null for null
     */
    @Nullable static ObjectMemento singularOrEmpty(
            final @Nullable ManagedObject adapter) {
        MmAssertionUtils.assertPojoIsScalar(adapter);
        return singular(adapter)
            .orElseGet(()->ManagedObjects.isSpecified(adapter)
                    ? ObjectMemento.empty(adapter.logicalType())
                    : null);
    }
    static ObjectMemento packed(
            final LogicalType logicalType,
            final ArrayList<ObjectMemento> listOfMementos) {
        return new ObjectMementoPacked(logicalType, listOfMementos);
    }
    static ObjectMemento packed(
            final LogicalType logicalType,
            final Collection<ObjectMemento> container) {
        // ArrayList is serializable
        @SuppressWarnings("unchecked")
        final ArrayList<ObjectMemento> arrayList = container instanceof ArrayList orig
                ? orig
                : _Lists.newArrayList(container);
        return new ObjectMementoPacked(logicalType, arrayList);
    }

    // -- UTILITY

    static ObjectMemento fromDto(final ObjectDisplayDto dto) {
        var bookmark = Bookmark.parse(dto.bookmark()).orElseThrow();
        var logicalType = new LogicalType(bookmark.logicalTypeName(), dto.correspondingClass());
        return bookmark.isEmpty()
            ? new ObjectMementoEmpty(logicalType, dto.title())
            : new ObjectMementoSingular(logicalType, bookmark, dto.title(), dto.iconHtml());
    }

    static String enstringToBase64(final ObjectMemento memento) {
        if(memento instanceof ObjectMementoEmpty objectMementoEmpty)
            return objectMementoEmpty.toDto().toJsonBase64();
        if(memento instanceof ObjectMementoSingular objectMementoSingular)
            return objectMementoSingular.toDto().toJsonBase64();

        throw _Exceptions.unexpectedCodeReach();
    }

    static ObjectMemento destringFromBase64(final String base64EncodedDto) {
        if(!StringUtils.hasLength(base64EncodedDto))
            throw _Exceptions.unexpectedCodeReach();

        try {
            return fromDto(ObjectDisplayDto.fromJsonBase64(base64EncodedDto));
        } catch (Exception e) {
            e.printStackTrace();
            return null; // map to null if anything goes wrong
        }
    }

    default boolean isEmpty() {
        return this instanceof ObjectMementoEmpty;
    }
    default boolean isScalar() {
        return this instanceof ObjectMementoSingular;
    }
    default boolean isPacked() {
        return this instanceof ObjectMementoPacked;
    }

}
