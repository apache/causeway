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

import org.apache.isis.commons.collections.Can;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.PackedManagedObject;
import org.apache.isis.viewer.wicket.model.models.interaction.BookmarkedObjectWkt;
import org.apache.isis.viewer.wicket.model.models.interaction.coll.DataTableModelWkt;

import lombok.NonNull;
import lombok.val;

public class EntityCollectionModelStandalone
extends EntityCollectionModelAbstract {

    private static final long serialVersionUID = 1L;

    // -- FACTORIES

    public static EntityCollectionModelStandalone forActionModel(
            final @NonNull PackedManagedObject collectionAsAdapter,
            final @NonNull ActionModel actionModel,
            final @NonNull Can<ManagedObject> args) {

        val action = actionModel.getAction();

        return new EntityCollectionModelStandalone(
                DataTableModelWkt.forActionModel(
                        BookmarkedObjectWkt
                            .ofAdapter(actionModel.getCommonContext(), actionModel.getParentObject()),
                        action,
                        args,
                        collectionAsAdapter));
    }

    // -- CONSTRUCTOR

    private EntityCollectionModelStandalone(
            final @NonNull DataTableModelWkt delegate) {
        super(delegate, Variant.STANDALONE);
    }

}
