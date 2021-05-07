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

package org.apache.isis.viewer.common.model.mementos;

import java.io.Serializable;

import org.apache.isis.applib.id.LogicalType;
import org.apache.isis.core.metamodel.spec.ActionType;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;

import lombok.Getter;

/**
 * {@link Serializable} representation of a {@link ObjectAction}
 */
public class ActionMemento implements Serializable {

    private static final long serialVersionUID = 1L;

    @Getter private final LogicalType owningType;
    private final ActionType actionType;
    private final String nameParmsId;

    private transient ObjectAction action;

    public static ActionMemento forAction(final ObjectAction action) {
        return new ActionMemento(action.getOnType().getLogicalType(),
                action.getType(),
                action.getIdentifier().getMemberNameAndParameterClassNamesIdentityString(),
                action);
    }

    public ActionMemento(
            final LogicalType owningType,
            final ActionType actionType,
            final String nameParmsId,
            final SpecificationLoader specificationLoader) {
        this(owningType, actionType, nameParmsId,
                actionFor(owningType, actionType, nameParmsId, specificationLoader));
    }

    protected ActionMemento(
            final LogicalType owningType,
            final ActionType actionType,
            final String nameParmsId,
            final ObjectAction action) {
        this.owningType = owningType;
        this.actionType = actionType;
        this.nameParmsId = nameParmsId;
        this.action = action;
    }

    public ActionType getActionType() {
        return actionType;
    }

    public String getNameParmsId() {
        return nameParmsId;
    }

    public ObjectAction getAction(final SpecificationLoader specificationLoader) {
        if (action == null) {
            action = actionFor(owningType, actionType, nameParmsId, specificationLoader);
        }
        return action;
    }

    // -- HELPER

    private static ObjectAction actionFor(
            LogicalType owningType,
            ActionType actionType,
            String nameParmsId,
            SpecificationLoader specificationLoader) {

        return specificationLoader
                .specForLogicalTypeElseFail(owningType)
                .getActionElseFail(nameParmsId, actionType);
    }

}
