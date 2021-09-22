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

import org.apache.isis.core.metamodel.spec.ManagedObjects;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.viewer.wicket.model.links.LinkAndLabel;
import org.apache.isis.viewer.wicket.model.models.EntityModel;
import org.apache.isis.viewer.wicket.model.models.ScalarModel;
import org.apache.isis.viewer.wicket.model.models.ToggledMementosProvider;
import org.apache.isis.viewer.wicket.model.util.CommonContextUtils;
import org.apache.isis.viewer.wicket.ui.components.widgets.linkandlabel.LinkAndLabelFactoryAbstract;

import lombok.val;

public final class EntityActionLinkFactory extends LinkAndLabelFactoryAbstract {

    private static final long serialVersionUID = 1L;

    public EntityActionLinkFactory(
            final String linkId,
            final EntityModel entityModel,
            final ScalarModel scalarModelForAssociationIfAny,
            final ToggledMementosProvider toggledMementosProviderIfAny) {
        super(linkId, entityModel, scalarModelForAssociationIfAny, toggledMementosProviderIfAny);
    }

    @Override
    public LinkAndLabel newActionLink(
            final ObjectAction objectAction,
            final String named) {

        val objectAdapter = this.targetEntityModel.getManagedObject();

        val isIdentifiable = ManagedObjects.isIdentifiable(objectAdapter);
        if (!isIdentifiable) {
            throw new IllegalArgumentException(String.format(
                    "Object '%s' is not identifiable (has no identifier).",
                    objectAdapter.titleString()));
        }

        // previously we computed visibility and usability here, but
        // this is now done at the point of rendering

        return LinkAndLabel.of(
                model->super.newLinkComponent(
                        model.getObjectAction(CommonContextUtils.getCommonContext()::getSpecificationLoader),
                        toggledMementosProviderIfAny),
                named,
                this.targetEntityModel,
                objectAction);
    }


}