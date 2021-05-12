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

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.util.ListModel;

/**
 * Panel containing a list of {@link CssMenuItem}s acting as submenus of a
 * parent {@link CssMenuItem}.
 */
class CssSubMenuItemsPanel
extends CssMenuItemPanelAbstract<List<CssMenuItem>, CssSubMenuItemsPanel.MyModel> {

    private static final long serialVersionUID = 1L;

    static class MyModel extends ListModel<CssMenuItem> {

        private static final long serialVersionUID = 1L;

        public MyModel(final List<CssMenuItem> cssMenuItems) {
            super(cssMenuItems);
        }
    }

    public CssSubMenuItemsPanel(final String id, final List<CssMenuItem> subMenuItems) {
        super(id, new MyModel(subMenuItems));
        setRenderBodyOnly(true);

        final RepeatingView menuItemRv = new RepeatingView(getId());
        add(menuItemRv);
        for (final CssMenuItem cssMenuItem : getModel().getObject()) {
            final WebMarkupContainer menuItemMarkup = new WebMarkupContainer(menuItemRv.newChildId());
            menuItemRv.add(menuItemMarkup);

            addSubMenuItems(menuItemMarkup, cssMenuItem);
        }
    }

}
