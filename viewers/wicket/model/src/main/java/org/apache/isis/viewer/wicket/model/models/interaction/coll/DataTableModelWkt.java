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
package org.apache.isis.viewer.wicket.model.models.interaction.coll;

import org.apache.isis.applib.Identifier;
import org.apache.isis.applib.annotation.Where;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.core.metamodel.interactions.managed.ManagedAction;
import org.apache.isis.core.metamodel.interactions.managed.ManagedCollection;
import org.apache.isis.core.metamodel.interactions.managed.nonscalar.DataTableModel;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.isis.viewer.common.model.HasParentUiModel;
import org.apache.isis.viewer.wicket.model.models.interaction.BookmarkedObjectWkt;
import org.apache.isis.viewer.wicket.model.models.interaction.HasBookmarkedOwnerAbstract;
import org.apache.isis.viewer.wicket.model.models.interaction.ObjectUiModelWkt;

import lombok.Getter;
import lombok.NonNull;
import lombok.val;

/**
 * Bound to a BookmarkedObjectWkt, with the {@link DataTableModel}
 * representing either a <i>Collection</i> or an <i>Action</i>'s result.
 *
 * @implSpec the state of the DataTableModel is held transient,
 * that means it does not survive a serialization/de-serialization cycle;
 * it is recreated on load
 *
 * @see HasBookmarkedOwnerAbstract
 */
public class DataTableModelWkt
extends HasBookmarkedOwnerAbstract<DataTableModel>
implements
    HasParentUiModel<ObjectUiModelWkt> {

    // -- FACTORIES

    public static DataTableModelWkt forActionModel(
            final @NonNull BookmarkedObjectWkt bookmarkedObjectModel,
            final @NonNull ObjectAction actMetaModel,
            final @NonNull Can<ManagedObject> args,
            final @NonNull ManagedObject actionResult) {

        val managedAction = ManagedAction
                .of(bookmarkedObjectModel.getObject(), actMetaModel, Where.NOT_SPECIFIED);

        val table = DataTableModel.forAction(
                managedAction,
                args,
                actionResult);

        val tableMemento = table.getMemento(managedAction.getMementoForArgs(args));

        val model = new DataTableModelWkt(
                bookmarkedObjectModel, actMetaModel.getFeatureIdentifier(), tableMemento);

        model.setObject(table); // memoize

        return model;
    }

    public static @NonNull DataTableModelWkt forCollection(
            final @NonNull BookmarkedObjectWkt bookmarkedObjectModel,
            final @NonNull OneToManyAssociation collMetaModel) {

        val table = DataTableModel.forCollection(
                ManagedCollection
                .of(bookmarkedObjectModel.getObject(), collMetaModel, Where.NOT_SPECIFIED));

        val tableMemento = table.getMemento(null);

        val model =  new DataTableModelWkt(
                bookmarkedObjectModel, collMetaModel.getFeatureIdentifier(), tableMemento);

        model.setObject(table); // memoize

        return model;
    }

    // -- CONSTRUCTION

    private static final long serialVersionUID = 1L;

    @Getter private final Identifier featureIdentifier;
    private final DataTableModel.Memento tableMemento;

    private DataTableModelWkt(
            final BookmarkedObjectWkt bookmarkedObject,
            final Identifier featureIdentifier,
            final DataTableModel.Memento tableMemento) {
        super(bookmarkedObject);
        this.featureIdentifier = featureIdentifier;
        this.tableMemento = tableMemento;
    }

    // --

    @Override
    public ObjectUiModelWkt getParentUiModel() {
        return ()->super.getBookmarkedOwner();
    }

    @Override
    protected DataTableModel load() {
        val dataTableModel = tableMemento.getDataTableModel(getBookmarkedOwner());
        return dataTableModel;
    }

    @Override
    public void detach() {
        // in support of table sorting, don't detach; instead reuse model
    }

}
