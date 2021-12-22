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
package org.apache.isis.viewer.wicket.model.models.interaction.act;

import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.apache.wicket.model.ChainingModel;
import org.springframework.lang.Nullable;

import org.apache.isis.applib.Identifier;
import org.apache.isis.applib.annotation.Where;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.internal.assertions._Assert;
import org.apache.isis.commons.internal.base._Lazy;
import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.core.metamodel.interactions.managed.ActionInteraction;
import org.apache.isis.core.metamodel.interactions.managed.ParameterNegotiationModel;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.viewer.wicket.model.models.EntityCollectionModel;
import org.apache.isis.viewer.wicket.model.models.EntityModel;
import org.apache.isis.viewer.wicket.model.models.InlinePromptContext;
import org.apache.isis.viewer.wicket.model.models.ScalarParameterModel;
import org.apache.isis.viewer.wicket.model.models.ScalarPropertyModel;
import org.apache.isis.viewer.wicket.model.models.interaction.BookmarkedObjectWkt;
import org.apache.isis.viewer.wicket.model.models.interaction.HasBookmarkedOwnerAbstract;

import lombok.val;

/**
 * The parent (container) model of multiple <i>parameter models</i> which implement
 * {@link ChainingModel}.
 * <pre>
 * IModel[ActionInteraction] ... placeOrder(X x, Yy)
 * |
 * +-- ParameterUiModel ... bound to X x (ParameterNegotiationModel)
 * +-- ParameterUiModel ... bound to Y y (ParameterNegotiationModel)
 * </pre>
 * This action might be associated with a <i>Collection</i> that acts as its multi-select
 * defaults provider. This is modeled with {@link #associatedWithCollectionIfAny}.
 *
 * @implSpec the state of pending parameters ParameterNegotiationModel is held transient,
 * that means it does not survive a serialization/de-serialization cycle; instead
 * is recreated with parameter defaults
 *
 * @see ChainingModel
 */
public class ActionInteractionWkt
extends HasBookmarkedOwnerAbstract<ActionInteraction> {

    private static final long serialVersionUID = 1L;

    private final String memberId;
    private final Where where;
    private Can<ParameterUiModelWkt> childModels;
    private @Nullable ScalarPropertyModel associatedWithPropertyIfAny;
    private @Nullable ScalarParameterModel associatedWithParameterIfAny;
    private @Nullable EntityCollectionModel associatedWithCollectionIfAny;

    public static ActionInteractionWkt forEntity(
            final EntityModel parentEntityModel,
            final Identifier actionIdentifier,
            final Where where,
            final ScalarPropertyModel associatedWithPropertyIfAny,
            final ScalarParameterModel associatedWithParameterIfAny,
            final EntityCollectionModel associatedWithCollectionIfAny) {
        return new ActionInteractionWkt(
                parentEntityModel.bookmarkedObjectModel(),
                actionIdentifier.getMemberLogicalName(),
                where,
                associatedWithPropertyIfAny,
                associatedWithParameterIfAny,
                associatedWithCollectionIfAny);
    }

    private ActionInteractionWkt(
            final BookmarkedObjectWkt bookmarkedObject,
            final String memberId,
            final Where where,
            final ScalarPropertyModel associatedWithPropertyIfAny,
            final ScalarParameterModel associatedWithParameterIfAny,
            final EntityCollectionModel associatedWithCollectionIfAny) {
        super(bookmarkedObject);
        this.memberId = memberId;
        this.where = where;
        this.associatedWithPropertyIfAny = associatedWithPropertyIfAny;
        this.associatedWithParameterIfAny = associatedWithParameterIfAny;
        this.associatedWithCollectionIfAny = associatedWithCollectionIfAny;
    }

    @Override
    protected ActionInteraction load() {

        // setup the lazy, don't yet evaluate
        parameterNegotiationModel =
                _Lazy.threadSafe(()->actionInteraction().startParameterNegotiation());

        if(associatedWithParameterIfAny!=null) {
            final int paramIndex = associatedWithParameterIfAny.getParameterIndex();
            val paramValue = associatedWithParameterIfAny.getParameterNegotiationModel().getParamValue(paramIndex);
            return ActionInteraction.start(paramValue, memberId, where);
        }

        return associatedWithCollectionIfAny!=null
                ? ActionInteraction.startWithMultiselect(getBookmarkedOwner(), memberId, where,
                        associatedWithCollectionIfAny.getDataTableModel())
                : ActionInteraction.start(getBookmarkedOwner(), memberId, where);
    }

    public final ActionInteraction actionInteraction() {
        return getObject();
    }

    public final ObjectAction getMetaModel() {
        return actionInteraction().getMetamodel().orElseThrow();
    }

    public Optional<ScalarPropertyModel> associatedWithProperty() {
        return Optional.ofNullable(associatedWithPropertyIfAny);
    }

    public Optional<ScalarParameterModel> associatedWithParameter() {
        return Optional.ofNullable(associatedWithParameterIfAny);
    }

    public Optional<EntityCollectionModel> associatedWithCollection() {
        return Optional.ofNullable(associatedWithCollectionIfAny);
    }

    // -- LAZY BINDING

    public Stream<ParameterUiModelWkt> streamParameterUiModels() {
        if(childModels==null) {
            final int paramCount = actionInteraction().getMetamodel().get().getParameterCount();
            final int tupleIndex = 0;
            this.childModels = IntStream.range(0, paramCount)
                    .mapToObj(paramIndex -> new ParameterUiModelWkt(this, paramIndex, tupleIndex))
                    .collect(Can.toCan());
        }
        return childModels.stream();
    }

    // -- PARAMETER NEGOTIATION WITH MEMOIZATION (TRANSIENT)

    private transient _Lazy<Optional<ParameterNegotiationModel>> parameterNegotiationModel;

    public final ParameterNegotiationModel parameterNegotiationModel() {
        _Assert.assertTrue(this.isAttached(), "model is not attached");
        return parameterNegotiationModel.get()
                .orElseThrow(()->_Exceptions.noSuchElement(memberId));
    }

    public void resetParametersToDefault() {
        parameterNegotiationModel.clear();
    }

    public InlinePromptContext getInlinePromptContext() {
        return associatedWithPropertyIfAny != null
                ? associatedWithPropertyIfAny.getInlinePromptContext()
                : null;
    }

}
