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
package org.apache.causeway.viewer.wicket.ui.components.actionmenu.serviceactions;

import java.util.function.Consumer;

import org.apache.wicket.MarkupContainer;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.panel.Fragment;

import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.core.metamodel.interactions.managed.ManagedAction;
import org.apache.causeway.viewer.commons.applib.services.menu.MenuItemDto;
import org.apache.causeway.viewer.commons.applib.services.menu.MenuUiModel;
import org.apache.causeway.viewer.commons.applib.services.menu.MenuVisitor;
import org.apache.causeway.viewer.wicket.model.links.LinkAndLabel;
import org.apache.causeway.viewer.wicket.model.models.UiObjectWkt;
import org.apache.causeway.viewer.wicket.ui.components.actionmenu.entityactions.LinkAndLabelFactory;
import org.apache.causeway.viewer.wicket.ui.util.Wkt;
import org.apache.causeway.viewer.wicket.ui.util.WktDecorators;

import lombok.RequiredArgsConstructor;
import lombok.experimental.UtilityClass;

@UtilityClass
class ServiceActionUtil {

    void addLeafItem(
            final CssMenuItem menuItem,
            final ListItem<CssMenuItem> listItem,
            final MarkupContainer parent) {

        var linkAndLabel = menuItem.getLinkAndLabel();
        var menuItemActionLink = linkAndLabel.getUiComponent();
        var menuItemLabel = Wkt.labelAdd(menuItemActionLink, "menuLinkLabel", menuItem.getName());

        WktDecorators
            .decorateMenuAction(listItem, menuItemActionLink, menuItemLabel, linkAndLabel.getMenuActionDecorationModel());

        var leafItem = new Fragment("content", "leafItem", parent);
        leafItem.add(menuItemActionLink);

        listItem.add(leafItem);
    }

    void addFolderItem(
            final CssMenuItem subMenuItem,
            final ListItem<CssMenuItem> listItem,
            final MarkupContainer parent) {

        Wkt.cssAppend(listItem, "dropdown-submenu");

        Fragment folderItem = new Fragment("content", "folderItem", parent);
        listItem.add(folderItem);

        Wkt.labelAdd(folderItem, "folderName", ()->subMenuItem.getLinkAndLabel().getFriendlyName());
        final Can<CssMenuItem> menuItems = subMenuItem.getSubMenuItems();

        Wkt.listViewAdd(folderItem, "subMenuItems", menuItems.toList(), item->{
            CssMenuItem menuItem = listItem.getModelObject();

            if (menuItem.hasSubMenuItems()) {
                addFolderItem(menuItem, item, parent);
            } else {
                addLeafItem(menuItem, item, parent);
            }
        });

    }

    @RequiredArgsConstructor(staticName = "of")
    private static class MenuBuilderWkt implements MenuVisitor {

        private final Consumer<CssMenuItem> onNewMenuItem;

        private CssMenuItem currentTopLevelMenu = null;

        @Override
        public void addTopLevel(final MenuItemDto menuDto) {
            currentTopLevelMenu = CssMenuItem.newMenuItemWithSubmenu(menuDto.getName());
            onNewMenuItem.accept(currentTopLevelMenu);
        }

        @Override
        public void addSectionSpacer() {
            var menuSection = CssMenuItem.newSpacer();
            currentTopLevelMenu.addSubMenuItem(menuSection);
        }

        @Override
        public void addSubMenu(final MenuItemDto menuDto) {
            var managedAction = menuDto.getManagedAction();

            var menuItem = CssMenuItem.newMenuItemWithLink(menuDto.getName());
            currentTopLevelMenu.addSubMenuItem(menuItem);

            menuItem.setLinkAndLabel(newActionLink(managedAction));
        }

        @Override
        public void addSectionLabel(final String named) {
            var menuSectionLabel = CssMenuItem.newSectionLabel(named);
            currentTopLevelMenu.addSubMenuItem(menuSectionLabel);
        }

        private LinkAndLabel newActionLink(final ManagedAction managedAction) {
            var serviceModel = UiObjectWkt.ofAdapter(managedAction.getOwner());
            return LinkAndLabelFactory.forMenu(serviceModel)
                    .apply(managedAction.getAction());
        }
    }

    void buildMenu(
            final MetaModelContext commonContext,
            final MenuUiModel menuUiModel,
            final Consumer<CssMenuItem> onNewMenuItem) {

        menuUiModel.buildMenuItems(commonContext.getMetaModelContext(),
                MenuBuilderWkt.of(onNewMenuItem));
    }

}
