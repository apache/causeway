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

import org.apache.wicket.extensions.ajax.markup.html.repeater.data.table.AjaxFallbackHeadersToolbar;
import org.apache.wicket.extensions.markup.html.repeater.data.sort.ISortStateLocator;
import org.apache.wicket.extensions.markup.html.repeater.data.sort.SortOrder;
import org.apache.wicket.markup.html.WebMarkupContainer;

import org.apache.isis.commons.internal.base._Casts;
import org.apache.isis.viewer.wicket.model.hints.UiHintContainer;
import org.apache.isis.viewer.wicket.model.models.EntityModel;

/**
 * Adapted from Wicket's own {@link AjaxFallbackHeadersToolbar}.
 */
public class IsisAjaxHeadersToolbar
extends IsisAjaxHeadersToolbarAbstract<String> {

    private static final long serialVersionUID = 1L;
    private final CollectionContentsSortableDataProvider stateLocator;
    private IsisAjaxDataTable table;

    public IsisAjaxHeadersToolbar(
            final IsisAjaxDataTable table,
            final CollectionContentsSortableDataProvider stateLocator) {
        super(table, _Casts.uncheckedCast(stateLocator));
        this.table = table;
        table.setOutputMarkupId(true);
        this.stateLocator = stateLocator;
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();
    }

    // //////////////////////////////////////

    @Override
    protected WebMarkupContainer newSortableHeader(final String borderId, final String property,
            final ISortStateLocator<String> locator) {
        return new IsisAjaxFallbackOrderByBorder<String>(borderId, table, property, locator/*, getAjaxCallListener()*/);
    }

    //    /**
    //     * Returns a decorator that will be used to decorate ajax links used in sortable headers
    //     *
    //     * @return decorator or null for none
    //     */
    //    protected IAjaxCallListener getAjaxCallListener()
    //    {
    //        return null;
    //    }

    // //////////////////////////////////////

    void honourSortOrderHints() {
        UiHintContainer uiHintContainer = getUiHintContainer();
        if(uiHintContainer == null) {
            return;
        }

        for (SortOrder sortOrder : SortOrder.values()) {
            String property = uiHintContainer.getHint(table, sortOrder.name());
            if(property != null) {
                stateLocator.getSortState().setPropertySortOrder(property, sortOrder);
            }
        }
    }

    private UiHintContainer getUiHintContainer() {
        return UiHintContainer.Util.hintContainerOf(this, EntityModel.class);
    }

}
