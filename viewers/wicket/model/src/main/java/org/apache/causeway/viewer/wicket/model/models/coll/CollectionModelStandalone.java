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

import org.apache.causeway.applib.annotation.Where;
import org.apache.causeway.core.metamodel.interactions.managed.ManagedAction;
import org.apache.causeway.core.metamodel.object.PackedManagedObject;
import org.apache.causeway.core.metamodel.tabular.DataTableInteractive;
import org.apache.causeway.viewer.wicket.model.models.ActionModel;
import org.apache.causeway.viewer.wicket.model.models.interaction.BookmarkedObjectWkt;

import lombok.NonNull;

public final class CollectionModelStandalone
extends CollectionModelAbstract {

    private static final long serialVersionUID = 1L;

    // -- FACTORIES

    public static CollectionModelStandalone forActionModel(
            final @NonNull PackedManagedObject collectionAsAdapter,
            final @NonNull ActionModel actionModel) {

        var bookmarkedObject = BookmarkedObjectWkt.ofAdapter(actionModel.getParentObject());
        var tableInteractive = DataTableInteractive.forAction(
            ManagedAction.of(bookmarkedObject.getObject(), actionModel.getAction(), Where.NOT_SPECIFIED),
            collectionAsAdapter);

        return new CollectionModelStandalone(bookmarkedObject, tableInteractive);
    }

    // -- CONSTRUCTOR

    private CollectionModelStandalone(
            final BookmarkedObjectWkt bookmarkedObject,
            final DataTableInteractive tableInteractive) {
        super(bookmarkedObject, tableInteractive, Variant.STANDALONE);
    }

}
