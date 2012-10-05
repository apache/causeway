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

import org.apache.wicket.Component;
import org.apache.wicket.extensions.ajax.markup.html.repeater.data.table.AjaxFallbackDefaultDataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.model.Model;

import org.apache.isis.applib.annotation.Where;
import org.apache.isis.applib.filter.Filter;
import org.apache.isis.applib.filter.Filters;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.spec.ActionType;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.ObjectActionContainer.Contributed;
import org.apache.isis.core.metamodel.spec.feature.ObjectActionFilters;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociationFilters;
import org.apache.isis.viewer.wicket.model.common.SelectionHandler;
import org.apache.isis.viewer.wicket.model.models.EntityCollectionModel;
import org.apache.isis.viewer.wicket.ui.components.collectioncontents.ajaxtable.columns.ColumnAbstract;
import org.apache.isis.viewer.wicket.ui.components.collectioncontents.ajaxtable.columns.ObjectAdapterPropertyColumn;
import org.apache.isis.viewer.wicket.ui.components.collectioncontents.ajaxtable.columns.ObjectAdapterSelectColumn;
import org.apache.isis.viewer.wicket.ui.components.collectioncontents.ajaxtable.columns.ObjectAdapterTitleColumn;
import org.apache.isis.viewer.wicket.ui.components.collectioncontents.ajaxtable.columns.ObjectAdapterToggleboxColumn;
import org.apache.isis.viewer.wicket.ui.components.widgets.cssmenu.CssMenuBuilder;
import org.apache.isis.viewer.wicket.ui.components.widgets.cssmenu.CssMenuLinkFactory;
import org.apache.isis.viewer.wicket.ui.components.widgets.cssmenu.CssMenuPanel;
import org.apache.isis.viewer.wicket.ui.panels.PanelAbstract;

/**
 * {@link PanelAbstract Panel} that represents a {@link EntityCollectionModel
 * collection of entity}s rendered using {@link AjaxFallbackDefaultDataTable}.
 */
public class CollectionContentsAsAjaxTable extends PanelAbstract<EntityCollectionModel> {

    private static final long serialVersionUID = 1L;

    private static final String ID_TABLE = "table";
    private static final String ID_ENTITY_ACTIONS = "entityActions";

    private DataTable<ObjectAdapter,String> dataTable;

    public CollectionContentsAsAjaxTable(final String id, final EntityCollectionModel model) {
        super(id, model);

        buildGui();
    }

    private void buildGui() {
        final EntityCollectionModel model = getModel();

        buildEntityActionsGui();
        
        final List<IColumn<ObjectAdapter,String>> columns = Lists.newArrayList();

        addToggleboxColumnIfRequired(columns);
        addTitleColumn(columns);
        addPropertyColumnsIfRequired(columns);
        addSelectedButtonIfRequired(columns);

        final SortableDataProvider<ObjectAdapter,String> dataProvider = new CollectionContentsSortableDataProvider(model);
        dataTable = new MyAjaxFallbackDefaultDataTable<ObjectAdapter,String>(ID_TABLE, columns, dataProvider, model.getPageSize());
        
        add(dataTable);
    }
    
    private void addToggleboxColumnIfRequired(final List<IColumn<ObjectAdapter,String>> columns) {
        final EntityCollectionModel model = getModel();
        
        if(model.isStandalone()) {
            columns.add(new ObjectAdapterToggleboxColumn(new SelectionHandler() {
                
                private static final long serialVersionUID = 1L;

                @Override
                public void onSelected(final Component context, final ObjectAdapter selectedAdapter) {
                    model.toggleSelectionOn(selectedAdapter);
                }
            }));
        }
    }

    private void buildEntityActionsGui() {
        final EntityCollectionModel model = getModel();
        
        if(model.isParented()) {
            permanentlyHide(ID_ENTITY_ACTIONS);
            return;
        }
        
        final ObjectSpecification typeSpec = model.getTypeOfSpecification();
        
        @SuppressWarnings("unchecked")
        final List<ObjectAction> userActions = typeSpec.getObjectActions(ActionType.USER, Contributed.INCLUDED, 
                Filters.and(
                        ObjectActionFilters.withNoBusinessRules(), ObjectActionFilters.contributedAnd1ParamAndVoid()
                ));

        if(!userActions.isEmpty()) {
            final CssMenuLinkFactory linkFactory = new BulkCollectionsLinkFactory(model, dataTable);

            final CssMenuBuilder cssMenuBuilder = new CssMenuBuilder(null, getServiceAdapters(), userActions, linkFactory);
            // TODO: i18n
            final CssMenuPanel cssMenuPanel = cssMenuBuilder.buildPanel(ID_ENTITY_ACTIONS, "Actions");

            this.addOrReplace(cssMenuPanel);
        } else {
            permanentlyHide(ID_ENTITY_ACTIONS);
        }
    }


    private static void addTitleColumn(final List<IColumn<ObjectAdapter,String>> columns) {
        columns.add(new ObjectAdapterTitleColumn());
    }

    private void addPropertyColumnsIfRequired(final List<IColumn<ObjectAdapter,String>> columns) {
        final ObjectSpecification typeOfSpec = getModel().getTypeOfSpecification();
        if (getModel().hasSelectionHandler()) {
            return;
        }
        
        @SuppressWarnings("unchecked")
        final Filter<ObjectAssociation> filter = Filters.and(
                ObjectAssociationFilters.PROPERTIES, 
                ObjectAssociationFilters.staticallyVisible(getModel().isParented()? Where.PARENTED_TABLES: Where.STANDALONE_TABLES));
        final List<? extends ObjectAssociation> propertyList = typeOfSpec.getAssociations(filter);
        for (final ObjectAssociation property : propertyList) {
            final ColumnAbstract<ObjectAdapter> nopc = createObjectAdapterPropertyColumn(property);
            columns.add(nopc);
        }
    }

    private void addSelectedButtonIfRequired(final List<IColumn<ObjectAdapter,String>> columns) {
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
