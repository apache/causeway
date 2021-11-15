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
package org.apache.isis.viewer.wicket.ui.components.property;

import org.apache.wicket.markup.html.WebMarkupContainer;

import org.apache.isis.core.metamodel.commons.ScalarRepresentation;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.viewer.common.model.components.ComponentType;
import org.apache.isis.viewer.wicket.model.models.EntityModel;
import org.apache.isis.viewer.wicket.model.models.ScalarModel;
import org.apache.isis.viewer.wicket.model.models.ScalarPropertyModel;
import org.apache.isis.viewer.wicket.ui.components.actions.ActionParametersPanel;
import org.apache.isis.viewer.wicket.ui.panels.PanelAbstract;
import org.apache.isis.viewer.wicket.ui.util.Wkt;

/**
 * Corresponding component to prompt for action (parameters) is {@link ActionParametersPanel}.
 */
public class PropertyEditPanel
extends PanelAbstract<ManagedObject, ScalarPropertyModel> {

    private static final long serialVersionUID = 1L;

    private static final String ID_HEADER = "header";

    private static final String ID_PROPERTY_NAME = "propertyName";

    /**
     * Gives a chance to hide the header part of this panel.
     */
    private boolean showHeader = true;

    public PropertyEditPanel(
            final String id,
            final ScalarPropertyModel scalarModel) {

        super(id, scalarModel.copyHaving(
                ScalarRepresentation.EDITING,
                EntityModel.RenderingHint.REGULAR));

        buildGui(scalarModel);
    }

    @Override
    protected void onConfigure() {
        super.onConfigure();

        buildGui(getScalarModel());
    }

    private void buildGui(final ScalarModel scalarModel) {
        buildGuiForParameters(scalarModel);
    }

    ScalarPropertyModel getScalarModel() {
        return super.getModel();
    }

    public PropertyEditPanel setShowHeader(final boolean showHeader) {
        this.showHeader = showHeader;
        return this;
    }

    private void buildGuiForParameters(final ScalarModel scalarModel) {

        WebMarkupContainer header = addHeader();

        getComponentFactoryRegistry().addOrReplaceComponent(this, ComponentType.PROPERTY_EDIT_FORM, getScalarModel());
        getComponentFactoryRegistry().addOrReplaceComponent(header, ComponentType.ENTITY_ICON_AND_TITLE, scalarModel.getParentUiModel());

        Wkt.labelAdd(header, ID_PROPERTY_NAME, getScalarModel()::getFriendlyName)
            .setEscapeModelStrings(true);
    }

    private WebMarkupContainer addHeader() {
        WebMarkupContainer header = new WebMarkupContainer(ID_HEADER) {
            private static final long serialVersionUID = 1L;

            @Override
            protected void onConfigure() {
                super.onConfigure();

                setVisible(showHeader);
            }
        };
        addOrReplace(header);
        return header;
    }


}
