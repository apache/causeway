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

import org.apache.wicket.MarkupContainer;
import org.apache.wicket.extensions.ajax.markup.html.repeater.data.table.AjaxNavigationToolbar;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.markup.html.navigation.paging.PagingNavigator;

import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.viewer.wicket.model.hints.UiHintContainer;
import org.apache.causeway.viewer.wicket.model.models.HasCommonContext;
import org.apache.causeway.viewer.wicket.model.models.UiObjectWkt;
import org.apache.causeway.viewer.wicket.model.util.WktContext;
import org.apache.causeway.viewer.wicket.ui.components.collectioncontents.ajaxtable.columns.GenericToggleboxColumn;
import org.apache.causeway.viewer.wicket.ui.util.Wkt;

public class CausewayAjaxNavigationToolbar extends AjaxNavigationToolbar
implements HasCommonContext {

    private static final long serialVersionUID = 1L;

    private static final String navigatorContainerId = "span";
    private static final String ID_SHOW_ALL = "showAll";
    private static final String HINT_KEY_SHOW_ALL = "showAll";
    private final GenericToggleboxColumn toggleboxColumn;

    public CausewayAjaxNavigationToolbar(
            final DataTable<?, ?> table,
            final GenericToggleboxColumn toggleboxColumn) {

        super(table);
        this.toggleboxColumn = toggleboxColumn;
        addShowAllButton(table);
    }

    @Override
    protected PagingNavigator newPagingNavigator(final String navigatorId, final DataTable<?, ?> table) {
        return new CausewayAjaxPagingNavigator(navigatorId, table);
    }

    // -- HELPER

    private void addShowAllButton(final DataTable<?, ?> table) {
        table.setOutputMarkupId(true);

        final MarkupContainer container = navigatorContainer();

        Wkt.linkAdd(container, ID_SHOW_ALL, target->{
            showAllItemsOn(table);

            final DataTable<?, ?> dataTable = getTable();
            final CollectionContentsSortableDataProvider dataProvider =
                    (CollectionContentsSortableDataProvider) dataTable.getDataProvider();

            if(toggleboxColumn != null) {
                // clear the underlying backend selection model
                dataProvider.getDataTableModel().getSelectAllToggle().setValue(false);
                // remove toggle UI components
                toggleboxColumn.removeToggles();
            }

            final UiHintContainer hintContainer = getUiHintContainer();
            if(hintContainer != null) {
                hintContainer.setHint(table, HINT_KEY_SHOW_ALL, "true");
            }
            target.add(table);
        });

        Wkt.labelAdd(container, "prototypingLabel", new PrototypingMessageProvider(getMetaModelContext())
                .getTookTimingMessageModel());

    }

    private MarkupContainer navigatorContainer() {
        return ((MarkupContainer)get(navigatorContainerId));
    }

    void honourHints() {
        UiHintContainer uiHintContainer = getUiHintContainer();
        if(uiHintContainer == null) {
            return;
        }

        final DataTable<?, ?> table = getTable();
        final String showAll = uiHintContainer.getHint(table, HINT_KEY_SHOW_ALL);
        if(showAll != null) {
            showAllItemsOn(table);
        }
    }

    static void showAllItemsOn(final DataTable<?, ?> table) {
        table.setItemsPerPage(Long.MAX_VALUE);
    }

    private UiHintContainer getUiHintContainer() {
        return UiHintContainer.Util.hintContainerOf(this, UiObjectWkt.class);
    }

    private transient MetaModelContext mmc;
    @Override
    public MetaModelContext getMetaModelContext() {
        return mmc = WktContext.computeIfAbsent(mmc);
    }

}
