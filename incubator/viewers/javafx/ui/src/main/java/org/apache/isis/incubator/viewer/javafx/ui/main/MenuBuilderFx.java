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
package org.apache.isis.incubator.viewer.javafx.ui.main;

import org.apache.isis.incubator.viewer.javafx.model.action.ActionUiModelFactoryFx;
import org.apache.isis.viewer.common.model.menu.MenuItemDto;
import org.apache.isis.viewer.common.model.menu.MenuVisitor;

import lombok.RequiredArgsConstructor;
import lombok.val;
import lombok.extern.log4j.Log4j2;

import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.SeparatorMenuItem;

@RequiredArgsConstructor(staticName = "of")
@Log4j2
public class MenuBuilderFx implements MenuVisitor {
    
    private final MenuBar menuBar;
    
    private Menu currentTopLevelMenu = null;
    private ActionUiModelFactoryFx actionUiModelFactory = new ActionUiModelFactoryFx();

    @Override
    public void addTopLevel(MenuItemDto menu) {
        log.info("top level menu {}", menu.getName());
        
        menuBar.getMenus()
        .add(currentTopLevelMenu = new Menu(menu.getName()));
    }

    @Override
    public void addSubMenu(MenuItemDto menu) {
        val managedAction = menu.getManagedAction();
        
        log.info("sub menu {}", menu.getName());
        
        val actionLink = actionUiModelFactory.newActionUiModel(managedAction);
        currentTopLevelMenu.getItems().add(actionLink.createMenuUiComponent());
    }
    
    @Override
    public void addSectionSpacer() {
        log.info("spacer");
        currentTopLevelMenu.getItems()
        .add(new SeparatorMenuItem());
    }
    
}
