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

import java.util.function.Predicate;

import org.apache.wicket.model.IModel;

import org.apache.causeway.applib.Identifier;
import org.apache.causeway.applib.annotation.ActionLayout.Position;
import org.apache.causeway.applib.annotation.PromptStyle;
import org.apache.causeway.applib.annotation.Where;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.core.metamodel.interactions.managed.ActionInteractionHead;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.object.ManagedObjects;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;
import org.apache.causeway.viewer.commons.model.action.HasManagedAction;
import org.apache.causeway.viewer.commons.model.action.UiActionForm;
import org.apache.causeway.viewer.wicket.model.models.interaction.act.ActionInteractionWkt;

public interface ActionModel
extends UiActionForm, FormExecutorContext, BookmarkableModel, IModel<ManagedObject> {

    // -- FACTORY METHODS

    public static ActionModel forEntity(
            final UiObjectWkt parentEntityModel,
            final Identifier actionIdentifier,
            final Where where,
            final ColumnActionModifier columnActionModifier,
            final ScalarPropertyModel associatedWithPropertyIfAny,
            final ScalarParameterModel associatedWithParameterIfAny,
            final EntityCollectionModel associatedWithCollectionIfAny) {
        var delegate = ActionInteractionWkt.forEntity(
                parentEntityModel,
                actionIdentifier,
                where,
                associatedWithPropertyIfAny,
                associatedWithParameterIfAny,
                associatedWithCollectionIfAny);
        return new ActionModelImpl(parentEntityModel, delegate, columnActionModifier);
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
            final EntityCollectionModelParented collectionModel) {
        return forEntity(
                        collectionModel.getEntityModel(),
                        action.getFeatureIdentifier(),
                        Where.OBJECT_FORMS,
                        ColumnActionModifier.NONE,
                        null, null, collectionModel);
    }

    public static ActionModel forPropertyOrParameter(
            final ObjectAction action,
            final ScalarModel scalarModel) {
        return scalarModel instanceof ScalarPropertyModel
                ? forProperty(action, (ScalarPropertyModel)scalarModel)
                : forParameter(action, (ScalarParameterModel)scalarModel);
    }

    public static ActionModel forProperty(
            final ObjectAction action,
            final ScalarPropertyModel propertyModel) {
        return forEntity(
                        propertyModel.getParentUiModel(),
                        action.getFeatureIdentifier(),
                        Where.OBJECT_FORMS,
                        ColumnActionModifier.NONE,
                        propertyModel, null, null);
    }

    public static ActionModel forParameter(
            final ObjectAction action,
            final ScalarParameterModel parameterModel) {
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

    /** Resets arguments to their fixed point default values
     * @see ActionInteractionHead#defaults(org.apache.causeway.core.metamodel.interactions.managed.ManagedAction)
     */
    void clearArguments();

    ManagedObject executeActionAndReturnResult();
    Can<ManagedObject> snapshotArgs();

    default boolean isVisible() {
        return getVisibilityConsent().isAllowed();
    }

    default boolean isEnabled() {
        return getUsabilityConsent().isAllowed();
    }

    /**
     * If underlying action, originates from an action-column, it has special page redirect semantics:
     * <ul>
     * <li>if action return is void or matches the originating table/collection's element-type, then just RELOAD page</li>
     * <li>otherwise open action result page in NEW browser tab</li>
     * </ul>
     * @since CAUSEWAY-3815
     */
    enum ColumnActionModifier {
        NONE,
        FORCE_STAY_ON_PAGE,
        FORCE_NEW_BROWSER_WINDOW;
        public boolean isNone() { return this == NONE; }
        public boolean isForceStayOnPage() { return this == FORCE_STAY_ON_PAGE; }
        public boolean isForceNewBrowserWindow() { return this == FORCE_NEW_BROWSER_WINDOW; }
    }

    /**
     * If underlying action, originates from an action-column, it has special page redirect semantics:
     * <ul>
     * <li>if action return is void or matches the originating table/collection's element-type, then just RELOAD page</li>
     * <li>otherwise open action result page in NEW browser tab</li>
     * </ul>
     * @since CAUSEWAY-3815
     */
    ColumnActionModifier getColumnActionModifier();

    @Override
    default PromptStyle getPromptStyle() {
        var promptStyle = getAction().getPromptStyle();
        return promptStyle;
    }

    public static Predicate<ActionModel> isPositionedAt(final Position panel) {
        return HasManagedAction.isPositionedAt(panel);
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
