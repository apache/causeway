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
package org.apache.causeway.viewer.wicket.ui.components.header;

import org.apache.wicket.MarkupContainer;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import org.apache.causeway.viewer.commons.applib.services.branding.BrandingUiModel;
import org.apache.causeway.viewer.commons.applib.services.header.HeaderUiModel;
import org.apache.causeway.viewer.commons.applib.services.menu.MenuUiModel;
import org.apache.causeway.viewer.commons.applib.services.userprof.UserProfileUiModel;
import org.apache.causeway.viewer.commons.model.components.UiComponentType;
import org.apache.causeway.viewer.wicket.model.models.ServiceActionsModel;
import org.apache.causeway.viewer.wicket.model.util.PageParameterUtils;
import org.apache.causeway.viewer.wicket.ui.components.widgets.navbar.AvatarImage;
import org.apache.causeway.viewer.wicket.ui.components.widgets.navbar.BrandLogo;
import org.apache.causeway.viewer.wicket.ui.components.widgets.navbar.BrandName;
import org.apache.causeway.viewer.wicket.ui.pages.error.ErrorPage;
import org.apache.causeway.viewer.wicket.ui.panels.PanelAbstract;
import org.apache.causeway.viewer.wicket.ui.util.Wkt;
import org.apache.causeway.viewer.wicket.ui.util.WktComponents;

import lombok.val;

/**
 * A panel for the default page header
 */
public class HeaderPanel
extends PanelAbstract<String, Model<String>> {

    private static final long serialVersionUID = 1L;

    private static final String ID_USER_NAME = "userName";
    private static final String ID_USER_ICON = "userIcon";
    private static final String ID_USER_AVATAR = "userAvatar";
    private static final String ID_PRIMARY_MENU_BAR = "primaryMenuBar";
    private static final String ID_SECONDARY_MENU_BAR = "secondaryMenuBar";
    private static final String ID_TERTIARY_MENU_BAR = "tertiaryMenuBar";

    /**
     * Constructor.
     *
     * @param id The component id
     */
    public HeaderPanel(final String id) {
        super(id);
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();

        val headerUiModel = super.getHeaderModel();

        addApplicationName(headerUiModel.getBranding());
        addUserName(headerUiModel.getUserProfile());
        addServiceActionMenuBars(headerUiModel);
    }

    @Override
    protected void onConfigure() {
        super.onConfigure();

        PageParameters parameters = getPage().getPageParameters();
        setVisible(parameters.get(PageParameterUtils.CAUSEWAY_NO_HEADER_PARAMETER_NAME).isNull());
    }

    protected void addApplicationName(final BrandingUiModel branding) {
        val homePage = getApplication().getHomePage();
        val applicationNameLink = new BookmarkablePageLink<Void>("applicationName", homePage);
        applicationNameLink.add(
                new BrandName("brandText", branding),
                new BrandLogo("brandLogo", branding));

        add(applicationNameLink);
    }

    protected void addUserName(final UserProfileUiModel userProfile) {
        add(new MarkupContainer(ID_USER_ICON){
            private static final long serialVersionUID = 1L;

            @Override
            protected void onConfigure() {
                super.onConfigure();
                setVisible(! userProfile.avatarUrl().isPresent());
            }
        });
        add(new AvatarImage(ID_USER_AVATAR, userProfile));
        Wkt.labelAdd(this, ID_USER_NAME, userProfile.getUserProfileName());
    }

    protected void addServiceActionMenuBars(final HeaderUiModel headerUiModel) {
        if (getPage() instanceof ErrorPage) {
            WktComponents.permanentlyHide(this, ID_PRIMARY_MENU_BAR);
            WktComponents.permanentlyHide(this, ID_SECONDARY_MENU_BAR);
            addMenuBar(ID_TERTIARY_MENU_BAR, headerUiModel.getTertiary());
        } else {
            addMenuBar(ID_PRIMARY_MENU_BAR, headerUiModel.getPrimary());
            addMenuBar(ID_SECONDARY_MENU_BAR, headerUiModel.getSecondary());
            addMenuBar(ID_TERTIARY_MENU_BAR, headerUiModel.getTertiary());
        }
    }

    private void addMenuBar(
            final String id,
            final MenuUiModel menuUiModel) {

        final MarkupContainer container = this;
        val menuModel = new ServiceActionsModel(super.getMetaModelContext(), menuUiModel);
        val menuBarComponent = getComponentFactoryRegistry()
                .createComponent(id, UiComponentType.SERVICE_ACTIONS, menuModel);
        Wkt.cssAppend(menuBarComponent, menuUiModel.getCssClass());
        container.add(menuBarComponent);
    }

}
