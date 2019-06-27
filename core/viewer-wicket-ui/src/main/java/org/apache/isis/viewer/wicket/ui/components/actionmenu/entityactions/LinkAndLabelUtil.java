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

import java.util.Collections;
import java.util.List;

import org.apache.isis.commons.internal.base._NullSafe;
import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.metamodel.spec.feature.ObjectAction;
import org.apache.isis.viewer.wicket.model.links.LinkAndLabel;
import org.apache.isis.viewer.wicket.model.models.EntityModel;
import org.apache.isis.viewer.wicket.model.models.ScalarModel;
import org.apache.isis.viewer.wicket.model.models.ToggledMementosProvider;
import org.apache.isis.viewer.wicket.ui.components.widgets.linkandlabel.ActionLinkFactory;

public final class LinkAndLabelUtil {

    private LinkAndLabelUtil(){}

    public static List<LinkAndLabel> asActionLinks(
            final ScalarModel scalarModel,
            final List<ObjectAction> associatedActions) {

        final EntityModel parentEntityModel = scalarModel.getParentEntityModel();
        return asActionLinksForAdditionalLinksPanel(parentEntityModel, associatedActions, scalarModel);
    }

    public static LinkAndLabel asActionLink(final ScalarModel scalarModel, final ObjectAction inlineActionIfAny) {
        if(inlineActionIfAny == null) {
            return null;
        }
        return asActionLinks(scalarModel, Collections.singletonList(inlineActionIfAny)).get(0);
    }

    /**
     * Converts an {@link org.apache.isis.viewer.wicket.model.models.EntityModel} and a (subset of its) {@link org.apache.isis.metamodel.spec.feature.ObjectAction}s into a
     * list of {@link org.apache.isis.viewer.wicket.model.links.LinkAndLabel}s intended to be passed
     * to the {@link AdditionalLinksPanel}.
     *
     * <p>
     *     The length of the list returned may smaller than the inbound actions; any null links
     *     (for invisible actions) will be discarded.
     * </p>
     */
    public static List<LinkAndLabel> asActionLinksForAdditionalLinksPanel(
            final EntityModel parentEntityModel,
            final List<ObjectAction> objectActions,
            final ScalarModel scalarModelIfAny) {

        return asActionLinksForAdditionalLinksPanel(parentEntityModel, objectActions, scalarModelIfAny, null);
    }

    public static List<LinkAndLabel> asActionLinksForAdditionalLinksPanel(
            final EntityModel parentEntityModel,
            final List<ObjectAction> objectActions,
            final ScalarModel scalarModelIfAny,
            final ToggledMementosProvider toggledMementosProviderIfAny) {

        final ActionLinkFactory linkFactory = new EntityActionLinkFactory(parentEntityModel, scalarModelIfAny);

        return _Lists.transform(objectActions, stream -> stream
                .map((ObjectAction objectAction) ->
                        linkFactory.newLink(
                                objectAction, AdditionalLinksPanel.ID_ADDITIONAL_LINK, toggledMementosProviderIfAny))
                .filter(_NullSafe::isPresent));
    }

}
