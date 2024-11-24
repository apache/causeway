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
package org.apache.causeway.viewer.wicket.ui.components.property;

import org.apache.wicket.markup.html.WebMarkupContainer;

import org.apache.causeway.core.metamodel.commons.ViewOrEditMode;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.viewer.commons.model.components.UiComponentType;
import org.apache.causeway.viewer.commons.model.hints.RenderingHint;
import org.apache.causeway.viewer.wicket.model.models.PropertyModel;
import org.apache.causeway.viewer.wicket.ui.panels.PanelAbstract;
import org.apache.causeway.viewer.wicket.ui.util.Wkt;

/**
 * Corresponding component to prompt for action (parameters) is
 * {@link org.apache.causeway.viewer.wicket.ui.components.actions.ActionParametersPanel}.
 */
class PropertyEditPanel
extends PanelAbstract<ManagedObject, PropertyModel> {

    private static final long serialVersionUID = 1L;

    private static final String ID_HEADER = "header";
    private static final String ID_PROPERTY_NAME = "propertyName";

    /**
     * Gives a chance to hide the header part of this panel.
     */
    private boolean showHeader = true;

    public PropertyEditPanel(
            final String id,
            final PropertyModel propertyModel) {

        super(id, propertyModel.copyHaving(
                ViewOrEditMode.EDITING,
                RenderingHint.REGULAR));

        buildGui(propertyModel);
    }

    @Override
    protected void onConfigure() {
        super.onConfigure();

        buildGui(propertyModel());
    }

    PropertyModel propertyModel() {
        return super.getModel();
    }

    public PropertyEditPanel setShowHeader(final boolean showHeader) {
        this.showHeader = showHeader;
        return this;
    }

    private void buildGui(final PropertyModel propertyModel) {

        WebMarkupContainer header = addHeader();

        getComponentFactoryRegistry().addOrReplaceComponent(this, UiComponentType.PROPERTY_EDIT_FORM, propertyModel());
        getComponentFactoryRegistry().addOrReplaceComponent(header, UiComponentType.ENTITY_ICON_AND_TITLE, propertyModel.getParentUiModel());

        Wkt.labelAdd(header, ID_PROPERTY_NAME, propertyModel()::getFriendlyName)
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
