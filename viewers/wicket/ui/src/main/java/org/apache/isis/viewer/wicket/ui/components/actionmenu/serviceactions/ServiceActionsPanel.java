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
import java.util.stream.Collectors;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.request.resource.CssResourceReference;

import org.apache.isis.commons.internal.base._NullSafe;
import org.apache.isis.viewer.wicket.ui.util.CssClassAppender;
import org.apache.isis.viewer.wicket.ui.util.SSESupport;
import org.apache.isis.viewer.wicket.ui.util.Tooltips;

import lombok.val;

// import de.agilecoders.wicket.extensions.markup.html.bootstrap.button.DropdownAutoOpenJavaScriptReference;

/**
 * A panel responsible to render the application actions as menu in a navigation bar.
 *
 * <p>
 *     The multi-level sub menu support is borrowed from
 *     <a href="http://bootsnipp.com/snippets/featured/multi-level-dropdown-menu-bs3">Bootsnip</a>
 * </p>
 */
public class ServiceActionsPanel extends MenuActionPanel {

    private static final long serialVersionUID = 1L;

    public ServiceActionsPanel(final String id, final List<CssMenuItem> menuItems) {
        super(id);
        val menuItemsView = new ListView<CssMenuItem>("menuItems", menuItems) {
            private static final long serialVersionUID = 1L;

            @Override
            protected void populateItem(final ListItem<CssMenuItem> listItem) {
                val menuItem = listItem.getModelObject();

                val topMenu = new WebMarkupContainer("topMenu");
                topMenu.add(subMenuItemsView(menuItem.getSubMenuItems()));
                topMenu.add(new CssClassAppender(cssForTopMenu(menuItem)));

                listItem.add(new Label("name", menuItem.getName()));
                listItem.add(topMenu);
                if(menuItem.getItemType().isActionOrSubMenuContainer()) {
                    listItem.add(new CssClassAppender(cssForServices(menuItem)));
                }

            }
        };
        add(menuItemsView);
    }

    @Override
    public void renderHead(final IHeaderResponse response) {
        super.renderHead(response);

        response.render(CssHeaderItem.forReference(new CssResourceReference(ServiceActionsPanel.class, "ServiceActionsPanel.css")));
        Tooltips.renderHead(response);
        SSESupport.renderHead(response);

    }

    // -- HELPER

    private static String cssForTopMenu(final CssMenuItem menuItem) {
        return "top-menu-" + CssClassAppender.asCssStyle(menuItem.getName());
    }

    private static String cssForServices(final CssMenuItem menuItem) {
        return _NullSafe.stream(menuItem.getSubMenuItems())
        .filter(cssMenuItem->cssMenuItem.getItemType().isActionOrSubMenuContainer())
        .map(cssMenuItem -> {
            val actionIdentifier = cssMenuItem.getLinkAndLabel().getCssClassForAction();
            if (actionIdentifier != null) {
                // busrules-busrulesobjects-findbyname
                final String actionId = CssClassAppender.asCssStyle(actionIdentifier);
                final int i = actionId.lastIndexOf("-");
                // busrules-busrulesobjects
                return i == -1 ? actionId : actionId.substring(0, i);
            } else {
                return null;
            }
        })
        .filter(_NullSafe::isPresent)
        .map((final String input) -> "isis-" + input)
        .distinct()
        .collect(Collectors.joining(" "));
    }



}
