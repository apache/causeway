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

import java.util.concurrent.atomic.LongAdder;

import org.apache.causeway.applib.annotation.Where;
import org.apache.causeway.applib.layout.component.ServiceActionLayoutData;
import org.apache.causeway.applib.layout.menubars.bootstrap.BSMenu;
import org.apache.causeway.applib.layout.menubars.bootstrap.BSMenuBar;
import org.apache.causeway.applib.layout.menubars.bootstrap.BSMenuSection;
import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.core.metamodel.interactions.managed.ManagedAction;
import org.apache.causeway.viewer.commons.applib.services.menu.MenuItemDto;
import org.apache.causeway.viewer.commons.services.userprof.UserProfileUiServiceDefault;

import org.jspecify.annotations.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
final class _MenuItemBuilder {

    static interface Visitor {

        void addTopLevel(MenuItemDto menuDto);
        void addSectionSpacer();
        void addMenuAction(MenuItemDto menuDto);

        /**
         * @param named - not null and not empty
         */
        void addSectionLabel(String named);

    }

    static void buildMenuItems(
            final MetaModelContext mmc,
            final BSMenuBar menuBar,
            final Visitor menuBuilder) {

        var itemsPerSectionCounter = new LongAdder();

        var menuVisitor = MenuProcessor.of(mmc, menuBuilder);

        for (var menu : menuBar.getMenus()) {

            menuVisitor.addTopLevel(menu);

            for (var menuSection : menu.getSections()) {

                itemsPerSectionCounter.reset();

                for (var actionLayoutData : menuSection.getServiceActions()) {
                    var serviceBeanName = actionLayoutData.getLogicalTypeName();

                    var serviceAdapter = mmc.lookupServiceAdapterById(serviceBeanName);
                    if(serviceAdapter == null) {
                        // service not recognized, presumably the menu layout is out of sync with actual configured modules
                        continue;
                    }

                    var managedAction = ManagedAction
                            .lookupAction(serviceAdapter, actionLayoutData.getId(), Where.EVERYWHERE)
                            .orElse(null);
                    if (managedAction == null) {
                        log.warn("No such action: bean-name '{}' action-id '{}'",
                                serviceBeanName,
                                actionLayoutData.getId());
                        continue;
                    }

                    var visibilityVeto = managedAction.checkVisibility();
                    if (visibilityVeto.isPresent()) {
                        continue;
                    }

                    var isFirstInSection = itemsPerSectionCounter.intValue()==0;

                    menuVisitor.addSubMenu(menuSection, managedAction, isFirstInSection, actionLayoutData);
                    itemsPerSectionCounter.increment();

                }
            }

        }
    }

    // -- HELPER

    @RequiredArgsConstructor(staticName = "of")
    private static class MenuProcessor {

        private final MetaModelContext metaModelContext;
        private final Visitor menuVisitor;

        private BSMenu currentTopLevel;
        private boolean pushedCurrentTopLevel = false;

        public void addTopLevel(final BSMenu menu) {
            currentTopLevel = menu;
            pushedCurrentTopLevel = false;
        }

        public void addSubMenu(
                final @NonNull BSMenuSection menuSection,
                final @NonNull ManagedAction managedAction,
                final boolean isFirstInSection,
                final ServiceActionLayoutData actionLayoutData) {

            if(!pushedCurrentTopLevel) {
                var topLevelDto = topLevelDto(metaModelContext, currentTopLevel);

                menuVisitor.addTopLevel(topLevelDto);
                pushedCurrentTopLevel = true;

                // add section label if first
                if(isFirstInSection) {
                    if(_Strings.isNotEmpty(menuSection.getNamed())) {
                        menuVisitor.addSectionLabel(menuSection.getNamed());
                    }
                }

            } else {
                if(isFirstInSection) {
                    if(_Strings.isEmpty(menuSection.getNamed())) {
                        menuVisitor.addSectionSpacer();
                    } else {
                        //XXX could make it a config option whether non-top sections are preceded with a spacer or not
                        menuVisitor.addSectionSpacer();
                        menuVisitor.addSectionLabel(menuSection.getNamed());
                    }
                }
            }
            var menuDto = MenuItemDto.subMenu(
                    managedAction,
                    actionLayoutData.getNamed(),
                    actionLayoutData.getCssClassFa());

            menuVisitor.addMenuAction(menuDto);
        }

    }

    /**
     * @implNote when ever the top level MenuItem name is empty or {@code null} we set the name
     * to the current user's profile name
     */
    private static MenuItemDto topLevelDto(
            final MetaModelContext mmc,
            final BSMenu menu) {

        var menuItemIsUserProfile = _Strings.isNullOrEmpty(menu.getNamed()); // top level menu item name

        var menuItemName = menuItemIsUserProfile
                ? userProfileName(mmc)
                : menu.getNamed();

        return menuItemIsUserProfile
                // under the assumption that this can only be the case when we have discovered the empty named top level menu
                ? MenuItemDto.tertiaryRoot(menuItemName, menu.getCssClassFa())
                : MenuItemDto.topLevel(menuItemName, menu.getCssClassFa());
    }

    private static String userProfileName(
            final MetaModelContext mmc) {
        var userProfile = mmc
                .getServiceRegistry()
                .lookupServiceElseFail(UserProfileUiServiceDefault.class)
                .userProfile();
        return userProfile.getUserProfileName();
    }

}
