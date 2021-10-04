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

import java.util.stream.Stream;

import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.viewer.wicket.model.links.LinkAndLabel;
import org.apache.isis.viewer.wicket.model.models.EntityCollectionModel;
import org.apache.isis.viewer.wicket.model.models.EntityModel;
import org.apache.isis.viewer.wicket.model.models.ScalarModel;

import lombok.val;
import lombok.experimental.UtilityClass;

@UtilityClass
public final class LinkAndLabelUtil {

    public static Stream<LinkAndLabel> asActionLinks(
            final ScalarModel scalarModel,
            final Stream<ObjectAction> associatedActions) {

        final EntityModel parentEntityModel = scalarModel.getParentUiModel();
        return asActionLinksForAdditionalLinksPanel(parentEntityModel, associatedActions, scalarModel, null);
    }

    public static Stream<LinkAndLabel> asActionLink(
            final ScalarModel scalarModel,
            final ObjectAction inlineAction) {
        return asActionLinks(scalarModel, Stream.of(inlineAction));
    }

    /**
     * Converts an {@link org.apache.isis.viewer.wicket.model.models.EntityModel} and a (subset of its)
     * {@link ObjectAction}s into a
     * list of {@link org.apache.isis.viewer.wicket.model.links.LinkAndLabel}s intended to be passed
     * to the {@link AdditionalLinksPanel}.
     *
     * <p>
     *     The length of the list returned may smaller than the inbound actions; any null links
     *     (for invisible actions) will be discarded.
     * </p>
     */
    public static Stream<LinkAndLabel> asActionLinksForAdditionalLinksPanel(
            final EntityModel parentEntityModel,
            final Stream<ObjectAction> objectActions,
            final ScalarModel scalarModelIfAny,
            final EntityCollectionModel collectionModelForAssociationIfAny) {

        val actionLinkFactory = new EntityActionLinkFactory(
                AdditionalLinksPanel.ID_ADDITIONAL_LINK,
                parentEntityModel,
                scalarModelIfAny,
                collectionModelForAssociationIfAny);

        return objectActions
                .map(objectAction->actionLinkFactory.newActionLink(objectAction, /*named*/null));
    }

}
