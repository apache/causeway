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
package org.apache.causeway.viewer.wicket.ui.components.collectioncontents.ajaxtable;

import java.util.Iterator;
import java.util.Optional;
import java.util.OptionalInt;

import org.apache.wicket.extensions.ajax.markup.html.repeater.data.table.AjaxFallbackDefaultDataTable;
import org.apache.wicket.extensions.markup.html.repeater.util.SortParam;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.model.IModel;

import org.springframework.lang.Nullable;

import org.apache.causeway.applib.annotation.TableDecorator;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.core.metamodel.facets.object.tabledec.TableDecoratorFacet;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.object.ManagedObjects;
import org.apache.causeway.core.metamodel.object.MmSortUtils;
import org.apache.causeway.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.causeway.core.metamodel.tabular.DataRow;
import org.apache.causeway.core.metamodel.tabular.DataTableInteractive;
import org.apache.causeway.core.metamodel.tabular.DataTableInteractive.TableImplementation;
import org.apache.causeway.viewer.wicket.model.models.EntityCollectionModelAbstract;
import org.apache.causeway.viewer.wicket.model.models.interaction.coll.DataRowWkt;

import lombok.NonNull;
import lombok.val;

/**
 * Part of the {@link AjaxFallbackDefaultDataTable} API.
 */
public class CollectionContentsSortableDataProvider
extends SortableDataProvider<DataRow, String> {

    private static final long serialVersionUID = 1L;

    private final IModel<DataTableInteractive> dataTableModelHolder;

    public CollectionContentsSortableDataProvider(final IModel<DataTableInteractive> dataTableModelHolder) {
        this.dataTableModelHolder = dataTableModelHolder instanceof EntityCollectionModelAbstract
                ? ((EntityCollectionModelAbstract)dataTableModelHolder).delegate()
                : dataTableModelHolder;
    }

    public boolean isDecoratedWithDataTablesNet() {
        return getDataTableModel().getMetaModel().getFacetHolder().lookupFacet(TableDecoratorFacet.class)
        .map(TableDecoratorFacet::value)
        .map(TableDecorator.DatatablesNet.class::equals)
        .orElse(false);
    }

    public DataTableInteractive getDataTableModel() {
        return dataTableModelHolder.getObject();
    }

    @Override
    public IModel<DataRow> model(final DataRow dataRow) {
        return DataRowWkt.chain(dataTableModelHolder, dataRow);
    }

    @Override
    public long size() {
        return getDataTableModel().getFilteredElementCount();
    }

    @Override
    public Iterator<DataRow> iterator(final long skip, final long limit) {
        switch (TableImplementation.getSelected()) {
        case OPTIMISTIC:
            return iteratorO(skip, limit);
        case DEFAULT:
        default:
            return iteratorD(skip, limit);
        }
    }

    // -- HELPER FOR DEFAULT

    private Iterator<DataRow> iteratorD(final long skip, final long limit) {
        var dataTable = getDataTableModel();
        // honor (single) column sort (if any)
        dataTable.getColumnSort().setValue(columnSort().orElse(null));
        return dataTable.getDataRowsFilteredAndSorted().getValue()
                .iterator(Math.toIntExact(skip), Math.toIntExact(limit));
    }

    private Optional<DataTableInteractive.ColumnSort> columnSort() {
        val sortParam = getSort();
        return lookupColumnIndexFor(sortParam).stream()
                .mapToObj(columnIndex->
                    new DataTableInteractive.ColumnSort(columnIndex, sortDirection(sortParam)))
                .findFirst();
    }

    private OptionalInt lookupColumnIndexFor(final @Nullable SortParam<String> sortParam) {
        if(sortParam==null) return OptionalInt.empty();
        int columnIndex = 0;
        for(var column : getDataTableModel().getDataColumns().getValue()) {
            if(column.getAssociationMetaModel().getId().equals(sortParam.getProperty())) {
                return OptionalInt.of(columnIndex);
            }
            ++columnIndex;
        }
        return OptionalInt.empty();
    }

    private static MmSortUtils.SortDirection sortDirection(final @NonNull SortParam<String> sortParam) {
        return sortParam.isAscending()
                ? MmSortUtils.SortDirection.ASCENDING
                : MmSortUtils.SortDirection.DESCENDING;
    }

    // -- HELPER FOR OPTIMISTIC

    private Iterator<DataRow> iteratorO(final long skip, final long limit) {
        val visibleRows = getDataTableModel().getDataRowsFilteredAndSorted().getValue();
        return sorted(visibleRows).iterator(Math.toIntExact(skip), Math.toIntExact(limit));
    }

    private Can<DataRow> sorted(final Can<DataRow> dataRows) {
        val sort = getSort();
        val sortProperty = lookupPropertyFor(sort).orElse(null);
        if(sortProperty != null) {
            val objComparator = MmSortUtils.orderingBy(sortProperty, sortDirection(sort));
            return dataRows.sorted((a, b)-> {
                ManagedObject managedObjectA = a.getRowElement();
                if(managedObjectA.getSpecialization().isViewmodel()) {
                    // make sure any referenced entities are made live if currently hollow
                    ManagedObjects.refreshViewmodel(managedObjectA, /*bookmark supplier*/ null);
                }
                ManagedObject managedObjectB = b.getRowElement();
                if(managedObjectA.getSpecialization().isViewmodel()) {
                    // make sure any referenced entities are made live if currently hollow
                    ManagedObjects.refreshViewmodel(managedObjectB, /*bookmark supplier*/ null);
                }
                return objComparator.compare(managedObjectA, managedObjectB);
            });
        }
        return dataRows;
    }

    private Optional<OneToOneAssociation> lookupPropertyFor(final SortParam<String> sort) {
        return Optional.ofNullable(sort)
        .map(SortParam::getProperty)
        .flatMap(getDataTableModel().getElementType()::getProperty);
    }

}