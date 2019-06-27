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
import java.util.Map;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.markup.html.repeater.data.sort.SortOrder;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.ISortableDataProvider;
import org.apache.wicket.extensions.markup.html.repeater.data.table.NoRecordsToolbar;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.repeater.IItemFactory;
import org.apache.wicket.markup.repeater.IItemReuseStrategy;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.OddEvenItem;
import org.apache.wicket.model.IModel;
import org.apache.wicket.util.lang.Generics;
import org.apache.isis.metamodel.adapter.ObjectAdapter;
import org.apache.isis.metamodel.spec.ObjectSpecification;
import org.apache.isis.viewer.wicket.model.hints.UiHintContainer;
import org.apache.isis.viewer.wicket.model.models.EntityModel;
import org.apache.isis.viewer.wicket.ui.components.collectioncontents.ajaxtable.columns.ObjectAdapterToggleboxColumn;
import org.apache.isis.viewer.wicket.ui.util.CssClassAppender;

public class IsisAjaxFallbackDataTable<T, S> extends DataTable<T, S> {

    private static final long serialVersionUID = 1L;

    static final String UIHINT_PAGE_NUMBER = "pageNumber";

    private final CollectionContentsSortableDataProvider dataProvider;
    private final ObjectAdapterToggleboxColumn toggleboxColumn;

    private IsisAjaxFallbackHeadersToolbar<S> headersToolbar;
    private IsisAjaxNavigationToolbar navigationToolbar;

    public IsisAjaxFallbackDataTable(
            final String id,
            final List<? extends IColumn<T, S>> columns,
                    final CollectionContentsSortableDataProvider dataProvider,
                    final int rowsPerPage,
                    final ObjectAdapterToggleboxColumn toggleboxColumn)
    {
        super(id, columns, (ISortableDataProvider<T, S>)dataProvider, rowsPerPage);
        this.dataProvider = dataProvider;
        this.toggleboxColumn = toggleboxColumn;
        setOutputMarkupId(true);
        setVersioned(false);
        setItemReuseStrategy(new PreserveModelReuseStrategy());
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();
        buildGui();
    }

    private void buildGui() {
        headersToolbar = new IsisAjaxFallbackHeadersToolbar<>(this, this.dataProvider);
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
    protected Item<T> newRowItem(final String id, final int index, final IModel<T> model)
    {
        return new OddEvenItem<T>(id, index, model) {
            @Override
            protected void onComponentTag(ComponentTag tag) {
                super.onComponentTag(tag);

                if (model instanceof EntityModel) {
                    EntityModel entityModel = (EntityModel) model;
                    final ObjectAdapter objectAdapter = entityModel.getObject();
                    final ObjectSpecification typeOfSpecification = entityModel.getTypeOfSpecification();
                    String cssClass = typeOfSpecification.getCssClass(objectAdapter);
                    CssClassAppender.appendCssClassTo(tag, cssClass);
                }
            }
        };
    }

    static class PreserveModelReuseStrategy implements IItemReuseStrategy {
        private static final long serialVersionUID = 1L;

        private static IItemReuseStrategy instance = new PreserveModelReuseStrategy();

        /**
         * @return static instance
         */
        public static IItemReuseStrategy getInstance()
        {
            return instance;
        }

        /**
         * @see org.apache.wicket.markup.repeater.IItemReuseStrategy#getItems(org.apache.wicket.markup.repeater.IItemFactory,
         *      java.util.Iterator, java.util.Iterator)
         */
        @Override
        public <T> Iterator<Item<T>> getItems(final IItemFactory<T> factory,
                final Iterator<IModel<T>> newModels, Iterator<Item<T>> existingItems)
        {
            final Map<IModel<T>, Item<T>> modelToItem = Generics.newHashMap();
            while (existingItems.hasNext())
            {
                final Item<T> item = existingItems.next();
                modelToItem.put(item.getModel(), item);
            }

            return new Iterator<Item<T>>()
            {
                private int index = 0;

                @Override
                public boolean hasNext()
                {
                    return newModels.hasNext();
                }

                @Override
                public Item<T> next()
                {
                    final IModel<T> model = newModels.next();
                    final Item<T> oldItem = modelToItem.get(model);

                    final IModel<T> model2 = oldItem != null ? oldItem.getModel() : model;
                    return factory.newItem(index++, model2);
                }

                @Override
                public void remove()
                {
                    throw new UnsupportedOperationException();
                }

            };
        }

    }

    public void honourHints() {
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

    public void setPageNumberHintAndBroadcast(AjaxRequestTarget target) {
        final UiHintContainer uiHintContainer = getUiHintContainer();
        if(uiHintContainer == null) {
            return;
        }
        uiHintContainer.setHint(this, IsisAjaxFallbackDataTable.UIHINT_PAGE_NUMBER, ""+getCurrentPage());
    }

    public void setSortOrderHintAndBroadcast(SortOrder order, String property, AjaxRequestTarget target) {
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

    private UiHintContainer getUiHintContainer() {
        return UiHintContainer.Util.hintContainerOf(this, EntityModel.class);
    }

}
