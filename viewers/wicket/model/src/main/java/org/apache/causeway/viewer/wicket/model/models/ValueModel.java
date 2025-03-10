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
package org.apache.causeway.viewer.wicket.model.models;

import java.io.Serializable;
import org.apache.wicket.model.IModel;
import org.jspecify.annotations.NonNull;

import org.apache.causeway.commons.internal.assertions._Assert;
import org.apache.causeway.commons.internal.ref.TransientObjectRef;
import org.apache.causeway.core.metamodel.context.HasMetaModelContext;
import org.apache.causeway.core.metamodel.facets.object.value.CompositeValueUpdater;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.objectmanager.memento.ObjectMemento;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.ObjectMember;

/**
 * Represents a standalone value (used for standalone value page).
 */
public record ValueModel(
        /**
         * The originating {@link ObjectMember} this {@link ValueModel} is provided by.
         */
        ObjectMember objectMember,
        ObjectMemento objectMemento,
        TransientObjectRef<ManagedObject> managedObjectRef)
implements IModel<ManagedObject>, HasMetaModelContext {

    public ValueModel(@NonNull final ActionModel actionModel, final ManagedObject managedObject) {
        this(actionModel.getAction(), managedObject);
    }

    public ValueModel(
            final @NonNull ObjectMember objectMember,
            final @NonNull ManagedObject managedObject) {
        this(objectMember,
            managedObject.getMemento().orElseThrow(),
            new TransientObjectRef<>(managedObject));
    }

    // canonical constructor
    public ValueModel(
            final ObjectMember objectMember,
            final ObjectMemento objectMemento,
            final TransientObjectRef<ManagedObject> managedObjectRef) {
        this.objectMember = objectMember instanceof CompositeValueUpdater compositeValueUpdater
            ? compositeValueUpdater.mixedInAction()
            : objectMember;
        this.objectMemento = objectMemento;
        this.managedObjectRef = managedObjectRef;

        _Assert.assertTrue(this.objectMember instanceof Serializable);
    }

    @Override
    public ManagedObject getObject() {
        return managedObjectRef.computeIfAbsent(()->getObjectManager().demementify(objectMemento));
    }

    public ObjectSpecification elementType() {
        return getObject().getSpecification();
    }

}
