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

import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.viewer.wicket.model.links.LinkAndLabel;
import org.apache.isis.viewer.wicket.model.models.EntityCollectionModelParented;
import org.apache.isis.viewer.wicket.model.models.EntityModel;
import org.apache.isis.viewer.wicket.model.models.ScalarModel;
import org.apache.isis.viewer.wicket.model.models.ScalarParameterModel;
import org.apache.isis.viewer.wicket.model.models.ScalarPropertyModel;

import lombok.val;
import lombok.experimental.UtilityClass;

@UtilityClass
public final class LinkAndLabelUtil {

    public Function<ObjectAction, LinkAndLabel> forEntity(
            final EntityModel parentEntityModel) {

        val actionLinkFactory = new EntityActionLinkFactory(
                AdditionalLinksPanel.ID_ADDITIONAL_LINK,
                parentEntityModel,
                null/*scalarModelIfAny*/,
                null/*collectionModelForAssociationIfAny*/);

        return objectAction->actionLinkFactory.newActionLink(objectAction);
    }

    public Function<ObjectAction, LinkAndLabel> forCollection(
            final EntityCollectionModelParented collectionModel) {

        val actionLinkFactory = new EntityActionLinkFactory(
                AdditionalLinksPanel.ID_ADDITIONAL_LINK,
                collectionModel.getEntityModel(),
                null/*scalarModelIfAny*/,
                collectionModel);

        return objectAction->actionLinkFactory.newActionLink(objectAction);
    }

    public Function<ObjectAction, LinkAndLabel> forPropertyOrParameter(
            final ScalarModel scalarModel) {
        return scalarModel instanceof ScalarPropertyModel
                ? forProperty((ScalarPropertyModel)scalarModel)
                : forParameter((ScalarParameterModel)scalarModel);

    }

    public Function<ObjectAction, LinkAndLabel> forProperty(
            final ScalarPropertyModel scalarModel) {

        val actionLinkFactory = new EntityActionLinkFactory(
                AdditionalLinksPanel.ID_ADDITIONAL_LINK,
                scalarModel.getParentUiModel(),
                scalarModel,
                null/*collectionModelForAssociationIfAny*/);

        return objectAction->actionLinkFactory.newActionLink(objectAction);
    }

    public Function<ObjectAction, LinkAndLabel> forParameter(
            final ScalarParameterModel scalarModel) {

        val actionLinkFactory = new ParameterAssociatedActionLinkFactory(
                AdditionalLinksPanel.ID_ADDITIONAL_LINK,
                scalarModel.getParentUiModel(),
                scalarModel);

        return objectAction->actionLinkFactory.newActionLink(objectAction);
    }

}
