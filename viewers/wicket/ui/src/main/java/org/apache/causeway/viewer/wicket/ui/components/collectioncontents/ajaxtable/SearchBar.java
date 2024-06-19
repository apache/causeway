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

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.OnChangeAjaxBehavior;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;

import org.apache.causeway.core.metamodel.tabular.DataTableInteractive;
import org.apache.causeway.viewer.wicket.ui.util.Wkt;

import lombok.Getter;
import lombok.NonNull;

public class SearchBar extends Panel {

    private static final long serialVersionUID = 1L;
    private static final String ID_TABLE_SEARCH_INPUT = "table-search-input";

    /**
     * DataTable this search bar is attached to.
     */
    @Getter
    private final DataTable<?, ?> table;

    public SearchBar(final String id, final DataTable<?, ?> table) {
        super(id);
        this.table = table;
    }

    void bindSearchField(
            final @NonNull GenericPanel<DataTableInteractive> dataTableInteractiveHolder) {
        // init searchArg from interactive model
        var dataTableInteractive = dataTableInteractiveHolder.getModelObject();
        var searchField = new TextField<>(ID_TABLE_SEARCH_INPUT, Model.of(dataTableInteractive.getSearchArgument().getValue()));
        Wkt.attributeReplace(searchField, "placeholder", dataTableInteractive.getSearchPromptPlaceholderText());

        searchField.add(new OnChangeAjaxBehavior() {
            private static final long serialVersionUID = 1L;
            @Override
            protected void onUpdate(final AjaxRequestTarget target) {
                // on searchArg update originating from end-user in UI,
                // update the interactive model
                var searchArg = searchField.getValue();
                var dataTableInteractive = dataTableInteractiveHolder.getModelObject();
                dataTableInteractive.getSearchArgument().setValue(searchArg);
                // tells the table component to re-render
                target.add(table);
            }
        });

        add(searchField);
    }

    /**
     * Only shows this search bar when there are more than 1 rows.
     */
    @Override
    protected void onConfigure() {
        super.onConfigure();
        setVisible(getTable().getRowCount() > 1);
    }

}
