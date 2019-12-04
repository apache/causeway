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

import javax.inject.Inject;

import org.apache.isis.webapp.wormhole.AuthenticationSessionWormhole;
import org.apache.isis.security.authentication.AuthenticationSession;
import org.apache.isis.viewer.wicket.ui.util.CssClassAppender;
import org.apache.isis.viewer.wicket.ui.util.Tooltips;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.Page;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.request.resource.CssResourceReference;

import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.viewer.wicket.model.models.PageType;
import org.apache.isis.viewer.wicket.ui.pages.PageClassRegistry;
import org.apache.isis.viewer.wicket.ui.panels.PanelBase;

/**
 * A panel responsible to render the application actions as menu in a navigation bar.
 *
 * <p>
 *     The multi-level sub menu support is borrowed from
 *     <a href="http://bootsnipp.com/snippets/featured/multi-level-dropdown-menu-bs3">Bootsnip</a>
 * </p>
 */
public class TertiaryActionsPanel extends PanelBase {

    private static final long serialVersionUID = 1L;

    public TertiaryActionsPanel(String id, List<CssMenuItem> menuItems) {
        super(id);
        addLogoutLink(this);
        final List<CssMenuItem> subMenuItems = flatten(menuItems);
        final ListView<CssMenuItem> subMenuItemsView = new ListView<CssMenuItem>("subMenuItems", subMenuItems) {
            private static final long serialVersionUID = 1L;

            @Override
            protected void populateItem(ListItem<CssMenuItem> listItem) {
                CssMenuItem subMenuItem = listItem.getModelObject();
                if (subMenuItem.hasSubMenuItems()) {
                    addFolderItem(subMenuItem, listItem);
                } else {
                    ServiceActionUtil.addLeafItem(
                            TertiaryActionsPanel.super.getCommonContext(), subMenuItem, listItem, TertiaryActionsPanel.this);
                }
            }
        };

        WebComponent divider = new WebComponent("divider") {
            private static final long serialVersionUID = 1L;

            @Override
            protected void onConfigure() {
                super.onConfigure();

                subMenuItemsView.configure();
                setVisible(!subMenuItems.isEmpty());
            }
        };

        add(subMenuItemsView, divider);
    }

    protected List<CssMenuItem> flatten(List<CssMenuItem> menuItems) {
        List<CssMenuItem> subMenuItems = _Lists.newArrayList();
        for (CssMenuItem menuItem : menuItems) {
            subMenuItems.addAll(menuItem.getSubMenuItems());
        }
        return subMenuItems;
    }

    private void addLogoutLink(MarkupContainer themeDiv) {

        Link<Void> logoutLink = new Link<Void>("logoutLink") {

            private static final long serialVersionUID = 1L;

            @Override
            public void onClick() {
                getSession().invalidate();
                setResponsePage(getSignInPage());
            }

        };
        themeDiv.add(logoutLink);

        // this is hacky, would rather ask AuthenticatedWebSessionForIsis, but that type isn't visible here.
        final AuthenticationSession authenticationSession = AuthenticationSessionWormhole.sessionByThread.get();
        if(authenticationSession != null && authenticationSession.getType() == AuthenticationSession.Type.EXTERNAL) {
            logoutLink.setEnabled(false);
            // TODO: need to improve the styling, show as grayed out.
            logoutLink.add(new CssClassAppender("disabled"));
            Tooltips.addTooltip(logoutLink, "External");
        }

    }

    private Class<? extends Page> getSignInPage() {
        return pageClassRegistry.getPageClass(PageType.SIGN_IN);
    }


    private void addFolderItem(CssMenuItem subMenuItem, ListItem<CssMenuItem> listItem) {
        final MarkupContainer parent = TertiaryActionsPanel.this;
        ServiceActionUtil.addFolderItem(super.getCommonContext(), subMenuItem, listItem, parent, ServiceActionUtil.SeparatorStrategy.WITHOUT_SEPARATORS);
    }

    @Override
    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);
        response.render(CssHeaderItem.forReference(new CssResourceReference(TertiaryActionsPanel.class, "TertiaryActionsPanel.css")));
    }

    @Inject
    private PageClassRegistry pageClassRegistry;

}
