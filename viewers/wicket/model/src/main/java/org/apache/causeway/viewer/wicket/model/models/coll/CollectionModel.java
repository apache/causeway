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
package org.apache.causeway.viewer.wicket.model.models.coll;

import java.util.Optional;

import org.apache.wicket.model.IModel;

import org.apache.causeway.applib.Identifier;
import org.apache.causeway.applib.annotation.TableDecorator;
import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.core.metamodel.context.HasMetaModelContext;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.ObjectMember;
import org.apache.causeway.core.metamodel.tabular.DataTableInteractive;
import org.apache.causeway.viewer.commons.model.hints.RenderingHint;
import org.apache.causeway.viewer.wicket.model.links.LinksProvider;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

public sealed interface CollectionModel
extends
    IModel<DataTableInteractive>,
    HasMetaModelContext,
    LinksProvider
permits CollectionModelAbstract, CollectionModelEmpty {

    // -- VARIANTS

    @RequiredArgsConstructor
    public enum Variant {
        /**
         * A simple list of object mementos, eg the result of invoking an action
         *
         * <p>
         * This deals with both persisted and transient objects.
         */
        STANDALONE(RenderingHint.STANDALONE_PROPERTY_COLUMN, 25),

        /**
         * A collection of an entity (eg Order/OrderDetail).
         */
        PARENTED(RenderingHint.PARENTED_PROPERTY_COLUMN, 12),
        ;
        public boolean isStandalone() { return this == STANDALONE; }
        public boolean isParented() { return this == PARENTED; }

        @Getter private final RenderingHint columnRenderingHint;
        @Getter private final int pageSizeDefault;

        public RenderingHint getTitleColumnRenderingHint() {
            return isParented()
                ? RenderingHint.PARENTED_TITLE_COLUMN
                : RenderingHint.STANDALONE_TITLE_COLUMN;
        }
    }

    // -- EMPTY

    public static CollectionModel empty() {
        return new CollectionModelEmpty();
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

    DataTableInteractive getDataTableModel();

    // -- METAMODEL SUPPORT

    ObjectMember getMetaModel();

    default ObjectSpecification getElementType() {
        return getMetaModel().getElementType();
    }

    // -- OWNER SUPPORT

    ManagedObject getParentObject();

    // -- BASIC PROPERTIES

    /**
     * Element count after filtering.
     */
    default int getElementCount() {
        return getDataTableModel().getFilteredElementCount();
    }

    default String getName() {
        return getDataTableModel().titleObservable().getValue();
    }

    default int getPageSize() {
        return getDataTableModel().getPageSize(getVariant().getPageSizeDefault());
    }

    default Optional<TableDecorator> getTableDecoratorIfAny() {
        return getDataTableModel().getTableDecoratorIfAny();
    }

    /**
     * Whether the hidden table view is to be rendered. Corresponds to the {@link CollectionModelEmpty}.
     */
    default boolean isHidden() {
        return "hidden".equals(getName());
    }

    // -- INITIALIZATION STATE

    /**
     * Whether the table model is initialized with row data.
     */
    boolean isTableDataLoaded();

    // -- PARENTED SPECIFICS

    /**
     * Used to create the first part of the collection presentation selection hint,
     * the second part is the id of the component factory that creates the UI component
     * used to render the selected presentation.
     * <p>
     * Currently not available for standalone collections.
     * Perhaps it would be possible to add support, by instead of using the hint-store
     * use a simple page parameter like ?presentation=excel.
     */
    default Optional<Bookmark> parentedHintingBookmark() {
        return this instanceof CollectionModelParented parented
            ? Optional.ofNullable(parented.asHintingBookmark())
            : Optional.empty();
    }

}
