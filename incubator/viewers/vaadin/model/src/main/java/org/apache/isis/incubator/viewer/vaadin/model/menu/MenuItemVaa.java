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
package org.apache.isis.incubator.viewer.vaadin.model.menu;

import com.vaadin.flow.component.Component;

import org.apache.isis.incubator.viewer.vaadin.model.action.MenuActionVaa;
import org.apache.isis.viewer.common.model.action.ActionUiModel;
import org.apache.isis.viewer.common.model.menuitem.MenuItemUiModel;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.val;

/**
 * @since 2.0.0
 */
//@Log4j2
public class MenuItemVaa 
extends MenuItemUiModel<Component, MenuItemVaa> {

    @Getter @Setter(AccessLevel.PRIVATE) private MenuActionVaa menuActionUiModel;
    
    public static MenuItemVaa newMenuItem(final String name) {
        return new MenuItemVaa(name);
    }

    private MenuItemVaa(final String name) {
        super(name);
    }
    
    @Override
    protected MenuItemVaa newSubMenuItem(final String name) {
        val subMenuItem = newMenuItem(name);
        subMenuItem.setParent(this);
        return subMenuItem;
    }
    
    /**
     * Optionally creates a sub-menu item invoking an action on the provided 
     * {@link ActionUiModel}, based on visibility and usability.
     */
    public void addMenuItemFor(final MenuActionVaa menuActionModel) {
        
        super.addMenuItemFor(menuActionModel, 
                subMenuItem->((MenuItemVaa)subMenuItem).setMenuActionUiModel(menuActionModel));
        
    }
}
