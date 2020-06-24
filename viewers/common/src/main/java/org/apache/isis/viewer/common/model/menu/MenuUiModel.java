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

import java.io.Serializable;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.Consumer;
import java.util.function.Function;

import org.apache.isis.applib.annotation.DomainServiceLayout;
import org.apache.isis.applib.layout.menubars.bootstrap3.BS3MenuBar;
import org.apache.isis.core.metamodel.interactions.managed.ManagedAction;
import org.apache.isis.core.webapp.context.IsisWebAppCommonContext;
import org.apache.isis.viewer.common.model.action.ActionLinkUiModelFactory;
import org.apache.isis.viewer.common.model.menuitem.MenuItemUiModel;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;
import lombok.extern.log4j.Log4j2;

@Getter
@RequiredArgsConstructor(staticName = "of")
@Log4j2
public class MenuUiModel implements Serializable {

    private static final long serialVersionUID = 1L;
    
    @NonNull private final DomainServiceLayout.MenuBar menuBarSelect;
    @NonNull private final List<String> menuContributingServiceIds;
    
    public String getCssClass() {
        return menuBarSelect.name().toLowerCase(Locale.ENGLISH);
    }
    
    public <T, M extends MenuItemUiModel<T, M>> 
    void buildMenuItems(
            final IsisWebAppCommonContext commonContext,
            final ActionLinkUiModelFactory<T> menuActionFactory,
            final Function<String, M> menuItemFactory,
            final Consumer<M> onNewMenuItem) {
        
        val menuBars = commonContext.getMenuBarsService().menuBars();

        // TODO: remove hard-coded dependency on BS3
        final BS3MenuBar menuBar = (BS3MenuBar) menuBars.menuBarFor(getMenuBarSelect());
        
        MenuUiModel_buildMenuItems.buildMenuItems(
                commonContext, 
                menuBar,
                menuActionFactory,
                menuItemFactory,
                onNewMenuItem);
        
    }
    
    public void buildMenuItems(
            final IsisWebAppCommonContext commonContext,
            final MenuBuilder menuBuilder) {
        
        val menuBars = commonContext.getMenuBarsService().menuBars();

        // TODO: remove hard-coded dependency on BS3
        final BS3MenuBar menuBar = (BS3MenuBar) menuBars.menuBarFor(getMenuBarSelect());
        
        val itemsPerSectionCounter = new LongAdder();
        
        for (val menu : menuBar.getMenus()) {
            
            menuBuilder.addTopLevel(menu);

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
                    
                    //TODO call this only, if visible and usable 
                    menuBuilder.addSubMenu(actionLayoutData.getNamed(), managedAction);
                    
//                    val menuActionUiModel = menuActionFactory.newAction(
//                            commonContext,
//                            actionLayoutData.getNamed(),
//                            managedAction);

                    // Optionally creates a sub-menu item based on visibility and usability
//                    menuItemModel.addSubMenuItemFor(
//                            menuActionUiModel, 
//                            isFirstInSection,
//                            newSubMenuItem->{
//                                // increment counter only when a sub item was actually added
//                                itemsPerSectionCounter.increment();
//                                newSubMenuItem.setMenuActionUiModel(menuActionUiModel);
//                    });
                    
                }
            }
//            if (menuItemModel.hasSubMenuItems()) {
//                onNewMenuItem.accept(menuItemModel);
//            }
        }
        
    }
    

}
