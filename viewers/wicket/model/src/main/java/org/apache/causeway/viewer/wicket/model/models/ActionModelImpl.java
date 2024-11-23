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
package org.apache.causeway.viewer.wicket.model.models;

import java.util.Optional;
import java.util.stream.Stream;

import org.apache.wicket.model.IModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import org.apache.causeway.applib.annotation.BookmarkPolicy;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.core.metamodel.interactions.managed.ActionInteraction;
import org.apache.causeway.core.metamodel.interactions.managed.ActionInteractionHead;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;
import org.apache.causeway.viewer.wicket.model.models.interaction.act.ActionInteractionWkt;
import org.apache.causeway.viewer.wicket.model.models.interaction.act.UiParameterWkt;
import org.apache.causeway.viewer.wicket.model.util.PageParameterUtils;

/**
 * Represents an action (a member) of an entity.
 *
 * @implSpec
 * <pre>
 * ActionModel --chained-to--> EntityModel
 * ActionModel --bound-to--> ActionInteractionWkt (delegate)
 * </pre>
 */
record ActionModelImpl(
    UiObjectWkt parentEntityModel,
    ActionInteractionWkt delegate,
    ColumnActionModifier columnActionModifier)
implements IModel<ManagedObject>, ActionModel {

    // -- CONSTRUCTION

    ActionModelImpl(final UiObjectWkt parentEntityModel, final ActionInteractionWkt delegate) {
        this(parentEntityModel, delegate, ColumnActionModifier.NONE);
    }

    ActionModelImpl(final UiObjectWkt parentEntityModel, final ActionInteractionWkt delegate,
            final ColumnActionModifier columnActionModifier) {
        this.parentEntityModel = parentEntityModel;
        this.delegate = delegate;
        this.columnActionModifier = columnActionModifier;
    }

    // --

    @Override
    public ObjectAction getAction() {
        return delegate.getMetaModel();
    }

    @Override
    public ActionInteraction getActionInteraction() {
        return delegate.actionInteraction();
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
    public BookmarkPolicy getBookmarkPolicy() {
        return BookmarkPolicy.AS_ROOT;
    }

    @Override
    public UiObjectWkt getParentUiModel() {
        return parentEntityModel;
    }

    @Override
    public Can<ManagedObject> snapshotArgs() {
        return delegate.parameterNegotiationModel().getParamValues();
    }

    @Override
    public ManagedObject executeActionAndReturnResult() {
        var pendingArgs = delegate.parameterNegotiationModel();
        var result = delegate.actionInteraction().invokeWithRuleChecking(pendingArgs);
        return result;
    }

    @Override
    public void setObject(final ManagedObject object) {
        throw new UnsupportedOperationException("ActionModel is a chained model - don't mess with the chain");
    }

    /** Resets arguments to their fixed point default values
     * @see ActionInteractionHead#defaults(org.apache.causeway.core.metamodel.interactions.managed.ManagedAction)
     */
    @Override
    public void clearArguments() {
        delegate.resetParametersToDefault();
    }

    // --

    @Override
    public InlinePromptContext getInlinePromptContext() {
        return delegate.getInlinePromptContext();
    }

    @Override
    public Stream<UiParameterWkt> streamPendingParamUiModels() {
        return delegate.streamParameterUiModels();
    }

    @Override
    public Optional<ScalarParameterModel> getAssociatedParameter() {
        return delegate.associatedWithParameter();
    }

    // -- MODEL CHAINING

    @Override
    public void detach() {
        // Detach nested object
        parentEntityModel.detach();
    }

    @Override
    public ManagedObject getObject() {
        return parentEntityModel.getObject();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Model:classname=[");
        sb.append(getClass().getName()).append(']');
        sb.append(":nestedModel=[").append(parentEntityModel).append(']');
        return sb.toString();
    }

}
