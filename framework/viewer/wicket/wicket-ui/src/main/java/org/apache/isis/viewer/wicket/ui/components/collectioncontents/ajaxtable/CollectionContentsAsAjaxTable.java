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

package org.apache.isis.viewer.wicket.ui.components.collectioncontents.ajaxtable;

import java.util.List;

import com.google.common.collect.Lists;

import org.apache.wicket.extensions.ajax.markup.html.repeater.data.table.AjaxFallbackDefaultDataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.model.Model;

import org.apache.isis.applib.filter.Filter;
import org.apache.isis.applib.filter.Filters;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociationFilters;
import org.apache.isis.viewer.wicket.model.common.SelectionHandler;
import org.apache.isis.viewer.wicket.model.models.EntityCollectionModel;
import org.apache.isis.viewer.wicket.ui.components.collectioncontents.ajaxtable.columns.ColumnAbstract;
import org.apache.isis.viewer.wicket.ui.components.collectioncontents.ajaxtable.columns.ObjectAdapterPropertyColumn;
import org.apache.isis.viewer.wicket.ui.components.collectioncontents.ajaxtable.columns.ObjectAdapterSelectColumn;
import org.apache.isis.viewer.wicket.ui.components.collectioncontents.ajaxtable.columns.ObjectAdapterTitleColumn;
import org.apache.isis.viewer.wicket.ui.panels.PanelAbstract;

/**
 * {@link PanelAbstract Panel} that represents a {@link EntityCollectionModel
 * collection of entity}s rendered using {@link AjaxFallbackDefaultDataTable}.
 */
public class CollectionContentsAsAjaxTable extends PanelAbstract<EntityCollectionModel> {

    private static final long serialVersionUID = 1L;
    
    @SuppressWarnings("unchecked")
    private static final Filter<ObjectAssociation> STATICALLY_VISIBLE_PROPERTIES = Filters.and(ObjectAssociationFilters.PROPERTIES, ObjectAssociationFilters.STATICALLY_VISIBLE_ASSOCIATIONS);

    public CollectionContentsAsAjaxTable(final String id, final EntityCollectionModel model) {
        super(id, model);

        buildGui();
    }

    private void buildGui() {
        final EntityCollectionModel model = getModel();

        final List<IColumn<ObjectAdapter>> columns = Lists.newArrayList();

        addTitleColumn(columns);
        addPropertyColumnsIfRequired(columns);
        addSelectedButtonIfRequired(columns);

        final SortableDataProvider<ObjectAdapter> dataProvider = new CollectionContentsSortableDataProvider(model);
        final AjaxFallbackDefaultDataTable<ObjectAdapter> dataTable = new AjaxFallbackDefaultDataTable<ObjectAdapter>("table", columns, dataProvider, 8);
        add(dataTable);
    }

    private void addTitleColumn(final List<IColumn<ObjectAdapter>> columns) {
        columns.add(new ObjectAdapterTitleColumn());
    }

    private void addPropertyColumnsIfRequired(final List<IColumn<ObjectAdapter>> columns) {
        final ObjectSpecification typeOfSpec = getModel().getTypeOfSpecification();
        if (getModel().hasSelectionHandler()) {
            return;
        }
        final List<? extends ObjectAssociation> propertyList = typeOfSpec.getAssociations(STATICALLY_VISIBLE_PROPERTIES);
        for (final ObjectAssociation property : propertyList) {
            final ColumnAbstract<ObjectAdapter> nopc = createObjectAdapterPropertyColumn(property);
            columns.add(nopc);
        }
    }

    private void addSelectedButtonIfRequired(final List<IColumn<ObjectAdapter>> columns) {
        if (!getModel().hasSelectionHandler()) {
            return;
        }
        final SelectionHandler handler = getModel().getSelectionHandler();

        columns.add(new ObjectAdapterSelectColumn(Model.of(""), handler));
    }

    private ObjectAdapterPropertyColumn createObjectAdapterPropertyColumn(final ObjectAssociation property) {
        return new ObjectAdapterPropertyColumn(Model.of(property.getName()), property.getId(), property.getId());
    }

}
