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


package org.apache.isis.extensions.wicket.ui.components.collectioncontents.ajaxtable;

import java.util.List;

import org.apache.isis.extensions.wicket.model.common.SelectionHandler;
import org.apache.isis.extensions.wicket.model.models.EntityCollectionModel;
import org.apache.isis.extensions.wicket.ui.components.collectioncontents.ajaxtable.columns.ColumnAbstract;
import org.apache.isis.extensions.wicket.ui.components.collectioncontents.ajaxtable.columns.ObjectAdapterPropertyColumn;
import org.apache.isis.extensions.wicket.ui.components.collectioncontents.ajaxtable.columns.ObjectAdapterSelectColumn;
import org.apache.isis.extensions.wicket.ui.components.collectioncontents.ajaxtable.columns.ObjectAdapterTitleColumn;
import org.apache.isis.extensions.wicket.ui.panels.PanelAbstract;
import org.apache.isis.metamodel.adapter.ObjectAdapter;
import org.apache.isis.metamodel.spec.ObjectSpecification;
import org.apache.isis.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.metamodel.spec.feature.ObjectAssociationFilters;
import org.apache.wicket.extensions.ajax.markup.html.repeater.data.table.AjaxFallbackDefaultDataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.model.Model;

import com.google.common.collect.Lists;

/**
 * {@link PanelAbstract Panel} that represents a {@link EntityCollectionModel collection of entity}s
 * rendered using {@link AjaxFallbackDefaultDataTable}.  
 */
public class CollectionContentsAsAjaxTable extends
		PanelAbstract<EntityCollectionModel> {

	private static final long serialVersionUID = 1L;

	public CollectionContentsAsAjaxTable(String id, EntityCollectionModel model) {
		super(id, model);

		buildGui();
	}

	private void buildGui() {
		EntityCollectionModel model = getModel();

		List<IColumn<ObjectAdapter>> columns = Lists.newArrayList();

		addTitleColumn(columns);
		addPropertyColumnsIfRequired(columns);
		addSelectedButtonIfRequired(columns);

		SortableDataProvider<ObjectAdapter> dataProvider = new CollectionContentsSortableDataProvider(model);
		final AjaxFallbackDefaultDataTable<ObjectAdapter> dataTable = new AjaxFallbackDefaultDataTable<ObjectAdapter>("table", columns,
				dataProvider, 8);
        add(dataTable);
	}

	private void addTitleColumn(List<IColumn<ObjectAdapter>> columns) {
		columns.add(new ObjectAdapterTitleColumn());
	}

	private void addPropertyColumnsIfRequired(List<IColumn<ObjectAdapter>> columns) {
		ObjectSpecification typeOfSpec = getModel()
				.getTypeOfSpecification();
		if (getModel().hasSelectionHandler()) {
			return;
		}
		List<? extends ObjectAssociation> propertyList = typeOfSpec
				.getAssociationList(ObjectAssociationFilters.PROPERTIES);
		for (ObjectAssociation property : propertyList) {
			ColumnAbstract<ObjectAdapter> nopc = createObjectAdapterPropertyColumn(property);
			columns.add(nopc);
		}
	}

	private void addSelectedButtonIfRequired(List<IColumn<ObjectAdapter>> columns) {
		if (!getModel().hasSelectionHandler()) {
			return;
		}
		final SelectionHandler handler = getModel().getSelectionHandler();

		columns.add(new ObjectAdapterSelectColumn(Model.of(""), handler));
	}

	private ObjectAdapterPropertyColumn createObjectAdapterPropertyColumn(
			ObjectAssociation property) {
		return new ObjectAdapterPropertyColumn(Model.of(property.getName()),
				property.getId(), property.getId());
	}

}
