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
package org.apache.isis.incubator.viewer.javafx.model.menu;

import org.apache.isis.viewer.common.model.menuitem.MenuItemUiModel;

import lombok.val;

/**
 * @since 2.0.0
 */
//@Log4j2
public class MenuItemFx 
extends MenuItemUiModel<javafx.scene.Node, MenuItemFx> {

    public static MenuItemFx newMenuItem(final String label) {
        return new MenuItemFx(label);
    }

    private MenuItemFx(final String label) {
        super(label);
    }
    
    @Override
    protected MenuItemFx newSubMenuItem(final String label) {
        val subMenuItem = newMenuItem(label);
        subMenuItem.setParent(this);
        return subMenuItem;
    }

}
