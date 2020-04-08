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

import javax.inject.Inject;

import org.apache.wicket.MarkupContainer;
import org.apache.wicket.Page;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.request.resource.CssResourceReference;

import org.apache.isis.core.security.authentication.AuthenticationSession;
import org.apache.isis.viewer.wicket.model.models.PageType;
import org.apache.isis.viewer.wicket.ui.pages.PageClassRegistry;
import org.apache.isis.viewer.wicket.ui.util.CssClassAppender;
import org.apache.isis.viewer.wicket.ui.util.Tooltips;

import lombok.val;

/**
 * A panel responsible to render the application actions as menu in a navigation bar.
 *
 * <p>
 *     The multi-level sub menu support is borrowed from
 *     <a href="http://bootsnipp.com/snippets/featured/multi-level-dropdown-menu-bs3">Bootsnip</a>
 * </p>
 */
public class TertiaryActionsPanel extends MenuActionPanel {

    private static final long serialVersionUID = 1L;
    
    @Inject private PageClassRegistry pageClassRegistry;

    public TertiaryActionsPanel(String id, List<CssMenuItem> menuItems) {
        super(id);
        addLogoutLink(this);
        val subMenuItems = flatten(menuItems);
        val subMenuItemsView = subMenuItemsView(subMenuItems);

        final BooleanSupplier dividerVisibility = ()->{
            subMenuItemsView.configure();
            return !subMenuItems.isEmpty();
        };
        
        val divider = divider(dividerVisibility); 

        add(subMenuItemsView, divider);
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

        super.getMetaModelContext().getAuthenticationSessionTracker()
        .currentAuthenticationSession()
        .ifPresent(authenticationSession->{
        
            if(authenticationSession.getType() == AuthenticationSession.Type.EXTERNAL) {
                logoutLink.setEnabled(false);
                // TODO: need to improve the styling, show as grayed out.
                logoutLink.add(new CssClassAppender("disabled"));
                Tooltips.addTooltip(logoutLink, "External");
            }
            
        });

    }

    private Class<? extends Page> getSignInPage() {
        return pageClassRegistry.getPageClass(PageType.SIGN_IN);
    }

    @Override
    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);
        response.render(CssHeaderItem.forReference(new CssResourceReference(TertiaryActionsPanel.class, "TertiaryActionsPanel.css")));
    }

    

}
