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

import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.extensions.ajax.markup.html.repeater.data.table.AjaxNavigationToolbar;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.markup.html.navigation.paging.PagingNavigator;


public class IsisAjaxNavigationToolbar extends AjaxNavigationToolbar {

    private static final long serialVersionUID = 1L;

    public IsisAjaxNavigationToolbar(final DataTable<?, ?> table) {
        super(table);

        addShowAllButton(table);
    }

    private void addShowAllButton(final DataTable<?, ?> table) {
        table.setOutputMarkupId(true);

        ((MarkupContainer)get("span")).add(new AjaxLink("showAll") {

            @Override
            public void onClick(AjaxRequestTarget target) {
                table.setItemsPerPage(Long.MAX_VALUE);
                target.add(table);
            }
        });
    }

    @Override
    protected PagingNavigator newPagingNavigator(String navigatorId, DataTable<?, ?> table) {
        return new IsisAjaxPagingNavigator(navigatorId, table);
    }
    
}
