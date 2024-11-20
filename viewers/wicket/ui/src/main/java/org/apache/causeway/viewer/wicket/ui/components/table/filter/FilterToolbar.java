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
package org.apache.causeway.viewer.wicket.ui.components.table.filter;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.OnChangeAjaxBehavior;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;

import org.apache.causeway.viewer.wicket.ui.components.table.DataTableWithPagesAndFilter;
import org.apache.causeway.viewer.wicket.ui.components.table.internal._TableUtils;
import org.apache.causeway.viewer.wicket.ui.util.Wkt;

import lombok.Getter;

public class FilterToolbar extends Panel {

    private static final long serialVersionUID = 1L;
    private static final String ID_TABLE_SEARCH_INPUT = "table-search-input";
    private static final String ID_TABLE_SEARCH_BUTTON = "table-search-button";
    private static final String ID_TABLE_SEARCH_CLEAR = "table-search-clear";

    /**
     * DataTable this search bar is attached to.
     */
    @Getter
    private final DataTableWithPagesAndFilter<?, ?> table;

    public FilterToolbar(final String id, final DataTableWithPagesAndFilter<?, ?> table) {
        super(id);
        this.table = table;
    }

    /**
     * Only shows this search bar when there are more than 1 rows.
     */
    @Override
    protected void onConfigure() {
        super.onConfigure();
        buildGui();
        setVisible(getTable().getRowCount() > 1);
    }

    // -- HELPER

    private void buildGui() {

        var dataTableInteractive = _TableUtils.interactive(table);
        var searchField = new TextField<>(ID_TABLE_SEARCH_INPUT, Model.of(dataTableInteractive.getSearchArgument().getValue()));
        Wkt.attributeReplace(searchField, "placeholder", dataTableInteractive.getSearchPromptPlaceholderText());

        Wkt.linkAdd(this, ID_TABLE_SEARCH_BUTTON, target->{
            propagateUpdate(target, searchField.getValue());
        });

        Wkt.linkAdd(this, ID_TABLE_SEARCH_CLEAR, target->{
            searchField.setModelObject(null);
            target.add(searchField);
            propagateUpdate(target, searchField.getValue());
        });

        searchField.add(new OnChangeAjaxBehavior() {
            private static final long serialVersionUID = 1L;
            @Override
            protected void onUpdate(final AjaxRequestTarget target) {
                // no-op; however, it seems we need to install this behavior,
                // in order for the server side model to receive updates at all
            }
        });

        addOrReplace(searchField);
    }

    private void propagateUpdate(final AjaxRequestTarget target, final String searchArg) {
        // on searchArg update originating from end-user in UI
        table.setSearchArg(searchArg);
        table.setSearchHintAndBroadcast(target);
        // tells the table component to re-render
        target.add(table);
    }

}
