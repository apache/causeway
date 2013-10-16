/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.isis.viewer.wicket.ui.components.collectioncontents.ajaxtable;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.IAjaxCallListener;
import org.apache.wicket.extensions.ajax.markup.html.repeater.data.sort.AjaxFallbackOrderByBorder;
import org.apache.wicket.extensions.ajax.markup.html.repeater.data.table.AjaxFallbackHeadersToolbar;
import org.apache.wicket.extensions.markup.html.repeater.data.sort.ISortStateLocator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.markup.html.WebMarkupContainer;


/**
 * Adapted from Wicket's own {@link AjaxFallbackHeadersToolbar}.
 */
public class IsisAjaxFallbackHeadersToolbar<S> extends IsisAjaxHeadersToolbar<S>
{
    private static final long serialVersionUID = 1L;

    public IsisAjaxFallbackHeadersToolbar(final DataTable<?, S> table, final ISortStateLocator<S> stateLocator)
    {
        super(table, stateLocator);
        table.setOutputMarkupId(true);
    }

    @Override
    protected WebMarkupContainer newSortableHeader(final String borderId, final S property,
        final ISortStateLocator<S> locator)
    {
        return new AjaxFallbackOrderByBorder<S>(borderId, property, locator, getAjaxCallListener())
        {
            private static final long serialVersionUID = 1L;

            @Override
            protected void onAjaxClick(final AjaxRequestTarget target)
            {
                target.add(getTable());
            }

            @Override
            protected void onSortChanged()
            {
                super.onSortChanged();
                getTable().setCurrentPage(0);
            }
        };
    }

    /**
     * Returns a decorator that will be used to decorate ajax links used in sortable headers
     * 
     * @return decorator or null for none
     */
    protected IAjaxCallListener getAjaxCallListener()
    {
        return null;
    }
}
