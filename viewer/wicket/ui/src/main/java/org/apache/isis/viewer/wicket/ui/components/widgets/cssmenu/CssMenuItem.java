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

import static org.hamcrest.CoreMatchers.is;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import org.apache.isis.commons.ensure.Ensure;
import org.apache.isis.metamodel.adapter.ObjectAdapter;
import org.apache.isis.metamodel.authentication.AuthenticationSession;
import org.apache.isis.metamodel.consent.Consent;
import org.apache.isis.metamodel.spec.feature.ObjectAction;
import org.apache.isis.viewer.wicket.model.mementos.ObjectAdapterMemento;
import org.apache.isis.viewer.wicket.model.nof.AuthenticationSessionAccessor;
import org.apache.isis.viewer.wicket.ui.components.widgets.cssmenu.CssMenuLinkFactory.LinkAndLabel;
import org.apache.isis.viewer.wicket.ui.pages.PageAbstract;
import org.apache.isis.viewer.wicket.ui.util.Components;
import org.apache.isis.viewer.wicket.ui.util.CssClassAppender;
import org.apache.wicket.Application;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.Page;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.Model;

import com.google.common.collect.Lists;

public class CssMenuItem implements Serializable {

	private static final long serialVersionUID = 1L;

    public static final String ID_MENU_LINK = "menuLink";

	public static class Builder {
		private CssMenuItem cssMenuItem;
		private Builder(String name) {
			cssMenuItem = new CssMenuItem(name);
		}
		public Builder parent(CssMenuItem parent) {
			cssMenuItem.setParent(parent);
			return this;
		}
		public <T extends Page> Builder link(Class<T> pageClass) {
			return link(new BookmarkablePageLink<T>(
					ID_MENU_LINK, pageClass));
		}
		public <T extends Page> Builder link(Link<?> link) {
			Ensure.ensureThatArg(link.getId(), is(ID_MENU_LINK));
			cssMenuItem.setLink(link);
			return this;
		}
		public <T extends Page> Builder enabled(String disabledReasonIfAny) {
			cssMenuItem.setEnabled(disabledReasonIfAny == null);
			cssMenuItem.setDisabledReason(disabledReasonIfAny);
			return this;
		}
		
		/**
		 * Access the {@link CssMenuItem} before it is attached to its parent.
		 * @see #build()
		 */
		public CssMenuItem itemBeingBuilt() {
			return cssMenuItem;
		}
		/**
		 * Returns the built {@link CssMenuItem}, associating with {@link #parent(CssMenuItem) parent} (if specified). 
		 */
		public CssMenuItem build() {
			if (cssMenuItem.parent != null) {
				cssMenuItem.parent.subMenuItems.add(cssMenuItem);
			}
			return cssMenuItem;
		}
	}
	


	private String name;
	private List<CssMenuItem> subMenuItems = Lists.newArrayList();
	private CssMenuItem parent;
	
	private Link<?> link;
	private boolean enabled = true; // unless disabled
	
	private String disabledReason;

    static final String ID_MENU_LABEL = "menuLabel";

    static final String ID_SUB_MENU_ITEMS = "subMenuItems";
	
	/**
	 * Factory method returning {@link Builder builder}.
	 */
	public static Builder newMenuItem(String name) {
		return new Builder(name);
	}
	
	private CssMenuItem(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public boolean hasParent() {
		return parent != null;
	}
	
	private void setParent(CssMenuItem parent) {
		this.parent = parent;
	}
	
	public Builder newSubMenuItem(String name) {
		Builder builder = CssMenuItem.newMenuItem(name).parent(this);
		return builder;
	}
	
	public List<CssMenuItem> getSubMenuItems() {
		return Collections.unmodifiableList(subMenuItems);
	}
	
	public boolean hasSubMenuItems() {
		return subMenuItems.size()>0;
	}

	public Link<?> getLink() {
		return link;
	}
	
	private void setLink(Link<?> link) {
		this.link = link;
	}

	public boolean isEnabled() {
		return enabled;
	}

	private void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	/**
	 * Only populated if not {@link #isEnabled() enabled}. 
	 */
	public String getDisabledReason() {
		return disabledReason;
	}
	
	public void setDisabledReason(String disabledReason) {
		this.disabledReason = disabledReason;
	}
	
	
	////////////////////////////////////////////////////////////////
	// To add submenu items
	////////////////////////////////////////////////////////////////
	
	/**
	 * @return the builder, else <tt>null</tt> if the action is not visible for the current user.
	 */
	public Builder newSubMenuItem(final ObjectAdapterMemento adapterMemento, final ObjectAction noAction,
			CssMenuLinkFactory cssMenuLinkFactory) {
	    
	    AuthenticationSession session = getAuthenticationSession();
		
		final CssMenuItem parentMenuItem = this;

		final ObjectAdapter adapter = adapterMemento.getObjectAdapter();
		Consent visibility = noAction.isVisible(session, adapter);
		if (visibility.isVetoed()) {
			return null;
		}

		final LinkAndLabel linkAndLabel = cssMenuLinkFactory.newLink(adapterMemento, noAction, PageAbstract.ID_MENU_LINK);
		
		Link<?> link = linkAndLabel.getLink();
		String actionLabel = linkAndLabel.getLabel();
		
		final Consent usability = noAction.isUsable(session, adapter);
		final String reasonDisabledIfAny = usability.getReason();
	
		return parentMenuItem.newSubMenuItem(actionLabel).link(link).enabled(reasonDisabledIfAny);
	}


	////////////////////////////////////////////////////////////////
	// Build wicket components from the menu item. 
	////////////////////////////////////////////////////////////////
	
	void addTo(MarkupContainer markupContainer) {
		
		final Component menuItemComponent = addMenuItemComponentTo(markupContainer);
		addSubMenuItemComponentsIfAnyTo(markupContainer);
		
		addCssClassAttributesIfRequired(menuItemComponent);
	}

	private Component addMenuItemComponentTo(MarkupContainer markupContainer) {
		Link<?> link = getLink();
		Label label = new Label(CssMenuItem.ID_MENU_LABEL, Model.of(this.getName()));
		
		if (this.isEnabled() && link != null) {
			// show link...
			markupContainer.add(link);
			link.add(label);
			// .. and hide label
			Components.permanentlyHide(markupContainer, CssMenuItem.ID_MENU_LABEL);
			return link;
		} else {
			// hide link...
			Components.permanentlyHide(markupContainer, ID_MENU_LINK);
			// ... and show label, along with disabled reason
			label.add(new AttributeModifier("title", true, Model.of(this.getDisabledReason())));
			markupContainer.add(label);
			
			return label;
		}
	}

	private void addSubMenuItemComponentsIfAnyTo(MarkupContainer menuItemMarkup) {
		List<CssMenuItem> subMenuItems = getSubMenuItems();
		if(subMenuItems.isEmpty()) {
			Components.permanentlyHide(menuItemMarkup, CssMenuItem.ID_SUB_MENU_ITEMS);
		} else {
			menuItemMarkup.add(new CssSubMenuItemsPanel(CssMenuItem.ID_SUB_MENU_ITEMS, subMenuItems));
		}
	}
	
	private void addCssClassAttributesIfRequired(Component linkComponent) {
		if (!hasSubMenuItems()) {
			return;
		} 
		if (this.hasParent()) {
			linkComponent.add(new CssClassAppender("parent"));
		} else {
			linkComponent.add(new CssClassAppender("top-parent"));
		}
	}

    ////////////////////////////////////////////////////////////////
    // Build wicket components from the menu item. 
    ////////////////////////////////////////////////////////////////

   protected AuthenticationSession getAuthenticationSession() {
        return ((AuthenticationSessionAccessor)Application.get()).getAuthenticationSession();
    }
	    

	
}