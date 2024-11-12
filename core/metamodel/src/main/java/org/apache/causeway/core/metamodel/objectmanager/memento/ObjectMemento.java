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
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

import org.springframework.lang.Nullable;

import org.apache.causeway.applib.id.HasLogicalType;
import org.apache.causeway.applib.id.LogicalType;
import org.apache.causeway.applib.services.bookmark.BookmarkHolder;
import org.apache.causeway.commons.internal.base._Bytes;
import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.commons.internal.collections._Lists;
import org.apache.causeway.commons.internal.resources._Serializables;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.object.ManagedObjects;

/**
 * @since 2.0
 */
public sealed interface ObjectMemento
extends BookmarkHolder, HasLogicalType, Serializable
permits ObjectMementoForEmpty, ObjectMementoForScalar, ObjectMementoCollection {

    /** arbitrary/random string */
    static final String NULL_ID = "VGN6r6zKTiLhUsA0WkdQ17LvMU1IYdb0";

    /**
     * The object's title for rendering (before translation).
     * Corresponds to {@link ManagedObject#getTitle()}.
     * <p>
     * Directly support choice rendering, without the need to (re-)fetch entire object graphs.
     * (TODO translated or not?)
     */
    String getTitle();

    // -- FACTORIES

    static ObjectMemento empty(final LogicalType logicalType) {
        return new ObjectMementoForEmpty(logicalType);
    }
    static Optional<ObjectMemento> scalar(@Nullable final ManagedObject adapter) {
        return ManagedObjects.isNullOrUnspecifiedOrEmpty(adapter)
                ? Optional.empty()
                : Optional.of(ObjectMementoForScalar.create(adapter));
    }
    static ObjectMemento packed(
            final LogicalType logicalType,
            final ArrayList<ObjectMemento> listOfMementos) {
        return new ObjectMementoCollection(logicalType, listOfMementos);
    }
    static ObjectMemento packed(
            final LogicalType logicalType,
            final Collection<ObjectMemento> container) {
        // ArrayList is serializable
        @SuppressWarnings("unchecked")
        final ArrayList<ObjectMemento> arrayList = container instanceof ArrayList orig
                ? orig
                : _Lists.newArrayList(container);
        return new ObjectMementoCollection(logicalType, arrayList);

    }

    // -- UTILITY

    // ArrayList is serializable
    public static Optional<ArrayList<ObjectMemento>> unpackAsList(final ObjectMemento memento) {
        return memento instanceof ObjectMementoCollection packed
                ? packed.asList()
                : Optional.empty();
    }

    @Nullable
    static String enstringToUrlBase64(final @Nullable ObjectMemento memento) {
        var base64UrlEncodedMemento = memento!=null
                ? _Strings.ofBytes(
                    _Bytes.asUrlBase64.apply(
                            _Serializables.write(memento)),
                    StandardCharsets.US_ASCII)
                : null;
        return base64UrlEncodedMemento;
    }

    @Nullable
    static ObjectMemento destringFromUrlBase64(final @Nullable String base64UrlEncodedMemento) {
        try {
            return _Strings.isNotEmpty(base64UrlEncodedMemento)
                    && !NULL_ID.equals(base64UrlEncodedMemento)
                    ? _Serializables.read(
                            ObjectMemento.class,
                            _Bytes.ofUrlBase64.apply(
                                    base64UrlEncodedMemento.getBytes(StandardCharsets.US_ASCII)))
                    : null;
        } catch (Exception e) {
            return null; // map to null if anything goes wrong
        }

    }

    default boolean isEmpty() {
        return this instanceof ObjectMementoForEmpty;
    }
    default boolean isScalar() {
        return this instanceof ObjectMementoForScalar;
    }
    default boolean isPacked() {
        return this instanceof ObjectMementoCollection;
    }



}
