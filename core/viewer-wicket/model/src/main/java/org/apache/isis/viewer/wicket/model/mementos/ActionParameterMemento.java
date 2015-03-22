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

import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.ObjectActionParameter;

/**
 * {@link Serializable} representation of a {@link ObjectActionParameter parameter}
 * of a {@link ObjecObjectAction}.
 * 
 * @see ActionMemento
 */
public class ActionParameterMemento implements Serializable {

    private static final long serialVersionUID = 1L;

    private final ActionMemento actionMemento;
    private final int number;

    private transient ObjectActionParameter actionParameter;

    public ActionParameterMemento(final ActionMemento actionMemento, final int number) {
        this(actionMemento, number, actionParameterFor(actionMemento, number));
    }

    public ActionParameterMemento(final ObjectActionParameter actionParameter) {
        this(new ActionMemento(actionParameter.getAction()), actionParameter.getNumber(), actionParameter);
    }

    private ActionParameterMemento(
            final ActionMemento actionMemento, 
            final int number, 
            final ObjectActionParameter actionParameter) {
        this.actionMemento = actionMemento;
        this.number = number;
        this.actionParameter = actionParameter;
    }

    public ActionMemento getActionMemento() {
        return actionMemento;
    }

    public int getNumber() {
        return number;
    }

    public ObjectActionParameter getActionParameter() {
        if (actionParameter == null) {
            this.actionParameter = actionParameterFor(actionMemento, number);
        }
        return actionParameter;
    }

    private static ObjectActionParameter actionParameterFor(ActionMemento actionMemento, int number) {
        final ObjectAction action = actionMemento.getAction();
        return action.getParameters().get(number);
    }

    /**
     * Convenience.
     */
    public ObjectSpecification getSpecification() {
        return getActionParameter().getSpecification();
    }

}
