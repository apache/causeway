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
package org.apache.isis.viewer.wicket.model.models.interaction.action;

import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.internal.assertions._Assert;
import org.apache.isis.commons.internal.base._Lazy;
import org.apache.isis.core.metamodel.interactions.managed.ActionInteraction;
import org.apache.isis.core.metamodel.interactions.managed.ParameterNegotiationModel;
import org.apache.isis.core.runtime.context.IsisAppCommonContext;
import org.apache.isis.viewer.wicket.model.models.ModelAbstract;

/**
 *
 * The parent of multiple parameter models which implement
 * {@link _ActionInteractionHolder}.
 *
 * @see _ActionInteractionHolder
 */
public final class ActionInteractionModelWkt
extends ModelAbstract<ActionInteraction> {

    private static final long serialVersionUID = 1L;

    private final ActionInteraction.Memento memento;
    private final Can<ParameterUiModelWkt> childModels;

    public ActionInteractionModelWkt(
            final IsisAppCommonContext commonContext,
            final ActionInteraction model) {
        super(commonContext, model);
        this.memento = model.getMemento();

        final int paramCount = model.getMetamodel().get().getParameterCount();
        this.childModels = IntStream.range(0, paramCount)
                .mapToObj(paramIndex -> new ParameterUiModelWkt(this, paramIndex))
                .collect(Can.toCan());
    }

    @Override
    protected final ActionInteraction load() {
        childModels.forEach(childModel->childModel.attachToContainerModel(this));
        return memento.getActionInteraction(getCommonContext().getMetaModelContext());
    }

    @Override
    public void detach() {
        childModels.forEach(_ActionInteractionHolder::detachFromContainerModel);
        super.detach();
    }

    public final ActionInteraction actionInteraction() {
        return getObject();
    }

    public ParameterUiModelWkt getParameterUiModel(final int paramIndex) {
        return childModels.getElseFail(paramIndex);
    }

    public Stream<ParameterUiModelWkt> streamParameterUiModels() {
        return childModels.stream();
    }

    // -- PARAMETER NEGOTIATION WITH MEMOIZATION (TRANSIENT)

    private final transient _Lazy<Optional<ParameterNegotiationModel>> parameterNegotiationModel =
            _Lazy.threadSafe(()->actionInteraction().startParameterNegotiation());

    public final Optional<ParameterNegotiationModel> parameterNegotiationModel() {
        _Assert.assertTrue(this.isAttached(), "container model is not attached");
        return parameterNegotiationModel.get();
    }

    public void resetParametersToDefault() {
        parameterNegotiationModel.clear();
    }




}
