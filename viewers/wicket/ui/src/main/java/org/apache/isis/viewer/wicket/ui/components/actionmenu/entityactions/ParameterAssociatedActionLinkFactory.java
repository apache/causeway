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
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.viewer.common.model.object.ObjectUiModel;
import org.apache.isis.viewer.wicket.model.links.LinkAndLabel;
import org.apache.isis.viewer.wicket.model.models.ActionModel;
import org.apache.isis.viewer.wicket.model.models.ActionModelForEntity;
import org.apache.isis.viewer.wicket.model.models.ScalarModel;
import org.apache.isis.viewer.wicket.model.models.ScalarParameterModel;
import org.apache.isis.viewer.wicket.ui.components.widgets.linkandlabel.LinkAndLabelFactoryAbstract;

public final class ParameterAssociatedActionLinkFactory
extends LinkAndLabelFactoryAbstract
implements ObjectUiModel {

    private static final long serialVersionUID = 1L;

    public ParameterAssociatedActionLinkFactory(
            final ScalarParameterModel paramModel) {
        super(AdditionalLinksPanel.ID_ADDITIONAL_LINK,
                paramModel, null);
    }

    @Override
    public ManagedObject getManagedObject() {
        final int paramIndex = scalarParameterModel().getParameterIndex();
        return scalarParameterModel().getParameterNegotiationModel().getParamValue(paramIndex);
    }

    @Override
    protected ActionModel actionModel(
            final ManagedAction managedAction,
            final ScalarModel scalarModelForAssociationIfAny) {
        return ActionModelForEntity.supportingParameter(scalarParameterModel());
    }

    @Override
    public LinkAndLabel newActionLink(final ObjectAction action) {
        return LinkAndLabel.of(
                this::newLinkComponent,
                this, // ObjectUiModel: the action's owner provider
                action);
    }

    // -- HELPER

    private ScalarParameterModel scalarParameterModel() {
        return (ScalarParameterModel)scalarModelForAssociationIfAny;
    }

}