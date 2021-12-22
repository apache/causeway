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

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.markup.html.repeater.data.sort.SortOrder;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.NoRecordsToolbar;
import org.apache.wicket.markup.repeater.IItemFactory;
import org.apache.wicket.markup.repeater.IItemReuseStrategy;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;

import org.apache.isis.commons.internal.base._Casts;
import org.apache.isis.commons.internal.collections._Maps;
import org.apache.isis.core.metamodel.interactions.managed.nonscalar.DataRow;
import org.apache.isis.core.metamodel.spec.ManagedObjects;
import org.apache.isis.viewer.wicket.model.hints.UiHintContainer;
import org.apache.isis.viewer.wicket.model.models.EntityModel;
import org.apache.isis.viewer.wicket.model.models.interaction.coll.DataRowWkt;
import org.apache.isis.viewer.wicket.ui.components.collectioncontents.ajaxtable.columns.GenericToggleboxColumn;
import org.apache.isis.viewer.wicket.ui.util.Wkt;

import lombok.val;

public class IsisAjaxDataTable extends DataTable<DataRow, String> {

    private static final long serialVersionUID = 1L;

    static final String UIHINT_PAGE_NUMBER = "pageNumber";

    private final CollectionContentsSortableDataProvider dataProvider;
    private final GenericToggleboxColumn toggleboxColumn;

    private IsisAjaxHeadersToolbar headersToolbar;
    private IsisAjaxNavigationToolbar navigationToolbar;

    public IsisAjaxDataTable(
            final String id,
            final List<? extends IColumn<DataRow, String>> columns,
            final CollectionContentsSortableDataProvider dataProvider,
            final int rowsPerPage,
            final GenericToggleboxColumn toggleboxColumn) {

        super(id, columns, dataProvider, rowsPerPage);
        this.dataProvider = dataProvider;
        this.toggleboxColumn = toggleboxColumn;
        setOutputMarkupId(true);
        setVersioned(false);
        setItemReuseStrategy((IItemReuseStrategy & Serializable) IsisAjaxDataTable::itemReuseStrategyWithCast);
    }

    public void setPageNumberHintAndBroadcast(final AjaxRequestTarget target) {
        final UiHintContainer uiHintContainer = getUiHintContainer();
        if(uiHintContainer == null) {
            return;
        }
        uiHintContainer.setHint(this, IsisAjaxDataTable.UIHINT_PAGE_NUMBER, ""+getCurrentPage());
    }

    public void setSortOrderHintAndBroadcast(final SortOrder order, final String property, final AjaxRequestTarget target) {
        final UiHintContainer uiHintContainer = getUiHintContainer();
        if(uiHintContainer == null) {
            return;
        }

        // first clear all SortOrder hints...
        for (SortOrder eachSortOrder : SortOrder.values()) {
            uiHintContainer.clearHint(this, eachSortOrder.name());
        }
        // .. then set this one
        uiHintContainer.setHint(this, order.name(), property);
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();
        buildGui();
        honorHints();
    }

    private void buildGui() {
        headersToolbar = new IsisAjaxHeadersToolbar(this, this.dataProvider);
        addTopToolbar(headersToolbar);

        navigationToolbar = new IsisAjaxNavigationToolbar(this, this.toggleboxColumn);

        // implementation note: toolbars do decide for themselves, whether they are visible
        addBottomToolbar(navigationToolbar);
        addBottomToolbar(new NoRecordsToolbar(this));
        addBottomToolbar(new IsisTotalRecordsToolbar(this));
    }

    @Override
    protected void onConfigure() {
        super.onConfigure();
    }

    @Override
    protected Item<DataRow> newRowItem(final String id, final int index, final IModel<DataRow> model) {
        return Wkt.oddEvenItem(id, index, model, IsisAjaxDataTable::cssClassForRow);
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

    private static Iterator<Item<DataRow>> itemReuseStrategy(
            final IItemFactory<DataRow> factory,
            final Iterator<IModel<DataRow>> newModels,
            final Iterator<Item<DataRow>> existingItems) {

        val itemByUuid = _Maps.<UUID, Item<DataRow>>newHashMap();
        existingItems.forEachRemaining(item->{
            val model = item.getModel();
            if(model instanceof DataRowWkt) {
                val dataRowWkt = (DataRowWkt)item.getModel();
                itemByUuid.put(dataRowWkt.getUuid(), item);
            }
        });

        return new Iterator<Item<DataRow>>() {
            private int index = 0;

            @Override
            public boolean hasNext() {
                return newModels.hasNext();
            }

            @Override
            public Item<DataRow> next() {
                final DataRowWkt newModel = (DataRowWkt)newModels.next();
                final Item<DataRow> oldItem = itemByUuid.get(newModel.getUuid());

                final IModel<DataRow> model2 = oldItem != null
                        ? oldItem.getModel()
                        : newModel;
                return factory.newItem(index++, model2);
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }

        };

    }

    private static <T> Iterator<Item<T>> itemReuseStrategyWithCast(
            final IItemFactory<T> factory,
            final Iterator<IModel<T>> newModels,
            final Iterator<Item<T>> existingItems) {
        return _Casts.uncheckedCast(itemReuseStrategy(
                _Casts.uncheckedCast(factory),
                _Casts.uncheckedCast(newModels),
                _Casts.uncheckedCast(existingItems)));
    }

    private void honorHints() {
        headersToolbar.honourSortOrderHints();
        navigationToolbar.honourHints();
        honourPageNumberHint();
    }

    private void honourPageNumberHint() {
        UiHintContainer uiHintContainer = getUiHintContainer();
        if(uiHintContainer == null) {
            return;
        }
        final String pageNumberStr = uiHintContainer.getHint(this, UIHINT_PAGE_NUMBER);
        if(pageNumberStr != null) {
            try {
                long pageNumber = Long.parseLong(pageNumberStr);
                if(pageNumber >= 0) {
                    // dataTable is clever enough to deal with too-large numbers
                    this.setCurrentPage(pageNumber);
                }
            } catch(Exception ex) {
                // ignore.
            }
        }
        uiHintContainer.setHint(this, UIHINT_PAGE_NUMBER, ""+getCurrentPage());
        // don't broadcast (no AjaxRequestTarget, still configuring initial setup)
    }

    private UiHintContainer getUiHintContainer() {
        return UiHintContainer.Util.hintContainerOf(this, EntityModel.class);
    }

}
