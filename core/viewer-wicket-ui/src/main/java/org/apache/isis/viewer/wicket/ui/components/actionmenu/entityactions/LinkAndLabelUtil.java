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

import com.google.common.base.Function;
import com.google.common.base.Predicates;
import com.google.common.collect.FluentIterable;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.concurrency.ConcurrencyChecking;
import org.apache.isis.core.metamodel.deployment.DeploymentCategory;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.isis.viewer.wicket.model.links.LinkAndLabel;
import org.apache.isis.viewer.wicket.model.models.EntityModel;
import org.apache.isis.viewer.wicket.model.models.ScalarModel;
import org.apache.isis.viewer.wicket.model.models.ToggledMementosProvider;
import org.apache.isis.viewer.wicket.ui.components.widgets.linkandlabel.ActionLinkFactory;

public final class LinkAndLabelUtil {

    private LinkAndLabelUtil(){}

    public static List<LinkAndLabel> asActionLinksForAssociation(
            final ScalarModel scalarModelForAssociation,
            final DeploymentCategory deploymentCategory) {

        if (scalarModelForAssociation.getKind() != ScalarModel.Kind.PROPERTY) {
            return Collections.emptyList();
        }

        final EntityModel parentEntityModel = scalarModelForAssociation.getParentEntityModel();

        final ObjectAdapter parentAdapter = parentEntityModel.load(ConcurrencyChecking.NO_CHECK);

        final OneToOneAssociation oneToOneAssociation =
                scalarModelForAssociation.getPropertyMemento().getProperty(scalarModelForAssociation.getSpecificationLoader());

        final List<ObjectAction> associatedActions =
                ObjectAction.Util.findForAssociation(parentAdapter, oneToOneAssociation, deploymentCategory);

        return asActionLinksForAdditionalLinksPanel(parentEntityModel, associatedActions,
                scalarModelForAssociation);
    }

    /**
     * Converts an {@link org.apache.isis.viewer.wicket.model.models.EntityModel} and a (subset of its) {@link org.apache.isis.core.metamodel.spec.feature.ObjectAction}s into a
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
            final ScalarModel scalarModelForAssociationIfAny) {

        return asActionLinksForAdditionalLinksPanel(parentEntityModel, objectActions, scalarModelForAssociationIfAny, null);
    }

    public static List<LinkAndLabel> asActionLinksForAdditionalLinksPanel(
            final EntityModel parentEntityModel,
            final List<ObjectAction> objectActions,
            final ScalarModel scalarModelForAssociationIfAny,
            final ToggledMementosProvider toggledMementosProviderIfAny) {

        final ActionLinkFactory linkFactory = new EntityActionLinkFactory(parentEntityModel, scalarModelForAssociationIfAny);

        return FluentIterable.from(objectActions)
                .transform(new Function<ObjectAction, LinkAndLabel>() {

                    @Override
                    public LinkAndLabel apply(ObjectAction objectAction) {
                        return linkFactory.newLink(objectAction, AdditionalLinksPanel.ID_ADDITIONAL_LINK,toggledMementosProviderIfAny);
                    }
                })
                .filter(Predicates.<LinkAndLabel>notNull())
                .toList();
    }

}
