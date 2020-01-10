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

package org.apache.isis.viewer.wicket.ui.components.actionmenu.serviceactions;

import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.model.IModel;

import org.apache.isis.applib.annotation.DomainServiceLayout;
import org.apache.isis.applib.layout.menubars.MenuBars;
import org.apache.isis.viewer.wicket.model.models.ServiceActionsModel;
import org.apache.isis.viewer.wicket.ui.ComponentFactory;
import org.apache.isis.viewer.wicket.ui.ComponentFactoryAbstract;
import org.apache.isis.viewer.wicket.ui.ComponentType;

/**
 * {@link ComponentFactory} for a {@link ServiceActionsPanel} to represent the
 * {@link org.apache.isis.viewer.wicket.model.models.ServiceActionsModel application action}s.
 */
public class ServiceActionsPanelFactory extends ComponentFactoryAbstract {

    private final static long serialVersionUID = 1L;
    
    public ServiceActionsPanelFactory() {
        super(ComponentType.SERVICE_ACTIONS, ServiceActionsPanel.class);
    }

    /**
     * Applies to primary and secondary service action models.
     */
    @Override
    protected ApplicationAdvice appliesTo(final IModel<?> model) {
        if(!(model instanceof ServiceActionsModel)) {
            return ApplicationAdvice.DOES_NOT_APPLY;
        }
        final ServiceActionsModel serviceActionsModel = (ServiceActionsModel) model;
        final DomainServiceLayout.MenuBar menuBar = serviceActionsModel.getMenuBar();
        return appliesIf(menuBar != DomainServiceLayout.MenuBar.TERTIARY && menuBar != null);
    }

    @Override
    public Component createComponent(final String id, final IModel<?> model) {
        final ServiceActionsModel serviceActionsModel = (ServiceActionsModel) model;

        final MenuBars menuBars = super.getCommonContext().getMenuBarsService().menuBars();

        final List<CssMenuItem> menuItems = ServiceActionUtil.buildMenu(
                super.getCommonContext(), menuBars, serviceActionsModel);

        return new ServiceActionsPanel(id, menuItems);
    }


}
