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

import java.util.List;

import org.apache.wicket.model.LoadableDetachableModel;

import org.apache.causeway.applib.Identifier;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.internal.collections._Lists;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.spec.feature.ObjectMember;
import org.apache.causeway.core.metamodel.tabular.DataTableInteractive;
import org.apache.causeway.core.metamodel.tabular.DataTableMemento;
import org.apache.causeway.viewer.wicket.model.models.ActionModel;
import org.apache.causeway.viewer.wicket.model.models.interaction.BookmarkedObjectWkt;

import lombok.NonNull;

/**
 * Represents a collection (a member) of a domain object.
 *
 * Bound to a {@link BookmarkedObjectWkt} and {@link DataTableInteractive}
 * representing either a <i>Collection</i> or an <i>Action</i>'s result.
 *
 * @implSpec {@link DataTableInteractive} is not serializable,
 *      hence is held transient,
 *      that means it does not survive a serialization/de-serialization cycle;
 *      it is recreated on {@link LoadableDetachableModel#load}
 */
sealed abstract class CollectionModelAbstract
extends LoadableDetachableModel<DataTableInteractive>
implements CollectionModel
permits CollectionModelParented, CollectionModelStandalone {

    private static final long serialVersionUID = 1L;

    private final @NonNull Variant variant;
    private final BookmarkedObjectWkt bookmarkedObject;
    private final DataTableMemento tableMemento;

    protected CollectionModelAbstract(
            final BookmarkedObjectWkt bookmarkedObject,
            final DataTableInteractive tableInteractive,
            final @NonNull Variant variant) {

        this.bookmarkedObject = bookmarkedObject;
        this.tableMemento = tableInteractive.createMemento();
        setObject(tableInteractive); // memoize
        tableMemento.setupBindings(tableInteractive);
        this.variant = variant;
    }

    @Override
    protected DataTableInteractive load() {
        var tableInteractive = tableMemento.getDataTableModel(bookmarkedObject.asManagedObject());
        tableMemento.setupBindings(tableInteractive);
        return tableInteractive;
    }

    @Override
    public final boolean isTableDataLoaded() {
        return this.isAttached();
    }

    @Override
    public final DataTableInteractive getDataTableModel() {
        return getObject();
    }

    @Override
    public ObjectMember getMetaModel() {
        return getDataTableModel()
                .getMetaModel();
    }

    @Override
    public Identifier getIdentifier() {
        return getMetaModel().getFeatureIdentifier();
    }

    @Override
    public final ManagedObject getParentObject() {
        return bookmarkedObject.asManagedObject();
    }

    /* XXX[CAUSEWAY-3798] do not override (as it was for the hidden table)
     * Otherwise would store 1 as pags-size hint when table is initially hidden.
     * We also do want to keep pags-size hints,
     * even when switching back and forth between presentations ('table', 'hidden'). */
    @Override
    public final int getPageSize() {
        return CollectionModel.super.getPageSize();
    }

    // -- VARIANT SUPPORT

    @Override
    public final Variant getVariant() {
        return variant;
    }

    // -- LINKS PROVIDER

    /**
     * Additional links to render (if any)
     */
    private List<ActionModel> actionModels = _Lists.newArrayList();

    public final void setLinkAndLabels(final @NonNull Iterable<ActionModel> actionModels) {
        this.actionModels.clear();
        actionModels.forEach(this.actionModels::add);
    }

    @Override
    public final Can<ActionModel> getLinks() {
        return Can.ofCollection(actionModels);
    }

}
