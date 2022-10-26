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
import org.apache.causeway.viewer.commons.applib.services.menu.MenuVisitor;
import org.apache.causeway.viewer.commons.services.userprof.UserProfileUiServiceDefault;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;
import lombok.extern.log4j.Log4j2;

@Log4j2
final class _MenuItemBuilder {

    public static void buildMenuItems(
            final MetaModelContext mmc,
            final BSMenuBar menuBar,
            final MenuVisitor menuBuilder) {

        val itemsPerSectionCounter = new LongAdder();

        val menuVisitor = MenuProcessor.of(mmc, menuBuilder);

        for (val menu : menuBar.getMenus()) {

            menuVisitor.addTopLevel(menu);

            for (val menuSection : menu.getSections()) {

                itemsPerSectionCounter.reset();

                for (val actionLayoutData : menuSection.getServiceActions()) {
                    val serviceBeanName = actionLayoutData.getLogicalTypeName();

                    val serviceAdapter = mmc.lookupServiceAdapterById(serviceBeanName);
                    if(serviceAdapter == null) {
                        // service not recognized, presumably the menu layout is out of sync with actual configured modules
                        continue;
                    }

                    val managedAction = ManagedAction
                            .lookupAction(serviceAdapter, actionLayoutData.getId(), Where.EVERYWHERE)
                            .orElse(null);
                    if (managedAction == null) {
                        log.warn("No such action: bean-name '{}' action-id '{}'",
                                serviceBeanName,
                                actionLayoutData.getId());
                        continue;
                    }

                    val visibilityVeto = managedAction.checkVisibility();
                    if (visibilityVeto.isPresent()) {
                        continue;
                    }

                    val isFirstInSection = itemsPerSectionCounter.intValue()==0;

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
        private final MenuVisitor menuVisitor;

        private BSMenu currentTopLevel;
        private boolean pushedCurrentTopLevel = false;

        public void addTopLevel(final BSMenu menu) {
            currentTopLevel = menu;
            pushedCurrentTopLevel = false;
        }

        public void addSubMenu(
                @NonNull final BSMenuSection menuSection,
                @NonNull final ManagedAction managedAction,
                final boolean isFirstInSection,
                final ServiceActionLayoutData actionLayoutData) {

            if(!pushedCurrentTopLevel) {
                val topLevelDto = topLevelDto(metaModelContext, currentTopLevel);

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
            val menuDto = MenuItemDto.subMenu(
                    managedAction,
                    actionLayoutData.getNamed(),
                    actionLayoutData.getCssClassFa());

            menuVisitor.addSubMenu(menuDto);
        }

    }

    /**
     * @implNote when ever the top level MenuItem name is empty or {@code null} we set the name
     * to the current user's profile name
     */
    private static MenuItemDto topLevelDto(
            final MetaModelContext mmc,
            final BSMenu menu) {

        val menuItemIsUserProfile = _Strings.isNullOrEmpty(menu.getNamed()); // top level menu item name

        val menuItemName = menuItemIsUserProfile
                ? userProfileName(mmc)
                : menu.getNamed();

        return menuItemIsUserProfile
                // under the assumption that this can only be the case when we have discovered the empty named top level menu
                ? MenuItemDto.tertiaryRoot(menuItemName, menu.getCssClassFa())
                : MenuItemDto.topLevel(menuItemName, menu.getCssClassFa());
    }

    private static String userProfileName(
            final MetaModelContext mmc) {
        val userProfile = mmc
                .getServiceRegistry()
                .lookupServiceElseFail(UserProfileUiServiceDefault.class)
                .userProfile();
        return userProfile.getUserProfileName();
    }




}
