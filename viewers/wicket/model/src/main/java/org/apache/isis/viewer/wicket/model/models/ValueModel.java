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

import org.springframework.lang.Nullable;

import org.apache.isis.core.metamodel.objectmanager.memento.ObjectMemento;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.runtime.context.IsisAppCommonContext;

import lombok.NonNull;

/**
 * Represents a standalone value.
 */
public class ValueModel extends ModelAbstract<ManagedObject> {

    private static final long serialVersionUID = 1L;

    // -- FACTORIES

    public static ValueModel of(
            final @NonNull  IsisAppCommonContext commonContext,
            final @Nullable ManagedObject valueAdapter) {
        return new ValueModel(commonContext, valueAdapter);
    }

    // --

    private final ObjectMemento adapterMemento;

    private ValueModel(
            final IsisAppCommonContext commonContext,
            final @Nullable ManagedObject valueAdapter) {
        super(commonContext);
        adapterMemento = super.getMementoService().mementoForObject(valueAdapter);
    }

    @Override
    protected ManagedObject load() {
        return getCommonContext().reconstructObject(adapterMemento);
    }

    // -- HINTING SUPPORT

    private ActionModel actionModelHint;
    /**
     * The {@link ActionModel model} of the {@link ObjectAction action}
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
