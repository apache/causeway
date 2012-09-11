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
package org.apache.isis.viewer.wicket.ui.selector;

import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import org.apache.isis.viewer.wicket.ui.ComponentFactory;

/**
 * {@link ComponentFactory} for rendering a selection of
 * {@link ComponentFactory}s.
 * 
 * <p>
 * Used by {@link SelectorPanelAbstract}.
 */
public class DropDownChoiceComponentFactory extends DropDownChoice<ComponentFactory> {

    private static final long serialVersionUID = 1L;

    private final IModel<?> underlyingModel;
    private final String underlyingId;
    private final MarkupContainer container;

    private static final class ComponentFactoryChoiceRenderer implements IChoiceRenderer<ComponentFactory> {
        private static final long serialVersionUID = 1L;

        @Override
        public Object getDisplayValue(final ComponentFactory object) {
            return object.getName();
        }

        @Override
        public String getIdValue(final ComponentFactory object, final int index) {
            return Integer.toString(index);
        }
    }

    /**
     * @param id
     *            - id to use for the drop down
     * @param selectedComponentFactoryModel
     *            - currently selected in the drop-down
     * @param componentFactories
     *            - list of {@link ComponentFactory}s to show in drop-down
     * @param container
     *            - the container that should contain the {@link Component}
     *            created by the selected {@link ComponentFactory}
     * @param underlyingId
     *            - the id of the {@link Component} created
     * @param underlyingModel
     *            - the model for the {@link Component}
     */
    public DropDownChoiceComponentFactory(final String id, final Model<ComponentFactory> selectedComponentFactoryModel, final List<? extends ComponentFactory> componentFactories, final MarkupContainer container, final String underlyingId, final IModel<?> underlyingModel) {
        super(id, selectedComponentFactoryModel, componentFactories, new ComponentFactoryChoiceRenderer());
        this.underlyingId = underlyingId;
        this.underlyingModel = underlyingModel;
        this.container = container;
    }

    @Override
    protected boolean wantOnSelectionChangedNotifications() {
        return true;
    }

    @Override
    protected void onSelectionChanged(final ComponentFactory newSelection) {
        final ComponentFactory componentFactory = getModel().getObject();
        if (componentFactory != null) {
            container.addOrReplace(componentFactory.createComponent(underlyingId, underlyingModel));
        }
    }
}