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

package org.apache.isis.core.metamodel.spec.feature.memento;

import java.io.Serializable;

import javax.annotation.Nullable;

import org.apache.isis.applib.id.LogicalType;
import org.apache.isis.core.metamodel.spec.ActionType;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.Synchronized;

/**
 * {@link Serializable} representation of a {@link ObjectAction}
 *
 * @implNote thread-safe memoization
 *
 * @since 2.0 {index}
 */
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class ActionMemento implements Serializable {

    private static final long serialVersionUID = 1L;

    @Getter private final @NonNull LogicalType owningType;
    @Getter private final @NonNull ActionType actionType;
    @Getter private final @Nullable String nameParmsId; //nullable?

    // -- FACTORY

    public static ActionMemento forAction(final @NonNull ObjectAction action) {
        return new ActionMemento(
                action.getOnType().getLogicalType(),
                action.getType(),
                action.getIdentifier().getMemberNameAndParameterClassNamesIdentityString(),
                action);
    }

    // -- LOAD/UNMARSHAL

    private transient ObjectAction action;

    @Synchronized
    public ObjectAction getAction(final @NonNull SpecificationLoader specLoader) {
        if (action == null) {
            action = specLoader
                    .specForLogicalTypeElseFail(owningType)
                    .getActionElseFail(nameParmsId, actionType);
        }
        return action;
    }

    // -- OBJECT CONTRACT

    @Override
    public String toString() {
        return getOwningType().getLogicalTypeName() + "#" + getNameParmsId();
    }

}
