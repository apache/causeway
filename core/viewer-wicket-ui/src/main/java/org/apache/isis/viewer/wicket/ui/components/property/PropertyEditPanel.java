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
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.Model;

import org.apache.isis.metamodel.adapter.ObjectAdapter;
import org.apache.isis.metamodel.adapter.version.ConcurrencyException;
import org.apache.isis.metamodel.facets.all.named.NamedFacet;
import org.apache.isis.metamodel.spec.feature.OneToOneAssociation;
import org.apache.isis.security.authentication.MessageBroker;
import org.apache.isis.viewer.wicket.model.models.EntityModel;
import org.apache.isis.viewer.wicket.model.models.ScalarModel;
import org.apache.isis.viewer.wicket.ui.ComponentType;
import org.apache.isis.viewer.wicket.ui.components.actions.ActionParametersPanel;
import org.apache.isis.viewer.wicket.ui.pages.entity.EntityPage;
import org.apache.isis.viewer.wicket.ui.panels.PanelAbstract;

/**
 * Corresponding component to prompt for action (parameters) is {@link ActionParametersPanel}.
 */
public class PropertyEditPanel extends PanelAbstract<ScalarModel> {

    private static final long serialVersionUID = 1L;

    private static final String ID_HEADER = "header";

    private static final String ID_PROPERTY_NAME = "propertyName";

    /**
     * Gives a chance to hide the header part of this panel.
     */
    private boolean showHeader = true;

    public PropertyEditPanel(
            final String id,
            final ScalarModel scalarModel) {
        super(id, new ScalarModel(scalarModel.getParentEntityModel(), scalarModel.getPropertyMemento(),
                EntityModel.Mode.EDIT, EntityModel.RenderingHint.REGULAR));

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

    ScalarModel getScalarModel() {
        return super.getModel();
    }

    public PropertyEditPanel setShowHeader(boolean showHeader) {
        this.showHeader = showHeader;
        return this;
    }

    private void buildGuiForParameters(final ScalarModel scalarModel) {

        WebMarkupContainer header = addHeader();

        try {
            getComponentFactoryRegistry().addOrReplaceComponent(this, ComponentType.PROPERTY_EDIT_FORM, getScalarModel());
            getComponentFactoryRegistry().addOrReplaceComponent(header, ComponentType.ENTITY_ICON_AND_TITLE, scalarModel.getParentEntityModel());

            final OneToOneAssociation property = getScalarModel().getPropertyMemento().getProperty(scalarModel.getSpecificationLoader());
            final String propertyName = property.getName();
            final Label label = new Label(ID_PROPERTY_NAME, Model.of(propertyName));

            NamedFacet namedFacet = property.getFacet(NamedFacet.class);
            if (namedFacet != null) {
                label.setEscapeModelStrings(namedFacet.escaped());
            }

            header.add(label);

        } catch (final ConcurrencyException ex) {

            // should succeed, because the Oid would have
            // been updated in the attempt
            ObjectAdapter targetAdapter = scalarModel.getParentEntityModel().load();

            // page redirect/handling
            final EntityPage entityPage = new EntityPage(targetAdapter, null);
            setResponsePage(entityPage);

            getMessageBroker().addWarning(ex.getMessage());
        }
    }

    private WebMarkupContainer addHeader() {
        WebMarkupContainer header = new WebMarkupContainer(ID_HEADER) {
            @Override
            protected void onConfigure() {
                super.onConfigure();

                setVisible(showHeader);
            }
        };
        addOrReplace(header);
        return header;
    }



    ///////////////////////////////////////////////////////
    // Dependencies (from context)
    ///////////////////////////////////////////////////////

    protected MessageBroker getMessageBroker() {
        return getAuthenticationSession().getMessageBroker();
    }

}
