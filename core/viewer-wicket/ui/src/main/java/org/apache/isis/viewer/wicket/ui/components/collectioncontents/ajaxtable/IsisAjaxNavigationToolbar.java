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

import javax.inject.Inject;

import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.extensions.ajax.markup.html.repeater.data.table.AjaxNavigationToolbar;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.navigation.paging.PagingNavigator;

import org.apache.isis.commons.internal.environment.IsisSystemEnvironment;
import org.apache.isis.viewer.wicket.model.hints.UiHintContainer;
import org.apache.isis.viewer.wicket.model.models.EntityCollectionModel;
import org.apache.isis.viewer.wicket.model.models.EntityModel;
import org.apache.isis.viewer.wicket.ui.components.collectioncontents.ajaxtable.columns.ObjectAdapterToggleboxColumn;

public class IsisAjaxNavigationToolbar extends AjaxNavigationToolbar {

    private static final long serialVersionUID = 1L;

    @Inject private transient IsisSystemEnvironment systemEnvironment;
    
    private static final String navigatorContainerId = "span";
    private static final String ID_SHOW_ALL = "showAll";
    private static final String HINT_KEY_SHOW_ALL = "showAll";
    private final ObjectAdapterToggleboxColumn toggleboxColumn;

    public IsisAjaxNavigationToolbar(
            final DataTable<?, ?> table, 
            final ObjectAdapterToggleboxColumn toggleboxColumn) {
        
        super(table);
        this.toggleboxColumn = toggleboxColumn;
        addShowAllButton(table);
    }

    @Override
    protected PagingNavigator newPagingNavigator(String navigatorId, DataTable<?, ?> table) {
        return new IsisAjaxPagingNavigator(navigatorId, table);
    }

    // -- HELPER

    private void addShowAllButton(final DataTable<?, ?> table) {
        table.setOutputMarkupId(true);

        final MarkupContainer container = navigatorContainer();

        container.add(new AjaxLink<Void>(ID_SHOW_ALL) {

            private static final long serialVersionUID = 1L;

            @Override
            public void onClick(AjaxRequestTarget target) {
                showAllItemsOn(table);

                final DataTable<?, ?> dataTable = getTable();
                final CollectionContentsSortableDataProvider dataProvider = (CollectionContentsSortableDataProvider) dataTable.getDataProvider();
                final EntityCollectionModel collectionModel = dataProvider.getEntityCollectionModel();

                if(toggleboxColumn != null) {
                    collectionModel.clearToggleMementosList();
                    toggleboxColumn.clearToggles();
                }

                final UiHintContainer hintContainer = getUiHintContainer();
                if(hintContainer != null) {
                    hintContainer.setHint(table, HINT_KEY_SHOW_ALL, "true");
                }
                target.add(table);
            }
        });

        if(systemEnvironment.isPrototyping()) {
            container.add(new Label("prototypingLabel", PrototypingMessageProvider.getTookTimingMessageModel()));    
        }
        
         

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
        return UiHintContainer.Util.hintContainerOf(this, EntityModel.class);
    }

}
