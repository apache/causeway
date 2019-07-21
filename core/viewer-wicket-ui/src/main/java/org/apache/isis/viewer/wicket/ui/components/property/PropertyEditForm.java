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

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.IModel;

import org.apache.isis.applib.annotation.PromptStyle;
import org.apache.isis.viewer.wicket.model.hints.IsisPropertyEditCompletedEvent;
import org.apache.isis.viewer.wicket.model.isis.WicketViewerSettings;
import org.apache.isis.viewer.wicket.model.models.ScalarModel;
import org.apache.isis.viewer.wicket.ui.ComponentType;
import org.apache.isis.viewer.wicket.ui.components.scalars.ScalarPanelAbstract2;
import org.apache.isis.viewer.wicket.ui.panels.FormExecutorStrategy;
import org.apache.isis.viewer.wicket.ui.panels.PromptFormAbstract;

class PropertyEditForm extends PromptFormAbstract<ScalarModel> {

    private static final long serialVersionUID = 1L;



    public PropertyEditForm(
            final String id,
            final Component parentPanel,
            final WicketViewerSettings settings,
            final ScalarModel propertyModel) {
        super(id, parentPanel, settings, propertyModel);
    }

    private ScalarModel getScalarModel() {
        return (ScalarModel) super.getModel();
    }



    @Override
    protected void addParameters() {
        final ScalarModel scalarModel = getScalarModel();

        final WebMarkupContainer container = new WebMarkupContainer(PropertyEditFormPanel.ID_PROPERTY);
        add(container);

        newParamPanel(container, scalarModel);
    }

    private ScalarPanelAbstract2 newParamPanel(final WebMarkupContainer container, final IModel<?> model) {

        final Component component = getComponentFactoryRegistry()
                .addOrReplaceComponent(container, ComponentType.SCALAR_NAME_AND_VALUE, model);
        final ScalarPanelAbstract2 paramPanel =
                component instanceof ScalarPanelAbstract2
                ? (ScalarPanelAbstract2) component
                        : null;
                if (paramPanel != null) {
                    paramPanel.setOutputMarkupId(true);
                    paramPanel.notifyOnChange(this);
                }
                return paramPanel;
    }

    @Override
    protected Object newCompletedEvent(final AjaxRequestTarget target, final Form<?> form) {
        return new IsisPropertyEditCompletedEvent(getScalarModel(), target, form);
    }

    @Override
    public void onUpdate(
            final AjaxRequestTarget target, final ScalarPanelAbstract2 scalarPanel) {

    }

    // REVIEW: this overload may not be necessary, recall that the important call needed is getScalarModel().reset(),
    // which is called in the superclass.
    @Override
    public void onCancelSubmitted(
            final AjaxRequestTarget target) {

        final PromptStyle promptStyle = getScalarModel().getPromptStyle();

        if (promptStyle.isInlineOrInlineAsIfEdit()) {

            getScalarModel().toViewMode();
            getScalarModel().clearPending();
        }

        super.onCancelSubmitted(target);
    }

    @Override
    protected FormExecutorStrategy<ScalarModel> getFormExecutorStrategy() {
        ScalarModel scalarModel = getScalarModel();
        return new PropertyFormExecutorStrategy(scalarModel);
    }
}
