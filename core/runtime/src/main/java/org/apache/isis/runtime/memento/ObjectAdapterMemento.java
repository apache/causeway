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

package org.apache.isis.runtime.memento;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.commons.collections.Cardinality;
import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.metamodel.spec.ManagedObject;
import org.apache.isis.metamodel.spec.ObjectSpecId;
import org.apache.isis.metamodel.specloader.SpecificationLoader;

/**
 * @since 2.0
 */
public interface ObjectAdapterMemento extends Serializable {

    String asString();

    /**
     * TODO[2112] outdated
     * Returns a bookmark only if 
     * {@link org.apache.isis.runtime.memento.MementoHelper.RecreateStrategy#LOOKUP} and 
     * {@link #getCardinality() sort} is {@link Cardinality#SCALAR scalar}.
     * Returns {@code null} otherwise. 
     */
    Bookmark asBookmarkIfSupported();

    /**
     * TODO[2112] outdated 
     * Returns a bookmark only if 
     * {@link org.apache.isis.runtime.memento.MementoHelper.RecreateStrategy#LOOKUP} and 
     * {@link #getCardinality() sort} is {@link Cardinality#SCALAR scalar}.
     * Returns {@code null} otherwise. 
     */
    Bookmark asHintingBookmarkIfSupported();

    ObjectSpecId getObjectSpecId();
    
    ManagedObject reconstructObject(MementoStore mementoStore, SpecificationLoader specificationLoader);

    // -- FACTORIES

    static ObjectAdapterMemento wrapMementoList(
            Collection<ObjectAdapterMemento> container, 
            ObjectSpecId specId) {

        // ArrayList is serializable
        if(container instanceof ArrayList) {
            return ObjectAdapterMementoCollection.of((ArrayList<ObjectAdapterMemento>)container, specId);
        }
        return ObjectAdapterMementoCollection.of(_Lists.newArrayList(container), specId);
    }

    // ArrayList is serializable
    static Optional<ArrayList<ObjectAdapterMemento>> unwrapList(ObjectAdapterMemento memento) {
        if(memento==null) {
            return Optional.empty();
        }
        if(!(memento instanceof ObjectAdapterMementoCollection)) {
            return Optional.empty();
        }
        return Optional.ofNullable(((ObjectAdapterMementoCollection)memento).unwrapList());
    }


}
