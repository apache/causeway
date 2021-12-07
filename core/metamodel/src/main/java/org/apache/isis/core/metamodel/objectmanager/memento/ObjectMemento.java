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
package org.apache.isis.core.metamodel.objectmanager.memento;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

import org.apache.isis.applib.id.HasLogicalType;
import org.apache.isis.applib.id.LogicalType;
import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.commons.collections.Cardinality;
import org.apache.isis.commons.internal.collections._Lists;

/**
 * @since 2.0
 */
public interface ObjectMemento extends HasLogicalType, Serializable {

    String asString();

    /**
     * Returns a bookmark only if
     * {@link org.apache.isis.viewer.wicket.viewer.services.mementos.ObjectMementoWkt.RecreateStrategy#LOOKUP} and
     * {@link #getCardinality() sort} is {@link Cardinality#SCALAR scalar}.
     * Returns {@code null} otherwise.
     */
    Bookmark asBookmarkIfSupported();

    /**
     * Returns a bookmark only if
     * {@link org.apache.isis.viewer.wicket.viewer.services.mementos.ObjectMementoWkt.RecreateStrategy#LOOKUP} and
     * {@link #getCardinality() sort} is {@link Cardinality#SCALAR scalar}.
     * Returns {@code null} otherwise.
     */
    Bookmark asHintingBookmarkIfSupported();

    // -- FACTORIES

    static ObjectMemento pack(
            final Collection<ObjectMemento> container,
            final LogicalType logicalType) {

        // ArrayList is serializable
        if(container instanceof ArrayList) {
            return ObjectMementoCollection.of((ArrayList<ObjectMemento>)container, logicalType);
        }
        return ObjectMementoCollection.of(_Lists.newArrayList(container), logicalType);
    }

    // ArrayList is serializable
    static Optional<ArrayList<ObjectMemento>> unpack(final ObjectMemento memento) {
        if(memento==null) {
            return Optional.empty();
        }
        if(!(memento instanceof ObjectMementoCollection)) {
            return Optional.empty();
        }
        return Optional.ofNullable(((ObjectMementoCollection)memento).unwrapList());
    }


}
