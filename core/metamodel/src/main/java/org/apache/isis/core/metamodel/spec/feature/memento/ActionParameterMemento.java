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

import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.ObjectActionParameter;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

/**
 * {@link Serializable} representation of a {@link ObjectActionParameter parameter}
 * of a {@link ObjectAction}.
 *
 * @see ActionMemento
 */
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class ActionParameterMemento implements Serializable {

    private static final long serialVersionUID = 1L;

    @Getter private final @NonNull ActionMemento actionMemento;
    @Getter private final int number;

    // -- FACTORY

    public static ActionParameterMemento forActionParameter(final ObjectActionParameter actionParameter) {
        return new ActionParameterMemento(
                ActionMemento.forAction(actionParameter.getAction()),
                actionParameter.getNumber(),
                actionParameter);
    }

    // -- LOAD/UNMARSHAL

    private transient ObjectActionParameter actionParameter;

    public ObjectActionParameter getActionParameter(final SpecificationLoader specLoader) {
        if (actionParameter == null) {
            this.actionParameter = actionParameterFor(actionMemento, number, specLoader);
        }
        return actionParameter;
    }

    /**
     * Convenience.
     */
    public ObjectSpecification getSpecification(final SpecificationLoader specLoader) {
        return getActionParameter(specLoader).getSpecification();
    }

    @Override
    public String toString() {
        return getActionMemento().getNameParmsId() + "#" + getNumber();
    }

    // -- HELPER

    private static ObjectActionParameter actionParameterFor(
            final ActionMemento actionMemento,
            final int paramIndex,
            final SpecificationLoader specLoader) {
        final ObjectAction action = actionMemento.getAction(specLoader);
        return action.getParameters().getElseFail(paramIndex);
    }

}
