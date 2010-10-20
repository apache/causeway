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


package org.apache.isis.extensions.wicket.ui.components.collectioncontents.ajaxtable.columns;

import org.apache.isis.extensions.wicket.model.mementos.PropertyMemento;
import org.apache.isis.extensions.wicket.model.models.EntityModel;
import org.apache.isis.extensions.wicket.model.models.ScalarModel;
import org.apache.isis.extensions.wicket.ui.ComponentFactory;
import org.apache.isis.extensions.wicket.ui.ComponentType;
import org.apache.isis.extensions.wicket.ui.app.registry.ComponentFactoryRegistry;
import org.apache.isis.extensions.wicket.ui.components.collectioncontents.ajaxtable.CollectionContentsAsAjaxTable;
import org.apache.isis.extensions.wicket.ui.components.scalars.ScalarPanelAbstract;
import org.apache.isis.extensions.wicket.ui.components.scalars.ScalarPanelAbstract.Format;
import org.apache.isis.metamodel.adapter.ObjectAdapter;
import org.apache.isis.metamodel.spec.feature.OneToOneAssociation;
import org.apache.wicket.Component;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;

/**
 * A {@link ColumnAbstract column} within a {@link CollectionContentsAsAjaxTable}
 * representing a single property of the provided {@link ObjectAdapter}.
 * 
 * <p>
 * Looks up the {@link ComponentFactory} to render the property from the
 * {@link ComponentFactoryRegistry}. 
 */
public final class ObjectAdapterPropertyColumn extends ColumnAbstract<ObjectAdapter> {
	
	private static final long serialVersionUID = 1L;

	@SuppressWarnings("unused")
	private String sortProperty;
	private String propertyExpression;

	public ObjectAdapterPropertyColumn(IModel<String> columnNameModel, String sortProperty,
			String propertyName) {
		super(columnNameModel);
		this.sortProperty = sortProperty;
		this.propertyExpression = propertyName;
	}


	@Override
	public void populateItem(Item<ICellPopulator<ObjectAdapter>> cellItem,
			String componentId, IModel<ObjectAdapter> rowModel) {
		Component component = createComponent(componentId, rowModel);
		cellItem.add(component);
	}

	private Component createComponent(String id,
			IModel<ObjectAdapter> rowModel) {
		
		ObjectAdapter adapter = rowModel.getObject();
		EntityModel model = new EntityModel(adapter);
		OneToOneAssociation property = 
			(OneToOneAssociation) adapter.getSpecification().getAssociation(propertyExpression);
		PropertyMemento pm = new PropertyMemento(property);
		ScalarModel scalarModel = model.getPropertyModel(pm);
		
		ComponentFactory componentFactory = findComponentFactory(ComponentType.SCALAR_NAME_AND_VALUE, scalarModel);
		Component component = componentFactory.createComponent(id, scalarModel);
		if (component instanceof ScalarPanelAbstract) {
			ScalarPanelAbstract scalarPanel = (ScalarPanelAbstract) component;
			scalarPanel.setFormat(Format.COMPACT);
			scalarModel.toViewMode();
		}
		return component;
	}

}