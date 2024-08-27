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

import org.apache.wicket.MarkupContainer;
import org.apache.wicket.extensions.ajax.markup.html.repeater.data.table.AjaxNavigationToolbar;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.markup.html.navigation.paging.PagingNavigator;

import org.apache.causeway.viewer.wicket.model.models.HasCommonContext;
import org.apache.causeway.viewer.wicket.model.timetaken.TimeTakenModel;
import org.apache.causeway.viewer.wicket.ui.components.table.DataTableWithPagesAndFilter;
import org.apache.causeway.viewer.wicket.ui.components.table.nav.pageact.PageActionChooser;
import org.apache.causeway.viewer.wicket.ui.components.table.nav.pagesize.PagesizeChooser;
import org.apache.causeway.viewer.wicket.ui.components.table.nav.paging.PageNavigator;
import org.apache.causeway.viewer.wicket.ui.util.Wkt;

public class NavigationToolbar extends AjaxNavigationToolbar
implements HasCommonContext {
    private static final long serialVersionUID = 1L;

    private static final String NAVIGATOR_CONTAINER_ID = "span";
    private static final String ID_PAGE_ACTION_CHOOSER = "pageActionChooser";
    private static final String ID_PAGESIZE_CHOOSER = "pagesizeChooser";

    /**
     * @param table data-table this tool-bar is attached to
     */
    public NavigationToolbar(
            final DataTableWithPagesAndFilter<?, ?> table) {
        super(table);
        buildGui();
    }

    @Override
    protected PagingNavigator newPagingNavigator(final String navigatorId, final DataTable<?, ?> table) {
        return new PageNavigator(navigatorId, table);
    }

    @Override
    protected DataTableWithPagesAndFilter<?, ?> getTable() {
        return (DataTableWithPagesAndFilter<?, ?>) super.getTable();
    }

    // -- HELPER

    private void buildGui() {
        var navigatorContainer = navigatorContainer();

        Wkt.add(navigatorContainer,
                new PageActionChooser(ID_PAGE_ACTION_CHOOSER, getTable()));

        Wkt.add(navigatorContainer,
                new PagesizeChooser(ID_PAGESIZE_CHOOSER, getTable()));

        Wkt.labelAdd(navigatorContainer, "prototypingLabel",
                TimeTakenModel.createForPrototypingElseBlank(getMetaModelContext()));
    }

    private MarkupContainer navigatorContainer() {
        return ((MarkupContainer)get(NAVIGATOR_CONTAINER_ID));
    }

}
