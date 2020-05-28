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
package org.apache.isis.viewer.common.model.menu;

import java.util.concurrent.atomic.LongAdder;
import java.util.function.Consumer;
import java.util.function.Function;

import org.apache.isis.applib.layout.menubars.bootstrap3.BS3Menu;
import org.apache.isis.applib.layout.menubars.bootstrap3.BS3MenuBar;
import org.apache.isis.core.commons.internal.base._Strings;
import org.apache.isis.core.metamodel.interactions.managed.ManagedAction;
import org.apache.isis.core.webapp.context.IsisWebAppCommonContext;
import org.apache.isis.viewer.common.model.action.ActionUiModelFactory;
import org.apache.isis.viewer.common.model.menuitem.MenuItemUiModel;
import org.apache.isis.viewer.common.model.userprofile.UserProfileUiModelProvider;

import lombok.val;
import lombok.extern.log4j.Log4j2;

@Log4j2
final class MenuUiModel_buildMenuItems {

    public static <T, M extends MenuItemUiModel<T, M>> 
    void buildMenuItems(
            final IsisWebAppCommonContext commonContext,
            final BS3MenuBar menuBar,
            final ActionUiModelFactory<T> menuActionFactory,
            final Function<String, M> menuItemFactory,
            final Consumer<M> onNewMenuItem) {

        // we no longer use ServiceActionsModel#getObject() because the model only holds the services for the
        // menuBar in question, whereas the "Other" menu may reference a service which is defined for some other menubar

        val itemsPerSectionCounter = new LongAdder();
        
        for (val menu : menuBar.getMenus()) {
            
            val menuItemModel = processTopLevel(commonContext, menuItemFactory, menu);

            for (val menuSection : menu.getSections()) {

                itemsPerSectionCounter.reset();
                
                for (val actionLayoutData : menuSection.getServiceActions()) {
                    val serviceSpecId = actionLayoutData.getObjectType();

                    val serviceAdapter = commonContext.lookupServiceAdapterById(serviceSpecId);
                    if(serviceAdapter == null) {
                        // service not recognized, presumably the menu layout is out of sync with actual configured modules
                        continue;
                    }

                    val managedAction = ManagedAction.lookupAction(serviceAdapter, actionLayoutData.getId())
                            .orElse(null);
                    if (managedAction == null) {
                        log.warn("No such action {}", actionLayoutData.getId());
                        continue;
                    }
                    
                    val isFirstInSection = itemsPerSectionCounter.intValue()==0; 
                    
                    val menuActionUiModel = menuActionFactory.newAction(
                            commonContext,
                            actionLayoutData.getNamed(),
                            managedAction);

                    // Optionally creates a sub-menu item based on visibility and usability
                    menuItemModel.addSubMenuItemFor(
                            menuActionUiModel, 
                            isFirstInSection,
                            newSubMenuItem->{
                                // increment counter only when a sub item was actually added
                                itemsPerSectionCounter.increment();
                                newSubMenuItem.setMenuActionUiModel(menuActionUiModel);
                    });
                    
                }
            }
            if (menuItemModel.hasSubMenuItems()) {
                onNewMenuItem.accept(menuItemModel);
            }
        }
        
    }
    
    // -- HELPER

    /**
     * @implNote when ever the top level MenuItem name is empty or {@code null} we set the name
     * to the current user's profile name 
     */
    private static <T, M extends MenuItemUiModel<T, M>>  
    M processTopLevel(
            final IsisWebAppCommonContext commonContext,
            final Function<String, M> menuItemFactory, 
            final BS3Menu menu) {
        
        val menuItemIsUserProfile = _Strings.isNullOrEmpty(menu.getNamed()); // top level menu item name
            
        val menuItemName = menuItemIsUserProfile
                ? userProfileName(commonContext)
                : menu.getNamed();
        
        val menuItemModel = menuItemFactory.apply(menuItemName); 
        
        if(menuItemIsUserProfile) {
            // under the assumption that this can only be the case when we have discovered the empty named top level menu
            menuItemModel.setTertiaryRoot(true);  
        }
        
        return menuItemModel;
    }

    private static String userProfileName(
            final IsisWebAppCommonContext commonContext) {
        val userProfile = commonContext
                .lookupServiceElseFail(UserProfileUiModelProvider.class)
                .getUserProfile();
        return userProfile.getUserProfileName();
    }
    
    
}
