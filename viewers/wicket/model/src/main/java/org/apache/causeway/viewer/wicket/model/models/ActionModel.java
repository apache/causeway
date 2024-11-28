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
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.apache.wicket.model.IModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import org.apache.causeway.applib.Identifier;
import org.apache.causeway.applib.annotation.ActionLayout.Position;
import org.apache.causeway.applib.annotation.BookmarkPolicy;
import org.apache.causeway.applib.annotation.PromptStyle;
import org.apache.causeway.applib.annotation.Where;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.core.metamodel.interactions.managed.ActionInteraction;
import org.apache.causeway.core.metamodel.interactions.managed.ActionInteractionHead;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.object.ManagedObjects;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;
import org.apache.causeway.viewer.commons.model.action.HasManagedAction;
import org.apache.causeway.viewer.commons.model.action.UiActionForm;
import org.apache.causeway.viewer.wicket.model.models.coll.CollectionModel;
import org.apache.causeway.viewer.wicket.model.models.coll.CollectionModelParented;
import org.apache.causeway.viewer.wicket.model.models.interaction.act.ActionInteractionWkt;
import org.apache.causeway.viewer.wicket.model.models.interaction.act.UiParameterWkt;
import org.apache.causeway.viewer.wicket.model.util.PageParameterUtils;

public record ActionModel(
    UiObjectWkt parentEntityModel,
    ActionInteractionWkt delegate,
    /**
     * If underlying action, originates from an action-column, it has special page redirect semantics:
     * <ul>
     * <li>if action return is void or matches the originating table/collection's element-type, then just RELOAD page</li>
     * <li>otherwise open action result page in NEW browser tab</li>
     * </ul>
     * @since CAUSEWAY-3815
     */
    ColumnActionModifier columnActionModifier)
implements UiActionForm, FormExecutorContext, BookmarkableModel, IModel<ManagedObject> {

    /**
     * If underlying action, originates from an action-column, it has special page redirect semantics:
     * <ul>
     * <li>if action return is void or matches the originating table/collection's element-type, then just RELOAD page</li>
     * <li>otherwise open action result page in NEW browser tab</li>
     * </ul>
     * @since CAUSEWAY-3815
     */
    public enum ColumnActionModifier {
        /**
         * don't interfere with the default action result route
         */
        NONE,
        /**
         * reload current page, irrespective of the action result
         */
        FORCE_STAY_ON_PAGE,
        /**
         * open the action result in a new (blank) browser tab or window
         */
        FORCE_NEW_BROWSER_WINDOW;
        public boolean isNone() { return this == NONE; }
        public boolean isForceStayOnPage() { return this == FORCE_STAY_ON_PAGE; }
        public boolean isForceNewBrowserWindow() { return this == FORCE_NEW_BROWSER_WINDOW; }
    }

    // -- FACTORY METHODS

    public static ActionModel forEntity(
            final UiObjectWkt parentEntityModel,
            final Identifier actionIdentifier,
            final Where where,
            final ColumnActionModifier columnActionModifier,
            final PropertyModel associatedWithPropertyIfAny,
            final ParameterModel associatedWithParameterIfAny,
            final CollectionModel associatedWithCollectionIfAny) {
        var delegate = ActionInteractionWkt.forEntity(
                parentEntityModel,
                actionIdentifier,
                where,
                associatedWithPropertyIfAny,
                associatedWithParameterIfAny,
                associatedWithCollectionIfAny);
        return new ActionModel(parentEntityModel, delegate, columnActionModifier);
    }

    public static ActionModel forServiceAction(
            final ObjectAction action,
            final UiObjectWkt serviceModel) {
        return forEntity(
                        serviceModel,
                        action.getFeatureIdentifier(),
                        Where.ANYWHERE,
                        ColumnActionModifier.NONE,
                        null, null, null);
    }

    public static ActionModel forEntity(
            final ObjectAction action,
            final UiObjectWkt parentEntityModel) {
        guardAgainstNotBookmarkable(parentEntityModel.getBookmarkedOwner());
        return ActionModel.forEntity(
                        parentEntityModel,
                        action.getFeatureIdentifier(),
                        Where.OBJECT_FORMS,
                        ColumnActionModifier.NONE,
                        null, null, null);
    }

    public static ActionModel forEntityFromActionColumn(
            final ObjectAction action,
            final UiObjectWkt parentEntityModel,
            final ColumnActionModifier columnActionModifier) {
        return ActionModel.forEntity(
                        parentEntityModel,
                        action.getFeatureIdentifier(),
                        Where.OBJECT_FORMS,
                        columnActionModifier,
                        null, null, null);
    }

    public static ActionModel forCollection(
            final ObjectAction action,
            final CollectionModelParented collectionModel) {
        return forEntity(
                        collectionModel.getObjectModel(),
                        action.getFeatureIdentifier(),
                        Where.OBJECT_FORMS,
                        ColumnActionModifier.NONE,
                        null, null, collectionModel);
    }

    public static ActionModel forPropertyOrParameter(
            final ObjectAction action,
            final UiAttributeWkt attributeModel) {
        return attributeModel instanceof PropertyModel
                ? forProperty(action, (PropertyModel)attributeModel)
                : forParameter(action, (ParameterModel)attributeModel);
    }

    public static ActionModel forProperty(
            final ObjectAction action,
            final PropertyModel propertyModel) {
        return forEntity(
                        propertyModel.getParentUiModel(),
                        action.getFeatureIdentifier(),
                        Where.OBJECT_FORMS,
                        ColumnActionModifier.NONE,
                        propertyModel, null, null);
    }

    public static ActionModel forParameter(
            final ObjectAction action,
            final ParameterModel parameterModel) {
        //XXX[CAUSEWAY-3080] only supported, when parameter type is a singular composite value-type
        var param = parameterModel.getMetaModel();
        if(param.isSingular()
                && param.getElementType().isCompositeValue()) {
            return ActionModel.forEntity(
                            parameterModel.getParentUiModel(),
                            action.getFeatureIdentifier(),
                            Where.OBJECT_FORMS,
                            ColumnActionModifier.NONE,
                            null, parameterModel, null);
        }
        return null;
    }

    // -- CONSTRUCTION

    ActionModel(final UiObjectWkt parentEntityModel, final ActionInteractionWkt delegate) {
        this(parentEntityModel, delegate, ColumnActionModifier.NONE);
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

    public Can<ManagedObject> snapshotArgs() {
        return delegate.parameterNegotiationModel().getParamValues();
    }

    public ManagedObject executeActionAndReturnResult() {
        var pendingArgs = delegate.parameterNegotiationModel();
        var result = delegate.actionInteraction().invokeWithRuleChecking(pendingArgs);
        return result;
    }

    /** Resets arguments to their fixed point default values
     * @see ActionInteractionHead#defaults(org.apache.causeway.core.metamodel.interactions.managed.ManagedAction)
     */
    public void clearArguments() {
        delegate.resetParametersToDefault();
    }

    @Override
    public InlinePromptContext getInlinePromptContext() {
        return delegate.getInlinePromptContext();
    }

    @Override
    public Stream<UiParameterWkt> streamPendingParamUiModels() {
        return delegate.streamParameterUiModels();
    }

    @Override
    public Optional<ParameterModel> getAssociatedParameter() {
        return delegate.associatedWithParameter();
    }

    public boolean isVisible() {
        return getVisibilityConsent().isAllowed();
    }

    public boolean isEnabled() {
        return getUsabilityConsent().isAllowed();
    }

    @Override
    public PromptStyle getPromptStyle() {
        var promptStyle = getAction().getPromptStyle();
        return promptStyle;
    }

    public static Predicate<ActionModel> isPositionedAt(final Position panel) {
        return HasManagedAction.isPositionedAt(panel);
    }

    // -- MODEL CHAINING

    @Override
    public void setObject(final ManagedObject object) {
        throw new UnsupportedOperationException("ActionModel is a chained model - don't mess with the chain");
    }

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

    // -- HELPER

    private static void guardAgainstNotBookmarkable(final ManagedObject objectAdapter) {
        var isIdentifiable = ManagedObjects.isIdentifiable(objectAdapter);
        if (!isIdentifiable) {
            throw new IllegalArgumentException(String.format(
                    "Object '%s' is not identifiable (has no identifier).",
                    objectAdapter.getTitle()));
        }
    }

}
