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
import org.apache.wicket.Page;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import org.apache.isis.applib.annotation.DomainServiceLayout;
import org.apache.isis.applib.services.userprof.UserProfileService;
import org.apache.isis.viewer.wicket.model.common.PageParametersUtils;
import org.apache.isis.viewer.wicket.model.models.ServiceActionsModel;
import org.apache.isis.viewer.wicket.ui.ComponentType;
import org.apache.isis.viewer.wicket.ui.components.widgets.navbar.BrandLogo;
import org.apache.isis.viewer.wicket.ui.components.widgets.navbar.BrandName;
import org.apache.isis.viewer.wicket.ui.components.widgets.navbar.Placement;
import org.apache.isis.viewer.wicket.ui.pages.error.ErrorPage;
import org.apache.isis.viewer.wicket.ui.panels.PanelAbstract;
import org.apache.isis.viewer.wicket.ui.util.Components;

/**
 * A panel for the default page header
 */
public class HeaderPanel extends PanelAbstract<Model<String>> {

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

        addApplicationName();
        addUserName();
        addServiceActionMenuBars();
    }

    @Override
    protected void onConfigure() {
        super.onConfigure();

        PageParameters parameters = getPage().getPageParameters();
        setVisible(parameters.get(PageParametersUtils.ISIS_NO_HEADER_PARAMETER_NAME).isNull());
    }

    protected void addApplicationName() {
        Class<? extends Page> homePage = getApplication().getHomePage();
        final BookmarkablePageLink<Void> applicationNameLink = new BookmarkablePageLink<>("applicationName", homePage);

        final Placement placement = Placement.HEADER;
        final BrandLogo brandImage = new BrandLogo("brandLogo", placement);
        final BrandName brandName = new BrandName("brandText", Placement.HEADER);
        applicationNameLink.add(brandName, brandImage);

        add(applicationNameLink);
    }


    protected void addUserName() {
        final UserProfileService userProfileService = getUserProfileService();
        final Label userName = new Label(ID_USER_NAME, userProfileService.userProfileName());
        add(userName);
    }

    private UserProfileService getUserProfileService() {
        return new UserProfileService() {
            @Override
            public String userProfileName() {
                if(getPage() instanceof ErrorPage) {
                    return getAuthenticationSession().getUserName();
                }
                try {
                    final UserProfileService userProfileService = lookupService(UserProfileService.class);
                    return userProfileService.userProfileName();
                } catch (final Exception e) {
                    return getAuthenticationSession().getUserName();
                }
            }
        };
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

    private void addMenuBar(final MarkupContainer container, final String id, final DomainServiceLayout.MenuBar menuBar) {
        final ServiceActionsModel model = new ServiceActionsModel(menuBar);
        Component menuBarComponent = getComponentFactoryRegistry().createComponent(ComponentType.SERVICE_ACTIONS, id, model);
        menuBarComponent.add(AttributeAppender.append("class", menuBar.name().toLowerCase(Locale.ENGLISH)));
        container.add(menuBarComponent);

    }

}
