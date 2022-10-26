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

import org.apache.wicket.model.ChainingModel;

import org.apache.causeway.core.metamodel.context.MetaModelContext;
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
public final class UiParameterWkt
extends ChainingModel<ActionInteraction>
implements
    HasCommonContext,
    HasUiParentObject<UiObject>,
    UiParameter {

    private static final long serialVersionUID = 1L;

    final int paramIndex;

    UiParameterWkt(
            final ActionInteractionWkt model,
            final int paramIndex) {
        super(model);
        this.paramIndex = paramIndex;
    }

    public final ActionInteraction actionInteraction() {
        return getObject();
    }

    public final ActionInteractionWkt actionInteractionModel() {
        return (ActionInteractionWkt) getChainedModel();
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

    @Override
    public MetaModelContext getMetaModelContext() {
        return actionInteractionModel().getMetaModelContext();
    }

}
