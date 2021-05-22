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
import java.util.Optional;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import org.apache.wicket.Component;
import org.apache.wicket.extensions.ajax.markup.html.repeater.data.table.AjaxFallbackDefaultDataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.model.Model;

import org.apache.isis.applib.annotation.Where;
import org.apache.isis.applib.services.tablecol.TableColumnOrderService;
import org.apache.isis.applib.services.tablecol.TableColumnVisibilityService;
import org.apache.isis.commons.internal.base._NullSafe;
import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.commons.internal.collections._Maps;
import org.apache.isis.commons.internal.functions._Predicates;
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
import org.apache.isis.viewer.wicket.ui.components.collectioncontents.ajaxtable.columns.ObjectAdapterPropertyColumn;
import org.apache.isis.viewer.wicket.ui.components.collectioncontents.ajaxtable.columns.ObjectAdapterTitleColumn;
import org.apache.isis.viewer.wicket.ui.components.collectioncontents.ajaxtable.columns.ObjectAdapterToggleboxColumn;
import org.apache.isis.viewer.wicket.ui.panels.PanelAbstract;

import static org.apache.isis.commons.internal.base._With.mapIfPresentElse;

import lombok.NonNull;
import lombok.val;

/**
 * {@link PanelAbstract Panel} that represents a {@link EntityCollectionModel
 * collection of entity}s rendered using {@link AjaxFallbackDefaultDataTable}.
 */
public class CollectionContentsAsAjaxTablePanel
extends PanelAbstract<List<ManagedObject>, EntityCollectionModel>
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

        }

        val collectionModel = getModel();
        addTitleColumn(
                columns,
                collectionModel.parentedObjectAdapterMemento().orElse(null),
                getWicketViewerSettings().getMaxTitleLengthInParentedTables(),
                getWicketViewerSettings().getMaxTitleLengthInStandaloneTables());

        addPropertyColumnsIfRequired(columns);

        val dataProvider = new CollectionContentsSortableDataProvider(collectionModel);
        dataTable = new IsisAjaxFallbackDataTable<>(
                ID_TABLE, columns, dataProvider, collectionModel.getPageSize(), toggleboxColumn);
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

        val collectionModel = getModel();
        val elementTypeSpec = collectionModel.getTypeOfSpecification();
        if(elementTypeSpec == null) {
            return;
        }

        // the type that has the properties that make up this table's columns
        val elementType = elementTypeSpec.getCorrespondingClass();

        val whereContext = collectionModel.isParented()
                    ? Where.PARENTED_TABLES
                    : Where.STANDALONE_TABLES;

        val parentSpecIfAny =  collectionModel.parentedParentObjectSpecification()
                .orElse(null);

        val propertyById = _Maps.<String, ObjectAssociation>newLinkedHashMap();

        elementTypeSpec.streamProperties(MixedIn.INCLUDED)
        .filter(property->property.streamFacets()
                    .filter(facet -> facet instanceof HiddenFacet)
                    .map(WhereValueFacet.class::cast)
                    .map(WhereValueFacet::where)
                    .noneMatch(where -> where.includes(whereContext)))
        .filter(associationDoesNotReferenceParent(parentSpecIfAny))
        .filter(property->filterColumnsUsingSpi(property, elementType)) // optional SPI to filter columns
        .forEach(property->propertyById.put(property.getId(), property));

        val propertyIdsInOrder = _Lists.<String>newArrayList(propertyById.keySet());

        // sort by order of occurrence within associated layout, if any
        propertyIdComparator(elementTypeSpec)
        .ifPresent(propertyIdsInOrder::sort);

        // optional SPI to reorder columns
        sortColumnsUsingSpi(propertyIdsInOrder, elementType);

        // add all ordered columns to the table
        propertyIdsInOrder.stream()
        .map(propertyById::get)
        .filter(_NullSafe::isPresent)
        .map(this::createObjectAdapterPropertyColumn)
        .forEach(columns::add);

    }

    // comparator based on grid facet, that is by order of occurrence within associated layout
    private Optional<Comparator<String>> propertyIdComparator(
            final @NonNull ObjectSpecification elementTypeSpec) {

        // same code also appears in EntityPage.
        // we need to do this here otherwise any tables will render the columns in the wrong order until at least
        // one object of that type has been rendered via EntityPage.
        val elementTypeGridFacet = elementTypeSpec.getFacet(GridFacet.class);

        if(elementTypeGridFacet == null) {
            return Optional.empty();
        }

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
        return Optional.of(Comparator
                .<String>comparingInt(propertyId->
                propertyIdOrderWithinGrid.getOrDefault(propertyId, Integer.MAX_VALUE))
                .thenComparing(Comparator.naturalOrder()));
    }

    private boolean filterColumnsUsingSpi(
            final ObjectAssociation property,
            final Class<?> elementType) {
        return getServiceRegistry()
                .select(TableColumnVisibilityService.class)
                .stream()
                .noneMatch(x -> x.hides(elementType, property.getId()));
    }

    private void sortColumnsUsingSpi(
            final List<String> propertyIdsInOrder,
            final Class<?> elementType) {

        val tableColumnOrderServices = getServiceRegistry().select(TableColumnOrderService.class);
        if(tableColumnOrderServices.isEmpty()) {
            return;
        }

        val collectionModel = getModel();

        final Optional<ManagedObject> parentObject = collectionModel.parentedParentObject();

        tableColumnOrderServices.stream()
        .map(tableColumnOrderService->
            parentObject.isPresent()
                ? tableColumnOrderService.orderParented(
                        parentObject.get().getPojo(),
                        collectionModel.getIdentifier().getMemberName(),
                        elementType,
                        propertyIdsInOrder)

                : tableColumnOrderService.orderStandalone(
                        elementType,
                        propertyIdsInOrder)

                )
        .filter(_NullSafe::isPresent)
        .findFirst()
        .filter(propertyReorderedIds->propertyReorderedIds!=propertyIdsInOrder) // skip if its the same object
        .ifPresent(propertyReorderedIds->{
            propertyIdsInOrder.clear();
            propertyIdsInOrder.addAll(propertyReorderedIds);
        });

    }

    static Predicate<ObjectAssociation> associationDoesNotReferenceParent(
            final @Nullable ObjectSpecification parentSpec) {
        if(parentSpec == null) {
            return _Predicates.alwaysTrue();
        }
        return (ObjectAssociation property) -> {
                val hiddenFacet = property.getFacet(HiddenFacet.class);
                if(hiddenFacet == null) {
                    return true;
                }
                if (hiddenFacet.where() != Where.REFERENCES_PARENT) {
                    return true;
                }
                val propertySpec = property.getSpecification();
                final boolean propertySpecIsOfParentSpec = parentSpec.isOfType(propertySpec);
                final boolean isVisible = !propertySpecIsOfParentSpec;
                return isVisible;
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
