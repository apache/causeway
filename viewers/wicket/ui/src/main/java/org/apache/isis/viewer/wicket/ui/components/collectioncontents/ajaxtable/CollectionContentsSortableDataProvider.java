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

import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import org.apache.wicket.extensions.ajax.markup.html.repeater.data.table.AjaxFallbackDefaultDataTable;
import org.apache.wicket.extensions.markup.html.repeater.util.SortParam;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.model.IModel;

import org.apache.isis.core.metamodel.interactions.managed.nonscalar.DataRow;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.isis.viewer.wicket.model.models.EntityCollectionModel;
import org.apache.isis.viewer.wicket.model.models.interaction.coll.DataRowWkt;

import lombok.val;

/**
 * Part of the {@link AjaxFallbackDefaultDataTable} API.
 */
public class CollectionContentsSortableDataProvider
extends SortableDataProvider<DataRow, String> {

    private static final long serialVersionUID = 1L;

    private final EntityCollectionModel model;

    public CollectionContentsSortableDataProvider(final EntityCollectionModel model) {
        this.model = model;
    }

    @Override
    public IModel<DataRow> model(final DataRow dataRow) {
        return DataRowWkt.chain(model, dataRow);

//
//        if(ManagedObjects.isNullOrUnspecifiedOrEmpty(adapter)
//                || ManagedObjects.isValue(adapter)) {
//            return ValueModel.of(model.getCommonContext(), adapter);
//        }
//        return EntityModel.ofAdapter(model.getCommonContext(), adapter);
    }

    @Override
    public long size() {
        return model.getCount();
    }

    @Override
    public void detach() {
        super.detach();
        model.detach();
    }

    public EntityCollectionModel getEntityCollectionModel() {
        return model;
    }

    @Override
    public Iterator<DataRow> iterator(final long first, final long count) {

        val visibleRows = model.getDataTableModel().getDataRowsFiltered()
                .getValue()
                .toList();

        final List<DataRow> sortedVisibleRows = sortedCopy(visibleRows, getSort());
        final List<DataRow> pagedRows = subList(first, count, sortedVisibleRows);
        return pagedRows.iterator();
    }

    private static List<DataRow> subList(
            final long first,
            final long count,
            final List<DataRow> dataRows) {

        final int fromIndex = (int) first;
        // if adapters where filter out (as invisible), then make sure don't run off the end
        final int toIndex = Math.min((int) (first + count), dataRows.size());

        return dataRows.subList(fromIndex, toIndex);
    }

    private List<DataRow> sortedCopy(
            final List<DataRow> dataRows,
            final SortParam<String> sort) {
        return dataRows;

//FIXME no sorting yet, this is client-side
//        val copy = _Lists.newArrayList(adapters);
//
//        var sortProperty = lookupPropertyFor(sort);
//        if(sortProperty != null) {
//            Collections.sort(copy, ManagedObjects.orderingBy(sortProperty, sort.isAscending()));
//        }
//
//        return copy;
    }

    private Optional<OneToOneAssociation> lookupPropertyFor(final SortParam<String> sort) {
        return Optional.ofNullable(sort)
        .map(SortParam::getProperty)
        .flatMap(model.getElementType()::getProperty);
    }

}