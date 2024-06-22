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
package org.apache.causeway.viewer.wicket.ui.components.table;

import java.util.List;

import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.NoRecordsToolbar;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.model.IModel;

import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.core.metamodel.object.ManagedObjects;
import org.apache.causeway.core.metamodel.tabular.DataRow;
import org.apache.causeway.viewer.wicket.ui.components.collectioncontents.ajaxtable.CollectionContentsSortableDataProvider;
import org.apache.causeway.viewer.wicket.ui.components.collectioncontents.ajaxtable.columns.ToggleboxColumn;
import org.apache.causeway.viewer.wicket.ui.components.table.head.HeadersToolbar;
import org.apache.causeway.viewer.wicket.ui.components.table.nav.NavigationToolbar;
import org.apache.causeway.viewer.wicket.ui.components.table.nonav.TotalRecordsToolbar;
import org.apache.causeway.viewer.wicket.ui.util.Wkt;

import lombok.val;

public class CausewayAjaxDataTable extends DataTableWithPagesAndFilter<DataRow, String> {

    private static final long serialVersionUID = 1L;

    private final CollectionContentsSortableDataProvider dataProvider;
    private final ToggleboxColumn toggleboxColumn;

    public CausewayAjaxDataTable(
            final String id,
            final List<? extends IColumn<DataRow, String>> columns,
            final CollectionContentsSortableDataProvider dataProvider,
            final int rowsPerPage,
            final ToggleboxColumn toggleboxColumn) {
        super(id, columns, dataProvider, rowsPerPage);
        this.dataProvider = dataProvider;
        this.toggleboxColumn = toggleboxColumn;
        //[CAUSEWAY-3772] optimization reinstate? though I have no clue what that is doing
        //setItemReuseStrategy((IItemReuseStrategy & Serializable) CausewayAjaxDataTable::itemReuseStrategyWithCast);
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();
        buildGui();
    }

    private void buildGui() {
        var wicketConfig = MetaModelContext.instanceElseFail().getConfiguration().getViewer().getWicket();

        addTopToolbar(new HeadersToolbar(this, this.dataProvider, wicketConfig));

        if (!isDecoratedWithDataTablesNet()) {
            // toolbars do decide for themselves, whether they are visible
            addBottomToolbar(new NavigationToolbar(this, this.toggleboxColumn));
            addBottomToolbar(new NoRecordsToolbar(this));
            addBottomToolbar(new TotalRecordsToolbar(this));
        }
    }

    public boolean isDecoratedWithDataTablesNet() {
        IDataProvider<?> dataProvider = getDataProvider();
        return dataProvider instanceof CollectionContentsSortableDataProvider &&
                ((CollectionContentsSortableDataProvider) dataProvider).isDecoratedWithDataTablesNet();
    }

    @Override
    protected Item<DataRow> newRowItem(final String id, final int index, final IModel<DataRow> model) {
        return Wkt.oddEvenItem(id, index, model, CausewayAjaxDataTable::cssClassForRow);
    }

    // -- HELPER

    private static String cssClassForRow(final DataRow model) {
        if(model==null
                || ManagedObjects.isNullOrUnspecifiedOrEmpty(model.getRowElement())) {
            return null;
        }
        val rowElement = model.getRowElement();
        return rowElement.getSpecification().getCssClass(rowElement);
    }

//    private static Iterator<Item<DataRow>> itemReuseStrategy(
//            final IItemFactory<DataRow> factory,
//            final Iterator<IModel<DataRow>> newModels,
//            final Iterator<Item<DataRow>> existingItems) {
//
//        val itemByRowIndex = _Maps.<Integer, Item<DataRow>>newHashMap();
//        existingItems.forEachRemaining(item->{
//            val model = item.getModel();
//            if(model instanceof DataRowWkt) {
//                val dataRowWkt = (DataRowWkt)item.getModel();
//                itemByRowIndex.put(dataRowWkt.getRowIndex(), item);
//            }
//        });
//
//        return new Iterator<Item<DataRow>>() {
//            private int index = 0;
//
//            @Override
//            public boolean hasNext() {
//                return newModels.hasNext();
//            }
//
//            @Override
//            public Item<DataRow> next() {
//                final DataRowWkt newModel = (DataRowWkt)newModels.next();
//                final Item<DataRow> oldItem = itemByRowIndex.get(newModel.getRowIndex());
//
//                final IModel<DataRow> model2 = oldItem != null
//                        ? oldItem.getModel()
//                        : newModel;
//                return factory.newItem(index++, model2);
//            }
//
//            @Override
//            public void remove() {
//                throw new UnsupportedOperationException();
//            }
//
//        };
//
//    }
//
//    @SuppressWarnings("unused")
//    private static <T> Iterator<Item<T>> itemReuseStrategyWithCast(
//            final IItemFactory<T> factory,
//            final Iterator<IModel<T>> newModels,
//            final Iterator<Item<T>> existingItems) {
//        return _Casts.uncheckedCast(itemReuseStrategy(
//                _Casts.uncheckedCast(factory),
//                _Casts.uncheckedCast(newModels),
//                _Casts.uncheckedCast(existingItems)));
//    }



}
