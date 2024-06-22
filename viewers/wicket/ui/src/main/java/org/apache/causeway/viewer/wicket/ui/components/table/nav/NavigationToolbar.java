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
package org.apache.causeway.viewer.wicket.ui.components.table.nav;

import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.extensions.ajax.markup.html.repeater.data.table.AjaxNavigationToolbar;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.markup.html.navigation.paging.PagingNavigator;

import org.apache.causeway.viewer.wicket.model.hints.UiHintContainer;
import org.apache.causeway.viewer.wicket.model.models.HasCommonContext;
import org.apache.causeway.viewer.wicket.model.models.UiObjectWkt;
import org.apache.causeway.viewer.wicket.model.timetaken.TimeTakenModel;
import org.apache.causeway.viewer.wicket.ui.components.collectioncontents.ajaxtable.columns.ToggleboxColumn;
import org.apache.causeway.viewer.wicket.ui.components.table.DataTableWithPagesAndFilter;
import org.apache.causeway.viewer.wicket.ui.components.table.internal._TableUtils;
import org.apache.causeway.viewer.wicket.ui.components.table.nav.pagesize.PagesizeChooser;
import org.apache.causeway.viewer.wicket.ui.components.table.nav.paging.PageNavigator;
import org.apache.causeway.viewer.wicket.ui.util.Wkt;

public class NavigationToolbar extends AjaxNavigationToolbar
implements HasCommonContext {
    private static final long serialVersionUID = 1L;

    private static final String NAVIGATOR_CONTAINER_ID = "span";
    private static final String ID_PAGESIZE_CHOOSER = "pagesizeChooser";
    private static final String ID_SHOW_ALL = "showAll";
    private static final String HINT_KEY_SHOW_ALL = "showAll";

    private final ToggleboxColumn toggleboxColumn;

    public NavigationToolbar(
            final DataTableWithPagesAndFilter<?, ?> table,
            final ToggleboxColumn toggleboxColumn) {

        super(table);
        this.toggleboxColumn = toggleboxColumn;
        buildGui();
    }

    @Override
    protected PagingNavigator newPagingNavigator(final String navigatorId, final DataTable<?, ?> table) {
        return new PageNavigator(navigatorId, table);
    }

    @Override
    protected void onConfigure() {
        super.onConfigure();
        honorShowAllHints();
    }

    @Override
    protected DataTableWithPagesAndFilter<?, ?> getTable() {
        return (DataTableWithPagesAndFilter<?, ?>) super.getTable();
    }

    // -- HELPER

    private void buildGui() {
        var navigatorContainer = navigatorContainer();

        Wkt.add(navigatorContainer,
                new PagesizeChooser(ID_PAGESIZE_CHOOSER, getTable()));

        addShowAllButton(navigatorContainer);

        Wkt.labelAdd(navigatorContainer, "prototypingLabel",
                TimeTakenModel.createForPrototypingElseBlank(getMetaModelContext()));
    }

    private void addShowAllButton(final MarkupContainer container) {
        Wkt.linkAdd(container, ID_SHOW_ALL, target->{

            var table = getTable();

            showAllItemsOn(table);

            if(toggleboxColumn != null) {
                // clear the underlying backend selection model
                _TableUtils.interactive(table).getSelectAllToggle().setValue(false);
                // remove toggle UI components
                toggleboxColumn.removeToggles();
            }

            setShowAllHintActive(table);
            target.add(table);
        });
    }

    private MarkupContainer navigatorContainer() {
        return ((MarkupContainer)get(NAVIGATOR_CONTAINER_ID));
    }

    private void honorShowAllHints() {
        var uiHintContainer = getUiHintContainer();
        if(uiHintContainer == null) return;

        var table = getTable();
        final String showAll = uiHintContainer.getHint(table, HINT_KEY_SHOW_ALL);
        if(showAll != null) {
            showAllItemsOn(table);
        }
    }

    private static void showAllItemsOn(final DataTable<?, ?> table) {
        table.setItemsPerPage(Long.MAX_VALUE);
    }

    private UiHintContainer getUiHintContainer() {
        return UiHintContainer.Util.hintContainerOf(this, UiObjectWkt.class);
    }

    private void setShowAllHintActive(final Component table) {
        final UiHintContainer hintContainer = getUiHintContainer();
        if(hintContainer != null) {
            hintContainer.setHint(table, HINT_KEY_SHOW_ALL, "true");
        }
    }

}
