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
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.ajax.markup.html.repeater.data.table.AjaxFallbackDefaultDataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.model.Model;

import org.apache.isis.applib.annotation.Where;
import org.apache.isis.applib.filter.Filter;
import org.apache.isis.applib.filter.Filters;
import org.apache.isis.applib.layout.grid.Grid;
import org.apache.isis.applib.services.tablecol.TableColumnOrderService;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.mgr.AdapterManager.ConcurrencyChecking;
import org.apache.isis.core.metamodel.adapter.version.ConcurrencyException;
import org.apache.isis.core.metamodel.facets.all.hide.HiddenFacet;
import org.apache.isis.core.metamodel.facets.all.named.NamedFacet;
import org.apache.isis.core.metamodel.facets.object.grid.GridFacet;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.Contributed;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.viewer.wicket.model.common.OnConcurrencyExceptionHandler;
import org.apache.isis.viewer.wicket.model.mementos.ObjectAdapterMemento;
import org.apache.isis.viewer.wicket.model.models.EntityCollectionModel;
import org.apache.isis.viewer.wicket.model.models.EntityModel;
import org.apache.isis.viewer.wicket.ui.components.collection.bulk.BulkActionsProvider;
import org.apache.isis.viewer.wicket.ui.components.collection.count.CollectionCountProvider;
import org.apache.isis.viewer.wicket.ui.components.collectioncontents.ajaxtable.columns.ColumnAbstract;
import org.apache.isis.viewer.wicket.ui.components.collectioncontents.ajaxtable.columns.ObjectAdapterPropertyColumn;
import org.apache.isis.viewer.wicket.ui.components.collectioncontents.ajaxtable.columns.ObjectAdapterTitleColumn;
import org.apache.isis.viewer.wicket.ui.components.collectioncontents.ajaxtable.columns.ObjectAdapterToggleboxColumn;
import org.apache.isis.viewer.wicket.ui.panels.PanelAbstract;

/**
 * {@link PanelAbstract Panel} that represents a {@link EntityCollectionModel
 * collection of entity}s rendered using {@link AjaxFallbackDefaultDataTable}.
 */
public class CollectionContentsAsAjaxTablePanel
        extends PanelAbstract<EntityCollectionModel> implements CollectionCountProvider {

    private static final long serialVersionUID = 1L;

    private static final String ID_TABLE = "table";

    private IsisAjaxFallbackDataTable<ObjectAdapter,String> dataTable;


    public CollectionContentsAsAjaxTablePanel(final String id, final EntityCollectionModel model) {
        super(id, model);
    }
    
    @Override
    protected void onInitialize() {
        super.onInitialize();
        buildGui();
    }

    private void buildGui() {

        final List<IColumn<ObjectAdapter,String>> columns = Lists.newArrayList();

        // bulkactions
        final BulkActionsProvider bulkActionsProvider = getBulkActionsProvider();

        ObjectAdapterToggleboxColumn toggleboxColumn = null;
        if(bulkActionsProvider != null) {

            toggleboxColumn = bulkActionsProvider.getToggleboxColumn();
            if(toggleboxColumn != null) {
                columns.add(toggleboxColumn);
            }
            bulkActionsProvider.configureBulkActions(toggleboxColumn);
        }

        final EntityCollectionModel model = getModel();
        addTitleColumn(columns, model.getParentObjectAdapterMemento(), getSettings().getMaxTitleLengthInStandaloneTables(), getSettings().getMaxTitleLengthInStandaloneTables());
        addPropertyColumnsIfRequired(columns);

        final CollectionContentsSortableDataProvider dataProvider = new CollectionContentsSortableDataProvider(model);
        dataTable = new IsisAjaxFallbackDataTable<>(ID_TABLE, columns, dataProvider, model.getPageSize(), toggleboxColumn);
        addOrReplace(dataTable);
        dataTable.honourHints();

        if(toggleboxColumn != null) {
            final OnConcurrencyExceptionHandler handler2 = new OnConcurrencyExceptionHandler() {

                private static final long serialVersionUID = 1L;

                @Override
                public void onConcurrencyException(
                        final Component context,
                        final ObjectAdapter selectedAdapter,
                        final ConcurrencyException ex,
                        final AjaxRequestTarget ajaxRequestTarget) {

                    // this causes the row to be repainted
                    // but it isn't possible (yet) to raise any warning
                    // because that only gets flushed on page refresh.
                    //

                    // perhaps something to tackle in a separate ticket....
                    ajaxRequestTarget.add(dataTable);

                    // hmm... just reading this;
                    // could perhaps use ajaxRequestTarget.addJavaScript(JGrowlUtils....)
                }
            };
            toggleboxColumn.setOnConcurrencyExceptionHandler(handler2);
        }
    }

    private BulkActionsProvider getBulkActionsProvider() {
        Component component = this;
        while(component != null) {
            if(component instanceof BulkActionsProvider) {
                return (BulkActionsProvider) component;
            }
            component = component.getParent();
        }
        return null;
    }



    private void addTitleColumn(
            final List<IColumn<ObjectAdapter,String>> columns,
            final ObjectAdapterMemento parentAdapterMementoIfAny,
            final int maxTitleParented,
            final int maxTitleStandalone) {
        final int maxTitleLength = getModel().isParented()? maxTitleParented: maxTitleStandalone;
        columns.add(new ObjectAdapterTitleColumn(parentAdapterMementoIfAny, maxTitleLength));
    }

    private void addPropertyColumnsIfRequired(final List<IColumn<ObjectAdapter,String>> columns) {
        final ObjectSpecification typeOfSpec = getModel().getTypeOfSpecification();

        // same code also appears in EntityPage.
        // we need to do this here otherwise any tables will render the columns in the wrong order until at least
        // one object of that type has been rendered via EntityPage.
        final GridFacet gridFacet = typeOfSpec.getFacet(GridFacet.class);
        if(gridFacet != null) {
            // the facet should always exist, in fact
            // just enough to ask for the metadata.
            // This will cause the current ObjectSpec to be updated as a side effect.
            final EntityModel entityModel = getModel().getEntityModel();
            final ObjectAdapter objectAdapterIfAny = entityModel != null ? entityModel.getObject() : null;
            final Grid unused = gridFacet.getGrid(objectAdapterIfAny);
        }

        final Where whereContext =
                getModel().isParented()
                    ? Where.PARENTED_TABLES
                    : Where.STANDALONE_TABLES;
        
        final ObjectSpecification parentSpecIfAny = 
                getModel().isParented() 
                    ? getModel().getParentObjectAdapterMemento().getObjectAdapter(ConcurrencyChecking.NO_CHECK,
                        getPersistenceSession(), getSpecificationLoader()).getSpecification()
                    : null;
        
        @SuppressWarnings("unchecked")
        final Filter<ObjectAssociation> filter = Filters.and(
                ObjectAssociation.Filters.PROPERTIES, 
                ObjectAssociation.Filters.staticallyVisible(whereContext),
                associationDoesNotReferenceParent(parentSpecIfAny));
        
        final List<? extends ObjectAssociation> propertyList = typeOfSpec.getAssociations(Contributed.INCLUDED, filter);
        final Map<String, ObjectAssociation> propertyById = Maps.newLinkedHashMap();
        for (final ObjectAssociation property : propertyList) {
            propertyById.put(property.getId(), property);
        }
        List<String> propertyIds = Lists.newArrayList(propertyById.keySet());

        // optional SPI to reorder
        final List<TableColumnOrderService> tableColumnOrderServices =
                getServicesInjector().lookupServices(TableColumnOrderService.class);

        for (final TableColumnOrderService tableColumnOrderService : tableColumnOrderServices) {
            final List<String> propertyReorderedIds = reordered(tableColumnOrderService, propertyIds);
            if(propertyReorderedIds != null) {
                propertyIds = propertyReorderedIds;
                break;
            }
        }

        for (final String propertyId : propertyIds) {
            final ObjectAssociation property = propertyById.get(propertyId);
            if(property != null) {
                final ColumnAbstract<ObjectAdapter> nopc = createObjectAdapterPropertyColumn(property);
                columns.add(nopc);
            }
        }
    }

    private List<String> reordered(
            final TableColumnOrderService tableColumnOrderService,
            final List<String> propertyIds) {

        final Class<?> collectionType = getModel().getTypeOfSpecification().getCorrespondingClass();

        final ObjectAdapterMemento parentObjectAdapterMemento = getModel().getParentObjectAdapterMemento();
        if(parentObjectAdapterMemento != null) {
            final ObjectAdapter parentObjectAdapter = parentObjectAdapterMemento
                    .getObjectAdapter(ConcurrencyChecking.NO_CHECK, getPersistenceSession(), getSpecificationLoader());
            final Object parent = parentObjectAdapter.getObject();
            final String collectionId = getModel().getCollectionMemento().getId();

            return tableColumnOrderService.orderParented(parent, collectionId, collectionType, propertyIds);
        } else {
            return tableColumnOrderService.orderStandalone(collectionType, propertyIds);
        }
    }

    static Filter<ObjectAssociation> associationDoesNotReferenceParent(final ObjectSpecification parentSpec) {
        if(parentSpec == null) {
            return Filters.any();
        }
        return new Filter<ObjectAssociation>() {
            @Override
            public boolean accept(ObjectAssociation association) {
                final HiddenFacet facet = association.getFacet(HiddenFacet.class);
                if(facet == null) {
                    return true;
                }
                if (facet.where() != Where.REFERENCES_PARENT) {
                    return true;
                }
                final ObjectSpecification assocSpec = association.getSpecification();
                final boolean associationSpecIsOfParentSpec = parentSpec.isOfType(assocSpec);
                final boolean isVisible = !associationSpecIsOfParentSpec;
                return isVisible;
            }
        };
    }

    private ObjectAdapterPropertyColumn createObjectAdapterPropertyColumn(final ObjectAssociation property) {

        final NamedFacet facet = property.getFacet(NamedFacet.class);
        final boolean escaped = facet == null || facet.escaped();

        final String parentTypeName = property.getOnType().getSpecId().asString();
        return new ObjectAdapterPropertyColumn(getModel().getType(), Model.of(property.getName()), property.getId(), property.getId(), escaped, parentTypeName);
    }



    @Override
    protected void onModelChanged() {
        buildGui();
    }

    @Override
    public Integer getCount() {
        final EntityCollectionModel model = getModel();
        return model.getCount();
    }



}
