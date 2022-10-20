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

import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.objectmanager.memento.ObjectMemento;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;
import org.apache.causeway.core.metamodel.spec.feature.ObjectMember;
import org.apache.causeway.core.metamodel.spec.feature.memento.ObjectMemberMemento;

import lombok.NonNull;

/**
 * Represents a standalone value (used for standalone value page).
 */
public class ValueModel
extends ModelAbstract<ManagedObject> {

    private static final long serialVersionUID = 1L;

    // -- FACTORIES

    public static ValueModel of(
            final @NonNull  MetaModelContext commonContext,
            final @NonNull  ObjectMember objectMember,
            final @NonNull ManagedObject valueAdapter) {
        return new ValueModel(commonContext, objectMember, valueAdapter);
    }

    // --

    private final ObjectMemento adapterMemento;

    private ValueModel(
            final MetaModelContext commonContext,
            final @NonNull  ObjectMember objectMember,
            final @NonNull ManagedObject valueAdapter) {
        super(commonContext);
        this.objectMemberMemento = ObjectMemberMemento.forMember(objectMember);
        adapterMemento = valueAdapter.getMemento().orElseThrow();
    }

    @Override
    protected ManagedObject load() {
        return getObjectManager().demementify(adapterMemento);
    }

    // -- META MODEL

    private final ObjectMemberMemento objectMemberMemento;

    /**
     * The originating {@link ObjectMember} this {@link ValueModel} is provided by.
     */
    public ObjectMember getObjectMember() {
        return objectMemberMemento.getObjectMember(getMetaModelContext()::getSpecificationLoader);
    }

//    @SuppressWarnings("unchecked")
//    public Optional<Renderer<?>> lookupRenderer() {
//        return getObjectMember().getElementType().lookupFacet(ValueFacet.class)
//                .flatMap(valueFacet->valueFacet.selectRendererForFeature(getObjectMember()));
//    }

    // -- HINTING SUPPORT

    private ActionModel actionModelHint;
    /**
     * The {@link ActionModelImpl model} of the {@link ObjectAction action}
     * that generated this {@link ValueModel}.
     *
     * @see #setActionHint(ActionModel)
     */
    public ActionModel getActionModelHint() {
        return actionModelHint;
    }
    /**
     * Called by action.
     *
     * @see #getActionModelHint()
     */
    public void setActionHint(final ActionModel actionModelHint) {
        this.actionModelHint = actionModelHint;
    }

}
