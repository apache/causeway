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

import org.apache.isis.applib.layout.menubars.bootstrap3.BS3Menu;
import org.apache.isis.core.metamodel.interactions.managed.ManagedAction;
import org.apache.isis.core.webapp.context.IsisAppCommonContext;
import org.apache.isis.incubator.viewer.javafx.model.action.ActionLinkFactoryFx;
import org.apache.isis.viewer.common.model.menu.MenuBuilder;

import lombok.RequiredArgsConstructor;
import lombok.val;
import lombok.extern.log4j.Log4j2;

import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;

@RequiredArgsConstructor(staticName = "of")
@Log4j2
public class MenuBuilderFx implements MenuBuilder {
    
    private final IsisAppCommonContext commonContext;
    private final MenuBar menuBar;
    
    private Menu currentTopLevelMenu = null;
    private ActionLinkFactoryFx actionLinkFactory = new ActionLinkFactoryFx();

    
    @Override
    public void addTopLevel(BS3Menu menu) {
        log.info("top level menu {}", menu.getNamed());
        
        menuBar.getMenus()
        .add(currentTopLevelMenu = new Menu(menu.getNamed()));
        
    }
    
    @Override
    public void addSectionSpacer() {
        // TODO Auto-generated method stub
        log.info("spacer");
    }
    
    @Override
    public void addSubMenu(String named, ManagedAction managedAction) {
        log.info("top level menu {}", managedAction.getName());
        
        val actionLink = actionLinkFactory.newAction(commonContext, named, managedAction);
        currentTopLevelMenu.getItems().add(actionLink.getUiMenuItem());
    }
    
}
