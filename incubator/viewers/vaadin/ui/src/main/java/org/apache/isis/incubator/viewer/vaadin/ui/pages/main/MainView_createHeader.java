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
package org.apache.isis.incubator.viewer.vaadin.ui.pages.main;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.FlexLayout;

import org.apache.isis.core.webapp.context.IsisAppCommonContext;
import org.apache.isis.incubator.viewer.vaadin.model.action.ActionLinkFactoryVaa;
import org.apache.isis.incubator.viewer.vaadin.model.action.ActionLinkVaa;
import org.apache.isis.incubator.viewer.vaadin.model.decorator.Decorators;
import org.apache.isis.incubator.viewer.vaadin.model.menu.MenuItemVaa;
import org.apache.isis.viewer.common.model.branding.BrandingUiModel;
import org.apache.isis.viewer.common.model.header.HeaderUiModel;
import org.apache.isis.viewer.common.model.menu.MenuUiModel;

import lombok.val;

//@Log4j2
final class MainView_createHeader {

    static Component createHeader(
            final IsisAppCommonContext commonContext, 
            final HeaderUiModel headerUiModel,
            final Consumer<ActionLinkVaa> subMenuEventHandler) {
        
        val titleOrLogo = createTitleOrLogo(commonContext, headerUiModel.getBranding());
        val leftMenuBar = new MenuBar();
        val horizontalSpacer = new Div();
//        horizontalSpacer.setWidthFull();
        val rightMenuBar = new MenuBar();
        
        leftMenuBar.setOpenOnHover(true);
        rightMenuBar.setOpenOnHover(true);
        
        // holds the top level left and right aligned menu parts
        // TODO does not honor small displays yet, overflow is just not visible
        val menuBarContainer = new FlexLayout(titleOrLogo, leftMenuBar, horizontalSpacer, rightMenuBar);
        menuBarContainer.setWrapMode(FlexLayout.WrapMode.WRAP);
        menuBarContainer.setAlignSelf(Alignment.CENTER, leftMenuBar);
        menuBarContainer.setAlignSelf(Alignment.CENTER, rightMenuBar);

        // right align using css
        rightMenuBar.getStyle().set("margin-left", "auto");
        
        menuBarContainer.setWidthFull();
        
        // menu section handler, that creates and adds sub-menus to their parent top level menu   
        final BiConsumer<MenuBar, MenuItemVaa> menuSectionBuilder = (menuBar, menuSectionUiModel) -> {
            val menuItem = menuSectionUiModel.isTertiaryRoot() 
                    ? menuBar.addItem(Decorators.getUser()
                            .decorateWithAvatar(new Label(), commonContext))
                    : menuBar.addItem(Decorators.getMenu()
                            .decorateTopLevel(new Label(menuSectionUiModel.getName())));
            val subMenu = menuItem.getSubMenu();
            menuSectionUiModel.getSubMenuItems().forEach(menuItemModel -> {
                val menuActionModel = (ActionLinkVaa)menuItemModel.getMenuActionUiModel();
                
                if(menuItemModel.isFirstInSection() 
                        && subMenu.getItems().size()>0) {
                    val spacer = new Hr();
                    //spacer.addClassName("spacer"); TODO vertical margin or padding is currently a bit too large 
                    subMenu.addItem(spacer);
                }
                
                subMenu.addItem(
                        (Component)menuActionModel.getUiComponent(), 
                        e->subMenuEventHandler.accept(menuActionModel));
            });
                    
        };
        
        // top level left aligned ...
        buildMenuModel(commonContext, headerUiModel.getPrimary(), newMenuItem->
            menuSectionBuilder.accept(leftMenuBar, newMenuItem));
        
        // top level right aligned ...
        buildMenuModel(commonContext, headerUiModel.getSecondary(), newMenuItem->
            menuSectionBuilder.accept(rightMenuBar, newMenuItem));
        
        // tertiary menu items get collected under a top level menu labeled with the current user's name 
        buildMenuModel(commonContext, headerUiModel.getTertiary(), newMenuItem->
            menuSectionBuilder.accept(rightMenuBar, newMenuItem));
        
        return menuBarContainer;
        
    }
    
    // -- HELPER

    private static Component createTitleOrLogo(
            final IsisAppCommonContext commonContext, 
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
    
    private static void buildMenuModel(
            final IsisAppCommonContext commonContext,
            final MenuUiModel menuUiModel,
            final Consumer<MenuItemVaa> onNewMenuItem) {
        
        menuUiModel.buildMenuItems(
                commonContext, 
                new ActionLinkFactoryVaa(),
                MenuItemVaa::newMenuItem,
                onNewMenuItem);
    }
    
    
}
