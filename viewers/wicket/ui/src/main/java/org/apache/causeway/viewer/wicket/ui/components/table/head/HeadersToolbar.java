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

import org.apache.wicket.extensions.ajax.markup.html.repeater.data.table.AjaxFallbackHeadersToolbar;
import org.apache.wicket.extensions.markup.html.repeater.data.sort.ISortStateLocator;
import org.apache.wicket.extensions.markup.html.repeater.data.sort.SortOrder;
import org.apache.wicket.markup.html.WebMarkupContainer;

import org.apache.causeway.commons.internal.base._Casts;
import org.apache.causeway.core.config.CausewayConfiguration;
import org.apache.causeway.viewer.wicket.model.hints.UiHintContainer;
import org.apache.causeway.viewer.wicket.model.models.UiObjectWkt;
import org.apache.causeway.viewer.wicket.ui.components.collectioncontents.ajaxtable.CollectionContentsSortableDataProvider;
import org.apache.causeway.viewer.wicket.ui.components.table.DataTableWithPagesAndFilter;
import org.apache.causeway.viewer.wicket.ui.util.Wkt;

/**
 * Adapted from Wicket's own {@link AjaxFallbackHeadersToolbar}.
 */
public class HeadersToolbar
extends HeadersToolbarAbstract<String> {

    private static final long serialVersionUID = 1L;
    private final CollectionContentsSortableDataProvider singleSortStateLocator;
    private DataTableWithPagesAndFilter<?, String> table;

    /**
     * @param table data-table this tool-bar is attached to
     */
    public HeadersToolbar(
            final DataTableWithPagesAndFilter<?, String> table,
            final CollectionContentsSortableDataProvider singleSortStateLocator,
            final CausewayConfiguration.Viewer.Wicket wicketConfig) {
        super(table, _Casts.uncheckedCast(singleSortStateLocator), wicketConfig);
        this.table = table;
        this.singleSortStateLocator = singleSortStateLocator;
        Wkt.ajaxEnable(table);
    }

    @Override
    protected WebMarkupContainer newSortableHeader(final String borderId, final String property,
            final ISortStateLocator<String> locator) {
        return new OrderByBorder<String>(borderId, table, property, locator);
    }

    @Override
    protected void onConfigure() {
        super.onConfigure();
        honorSortOrderHints();
    }

    // -- HELPER

    private void honorSortOrderHints() {
        var uiHintContainer = getUiHintContainer();
        if(uiHintContainer == null) return;

        for (SortOrder sortOrder : SortOrder.values()) {
            String property = uiHintContainer.getHint(table, sortOrder.name());
            if(property != null) {
                singleSortStateLocator.getSortState().setPropertySortOrder(property, sortOrder);
            }
        }
    }

    private UiHintContainer getUiHintContainer() {
        return UiHintContainer.Util.hintContainerOf(this, UiObjectWkt.class);
    }

}
