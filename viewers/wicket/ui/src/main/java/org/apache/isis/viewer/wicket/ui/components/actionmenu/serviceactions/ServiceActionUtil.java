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
import java.util.function.Consumer;

import org.apache.wicket.MarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Fragment;

import org.apache.isis.core.metamodel.interactions.managed.ManagedAction;
import org.apache.isis.core.runtime.context.IsisAppCommonContext;
import org.apache.isis.viewer.common.model.action.ActionLinkUiModelFactory;
import org.apache.isis.viewer.common.model.menu.MenuUiModel;
import org.apache.isis.viewer.wicket.model.common.CommonContextUtils;
import org.apache.isis.viewer.wicket.model.links.LinkAndLabel;
import org.apache.isis.viewer.wicket.model.models.EntityModel;
import org.apache.isis.viewer.wicket.ui.pages.PageAbstract;
import org.apache.isis.viewer.wicket.ui.util.CssClassAppender;
import org.apache.isis.viewer.wicket.ui.util.Decorators;

import lombok.RequiredArgsConstructor;
import lombok.val;
import lombok.experimental.UtilityClass;

@UtilityClass
//@Log4j2
public final class ServiceActionUtil {

    static void addLeafItem(
            IsisAppCommonContext commonContext, 
            CssMenuItem menuItem,
            ListItem<CssMenuItem> listItem,
            MarkupContainer parent) {
        
        val actionUiModel = menuItem.getMenuActionUiModel();
        val menuItemActionLink = actionUiModel.getUiComponent();

        val menuItemLabel = new Label("menuLinkLabel", menuItem.getName());
        menuItemActionLink.addOrReplace(menuItemLabel);
        
        Decorators.getActionLink().decorateMenuItem(
                listItem, 
                actionUiModel,
                commonContext.getTranslationService());
        
        val actionMeta = actionUiModel.getActionUiMetaModel();
        val fontAwesome = actionMeta.getFontAwesomeUiModel();
        Decorators.getIcon().decorate(menuItemLabel, fontAwesome);
        Decorators.getMissingIcon().decorate(menuItemActionLink, fontAwesome);
        
        val leafItem = new Fragment("content", "leafItem", parent);
        leafItem.add(menuItemActionLink);

        listItem.add(leafItem);
    }

    static void addFolderItem(
            IsisAppCommonContext commonContext,
            CssMenuItem subMenuItem,
            ListItem<CssMenuItem> listItem,
            MarkupContainer parent) {

        listItem.add(new CssClassAppender("dropdown-submenu"));

        Fragment folderItem = new Fragment("content", "folderItem", parent);
        listItem.add(folderItem);

        folderItem.add(new Label("folderName", subMenuItem.getMenuActionUiModel().getLabel()));
        final List<CssMenuItem> menuItems = subMenuItem.getSubMenuItems();
        ListView<CssMenuItem> subMenuItemsView = new ListView<CssMenuItem>("subMenuItems",
                menuItems) {
            private static final long serialVersionUID = 1L;

            @Override
            protected void populateItem(ListItem<CssMenuItem> listItem) {
                CssMenuItem subMenuItem = listItem.getModelObject();

                if (subMenuItem.hasSubMenuItems()) {
                    addFolderItem(commonContext, subMenuItem, listItem, parent);
                } else {
                    addLeafItem(commonContext, subMenuItem, listItem, parent);
                }
            }
        };
        folderItem.add(subMenuItemsView);
    }

    @RequiredArgsConstructor
    private static class MenuActionFactoryWkt implements ActionLinkUiModelFactory<AbstractLink> {

        private final IsisAppCommonContext commonContext;
        
        @Override
        public LinkAndLabel newActionLink(
                final String named, 
                final ManagedAction managedAction) {
        
            val serviceModel = EntityModel.ofAdapter(commonContext, managedAction.getOwner());
            
            val actionLinkFactory = new MenuActionLinkFactory(
                    PageAbstract.ID_MENU_LINK, 
                    serviceModel);
            
            return LinkAndLabel.of(
                    model->actionLinkFactory.newActionLink(
                            model.getObjectAction(()->CommonContextUtils.getCommonContext().getSpecificationLoader()),
                            named)
                            .getUiComponent(),
                    named,
                    serviceModel,
                    managedAction.getAction());
        }
        
    }


    public static void buildMenu(
            final IsisAppCommonContext commonContext,
            final MenuUiModel menuUiModel,
            final Consumer<CssMenuItem> onNewMenuItem) {
        
        menuUiModel.buildMenuItems(
                commonContext, 
                new MenuActionFactoryWkt(commonContext),
                CssMenuItem::newMenuItem,
                onNewMenuItem);
    }

}
