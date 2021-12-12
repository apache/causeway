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

import java.util.Optional;

import org.apache.wicket.model.IModel;

import org.apache.isis.applib.Identifier;
import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.core.metamodel.interactions.managed.nonscalar.DataTableModel;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectMember;
import org.apache.isis.core.runtime.context.IsisAppCommonContext.HasCommonContext;
import org.apache.isis.viewer.wicket.model.links.LinksProvider;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

public interface EntityCollectionModel
extends
    IModel<DataTableModel>,
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

    // -- IDENTITY

    /**
     * This collection's <i>feature</i> {@link Identifier}.
     * @see Identifier
     */
    Identifier getIdentifier();

    // -- VARIANT SUPPORT

    Variant getVariant();

    // -- DATA TABLE

    DataTableModel getDataTableModel();

    // -- METAMODEL SUPPORT

    ObjectMember getMetaModel();

    default ObjectSpecification getElementType() {
        return getMetaModel().getElementType();
    }

    // -- OWNER SUPPORT

    ManagedObject getParentObject();

    // -- BASIC PROPERTIES

    @Deprecated // move to DataTableModel
    int getCount();
    @Deprecated // move to DataTableModel
    String getName();
    @Deprecated // move to DataTableModel
    int getPageSize();

    // -- PARENTED SPECIFICS

    @Deprecated // there is no reason to distinguish parented and standalone, I think
    default Optional<EntityCollectionModelParented> parented() {
        return this instanceof EntityCollectionModelParented
            ? Optional.of((EntityCollectionModelParented)this)
            : Optional.empty();
    }

    @Deprecated // there is no reason to distinguish parented and standalone, I think
    default Optional<Bookmark> parentedHintingBookmark() {
        return parented()
                .map(EntityCollectionModelParented::asHintingBookmark);
    }


}
