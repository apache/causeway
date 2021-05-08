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
package org.apache.isis.viewer.wicket.model.models;

import java.util.List;
import java.util.Optional;

import org.apache.wicket.model.IModel;

import org.apache.isis.applib.Identifier;
import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.core.metamodel.interactions.managed.ManagedCollection;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.ObjectMember;
import org.apache.isis.core.runtime.context.IsisAppCommonContext.HasCommonContext;
import org.apache.isis.core.runtime.memento.ObjectMemento;
import org.apache.isis.viewer.wicket.model.links.LinksProvider;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

public interface EntityCollectionModel
extends
    IModel<List<ManagedObject>>,
    HasCommonContext,
    LinksProvider {

    // -- VARIANTS

    @RequiredArgsConstructor
    public enum Variant {
        /**
         * A simple list of object mementos, eg the result of invoking an action
         *
         * <p>
         * This deals with both persisted and transient objects.
         */
        STANDALONE(EntityModel.RenderingHint.STANDALONE_PROPERTY_COLUMN, 25),

        /**
         * A collection of an entity (eg Order/OrderDetail).
         */
        PARENTED(EntityModel.RenderingHint.PARENTED_PROPERTY_COLUMN, 12),
        ;

        @Getter private final EntityModel.RenderingHint columnRenderingHint;
        @Getter private final int pageSizeDefault;


        public boolean isStandalone() {
            return this == STANDALONE;
        }

        public boolean isParented() {
            return this == PARENTED;
        }
    }

    // -- FACTORIES

    static EntityCollectionModelParented createParented(final @NonNull EntityModel entityModel) {
        return EntityCollectionModelParented.forParentObjectModel(entityModel);
    }

    static EntityCollectionModelStandalone createStandalone(
            ManagedObject collectionAsAdapter,
            ActionModel actionModel) {
        return EntityCollectionModelStandalone.forActionModel(collectionAsAdapter, actionModel);
    }

    // -- IDENTITY

    /**
     * This collection's <i>feature</i> {@link Identifier}.
     * @see Identifier
     */
    Identifier getIdentifier();

    // -- VARIANT SUPPORT

    Variant getVariant();

    default boolean isStandalone() { return getVariant().isStandalone(); }
    default boolean isParented() { return getVariant().isParented(); }

    // -- METAMODEL SUPPORT

    ObjectSpecification getTypeOfSpecification();
    ObjectMember getMetaModel();

    default Can<ObjectAction> getAssociatedActions() {
        return Can.empty();
    }

    // -- INTERACTION SUPPORT

    /**
     * Returns optionally the a {@link ManagedCollection}, based on whether
     * this is a parented collection.
     */
    Optional<ManagedCollection> getManagedCollection();

    default Optional<ManagedObject> getParentObject() {
        return getManagedCollection()
                .map(ManagedCollection::getOwner);
    }

    default Optional<ObjectSpecification> getParentObjectSpecification() {
        return getParentObject()
                .map(ManagedObject::getSpecification);
    }

    // -- TOGGLE SUPPORT

    Can<ObjectMemento> getToggleMementosList();
    void clearToggleMementosList();
    boolean toggleSelectionOn(ManagedObject selectedAdapter);

    // -- BASIC PROPERTIES

    int getCount();
    String getName();
    int getPageSize();

    // -- REFACTORING TODO ...

    @Deprecated
    ObjectMemento getParentObjectAdapterMemento();

    @Deprecated
    Bookmark asHintingBookmarkIfSupported();

}
