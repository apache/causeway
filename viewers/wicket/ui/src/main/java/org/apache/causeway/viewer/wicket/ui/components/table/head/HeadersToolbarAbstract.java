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
package org.apache.causeway.viewer.wicket.ui.components.table.head;

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

import org.apache.causeway.applib.services.i18n.TranslationContext;
import org.apache.causeway.commons.internal.base._Casts;
import org.apache.causeway.core.config.CausewayConfiguration;
import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.viewer.wicket.ui.components.collectioncontents.ajaxtable.columns.TitleColumn;
import org.apache.causeway.viewer.wicket.ui.components.collectioncontents.ajaxtable.columns.ToggleboxColumn;
import org.apache.causeway.viewer.wicket.ui.components.table.CausewayAjaxDataTable;
import org.apache.causeway.viewer.wicket.ui.util.Wkt;
import org.apache.causeway.viewer.wicket.ui.util.WktTooltips;

import de.agilecoders.wicket.core.util.Attributes;

/**
 * Adapted from Wicket's own {@link HeadersToolbar}.
 */
abstract class HeadersToolbarAbstract<S> extends AbstractToolbar {
    private static final long serialVersionUID = 1L;

    private static final String CLASS_SORT_NONE = "fa fa-sort";
    private static final String CLASS_SORT_UP = "fa fa-sort-up";
    private static final String CLASS_SORT_DOWN = "fa fa-sort-down";

    static abstract class CssAttributeBehavior extends Behavior {
        private static final long serialVersionUID = 1L;

        protected abstract String getCssClass();

        /**
         * @see Behavior#onComponentTag(Component, ComponentTag)
         */
        @Override
        public void onComponentTag(final Component component, final ComponentTag tag) {
            Wkt.cssAppend(tag, getCssClass());
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
    public <T> HeadersToolbarAbstract(
            final DataTable<T, S> table,
            final ISortStateLocator<S> stateLocator,
            final CausewayConfiguration.Viewer.Wicket wicketConfig) {
        super(table);

        this.useIndicatorForSortableColumn = wicketConfig.isUseIndicatorForSortableColumn();

        RefreshingView<IColumn<T, S>> headers = new RefreshingView<IColumn<T, S>>("headers") {
            private static final long serialVersionUID = 1L;

            @Override
            protected Iterator<IModel<IColumn<T, S>>> getItemModels() {
                List<IModel<IColumn<T, S>>> columnsModels = new LinkedList<IModel<IColumn<T, S>>>();
                for (IColumn<T, S> column : table.getColumns()) {
                    columnsModels.add(Model.of(column));
                }
                return columnsModels.iterator();
            }

            @Override
            protected void populateItem(final Item<IColumn<T, S>> item) {
                final IColumn<T, S> column = item.getModelObject();
                WebMarkupContainer header;

                if (!isDecoratedWithDataTablesNet()
                        && column.isSortable()) {

                    header = newSortableHeader("header", column.getSortProperty(), stateLocator);

                    if (column instanceof IStyledColumn) {
                        header.add(new CssAttributeBehavior() {
                            private static final long serialVersionUID = 1L;
                            @Override
                            protected String getCssClass() {
                                return ((IStyledColumn<?, S>)column).getCssClass();
                            }
                        });
                    }
                } else {
                    header = new WebMarkupContainer("header");
                }

                item.add(header);
                item.setRenderBodyOnly(true);
                Component label = column.getHeader("label");
                Component sortIcon = newSortIcon("sortIcon", column, stateLocator);
                header.add(label, sortIcon);

                if(column instanceof ToggleboxColumn) {
                    WktTooltips.addTooltip(header, 
                            translate("Toggle select/unselect all rows across all pages."));
                }
                if(column instanceof TitleColumn) {
                    Wkt.cssAppend(header, "title-column");
                }
                Wkt.cssAppend(header, column.isSortable()
                        ? "column-sortable"
                        : "column-nonsortable");
            }
            
            private String translate(String text) {
                return MetaModelContext.translationServiceOrFallback()
                        .translate(TranslationContext.named("Table"), text);
            }
            
        };
        add(headers);
    }

    private boolean isDecoratedWithDataTablesNet() {
        return _Casts.castTo(CausewayAjaxDataTable.class, getTable())
            .map(CausewayAjaxDataTable::isDecoratedWithDataTablesNet)
            .orElse(false);
    }

    private final boolean useIndicatorForSortableColumn;

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
            private static final long serialVersionUID = 1L;

            @Override
            protected void onComponentTag(final ComponentTag tag) {
                super.onComponentTag(tag);

                if(!isDecoratedWithDataTablesNet() && column.isSortable()) {
                    ISortState<S> sortState = stateLocator.getSortState();
                    S sortProperty = column.getSortProperty();
                    SortOrder sortOrder = sortProperty == null ? SortOrder.NONE : sortState.getPropertySortOrder(sortProperty);
                    if (sortOrder == SortOrder.ASCENDING) {
                        Attributes.addClass(tag, CLASS_SORT_UP);
                    } else if (sortOrder == SortOrder.DESCENDING) {
                        Attributes.addClass(tag, CLASS_SORT_DOWN);
                    } else if (useIndicatorForSortableColumn){
                        Attributes.addClass(tag, CLASS_SORT_NONE);
                    } else {
                        // add nothing
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
            final ISortStateLocator<S> locator) {
        return new OrderByBorder<S>(headerId, property, locator) {
            private static final long serialVersionUID = 1L;
            @Override
            protected void onSortChanged() {
                getTable().setCurrentPage(0);
            }
        };
    }
}
