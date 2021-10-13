/* Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License. */
package org.apache.isis.viewer.wicket.ui.components.widgets.linkandlabel;

import java.io.Serializable;

import org.apache.isis.core.metamodel.spec.ManagedObjects;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.viewer.common.model.action.ActionUiMetaModel;
import org.apache.isis.viewer.wicket.model.links.LinkAndLabel;
import org.apache.isis.viewer.wicket.model.models.ActionModel;
import org.apache.isis.viewer.wicket.model.models.EntityCollectionModel;
import org.apache.isis.viewer.wicket.model.models.EntityModel;
import org.apache.isis.viewer.wicket.model.models.ScalarModel;

import lombok.val;

public abstract class LinkAndLabelFactoryAbstract
implements Serializable {

    private static final long serialVersionUID = 1L;

    protected final String linkId;
    protected final EntityModel targetEntityModel;
    protected final ScalarModel scalarModelForAssociationIfAny;
    protected final EntityCollectionModel collectionModelForAssociationIfAny;

    protected LinkAndLabelFactoryAbstract(
            final String linkId,
            final EntityModel targetEntityModel,
            final ScalarModel scalarModelForAssociationIfAny,
            final EntityCollectionModel collectionModelForAssociationIfAny) {

        this.linkId = linkId;
        this.targetEntityModel = targetEntityModel;
        this.scalarModelForAssociationIfAny = scalarModelForAssociationIfAny;
        this.collectionModelForAssociationIfAny = collectionModelForAssociationIfAny;
    }

    public LinkAndLabel newActionLink(final ObjectAction action) {

        val objectAdapter = this.targetEntityModel.getManagedObject();
        val isIdentifiable = ManagedObjects.isIdentifiable(objectAdapter);
        if (!isIdentifiable) {
            throw new IllegalArgumentException(String.format(
                    "Object '%s' is not identifiable (has no identifier).",
                    objectAdapter.titleString()));
        }

        return LinkAndLabel.of(
                this::newLinkComponent,
                this.targetEntityModel,
                action);
    }

    protected ActionModel actionModel(final ObjectAction action) {
        return ActionModel.of(
                this.targetEntityModel,
                action.getFeatureIdentifier(),
                collectionModelForAssociationIfAny);
    }

    protected ActionLink newLinkComponent(final ActionUiMetaModel model) {
        val action = model.getActionMemento()
                .getAction(()->this.targetEntityModel.getCommonContext().getSpecificationLoader());
        val actionModel = actionModel(action);
        return new ActionLinkFactory(linkId, actionModel, scalarModelForAssociationIfAny)
                .newLinkComponent();
    }

}
