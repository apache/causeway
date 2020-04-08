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
package org.apache.isis.viewer.wicket.ui.components.actionmenu.serviceactions;

import java.util.List;
import java.util.function.BooleanSupplier;

import org.apache.wicket.MarkupContainer;
import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;

import org.apache.isis.core.commons.internal.collections._Lists;
import org.apache.isis.viewer.wicket.ui.panels.PanelBase;
import org.apache.isis.viewer.wicket.ui.util.CssClassAppender;

import lombok.val;

@SuppressWarnings("rawtypes")
public abstract class MenuActionPanel extends PanelBase {

    private static final long serialVersionUID = 1L;

    public MenuActionPanel(final String id) {
        super(id);
    }

    protected ListView<CssMenuItem> subMenuItemsView(final List<CssMenuItem> subMenuItems) {
        return new ListView<CssMenuItem>("subMenuItems", subMenuItems) {

            private static final long serialVersionUID = 1L;
            private transient int populationCount = 0;

            @Override
            protected void populateItem(ListItem<CssMenuItem> listItem) {
                val subMenuItem = listItem.getModelObject();

                if(subMenuItem.isFirstInSection() 
                        && populationCount>0) {
                    addSeparatorBefore(subMenuItem, listItem);
                }

                if (subMenuItem.hasSubMenuItems()) {
                    addFolderItem(subMenuItem, listItem);
                } else {
                    addLeafItem(subMenuItem, listItem);
                }

                populationCount++;
            }
        };
    }

    protected WebComponent divider(final BooleanSupplier visibilityCheck) {
        return new WebComponent("divider") {
            private static final long serialVersionUID = 1L;

            @Override
            protected void onConfigure() {
                super.onConfigure();
                setVisible(visibilityCheck.getAsBoolean());
            }
        };
    }
    
    
    //TODO candidate to be supported by the model itself (recursive)
    protected List<CssMenuItem> flatten(final List<CssMenuItem> menuItems) {
        val subMenuItems = _Lists.<CssMenuItem>newArrayList();
        for (val menuItem : menuItems) {
            subMenuItems.addAll(menuItem.getSubMenuItems());
        }
        return subMenuItems;
    }

    // -- HELPER

    private void addSeparatorBefore(CssMenuItem menuItem, ListItem<CssMenuItem> listItem) {
        listItem.add(new CssClassAppender("list-separator"));
    }

    private void addFolderItem(CssMenuItem menuItem, ListItem<CssMenuItem> listItem) {
        final MarkupContainer parent = this;
        ServiceActionUtil.addFolderItem(super.getCommonContext(), menuItem, listItem, parent);
    }

    private void addLeafItem(CssMenuItem menuItem, ListItem<CssMenuItem> listItem) {
        final MarkupContainer parent = this;
        ServiceActionUtil.addLeafItem(
                super.getCommonContext(), menuItem, listItem, parent);
    }


}
