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
package org.apache.isis.viewer.wicket.ui.components.actionmenu.entityactions;

import java.util.function.Function;

import org.apache.wicket.markup.html.link.AbstractLink;

import org.apache.isis.applib.annotations.Where;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ManagedObjects;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.viewer.wicket.model.links.ActionLinkUiComponentFactoryWkt;
import org.apache.isis.viewer.wicket.model.links.LinkAndLabel;
import org.apache.isis.viewer.wicket.model.models.ActionModel;
import org.apache.isis.viewer.wicket.model.models.ActionModelImpl;
import org.apache.isis.viewer.wicket.model.models.EntityCollectionModelParented;
import org.apache.isis.viewer.wicket.model.models.EntityModel;
import org.apache.isis.viewer.wicket.model.models.ScalarModel;
import org.apache.isis.viewer.wicket.model.models.ScalarParameterModel;
import org.apache.isis.viewer.wicket.model.models.ScalarPropertyModel;
import org.apache.isis.viewer.wicket.ui.components.widgets.linkandlabel.ActionLink;
import org.apache.isis.viewer.wicket.ui.pages.PageAbstract;

import lombok.NonNull;
import lombok.val;

@FunctionalInterface
public interface LinkAndLabelFactory
extends Function<ObjectAction, LinkAndLabel> {

    public static LinkAndLabelFactory forMenu(
            final EntityModel serviceModel) {

        val linkFactory = new MenuLinkFactory();

        return action -> LinkAndLabel.of(
                ActionModelImpl.forEntity(
                        serviceModel,
                        action.getFeatureIdentifier(),
                        Where.ANYWHERE,
                        null, null, null),
                linkFactory);
    }

    public static LinkAndLabelFactory forEntity(
            final EntityModel parentEntityModel) {

        guardAgainstNotBookmarkable(parentEntityModel.getBookmarkedOwner());
        val linkFactory = new AdditionalLinkFactory();

        return action -> LinkAndLabel.of(
                ActionModelImpl.forEntity(
                        parentEntityModel,
                        action.getFeatureIdentifier(),
                        Where.OBJECT_FORMS,
                        null, null, null),
                linkFactory);
    }

    public static LinkAndLabelFactory forCollection(
            final EntityCollectionModelParented collectionModel) {

        val linkFactory = new AdditionalLinkFactory();

        return action -> LinkAndLabel.of(
                ActionModelImpl.forEntity(
                        collectionModel.getEntityModel(),
                        action.getFeatureIdentifier(),
                        Where.OBJECT_FORMS,
                        null, null, collectionModel),
                linkFactory);
    }

    public static LinkAndLabelFactory forPropertyOrParameter(
            final ScalarModel scalarModel) {
        return scalarModel instanceof ScalarPropertyModel
                ? forProperty((ScalarPropertyModel)scalarModel)
                : forParameter((ScalarParameterModel)scalarModel);
    }

    public static LinkAndLabelFactory forProperty(
            final ScalarPropertyModel propertyModel) {

        val linkFactory = new AdditionalLinkFactory();

        return action -> LinkAndLabel.of(
                ActionModelImpl.forEntity(
                        propertyModel.getParentUiModel(),
                        action.getFeatureIdentifier(),
                        Where.OBJECT_FORMS,
                        propertyModel, null, null),
                linkFactory);
    }

    public static LinkAndLabelFactory forParameter(
            final ScalarParameterModel parameterModel) {

        // only supported, when parameter type is scalar and also is a value-type
        if(parameterModel.getMetaModel().isScalar()
                && parameterModel.getMetaModel().getElementType().isValue()) {

            val linkFactory = new AdditionalLinkFactory();

            return action -> LinkAndLabel.of(
                    ActionModelImpl.forEntity(
                            parameterModel.getParentUiModel(),
                            action.getFeatureIdentifier(),
                            Where.OBJECT_FORMS,
                            null, parameterModel, null),
                    linkFactory);
        }

        return action -> null;
    }

    // -- HELPER

    private static void guardAgainstNotBookmarkable(final ManagedObject objectAdapter) {
        val isIdentifiable = ManagedObjects.isIdentifiable(objectAdapter);
        if (!isIdentifiable) {
            throw new IllegalArgumentException(String.format(
                    "Object '%s' is not identifiable (has no identifier).",
                    objectAdapter.titleString()));
        }
    }

    static class MenuLinkFactory implements ActionLinkUiComponentFactoryWkt {
        private static final long serialVersionUID = 1L;
        @Override
        public AbstractLink newActionLinkUiComponent(@NonNull final ActionModel actionModel) {
            return ActionLink.create(PageAbstract.ID_MENU_LINK, actionModel);
        }
    }

    static class AdditionalLinkFactory implements ActionLinkUiComponentFactoryWkt {
        private static final long serialVersionUID = 1L;
        @Override
        public AbstractLink newActionLinkUiComponent(@NonNull final ActionModel actionModel) {
            return ActionLink.create(AdditionalLinksPanel.ID_ADDITIONAL_LINK, actionModel);
        }
    }

}
