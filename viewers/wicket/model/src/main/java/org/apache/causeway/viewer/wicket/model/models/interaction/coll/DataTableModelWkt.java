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
package org.apache.causeway.viewer.wicket.model.models.interaction.coll;

import org.apache.wicket.model.IModel;

import org.apache.causeway.applib.Identifier;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.internal.exceptions._Exceptions;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;
import org.apache.causeway.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.causeway.core.metamodel.tabular.DataTableInteractive;
import org.apache.causeway.core.metamodel.tabular.DataTableInteractive.TableImplementation;
import org.apache.causeway.viewer.commons.model.object.HasUiParentObject;
import org.apache.causeway.viewer.commons.model.object.UiObject;
import org.apache.causeway.viewer.wicket.model.models.interaction.BookmarkedObjectWkt;
import org.apache.causeway.viewer.wicket.model.models.interaction.HasBookmarkedOwnerAbstract;

import lombok.NonNull;

/**
 * Bound to a BookmarkedObjectWkt, with the {@link DataTableInteractive}
 * representing either a <i>Collection</i> or an <i>Action</i>'s result.
 *
 * @implSpec the state of the DataTableModel is held transient,
 * that means it does not survive a serialization/de-serialization cycle;
 * it is recreated on load
 *
 * @see HasBookmarkedOwnerAbstract
 */
public interface DataTableModelWkt
extends
    IModel<DataTableInteractive>,
    HasUiParentObject<UiObject> {

    // -- FACTORIES

    public static DataTableModelWkt forActionModel(
            final @NonNull BookmarkedObjectWkt bookmarkedObjectModel,
            final @NonNull ObjectAction actMetaModel,
            final @NonNull ManagedObject actionResult) {
        switch (TableImplementation.getSelected()) {
        case OPTIMISTIC:
            throw _Exceptions.unexpectedCodeReach();
        case DEFAULT:
        default:
            return DataTableModelWktD.forActionModel(bookmarkedObjectModel, actMetaModel, actionResult);
        }
    }

    public static DataTableModelWkt forActionModel(
            final @NonNull BookmarkedObjectWkt bookmarkedObjectModel,
            final @NonNull ObjectAction actMetaModel,
            final @NonNull Can<ManagedObject> args,
            final @NonNull ManagedObject actionResult) {
        switch (TableImplementation.getSelected()) {
        case OPTIMISTIC:
            return DataTableModelWktO.forActionModel(bookmarkedObjectModel, actMetaModel, args, actionResult);
        case DEFAULT:
        default:
            return DataTableModelWktD.forActionModel(bookmarkedObjectModel, actMetaModel, actionResult);
        }
    }

    public static @NonNull DataTableModelWkt forCollection(
            final @NonNull BookmarkedObjectWkt bookmarkedObjectModel,
            final @NonNull OneToManyAssociation collMetaModel) {
        switch (TableImplementation.getSelected()) {
        case OPTIMISTIC:
            return DataTableModelWktO.forCollection(bookmarkedObjectModel, collMetaModel);
        case DEFAULT:
        default:
            return DataTableModelWktD.forCollection(bookmarkedObjectModel, collMetaModel);
        }
    }

    Identifier getFeatureIdentifier();
    boolean isAttached();
    ManagedObject getBookmarkedOwner();

}
