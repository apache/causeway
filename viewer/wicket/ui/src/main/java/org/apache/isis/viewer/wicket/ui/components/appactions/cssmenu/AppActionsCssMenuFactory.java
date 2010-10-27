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


package org.apache.isis.viewer.wicket.ui.components.appactions.cssmenu;

import java.util.ArrayList;
import java.util.List;

import org.apache.isis.metamodel.adapter.ObjectAdapter;
import org.apache.isis.metamodel.adapter.oid.stringable.OidStringifier;
import org.apache.isis.metamodel.facets.actions.notinrepositorymenu.NotInRepositoryMenuFacet;
import org.apache.isis.metamodel.facets.naming.named.NamedFacet;
import org.apache.isis.metamodel.spec.ObjectSpecification;
import org.apache.isis.metamodel.spec.feature.ObjectAction;
import org.apache.isis.metamodel.spec.feature.ObjectActionType;
import org.apache.isis.runtime.context.IsisContext;
import org.apache.isis.viewer.wicket.model.mementos.ObjectAdapterMemento;
import org.apache.isis.viewer.wicket.model.models.ApplicationActionsModel;
import org.apache.isis.viewer.wicket.ui.ComponentFactory;
import org.apache.isis.viewer.wicket.ui.ComponentFactoryAbstract;
import org.apache.isis.viewer.wicket.ui.ComponentType;
import org.apache.isis.viewer.wicket.ui.components.widgets.cssmenu.CssMenuItem;
import org.apache.isis.viewer.wicket.ui.components.widgets.cssmenu.CssMenuLinkFactory;
import org.apache.isis.viewer.wicket.ui.components.widgets.cssmenu.CssMenuPanel;
import org.apache.isis.viewer.wicket.ui.components.widgets.cssmenu.CssMenuItem.Builder;
import org.apache.wicket.Component;
import org.apache.wicket.model.IModel;

/**
 * {@link ComponentFactory} for a {@link CssMenuPanel} to represent the {@link ApplicationActionsModel application action}s.
 */
public class AppActionsCssMenuFactory extends ComponentFactoryAbstract {

	private static final long serialVersionUID = 1L;
	private final CssMenuLinkFactory cssMenuLinkFactory = new AppActionsCssMenuLinkFactory();

	public AppActionsCssMenuFactory() {
		super(ComponentType.APPLICATION_ACTIONS);
	}

	/**
	 * Generic, so applies to all models.
	 */
	@Override
	protected ApplicationAdvice appliesTo(IModel<?> model) {
		return appliesIf(model instanceof ApplicationActionsModel);
	}
	
	public Component createComponent(String id, IModel<?> model) {
		ApplicationActionsModel applicationActionsModel = (ApplicationActionsModel) model;
		return new CssMenuPanel(id, CssMenuPanel.Style.REGULAR, buildMenu(applicationActionsModel));
	}
	

	private List<CssMenuItem> buildMenu(ApplicationActionsModel cssModel) {

		List<ObjectAdapter> serviceAdapters = cssModel.getObject();
		List<CssMenuItem> menuItems = new ArrayList<CssMenuItem>();
		for (ObjectAdapter serviceAdapter : serviceAdapters) {
			addMenuItemsIfVisible(menuItems, serviceAdapter);
		}
		
		//addPlaytimeMenu(menuItems);

		return menuItems;
	}

	private void addMenuItemsIfVisible(List<CssMenuItem> menuItems,
			ObjectAdapter serviceAdapter) {
		ObjectSpecification serviceSpec = serviceAdapter
				.getSpecification();
		if (serviceSpec.isHidden()) {
		    return;
		}
		final ObjectAdapterMemento serviceAdapterMemento = ObjectAdapterMemento.createOrNull(serviceAdapter);
		String serviceName = serviceSpec.getFacet(NamedFacet.class).value();
		CssMenuItem serviceMenuItem = CssMenuItem.newMenuItem(serviceName)
				.build();

		addActionSubMenuItems(serviceAdapterMemento, serviceMenuItem);
		if (serviceMenuItem.hasSubMenuItems()) {
			menuItems.add(serviceMenuItem);
		}
	}

//	@SuppressWarnings("unused")
//	private void addPlaytimeMenu(List<CssMenuItem> applicationActionMenuItems) {
//		CssMenuItem menuItem = CssMenuItem.newMenuItem("Playtime")
//		.build();
//		applicationActionMenuItems.add(menuItem);
//		menuItem.newSubMenuItem("Css").link(CssPlaytimePage.class).build();
//	}

	private void addActionSubMenuItems(final ObjectAdapterMemento serviceAdapterMemento,
			CssMenuItem serviceMenuItem) {

		ObjectSpecification serviceSpec = serviceAdapterMemento.getObjectAdapter()
				.getSpecification();

		for (final ObjectAction noAction : serviceSpec.getObjectActions(ObjectActionType.USER)) {

		    // skip if annotated to not be included in repository menu
		    if (noAction.getFacet(NotInRepositoryMenuFacet.class) != null) {
		        continue;
		    }
			final Builder subMenuItemBuilder = serviceMenuItem.newSubMenuItem(serviceAdapterMemento, noAction, getLinkFactory());
			if(subMenuItemBuilder!=null) {
			    // not visible
			    subMenuItemBuilder.build();
			}
		}
	}

	private CssMenuLinkFactory getLinkFactory() {
		return cssMenuLinkFactory;
	}

	static OidStringifier getOidStringifier() {
		return IsisContext.getPersistenceSession().getOidGenerator().getOidStringifier();
	}
	
}
