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

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.apache.wicket.Component;
import org.apache.wicket.extensions.ajax.markup.html.repeater.data.table.AjaxFallbackDefaultDataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.model.Model;

import org.apache.isis.applib.annotation.Where;
import org.apache.isis.applib.services.tablecol.TableColumnOrderService;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.commons.internal.collections._Maps;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facets.WhereValueFacet;
import org.apache.isis.core.metamodel.facets.all.describedas.DescribedAsFacet;
import org.apache.isis.core.metamodel.facets.all.hide.HiddenFacet;
import org.apache.isis.core.metamodel.facets.all.named.NamedFacet;
import org.apache.isis.core.metamodel.facets.object.grid.GridFacet;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.MixedIn;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.core.runtime.memento.ObjectMemento;
import org.apache.isis.viewer.wicket.model.models.EntityCollectionModel;
import org.apache.isis.viewer.wicket.ui.components.collection.bulk.BulkActionsProvider;
import org.apache.isis.viewer.wicket.ui.components.collection.count.CollectionCountProvider;
import org.apache.isis.viewer.wicket.ui.components.collectioncontents.ajaxtable.columns.ColumnAbstract;
import org.apache.isis.viewer.wicket.ui.components.collectioncontents.ajaxtable.columns.ObjectAdapterPropertyColumn;
import org.apache.isis.viewer.wicket.ui.components.collectioncontents.ajaxtable.columns.ObjectAdapterTitleColumn;
import org.apache.isis.viewer.wicket.ui.components.collectioncontents.ajaxtable.columns.ObjectAdapterToggleboxColumn;
import org.apache.isis.viewer.wicket.ui.panels.PanelAbstract;

import static org.apache.isis.commons.internal.base._With.mapIfPresentElse;

import lombok.val;

/**
 * {@link PanelAbstract Panel} that represents a {@link EntityCollectionModel
 * collection of entity}s rendered using {@link AjaxFallbackDefaultDataTable}.
 */
public class CollectionContentsAsAjaxTablePanel
extends PanelAbstract<EntityCollectionModel> 
implements CollectionCountProvider {

    private static final long serialVersionUID = 1L;

    private static final String ID_TABLE = "table";

    private IsisAjaxFallbackDataTable<ManagedObject, String> dataTable;


    public CollectionContentsAsAjaxTablePanel(final String id, final EntityCollectionModel model) {
        super(id, model);
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();
        buildGui();
    }

    private void buildGui() {

        final List<IColumn<ManagedObject, String>> columns = _Lists.newArrayList();

        // bulk actions
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
        addTitleColumn(
                columns, 
                model.getParentObjectAdapterMemento(), 
                getWicketViewerSettings().getMaxTitleLengthInParentedTables(), 
                getWicketViewerSettings().getMaxTitleLengthInStandaloneTables());

        addPropertyColumnsIfRequired(columns);

        val dataProvider = new CollectionContentsSortableDataProvider(model);
        dataTable = new IsisAjaxFallbackDataTable<>(ID_TABLE, columns, dataProvider, model.getPageSize(), toggleboxColumn);
        addOrReplace(dataTable);

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
            final List<IColumn<ManagedObject, String>> columns,
            final ObjectMemento parentAdapterMementoIfAny,
            final int maxTitleParented,
            final int maxTitleStandalone) {
        
        final int maxTitleLength = getModel().isParented()? maxTitleParented: maxTitleStandalone;
        columns.add(new ObjectAdapterTitleColumn(
                super.getCommonContext(), parentAdapterMementoIfAny, maxTitleLength));
    }

    private void addPropertyColumnsIfRequired(final List<IColumn<ManagedObject, String>> columns) {
        final ObjectSpecification typeOfSpec = getModel().getTypeOfSpecification();
        final Comparator<String> propertyIdComparator;

        // same code also appears in EntityPage.
        // we need to do this here otherwise any tables will render the columns in the wrong order until at least
        // one object of that type has been rendered via EntityPage.
        val elementTypeGridFacet = typeOfSpec.getFacet(GridFacet.class);
        if(elementTypeGridFacet != null) {
            // the facet should always exist, in fact
            // just enough to ask for the metadata.
            
            // don't pass in any object, just need the meta-data
            val elementTypeGrid = elementTypeGridFacet.getGrid(null);  

            final Map<String, Integer> propertyIdOrderWithinGrid = new HashMap<>();
            elementTypeGrid.getAllPropertiesById().forEach((propertyId, __)->{
                propertyIdOrderWithinGrid.put(propertyId, propertyIdOrderWithinGrid.size());
            });

            // if propertyId is mentioned within grid, put into first 'half' ordered by 
            // occurrence within grid
            // if propertyId is not mentioned within grid, put into second 'half' ordered by
            // propertyId (String) in natural order
            propertyIdComparator = Comparator
                    .<String>comparingInt(propertyId->
                    propertyIdOrderWithinGrid.getOrDefault(propertyId, Integer.MAX_VALUE))
                    .thenComparing(Comparator.naturalOrder());


        } else {
            propertyIdComparator = null;
        }

        final Where whereContext =
                getModel().isParented()
                ? Where.PARENTED_TABLES
                        : Where.STANDALONE_TABLES;

        final ObjectSpecification parentSpecIfAny =
                getModel().isParented()
                    ? getCommonContext().reconstructObject(getModel().getParentObjectAdapterMemento())
                            .getSpecification()
                        : null;

        final Predicate<ObjectAssociation> predicate = ObjectAssociation.Predicates.PROPERTIES
                .and((final ObjectAssociation association)->{
                    final Stream<Facet> facets = association.streamFacets()
                            .filter((final Facet facet)->
                            facet instanceof WhereValueFacet && facet instanceof HiddenFacet);
                    return !facets
                            .map(facet->(WhereValueFacet) facet)
                            .anyMatch(wawF->wawF.where().includes(whereContext));

                })
                .and(associationDoesNotReferenceParent(parentSpecIfAny));

        final Stream<? extends ObjectAssociation> propertyList = 
                typeOfSpec.streamAssociations(MixedIn.INCLUDED)
                .filter(predicate);

        final Map<String, ObjectAssociation> propertyById = _Maps.newLinkedHashMap();
        propertyList.forEach(property->
        propertyById.put(property.getId(), property));

        List<String> propertyIds = _Lists.newArrayList(propertyById.keySet());

        if(propertyIdComparator!=null) {
            propertyIds.sort(propertyIdComparator);   
        }

        // optional SPI to reorder
        final Can<TableColumnOrderService> tableColumnOrderServices =
                getServiceRegistry().select(TableColumnOrderService.class);

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
                final ColumnAbstract<ManagedObject> nopc = createObjectAdapterPropertyColumn(property);
                columns.add(nopc);
            }
        }
    }

    private List<String> reordered(
            final TableColumnOrderService tableColumnOrderService,
            final List<String> propertyIds) {

        final Class<?> collectionType = getModel().getTypeOfSpecification().getCorrespondingClass();

        final ObjectMemento parentObjectAdapterMemento = getModel().getParentObjectAdapterMemento();
        if(parentObjectAdapterMemento != null) {
            val parentObjectAdapter = getCommonContext().reconstructObject(parentObjectAdapterMemento);
            final Object parent = parentObjectAdapter.getPojo();
            final String collectionId = getModel().getCollectionMemento().getId();

            return tableColumnOrderService.orderParented(parent, collectionId, collectionType, propertyIds);
        } else {
            return tableColumnOrderService.orderStandalone(collectionType, propertyIds);
        }
    }

    static Predicate<ObjectAssociation> associationDoesNotReferenceParent(final ObjectSpecification parentSpec) {
        if(parentSpec == null) {
            return __->true;
        }
        return new Predicate<ObjectAssociation>() {
            @Override
            public boolean test(ObjectAssociation association) {
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

        final String parentTypeName = property.getOnType().getLogicalTypeName();
        final String describedAs = mapIfPresentElse(property.getFacet(DescribedAsFacet.class), 
                DescribedAsFacet::value, null);

        val commonContext = super.getCommonContext();
        
        return new ObjectAdapterPropertyColumn(
                commonContext,
                getModel().getVariant(), 
                Model.of(property.getName()), 
                property.getId(), 
                property.getId(), 
                escaped, 
                parentTypeName,
                describedAs);
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
