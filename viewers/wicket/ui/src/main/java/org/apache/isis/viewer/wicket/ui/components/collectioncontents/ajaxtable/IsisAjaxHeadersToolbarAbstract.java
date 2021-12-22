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
import java.util.LinkedList;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.extensions.markup.html.repeater.data.sort.ISortState;
import org.apache.wicket.extensions.markup.html.repeater.data.sort.ISortStateLocator;
import org.apache.wicket.extensions.markup.html.repeater.data.sort.OrderByBorder;
import org.apache.wicket.extensions.markup.html.repeater.data.sort.SortOrder;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractToolbar;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.HeadersToolbar;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IStyledColumn;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.RefreshingView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.string.Strings;

import org.apache.isis.viewer.wicket.ui.components.collectioncontents.ajaxtable.columns.GenericTitleColumn;
import org.apache.isis.viewer.wicket.ui.util.Wkt;

import de.agilecoders.wicket.core.util.Attributes;


/**
 * Adapted from Wicket's own {@link HeadersToolbar}.
 */
public abstract class IsisAjaxHeadersToolbarAbstract<S> extends AbstractToolbar
{
    private static final long serialVersionUID = 1L;

    private static final String CLASS_SORT_NONE = "fa fa-fw fa-sort";
    private static final String CLASS_SORT_UP = "fa fa-fw fa-sort-up";
    private static final String CLASS_SORT_DOWN = "fa fa-fw fa-sort-down";

    static abstract class CssAttributeBehavior extends Behavior
    {
        private static final long serialVersionUID = 1L;

        protected abstract String getCssClass();

        /**
         * @see Behavior#onComponentTag(Component, ComponentTag)
         */
        @Override
        public void onComponentTag(final Component component, final ComponentTag tag)
        {
            String className = getCssClass();
            if (!Strings.isEmpty(className))
            {
                tag.append("class", className, " ");
            }
        }
    }

    /**
     * Constructor
     *
     * @param <T>
     *            the column data type
     * @param table
     *            data table this toolbar will be attached to
     * @param stateLocator
     *            locator for the ISortState implementation used by sortable headers
     */
    public <T> IsisAjaxHeadersToolbarAbstract(final DataTable<T, S> table, final ISortStateLocator<S> stateLocator)
    {
        super(table);

        RefreshingView<IColumn<T, S>> headers = new RefreshingView<IColumn<T, S>>("headers")
        {
            private static final long serialVersionUID = 1L;

            @Override
            protected Iterator<IModel<IColumn<T, S>>> getItemModels()
            {
                List<IModel<IColumn<T, S>>> columnsModels = new LinkedList<IModel<IColumn<T, S>>>();

                for (IColumn<T, S> column : table.getColumns())
                {
                    columnsModels.add(Model.of(column));
                }

                return columnsModels.iterator();
            }

            @Override
            protected void populateItem(final Item<IColumn<T, S>> item)
            {
                final IColumn<T, S> column = item.getModelObject();

                WebMarkupContainer header;

                if (column.isSortable())
                {
                    header = newSortableHeader("header", column.getSortProperty(), stateLocator);

                    if (column instanceof IStyledColumn)
                    {
                        CssAttributeBehavior cssAttributeBehavior = new CssAttributeBehavior()
                        {
                            private static final long serialVersionUID = 1L;

                            @Override
                            protected String getCssClass()
                            {
                                return ((IStyledColumn<?, S>)column).getCssClass();
                            }
                        };

                        header.add(cssAttributeBehavior);
                    }

                }
                else
                {
                    header = new WebMarkupContainer("header");
                }


                item.add(header);
                item.setRenderBodyOnly(true);
                Component label = column.getHeader("label");
                Component sortIcon = newSortIcon("sortIcon", column, stateLocator);
                header.add(label, sortIcon);

                if(column instanceof GenericTitleColumn) {
                    Wkt.cssAppend(header, "title-column");
                }
            }
        };
        add(headers);
    }

    /**
     * Factory method for the sort icon
     *
     * @param id
     *          the component id
     * @param column
     *          the column for which a sort icon is needed
     * @param stateLocator
     *          locator for the ISortState implementation used by sortable headers
     * @param <T>
     *          The model object type of the data table
     * @return A component that should be used as a sort icon
     */
    protected <T> Component newSortIcon(final String id, final IColumn<T, S> column, final ISortStateLocator<S> stateLocator) {
        return new WebComponent(id) {
            @Override
            protected void onComponentTag(final ComponentTag tag) {
                super.onComponentTag(tag);

                if(column.isSortable()) {
                    ISortState<S> sortState = stateLocator.getSortState();
                    S sortProperty = column.getSortProperty();
                    SortOrder sortOrder = sortProperty == null ? SortOrder.NONE : sortState.getPropertySortOrder(sortProperty);
                    if (sortOrder == SortOrder.ASCENDING) {
                        Attributes.addClass(tag, CLASS_SORT_UP);
                    } else if (sortOrder == SortOrder.DESCENDING) {
                        Attributes.addClass(tag, CLASS_SORT_DOWN);
                    } else {
                        Attributes.addClass(tag, CLASS_SORT_NONE);
                    }
                }
            }
        };
    }

    /**
     * Factory method for sortable header components. A sortable header component must have id of
     * <code>headerId</code> and conform to markup specified in <code>HeadersToolbar.html</code>
     *
     * @param headerId
     *            header component id
     * @param property
     *            property this header represents
     * @param locator
     *            sort state locator
     * @return created header component
     */
    protected WebMarkupContainer newSortableHeader(final String headerId, final S property,
            final ISortStateLocator<S> locator)
    {
        return new OrderByBorder<S>(headerId, property, locator)
        {
            private static final long serialVersionUID = 1L;

            @Override
            protected void onSortChanged()
            {
                getTable().setCurrentPage(0);
            }
        };
    }
}
