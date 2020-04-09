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
package org.apache.isis.viewer.wicket.ui.components.header;

import java.util.Locale;

import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import org.apache.isis.applib.annotation.DomainServiceLayout;
import org.apache.isis.viewer.common.model.branding.BrandingUiModel;
import org.apache.isis.viewer.common.model.userprofile.UserProfileUiModel;
import org.apache.isis.viewer.wicket.model.common.PageParametersUtils;
import org.apache.isis.viewer.wicket.model.models.ServiceActionsModel;
import org.apache.isis.viewer.wicket.ui.ComponentType;
import org.apache.isis.viewer.wicket.ui.components.widgets.navbar.BrandLogo;
import org.apache.isis.viewer.wicket.ui.components.widgets.navbar.BrandName;
import org.apache.isis.viewer.wicket.ui.pages.error.ErrorPage;
import org.apache.isis.viewer.wicket.ui.panels.PanelAbstract;
import org.apache.isis.viewer.wicket.ui.util.Components;

import lombok.val;

/**
 * A panel for the default page header
 */
public class HeaderPanel extends PanelAbstract<Model<String>> {

    private static final long serialVersionUID = 1L;
    
    private static final String ID_USER_NAME = "userName";
    private static final String ID_PRIMARY_MENU_BAR = "primaryMenuBar";
    private static final String ID_SECONDARY_MENU_BAR = "secondaryMenuBar";
    private static final String ID_TERTIARY_MENU_BAR = "tertiaryMenuBar";

    /**
     * Constructor.
     *
     * @param id The component id
     */
    public HeaderPanel(String id) {
        super(id);
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();

        val headerUiModel = super.getHeaderModel();
        
        addApplicationName(headerUiModel.getBranding());
        addUserName(headerUiModel.getUserProfile());
        addServiceActionMenuBars();
    }

    @Override
    protected void onConfigure() {
        super.onConfigure();

        PageParameters parameters = getPage().getPageParameters();
        setVisible(parameters.get(PageParametersUtils.ISIS_NO_HEADER_PARAMETER_NAME).isNull());
    }

    protected void addApplicationName(BrandingUiModel branding) {
        val homePage = getApplication().getHomePage();
        val applicationNameLink = new BookmarkablePageLink<Void>("applicationName", homePage);
        applicationNameLink.add(
                new BrandName("brandText", branding),
                new BrandLogo("brandLogo", branding));

        add(applicationNameLink);
    }

    protected void addUserName(UserProfileUiModel userProfile) {
        val userName = new Label(ID_USER_NAME, userProfile.getUserProfileName());
        add(userName);
    }

    protected void addServiceActionMenuBars() {
        if (getPage() instanceof ErrorPage) {
            Components.permanentlyHide(this, ID_PRIMARY_MENU_BAR);
            Components.permanentlyHide(this, ID_SECONDARY_MENU_BAR);
            addMenuBar(this, ID_TERTIARY_MENU_BAR, DomainServiceLayout.MenuBar.TERTIARY);
        } else {
            addMenuBar(this, ID_PRIMARY_MENU_BAR, DomainServiceLayout.MenuBar.PRIMARY);
            addMenuBar(this, ID_SECONDARY_MENU_BAR, DomainServiceLayout.MenuBar.SECONDARY);
            addMenuBar(this, ID_TERTIARY_MENU_BAR, DomainServiceLayout.MenuBar.TERTIARY);
        }
    }

    private void addMenuBar(
            final MarkupContainer container, 
            final String id, 
            final DomainServiceLayout.MenuBar menuBar) {
        
        final ServiceActionsModel model = new ServiceActionsModel(super.getCommonContext(), menuBar);
        Component menuBarComponent = getComponentFactoryRegistry().createComponent(ComponentType.SERVICE_ACTIONS, id, model);
        menuBarComponent.add(AttributeAppender.append("class", menuBar.name().toLowerCase(Locale.ENGLISH)));
        container.add(menuBarComponent);
    }
    


}
