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
package org.apache.causeway.viewer.commons.services.menu;

import java.util.ArrayList;

import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import org.apache.causeway.applib.annotation.DomainServiceLayout.MenuBar;
import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.applib.layout.menubars.bootstrap.BSMenuBar;
import org.apache.causeway.applib.services.menu.MenuBarsService;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.viewer.commons.applib.services.menu.MenuItemDto;
import org.apache.causeway.viewer.commons.applib.services.menu.MenuUiService;
import org.apache.causeway.viewer.commons.applib.services.menu.model.MenuDropdownBuilder;
import org.apache.causeway.viewer.commons.applib.services.menu.model.NavbarSection;
import org.apache.causeway.viewer.commons.applib.services.menu.model.NavbarUiModel;
import org.apache.causeway.viewer.commons.services.CausewayModuleViewerCommonsServices;

import lombok.RequiredArgsConstructor;
import lombok.val;

@Service
@Named(CausewayModuleViewerCommonsServices.NAMESPACE + ".MenuUiServiceDefault")
@Priority(PriorityPrecedence.LATE)
@Qualifier("Default")
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class MenuUiServiceDefault
implements MenuUiService {

    private final MetaModelContext metaModelContext;
    private final MenuBarsService menuBarsService;

    @Override
    public NavbarUiModel getMenu() {
        return new NavbarUiModel(
                buildNavBarSection(MenuBar.PRIMARY),
                buildNavBarSection(MenuBar.SECONDARY),
                buildNavBarSection(MenuBar.TERTIARY));
    }

    // -- HELPER

    private NavbarSection buildNavBarSection(final MenuBar menuBarSelect) {

        val menuBar = (BSMenuBar) menuBarsService.menuBars()
                .menuBarFor(menuBarSelect);

        val topLevelEntries = new ArrayList<MenuDropdownBuilder>();

        _MenuItemBuilder.buildMenuItems(metaModelContext, menuBar, new _MenuItemBuilder.Visitor() {

            private MenuDropdownBuilder currentMenu;

            @Override
            public void addTopLevel(final MenuItemDto menuDto) {
                topLevelEntries.add(currentMenu = new MenuDropdownBuilder(menuDto.getName(), new ArrayList<>()));
            }

            @Override
            public void addSectionSpacer() {
                currentMenu.addSectionSpacer();
            }

            @Override
            public void addSectionLabel(final String named) {
                currentMenu.addSectionSpacer(named);
            }

            @Override
            public void addMenuAction(final MenuItemDto menuDto) {
                val action = menuDto.getManagedAction();
                currentMenu.addAction(action);
            }

        });

        return new NavbarSection(menuBarSelect, Can.ofCollection(topLevelEntries)
                .map(MenuDropdownBuilder::build));
    }

}
