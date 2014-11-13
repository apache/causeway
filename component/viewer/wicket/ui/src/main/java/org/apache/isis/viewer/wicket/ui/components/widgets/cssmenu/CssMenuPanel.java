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

package org.apache.isis.viewer.wicket.ui.components.widgets.cssmenu;

import java.util.List;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.util.ListModel;
import org.apache.isis.core.commons.lang.StringExtensions;
import org.apache.isis.viewer.wicket.ui.ComponentFactory;
import org.apache.isis.viewer.wicket.ui.panels.PanelAbstract;
import org.apache.isis.viewer.wicket.ui.panels.PanelUtil;
import org.apache.isis.viewer.wicket.ui.util.CssClassAppender;

/**
 * Top level panel for a CSS menu, consisting of a number of unparented
 * {@link CssMenuItem}s.
 * 
 * <p>
 * The {@link Style} enum allows the presentation to be altered.
 */
public class CssMenuPanel extends PanelAbstract<CssMenuPanel.ListOfCssMenuItemsModel> {

    private static final long serialVersionUID = 1L;

    public enum Style {
        REGULAR {
            @Override
            public String getAppendValue() {
                return null; // ie, append nothing
            }
        },
        SMALL {
            @Override
            public String getAppendValue() {
                return toString();
            }
        };
        @Override
        public String toString() {
            return StringExtensions.toCamelCase(name());
        }

        public String getAppendValue() {
            return toString();
        }
    }

    static class ListOfCssMenuItemsModel extends ListModel<CssMenuItem> {

        private static final long serialVersionUID = 1L;

        public ListOfCssMenuItemsModel(final List<CssMenuItem> cssMenuItems) {
            super(cssMenuItems);
        }
    }

    private final StyleAppender styleAppender;
    static final String ID_MENU_ITEMS = "menuItems";
    static final String ID_MENU_ITEM = "menuItem";

    public CssMenuPanel(final String id, final Style style, final List<CssMenuItem> topLevelMenuItems) {
        super(id, new ListOfCssMenuItemsModel(topLevelMenuItems));
        this.styleAppender = new StyleAppender(style);

        add(styleAppender);

        final RepeatingView menuItemRv = new RepeatingView(CssMenuPanel.ID_MENU_ITEMS);
        add(menuItemRv);

        for (final CssMenuItem cssMenuItem : this.getModel().getObject()) {
            final WebMarkupContainer menuItemMarkup = new WebMarkupContainer(menuItemRv.newChildId());
            menuItemRv.add(menuItemMarkup);

            menuItemMarkup.add(new CssMenuItemPanel(CssMenuPanel.ID_MENU_ITEM, cssMenuItem));
        }

    }

    static final class StyleAppender extends CssClassAppender {

        private static final long serialVersionUID = 1L;

        public StyleAppender(final Style style) {
            super(style.getAppendValue());
        }
    }

    /**
     * Because there is no {@link ComponentFactory} for this component,
     * its CSS must be contributed in this way instead (also meaning its CSS is not bundled).
     */
    @Override
    public void renderHead(final IHeaderResponse response) {
        PanelUtil.renderHead(response, this.getClass());
    }

}
