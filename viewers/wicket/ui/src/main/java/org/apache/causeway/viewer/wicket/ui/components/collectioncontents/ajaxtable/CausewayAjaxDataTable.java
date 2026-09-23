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

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.markup.html.repeater.data.sort.SortOrder;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.NoRecordsToolbar;
import org.apache.wicket.markup.repeater.IItemFactory;
import org.apache.wicket.markup.repeater.IItemReuseStrategy;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.OddEvenItem;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.model.IModel;

import org.apache.causeway.commons.internal.base._Casts;
import org.apache.causeway.commons.internal.collections._Maps;
import org.apache.causeway.core.config.observation.CausewayObservationIntegration;
import org.apache.causeway.core.metamodel.context.HasMetaModelContext;
import org.apache.causeway.core.metamodel.object.ManagedObjects;
import org.apache.causeway.core.metamodel.tabular.interactive.DataRow;
import org.apache.causeway.viewer.wicket.model.hints.UiHintContainer;
import org.apache.causeway.viewer.wicket.model.models.UiObjectWkt;
import org.apache.causeway.viewer.wicket.model.models.interaction.coll.DataRowWkt;
import org.apache.causeway.viewer.wicket.ui.components.collectioncontents.ajaxtable.columns.ToggleboxColumn;
import org.apache.causeway.viewer.wicket.ui.observation.WicketPreparationObservation;
import org.apache.causeway.viewer.wicket.ui.observation.WicketRenderObservationBehavior;
import org.apache.causeway.viewer.wicket.ui.observation.WicketRenderObservationDescriptor;
import org.apache.causeway.viewer.wicket.ui.util.Wkt;

import lombok.val;

public class CausewayAjaxDataTable extends DataTable<DataRow, String> {

    private static final long serialVersionUID = 1L;

    static final String UIHINT_PAGE_NUMBER = "pageNumber";

    private final CollectionContentsSortableDataProvider dataProvider;
    private final ToggleboxColumn toggleboxColumn;
    private final WicketRenderObservationDescriptor rowRenderDescriptor;

    private CausewayAjaxHeadersToolbar headersToolbar;
    private CausewayAjaxNavigationToolbar navigationToolbar;

    public CausewayAjaxDataTable(
            final String id,
            final List<? extends IColumn<DataRow, String>> columns,
            final CollectionContentsSortableDataProvider dataProvider,
            final int rowsPerPage,
            final ToggleboxColumn toggleboxColumn,
            final String elementObjectType,
            final String collectionId) {

        super(id, columns, dataProvider, rowsPerPage);
        this.dataProvider = dataProvider;
        this.toggleboxColumn = toggleboxColumn;
        this.rowRenderDescriptor = collectionId != null
                ? WicketRenderObservationDescriptor.row(
                        elementObjectType, collectionId)
                : null;
        final WicketRenderObservationDescriptor rowPreparationDescriptor =
                collectionId != null
                        ? WicketRenderObservationDescriptor.rowPreparation(
                                elementObjectType, collectionId)
                        : null;
        setOutputMarkupId(true);
        setVersioned(false);
        setItemReuseStrategy(new RowItemReuseStrategy(
                rowPreparationDescriptor));
    }

    public void setPageNumberHintAndBroadcast(final AjaxRequestTarget target) {
        final UiHintContainer uiHintContainer = getUiHintContainer();
        if(uiHintContainer == null) {
            return;
        }
        uiHintContainer.setHint(this, CausewayAjaxDataTable.UIHINT_PAGE_NUMBER, ""+getCurrentPage());
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
        headersToolbar = new CausewayAjaxHeadersToolbar(this, this.dataProvider);
        addTopToolbar(headersToolbar);

        navigationToolbar = new CausewayAjaxNavigationToolbar(this, this.toggleboxColumn);

        if (!isDecoratedWithDataTablesNet()) {
            // implementation note: toolbars do decide for themselves, whether they are visible
            addBottomToolbar(navigationToolbar);
            addBottomToolbar(new NoRecordsToolbar(this));
            addBottomToolbar(new CausewayTotalRecordsToolbar(this));
        }
    }

    public boolean isDecoratedWithDataTablesNet() {
        IDataProvider<?> dataProvider = getDataProvider();
        return dataProvider instanceof CollectionContentsSortableDataProvider &&
                ((CollectionContentsSortableDataProvider) dataProvider).isDecoratedWithDataTablesNet();
    }


    @Override
    protected void onConfigure() {
        super.onConfigure();
    }

    @Override
    protected Item<DataRow> newRowItem(
            final String id,
            final int index,
            final IModel<DataRow> model) {
        final Item<DataRow> item = new ObservedRowItem(id, index, model);
        if(rowRenderDescriptor != null) {
            WicketRenderObservationBehavior.addTo(
                    item, rowRenderDescriptor);
        }
        return item;
    }

    // -- HELPER

    private static String cssClassForRow(final DataRow model) {
        if(model==null
                || ManagedObjects.isNullOrUnspecifiedOrEmpty(model.rowElement())) {
            return null;
        }
        val rowElement = model.rowElement();
        return rowElement.objSpec().getCssClass(rowElement);
    }

    private static Iterator<Item<DataRow>> itemReuseStrategy(
            final IItemFactory<DataRow> factory,
            final Iterator<IModel<DataRow>> newModels,
            final Iterator<Item<DataRow>> existingItems,
            final HasMetaModelContext context,
            final WicketRenderObservationDescriptor preparationDescriptor) {

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
                final int itemIndex = index++;
                return preparationDescriptor != null
                        ? WicketPreparationObservation.observe(
                                context,
                                preparationDescriptor,
                                () -> factory.newItem(itemIndex, model2))
                        : factory.newItem(itemIndex, model2);
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
            final Iterator<Item<T>> existingItems,
            final HasMetaModelContext context,
            final WicketRenderObservationDescriptor preparationDescriptor) {
        return _Casts.uncheckedCast(itemReuseStrategy(
                _Casts.uncheckedCast(factory),
                _Casts.uncheckedCast(newModels),
                _Casts.uncheckedCast(existingItems),
                context,
                preparationDescriptor));
    }

    static final class RowItemReuseStrategy
    implements IItemReuseStrategy, HasMetaModelContext, Serializable {

        private static final long serialVersionUID = 1L;

        private final WicketRenderObservationDescriptor preparationDescriptor;
        private transient CausewayObservationIntegration observationIntegration;

        RowItemReuseStrategy(
                final WicketRenderObservationDescriptor preparationDescriptor) {
            this(preparationDescriptor, null);
        }

        RowItemReuseStrategy(
                final WicketRenderObservationDescriptor preparationDescriptor,
                final CausewayObservationIntegration observationIntegration) {
            this.preparationDescriptor = preparationDescriptor;
            this.observationIntegration = observationIntegration;
        }

        WicketRenderObservationDescriptor descriptor() {
            return preparationDescriptor;
        }

        @Override
        public <T> Optional<T> lookupService(final Class<T> serviceClass) {
            if(serviceClass == CausewayObservationIntegration.class
                    && observationIntegration != null) {
                return Optional.of(serviceClass.cast(observationIntegration));
            }
            return HasMetaModelContext.super.lookupService(serviceClass);
        }

        @Override
        public <T> Iterator<Item<T>> getItems(
                final IItemFactory<T> factory,
                final Iterator<IModel<T>> newModels,
                final Iterator<Item<T>> existingItems) {
            return itemReuseStrategyWithCast(
                    factory,
                    newModels,
                    existingItems,
                    this,
                    preparationDescriptor);
        }
    }

    private static final class ObservedRowItem
    extends OddEvenItem<DataRow>
    implements HasMetaModelContext {

        private static final long serialVersionUID = 1L;

        private ObservedRowItem(
                final String id,
                final int index,
                final IModel<DataRow> model) {
            super(id, index, model);
        }

        @Override
        protected void onComponentTag(final ComponentTag tag) {
            super.onComponentTag(tag);
            Wkt.cssAppend(tag, cssClassForRow(getModelObject()));
        }
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
        return UiHintContainer.Util.hintContainerOf(this, UiObjectWkt.class);
    }

}
