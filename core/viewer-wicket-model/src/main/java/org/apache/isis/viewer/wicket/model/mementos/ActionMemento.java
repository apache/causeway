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

package org.apache.isis.viewer.wicket.model.mementos;

import java.io.Serializable;

import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.metamodel.adapter.concurrency.ConcurrencyChecking;
import org.apache.isis.metamodel.spec.ActionType;
import org.apache.isis.metamodel.spec.ObjectSpecId;
import org.apache.isis.metamodel.spec.ObjectSpecification;
import org.apache.isis.metamodel.spec.feature.ObjectAction;
import org.apache.isis.metamodel.specloader.SpecificationLoader;

/**
 * {@link Serializable} represention of a {@link ObjectAction}
 */
public class ActionMemento implements Serializable {

    private static final long serialVersionUID = 1L;

    private final ObjectSpecId owningType;
    private final ActionType actionType;
    private final String nameParmsId;
    private final SemanticsOf actionSemantics;

    private transient ObjectAction action;

    public ActionMemento(final ObjectAction action) {
        this(action.getOnType().getSpecId(), action.getType(), action.getIdentifier().toNameParmsIdentityString(), action);
    }

    public ActionMemento(
            final ObjectSpecId owningType,
            final ActionType actionType,
            final String nameParmsId,
            final SpecificationLoader specificationLoader) {
        this(owningType, actionType, nameParmsId, actionFor(owningType, actionType, nameParmsId, specificationLoader));
    }

    private ActionMemento(
            final ObjectSpecId owningSpecId,
            final ActionType actionType,
            final String nameParmsId,
            final ObjectAction action) {
        this.owningType = owningSpecId;
        this.actionType = actionType;
        this.nameParmsId = nameParmsId;
        this.action = action;
        this.actionSemantics = action.getSemantics();
    }

    public ObjectSpecId getOwningType() {
        return owningType;
    }

    public ActionType getActionType() {
        return actionType;
    }

    public String getNameParmsId() {
        return nameParmsId;
    }

    public ConcurrencyChecking getConcurrencyChecking() {
        return ConcurrencyChecking.concurrencyCheckingFor(this.actionSemantics);
    }

    public ObjectAction getAction(final SpecificationLoader specificationLoader) {
        if (action == null) {
            action = actionFor(owningType, actionType, nameParmsId, specificationLoader);
        }
        return action;
    }

    private static ObjectAction actionFor(
            ObjectSpecId owningType,
            ActionType actionType,
            String nameParmsId,
            final SpecificationLoader specificationLoader) {
        final ObjectSpecification objectSpec = specificationLoader.lookupBySpecIdElseLoad(owningType);
        return objectSpec.getObjectAction(actionType, nameParmsId);
    }

}
