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
package org.apache.isis.viewer.wicket.model.models;

import java.util.Optional;
import java.util.stream.Stream;

import org.apache.wicket.model.ChainingModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import org.apache.isis.applib.Identifier;
import org.apache.isis.applib.annotation.Where;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.core.metamodel.interactions.managed.ActionInteraction;
import org.apache.isis.core.metamodel.interactions.managed.ActionInteractionHead;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.runtime.context.IsisAppCommonContext;
import org.apache.isis.viewer.wicket.model.models.interaction.act.ActionInteractionWkt;
import org.apache.isis.viewer.wicket.model.models.interaction.act.ParameterUiModelWkt;
import org.apache.isis.viewer.wicket.model.util.PageParameterUtils;

import lombok.val;

/**
 * Represents an action (a member) of an entity.
 *
 * @implSpec
 * <pre>
 * ActionModel --chained-to--> EntityModel
 * ActionModel --bound-to--> ActionInteractionWkt (delegate)
 * </pre>
 */
public final class ActionModelImpl
extends ChainingModel<ManagedObject>
implements ActionModel {

    private static final long serialVersionUID = 1L;

    // -- FACTORY METHODS

    public static ActionModelImpl forEntity(
            final EntityModel parentEntityModel,
            final Identifier actionIdentifier,
            final Where where,
            final ScalarPropertyModel associatedWithPropertyIfAny,
            final ScalarParameterModel associatedWithParameterIfAny,
            final EntityCollectionModel associatedWithCollectionIfAny) {
        val delegate = ActionInteractionWkt.forEntity(
                parentEntityModel,
                actionIdentifier,
                where,
                associatedWithPropertyIfAny,
                associatedWithParameterIfAny,
                associatedWithCollectionIfAny);
        return new ActionModelImpl(parentEntityModel, delegate);
    }

    // -- CONSTRUCTION

    private final ActionInteractionWkt delegate;

    private ActionModelImpl(final EntityModel parentEntityModel, final ActionInteractionWkt delegate) {
        super(parentEntityModel);
        this.delegate = delegate;
    }

    // --

    @Override
    public ActionInteraction getActionInteraction() {
        return delegate.actionInteraction();
    }

    @Override
    public IsisAppCommonContext getCommonContext() {
        return delegate.getCommonContext();
    }

    // -- BOOKMARKABLE

    @Override
    public PageParameters getPageParametersWithoutUiHints() {
        return PageParameterUtils
                .createPageParametersForAction(getParentObject(), getAction(), snapshotArgs());
    }

    @Override
    public PageParameters getPageParameters() {
        return getPageParametersWithoutUiHints();
    }

    // --

    @Override
    public boolean hasAsRootPolicy() {
        return true;
    }

    @Override
    public EntityModel getParentUiModel() {
        return (EntityModel) super.getTarget();
    }

    @Override
    public Can<ManagedObject> snapshotArgs() {
        return delegate.parameterNegotiationModel().getParamValues();
    }

    @Override
    public ManagedObject executeActionAndReturnResult() {
        val pendingArgs = delegate.parameterNegotiationModel();
        val result = delegate.actionInteraction().invokeWithRuleChecking(pendingArgs);
        return result;
    }

    @Override
    public void setObject(final ManagedObject object) {
        throw new UnsupportedOperationException("ActionModel is a chained model - don't mess with the chain");
    }

    /** Resets arguments to their fixed point default values
     * @see ActionInteractionHead#defaults(org.apache.isis.core.metamodel.interactions.managed.ManagedAction)
     */
    @Override
    public void clearArguments() {
        delegate.resetParametersToDefault();
    }

    //////////////////////////////////////////////////

    @Override
    public InlinePromptContext getInlinePromptContext() {
        return delegate.getInlinePromptContext();
    }

    @Override
    public Stream<ParameterUiModelWkt> streamPendingParamUiModels() {
        return delegate.streamParameterUiModels();
    }

    @Override
    public Optional<ScalarParameterModel> getAssociatedParameter() {
        return delegate.associatedWithParameter();
    }

}
