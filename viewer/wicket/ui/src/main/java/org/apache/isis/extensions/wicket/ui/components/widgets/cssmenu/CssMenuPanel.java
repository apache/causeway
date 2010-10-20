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


package org.apache.isis.extensions.wicket.ui.components.widgets.cssmenu;

import java.util.Arrays;
import java.util.List;

import org.apache.isis.extensions.wicket.model.util.Strings;
import org.apache.isis.extensions.wicket.ui.ComponentType;
import org.apache.isis.extensions.wicket.ui.components.widgets.cssmenu.CssMenuPanel.Style;
import org.apache.isis.extensions.wicket.ui.panels.PanelAbstract;
import org.apache.isis.extensions.wicket.ui.util.CssClassAppender;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.util.ListModel;

/**
 * Top level panel for a CSS menu, consisting of a number of unparented
 * {@link CssMenuItem}s.
 * 
 * <p>
 * The {@link Style} enum allows the presentation to be altered.
 */
public class CssMenuPanel extends PanelAbstract<CssMenuPanel.MyModel> {

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
		public String toString() {
			return Strings.toCamelCase(name());
		}

		public String getAppendValue() {
			return toString();
		}
	}

	static class MyModel extends ListModel<CssMenuItem> {

		private static final long serialVersionUID = 1L;

		public MyModel(List<CssMenuItem> cssMenuItems) {
			super(cssMenuItems);
		}
	}

	public static CssMenuItem.Builder newMenuItem(String name) {
		return CssMenuItem.newMenuItem(name);
	}

	private StyleAppender styleAppender;
    static final String ID_MENU_ITEMS = "menuItems";
    static final String ID_MENU_ITEM = "menuItem";

	public CssMenuPanel(String id, final Style style,
			List<CssMenuItem> topLevelMenuItems) {
		super(id, new MyModel(topLevelMenuItems));
		this.styleAppender = new StyleAppender(style);

		add(styleAppender);

		RepeatingView menuItemRv = new RepeatingView(CssMenuPanel.ID_MENU_ITEMS);
		add(menuItemRv);

		for (CssMenuItem cssMenuItem : this.getModel().getObject()) {
			WebMarkupContainer menuItemMarkup = new WebMarkupContainer(
					menuItemRv.newChildId());
			menuItemRv.add(menuItemMarkup);

			menuItemMarkup.add(new CssMenuItemPanel(CssMenuPanel.ID_MENU_ITEM,
					cssMenuItem));
		}

	}

	public CssMenuPanel(ComponentType componentType, Style style, CssMenuItem... topLevelMenuItems) {
		this(componentType.getWicketId(), style, Arrays.asList(topLevelMenuItems));
	}
	
	static final class StyleAppender extends CssClassAppender {
		
		private static final long serialVersionUID = 1L;
		
		public StyleAppender(Style style) {
			super(style.getAppendValue());
		}
		
	}

}



