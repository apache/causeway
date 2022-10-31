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
package org.apache.causeway.incubator.viewer.vaadin.ui.pages.main;

import java.util.function.Consumer;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.FlexLayout.FlexWrap;

import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.core.metamodel.interactions.managed.ManagedAction;
import org.apache.causeway.incubator.viewer.vaadin.model.util.Vaa;
import org.apache.causeway.viewer.commons.applib.services.branding.BrandingUiModel;
import org.apache.causeway.viewer.commons.applib.services.header.HeaderUiModel;
import org.apache.causeway.viewer.commons.applib.services.menu.MenuUiService;

import lombok.val;

//@Log4j2
final class MainView_createHeader {

    static Component createHeader(
            final MetaModelContext commonContext,
            final HeaderUiModel headerUiModel,
            final Consumer<ManagedAction> menuActionEventHandler,
            final Runnable onHomepageLinkClick) {

        val titleOrLogo = createTitleOrLogo(commonContext, headerUiModel.getBranding());
        Vaa.setOnClick(titleOrLogo, onHomepageLinkClick);

        val leftMenuBar = new MenuBar();
        val horizontalSpacer = new Div();
        //        horizontalSpacer.setWidthFull();
        val rightMenuBar = new MenuBar();

        leftMenuBar.setOpenOnHover(true);
        rightMenuBar.setOpenOnHover(true);

        // holds the top level left and right aligned menu parts
        // TODO does not honor small displays yet, overflow is just not visible
        val menuBarContainer = new FlexLayout(titleOrLogo, leftMenuBar, horizontalSpacer, rightMenuBar);
        menuBarContainer.setFlexWrap(FlexWrap.WRAP);
        menuBarContainer.setAlignSelf(Alignment.CENTER, leftMenuBar);
        menuBarContainer.setAlignSelf(Alignment.CENTER, rightMenuBar);

        // right align using css
        rightMenuBar.getStyle().set("margin-left", "auto");

        menuBarContainer.setWidthFull();

        val leftMenuBuilder = MenuBuilderVaa.of(commonContext, menuActionEventHandler, leftMenuBar);
        val rightMenuBuilder = MenuBuilderVaa.of(commonContext, menuActionEventHandler, rightMenuBar);

        val menuUiModelProvider = commonContext.lookupServiceElseFail(MenuUiService.class);

        headerUiModel.getPrimary().buildMenuItems(menuUiModelProvider, leftMenuBuilder);
        headerUiModel.getSecondary().buildMenuItems(menuUiModelProvider, rightMenuBuilder);
        headerUiModel.getTertiary().buildMenuItems(menuUiModelProvider, rightMenuBuilder);

        return menuBarContainer;

    }

    // -- HELPER


    private static Component createTitleOrLogo(
            final MetaModelContext commonContext,
            final BrandingUiModel brandingUiModel) {


        val brandingName = brandingUiModel.getName();
        val brandingLogo = brandingUiModel.getLogoHref();

        if(brandingLogo.isPresent()) {
            val webAppContextPath = commonContext.getWebAppContextPath();
            val logo = new Image(
                    webAppContextPath.prependContextPathIfLocal(brandingLogo.get()),
                    "brandingLogo");
            logo.setWidth("48px"); //TODO make this part of the UI model
            logo.setHeight("48px"); //TODO make this part of the UI model
            return logo;
        }
        return new Text(brandingName.orElse("App"));

    }


}
