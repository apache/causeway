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
package org.apache.causeway.viewer.wicket.model.models.interaction.act;

import org.apache.wicket.model.IModel;

import org.apache.causeway.core.metamodel.interactions.managed.ActionInteraction;
import org.apache.causeway.core.metamodel.interactions.managed.ParameterNegotiationModel;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.spec.feature.ObjectActionParameter;
import org.apache.causeway.viewer.commons.model.object.HasUiParentObject;
import org.apache.causeway.viewer.commons.model.object.UiObject;
import org.apache.causeway.viewer.commons.model.scalar.UiParameter;
import org.apache.causeway.viewer.wicket.model.models.HasCommonContext;

import lombok.NonNull;

/**
 * <i>Action Parameter Interaction</i> model bound to its owner {@link ActionInteractionWkt}.
 *
 * @see ActionInteractionWkt
 */
public record UiParameterWkt(
    ActionInteractionWkt actionInteractionModel,
    int paramIndex)
implements
    IModel<ActionInteraction>,
    HasCommonContext,
    HasUiParentObject<UiObject>,
    UiParameter {

    public ActionInteraction actionInteraction() {
        return getObject();
    }

    public ActionInteractionWkt actionInteractionModel() {
        return actionInteractionModel;
    }

    @Override
    public UiObject getParentUiModel() {
        return ()->getOwner();
    }

    @Override
    public ManagedObject getOwner() {
        return actionInteraction().getManagedAction().get().getOwner();
    }

    @Override
    public ObjectActionParameter getMetaModel() {
        return actionInteraction().getMetamodel().get().getParameters().getElseFail(paramIndex);
    }

    @Override
    public @NonNull ManagedObject getValue() {
        return getParameterNegotiationModel().getParamValue(paramIndex);
    }

    @Override
    public void setValue(final ManagedObject paramValue) {
        getParameterNegotiationModel().setParamValue(paramIndex, paramValue);
    }

    @Override
    public ParameterNegotiationModel getParameterNegotiationModel() {
        return actionInteractionModel().parameterNegotiationModel();
    }

    // -- MODEL CHAINING

    @Override
    public void detach() {
        actionInteractionModel.detach();
    }

    @Override
    public void setObject(final ActionInteraction object) {
        actionInteractionModel.setObject(object);
    }

    @Override
    public ActionInteraction getObject() {
        return actionInteractionModel.getObject();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Model:classname=[");
        sb.append(getClass().getName()).append(']');
        sb.append(":nestedModel=[").append(actionInteractionModel).append(']');
        return sb.toString();
    }

}
