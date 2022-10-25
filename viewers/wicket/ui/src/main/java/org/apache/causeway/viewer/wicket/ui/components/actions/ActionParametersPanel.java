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
package org.apache.causeway.viewer.wicket.ui.components.actions;

import org.apache.wicket.markup.html.WebMarkupContainer;

import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.viewer.commons.model.components.UiComponentType;
import org.apache.causeway.viewer.wicket.model.models.ActionModel;
import org.apache.causeway.viewer.wicket.ui.components.property.PropertyEditPanel;
import org.apache.causeway.viewer.wicket.ui.panels.PanelAbstract;
import org.apache.causeway.viewer.wicket.ui.util.Wkt;

import lombok.Getter;
import lombok.Setter;

/**
 * {@link PanelAbstract Panel} representing an action invocation, backed by an
 * {@link ActionModel}.
 * <p>
 * Will render either parameter dialog or the results.
 * <p>
 * Corresponding component to edit properties is {@link PropertyEditPanel}.
 */
public class ActionParametersPanel
extends PanelAbstract<ManagedObject, ActionModel> {

    private static final long serialVersionUID = 1L;

    private static final String ID_HEADER = "header";
    private static final String ID_ACTION_NAME = "actionName";

    public ActionParametersPanel(final String id, final ActionModel actionModel) {
        super(id, actionModel);
    }

    ActionModel getActionModel() {
        return super.getModel();
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();

        final ActionModel actionModel = getModel();

        if (!actionModel.hasParameters()) {
            // the factory should check for this already, so this should never occur...
            throw new IllegalStateException("model has no parameters!");
        }

        final WebMarkupContainer header =
                Wkt.containerWithVisibility(ID_HEADER, ActionParametersPanel.this::isShowHeader);

        addOrReplace(header);

        getComponentFactoryRegistry().addOrReplaceComponent(this, UiComponentType.PARAMETERS, actionModel);
        getComponentFactoryRegistry().addOrReplaceComponent(header, UiComponentType.ENTITY_ICON_AND_TITLE,
                actionModel.getParentUiModel());

        Wkt.labelAdd(header, ID_ACTION_NAME, ()->getActionModel().getFriendlyName());
    }

    /**
     * Gives a chance to hide the header part of this action panel,
     * e.g. when shown in an action prompt
     */
    @Setter @Getter
    private boolean showHeader = true;

}
