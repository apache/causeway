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

import org.apache.isis.core.metamodel.interactions.managed.ManagedAction;
import org.apache.isis.core.metamodel.spec.ManagedObjects;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.viewer.wicket.model.links.LinkAndLabel;
import org.apache.isis.viewer.wicket.model.models.ActionModel;
import org.apache.isis.viewer.wicket.model.models.EntityCollectionModel;
import org.apache.isis.viewer.wicket.model.models.EntityModel;
import org.apache.isis.viewer.wicket.model.models.ScalarModel;
import org.apache.isis.viewer.wicket.ui.components.widgets.linkandlabel.LinkAndLabelFactoryAbstract;
import org.apache.isis.viewer.wicket.ui.pages.PageAbstract;

import lombok.val;

public final class EntityActionLinkFactory
extends LinkAndLabelFactoryAbstract {

    private static final long serialVersionUID = 1L;

    private final EntityModel ownerEntityModel;

    // -- FACTORIES

    public static EntityActionLinkFactory entity(
            final EntityModel ownerEntityModel,
            final ScalarModel scalarModelForAssociationIfAny,
            final EntityCollectionModel collectionModelForAssociationIfAny) {
        return new EntityActionLinkFactory(
                AdditionalLinksPanel.ID_ADDITIONAL_LINK,
                ownerEntityModel, scalarModelForAssociationIfAny, collectionModelForAssociationIfAny);
    }

    public static EntityActionLinkFactory menu(
            final EntityModel serviceModel) {
        return new EntityActionLinkFactory(PageAbstract.ID_MENU_LINK, serviceModel, null, null);
    }

    // -- CONSTRUCTION

    private EntityActionLinkFactory(
            final String linkId,
            final EntityModel ownerEntityModel,
            final ScalarModel scalarModelForAssociationIfAny,
            final EntityCollectionModel collectionModelForAssociationIfAny) {
        super(linkId,
                scalarModelForAssociationIfAny, collectionModelForAssociationIfAny);
        this.ownerEntityModel = ownerEntityModel;
        guardAgainstNotBookmarkable();
    }

    @Override
    public LinkAndLabel newActionLink(final ObjectAction action) {
        return LinkAndLabel.of(
                this::newLinkComponent,
                this.ownerEntityModel,
                action);
    }

    @Override
    protected ActionModel actionModel(final ManagedAction managedAction) {
        return ActionModel.ofEntity(
                this.ownerEntityModel,
                managedAction.getAction().getFeatureIdentifier(),
                collectionModelForAssociationIfAny);
    }

    // -- HELPER

    private void guardAgainstNotBookmarkable() {
        val objectAdapter = this.ownerEntityModel.getManagedObject();
        val isIdentifiable = ManagedObjects.isIdentifiable(objectAdapter);
        if (!isIdentifiable) {
            throw new IllegalArgumentException(String.format(
                    "Object '%s' is not identifiable (has no identifier).",
                    objectAdapter.titleString()));
        }
    }

}