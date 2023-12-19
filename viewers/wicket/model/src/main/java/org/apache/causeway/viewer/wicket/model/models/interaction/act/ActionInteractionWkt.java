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

import java.util.Objects;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.apache.wicket.model.ChainingModel;
import org.springframework.lang.Nullable;

import org.apache.causeway.applib.Identifier;
import org.apache.causeway.applib.annotation.Where;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.internal.assertions._Assert;
import org.apache.causeway.commons.internal.exceptions._Exceptions;
import org.apache.causeway.core.metamodel.interactions.managed.ActionInteraction;
import org.apache.causeway.core.metamodel.interactions.managed.ParameterNegotiationModel;
import org.apache.causeway.core.metamodel.object.ManagedObjects;
import org.apache.causeway.core.metamodel.object.MmEntityUtils;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;
import org.apache.causeway.core.metamodel.spec.feature.memento.ActionMemento;
import org.apache.causeway.viewer.wicket.model.models.EntityCollectionModel;
import org.apache.causeway.viewer.wicket.model.models.InlinePromptContext;
import org.apache.causeway.viewer.wicket.model.models.ScalarParameterModel;
import org.apache.causeway.viewer.wicket.model.models.ScalarPropertyModel;
import org.apache.causeway.viewer.wicket.model.models.UiObjectWkt;
import org.apache.causeway.viewer.wicket.model.models.interaction.BookmarkedObjectWkt;
import org.apache.causeway.viewer.wicket.model.models.interaction.HasBookmarkedOwnerAbstract;

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

    /**
     * memoize, so if we only need the meta-model,
     * we don't have to re-attach the entire model (ActionInteraction)
     * <p>
     * nullable in support of lazy evaluation
     */
    private @Nullable ActionMemento actionMemento;

    private Can<UiParameterWkt> childModels;
    private @Nullable ScalarPropertyModel associatedWithPropertyIfAny;
    private @Nullable ScalarParameterModel associatedWithParameterIfAny;
    private @Nullable EntityCollectionModel associatedWithCollectionIfAny;

    public static ActionInteractionWkt forEntity(
            final UiObjectWkt parentEntityModel,
            final Identifier actionIdentifier,
            final Where where,
            final ScalarPropertyModel associatedWithPropertyIfAny,
            final ScalarParameterModel associatedWithParameterIfAny,
            final EntityCollectionModel associatedWithCollectionIfAny) {

        val onwerSpec = parentEntityModel.getBookmarkedOwner().getSpecification();
        val objectAction = onwerSpec.getAction(actionIdentifier.getMemberLogicalName());

        return new ActionInteractionWkt(
                parentEntityModel.bookmarkedObjectModel(),
                actionIdentifier.getMemberLogicalName(),
                where,
                objectAction,
                associatedWithPropertyIfAny,
                associatedWithParameterIfAny,
                associatedWithCollectionIfAny);
    }

    private ActionInteractionWkt(
            final BookmarkedObjectWkt bookmarkedObject,
            final String memberId,
            final Where where,
            /**
             *[CAUSEWAY-3648] Optional in support of the composite value type's 'Xxx_default' mixin,
             * which cannot be found via the parentEntityModel's ObjectSpecification,
             * a strategy used by the caller above.
             * <p>
             * If {@code Optional.empty()},
             * then we simply evaluate the {@link ObjectAction} later via {@link #getMetaModel()}.
             */
            final Optional<ObjectAction> objectAction,
            final ScalarPropertyModel associatedWithPropertyIfAny,
            final ScalarParameterModel associatedWithParameterIfAny,
            final EntityCollectionModel associatedWithCollectionIfAny) {
        super(bookmarkedObject);
        this.memberId = memberId;
        this.where = where;
        this.actionMemento = objectAction
                    .map(ObjectAction::getMemento) // if present, eagerly memoize
                    .orElse(null);
        this.associatedWithPropertyIfAny = associatedWithPropertyIfAny;
        this.associatedWithParameterIfAny = associatedWithParameterIfAny;
        this.associatedWithCollectionIfAny = associatedWithCollectionIfAny;
    }

    @Override
    protected ActionInteraction load() {

        if(associatedWithParameterIfAny!=null) {
            final int paramIndex = associatedWithParameterIfAny.getParameterIndex();
            // supports composite-value-types via mixin
            return ActionInteraction.startAsBoundToParameter(
                    associatedWithParameterIfAny.getParameterNegotiationModel(), paramIndex, memberId, where);
        }

        if(associatedWithCollectionIfAny!=null) {
            return ActionInteraction.startWithMultiselect(getBookmarkedOwner(), memberId, where,
                    associatedWithCollectionIfAny.getDataTableModel());
        }

        if(associatedWithPropertyIfAny!=null) {
            // supports composite-value-types via mixin
            return ActionInteraction.startAsBoundToProperty(
                    associatedWithPropertyIfAny.getManagedProperty(), memberId, where);
        }

        return ActionInteraction.start(getBookmarkedOwner(), memberId, where);
    }

    public final ActionInteraction actionInteraction() {
        return getObject();
    }

    public final ObjectAction getMetaModel() {

        //[CAUSEWAY-3648] In support of the composite value type's 'Xxx_default' mixin.
        if(actionMemento==null) {
            val objectAction = actionInteraction().getMetamodel()
                .orElseThrow(()->_Exceptions
                        .noSuchElement("could not resolve action by memberId '%s'", memberId));
            this.actionMemento = objectAction.getMemento();
            return objectAction;
        }

        // re-attachment fails, if the owner is not found (eg. deleted entity),
        // hence we return the directly memoized meta-model of the underlying action
        return Objects.requireNonNull(actionMemento.getAction(this::getSpecificationLoader),
                ()->"framework bug: lost objectAction on model recycling (serialization issue)");
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

    public Stream<UiParameterWkt> streamParameterUiModels() {
        if(childModels==null) {
            final int paramCount = getMetaModel().getParameterCount();
            this.childModels = IntStream.range(0, paramCount)
                    .mapToObj(paramIndex -> new UiParameterWkt(this, paramIndex))
                    .collect(Can.toCan());
        }
        return childModels.stream();
    }

    // -- PARAMETER NEGOTIATION

    public final ParameterNegotiationModel parameterNegotiationModel() {
        guardAgainstNotAttached();

        // [CAUSEWAY-3662] enforce parameter args to not reference any hollow entities
        //TODO[CAUSEWAY-3662] property negotiation might need the same kind of guard
        if(parameterNegotiationModel!=null) {
            ManagedObjects.stream(parameterNegotiationModel.getParamValues())
            .forEach(domainObj->{
                // domainObj might be an entity that is hollow
                MmEntityUtils.requiresAttached(domainObj);
                // domainObj might be a viewmodel that holds hollow entities
                // assuming, viewmodel has its bookmark memoized
                ManagedObjects.refreshViewmodel(domainObj, /*bookmark supplier*/ null);
            });
            return parameterNegotiationModel;
        }

        return startParameterNegotiationModel();
    }

    public void resetParametersToDefault() {
        // in effect invalidates the currently memoized parameterNegotiationModel (if any)
        this.parameterNegotiationModel = null;
    }

    public InlinePromptContext getInlinePromptContext() {
        return associatedWithPropertyIfAny != null
                ? associatedWithPropertyIfAny.getInlinePromptContext()
                : associatedWithParameterIfAny!=null
                    ? associatedWithParameterIfAny.getInlinePromptContext()
                    : null;
    }

    // -- HELPER

    /**
     * memoized transiently
     */
    private transient ParameterNegotiationModel parameterNegotiationModel;
    /**
     * Start and transiently memoize a new {@link ParameterNegotiationModel}.
     */
    private ParameterNegotiationModel startParameterNegotiationModel() {
        return this.parameterNegotiationModel = actionInteraction().startParameterNegotiation()
                .orElseThrow(()->_Exceptions.noSuchElement(memberId));
    }
    /**
     * [CAUSEWAY-3649] safe guard against access to the model while it is not attached
     */
    private void guardAgainstNotAttached() {
        if(!this.isAttached()) {
            // start over
            this.parameterNegotiationModel = null;
            getObject();
        }
        _Assert.assertTrue(this.isAttached(), ()->"model is not attached");
    }

}
