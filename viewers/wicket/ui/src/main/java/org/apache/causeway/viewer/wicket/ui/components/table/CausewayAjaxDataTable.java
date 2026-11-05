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
import java.util.Iterator;
import org.apache.wicket.markup.repeater.IItemFactory;
import org.apache.wicket.markup.repeater.IItemReuseStrategy;
import org.apache.wicket.markup.repeater.OddEvenItem;
import org.apache.wicket.markup.ComponentTag;
import org.apache.causeway.core.metamodel.context.HasMetaModelContext;
import org.apache.causeway.viewer.wicket.ui.observation.WicketPagePreparationObservation;
import org.apache.causeway.viewer.wicket.ui.observation.WicketRenderObservationBehavior;
import org.apache.causeway.viewer.wicket.ui.observation.WicketRenderObservationDescriptor;

import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.NoRecordsToolbar;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.model.IModel;

import org.apache.causeway.core.metamodel.object.ManagedObjects;
import org.apache.causeway.core.metamodel.tabular.DataRow;
import org.apache.causeway.viewer.wicket.model.itemreuse.ReuseIfRowIndexEqualsStrategy;
import org.apache.causeway.viewer.wicket.ui.components.collection.present.ajaxtable.CollectionContentsSortableDataProvider;
import org.apache.causeway.viewer.wicket.ui.components.table.head.HeadersToolbar;
import org.apache.causeway.viewer.wicket.ui.components.table.nav.NavigationToolbar;
import org.apache.causeway.viewer.wicket.ui.components.table.nonav.TotalRecordsToolbar;
import org.apache.causeway.viewer.wicket.ui.util.Wkt;

public class CausewayAjaxDataTable extends DataTableWithPagesAndFilter<DataRow, String> implements HasMetaModelContext {

    private static final long serialVersionUID = 1L;

    private final CollectionContentsSortableDataProvider dataProvider;
    private WicketRenderObservationDescriptor rowDescriptor;

    public CausewayAjaxDataTable(
            final String id,
            final List<? extends IColumn<DataRow, String>> columns,
            final CollectionContentsSortableDataProvider dataProvider,
            final int rowsPerPage) {
        super(id, columns, dataProvider, rowsPerPage);
        this.dataProvider = dataProvider;
        // optimization
        setItemReuseStrategy(ReuseIfRowIndexEqualsStrategy.getInstance());
    }

    public boolean isObservedCollection() { return rowDescriptor != null; }

    /** Adds collection semantics only when the caller has canonical parented identity. */
    public void observeCollection(final String ownerType, final String elementType, final String collectionId) {
        rowDescriptor = WicketRenderObservationDescriptor.row(elementType, collectionId);
        WicketRenderObservationBehavior.addTo(this, WicketRenderObservationDescriptor.table(ownerType, collectionId));
        WicketRenderObservationBehavior.addTo(getTopToolbars(), WicketRenderObservationDescriptor.tableHeader(ownerType, collectionId));
        WicketRenderObservationBehavior.addTo(getBody(), WicketRenderObservationDescriptor.tableBody(ownerType, collectionId));
        WicketRenderObservationBehavior.addTo(getBottomToolbars(), WicketRenderObservationDescriptor.tableFooter(ownerType, collectionId));
        setItemReuseStrategy(new ObservedRowReuseStrategy(
                WicketRenderObservationDescriptor.rowPreparation(elementType, collectionId)));
    }

    private final class ObservedRowReuseStrategy implements IItemReuseStrategy {
        private static final long serialVersionUID = 1L;
        private final WicketRenderObservationDescriptor descriptor;
        private ObservedRowReuseStrategy(final WicketRenderObservationDescriptor descriptor) { this.descriptor = descriptor; }
        @Override
        public <T> Iterator<Item<T>> getItems(final IItemFactory<T> factory,
                final Iterator<IModel<T>> models, final Iterator<Item<T>> existing) {
            // Keep main's index-based reuse and observe only work it already performs.
            final var items = ReuseIfRowIndexEqualsStrategy.getInstance().getItems(factory, models, existing);
            return new Iterator<>() {
                public boolean hasNext() { return items.hasNext(); }
                public Item<T> next() {
                    return WicketPagePreparationObservation.observe(CausewayAjaxDataTable.this, descriptor, items::next);
                }
                public void remove() { throw new UnsupportedOperationException(); }
            };
        }
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();
        buildGui();
    }

    protected void buildGui() {
        var wicketConfig = getConfiguration().viewer().wicket();

        addTopToolbar(new HeadersToolbar(this, this.dataProvider, wicketConfig));

        if (!isDecoratedWithDataTablesNet()) {
            // toolbars do decide for themselves, whether they are visible
            addBottomToolbar(new NavigationToolbar(this));
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
        final Item<DataRow> item = new ObservedRowItem(id, index, model);
        if(rowDescriptor != null) WicketRenderObservationBehavior.addTo(item, rowDescriptor);
        return item;
    }

    private static final class ObservedRowItem extends OddEvenItem<DataRow> {
        private static final long serialVersionUID = 1L;
        private ObservedRowItem(final String id, final int index, final IModel<DataRow> model) { super(id, index, model); }
        @Override
        protected void onComponentTag(final ComponentTag tag) {
            super.onComponentTag(tag);
            Wkt.cssAppend(tag, cssClassForRow(getModelObject()));
        }
    }

    // -- HELPER

    private static String cssClassForRow(final DataRow model) {
        if(model==null
                || ManagedObjects.isNullOrUnspecifiedOrEmpty(model.rowElement())) {
            return null;
        }
        var rowElement = model.rowElement();
        return rowElement.objSpec().getCssClass(rowElement);
    }

}
