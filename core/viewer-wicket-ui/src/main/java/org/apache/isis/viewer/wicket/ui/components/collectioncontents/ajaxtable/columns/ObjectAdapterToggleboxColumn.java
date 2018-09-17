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

package org.apache.isis.viewer.wicket.ui.components.collectioncontents.ajaxtable.columns;

import java.util.List;
import org.apache.isis.commons.internal.collections._Lists;
import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.concurrency.ConcurrencyChecking;
import org.apache.isis.core.metamodel.adapter.version.ConcurrencyException;
import org.apache.isis.viewer.wicket.model.common.OnConcurrencyExceptionHandler;
import org.apache.isis.viewer.wicket.model.common.OnSelectionHandler;
import org.apache.isis.viewer.wicket.model.models.EntityModel;
import org.apache.isis.viewer.wicket.ui.components.widgets.checkbox.ContainedToggleboxPanel;
import org.apache.isis.viewer.wicket.ui.util.CssClassAppender;

public final class ObjectAdapterToggleboxColumn extends ColumnAbstract<ObjectAdapter> {

    private static final long serialVersionUID = 1L;


    public ObjectAdapterToggleboxColumn() {
        this(null, null);
    }

    public ObjectAdapterToggleboxColumn(
            final OnSelectionHandler onSelectionHandler,
            final OnConcurrencyExceptionHandler onConcurrencyExceptionHandler) {
        super("");
        this.onSelectionHandler = onSelectionHandler;
        this.onConcurrencyExceptionHandler = onConcurrencyExceptionHandler;
    }

    // -- OnSelectionHandler
    private OnSelectionHandler onSelectionHandler;
    public OnSelectionHandler getOnSelectionHandler() {
        return onSelectionHandler;
    }

    public void setOnSelectionHandler(OnSelectionHandler onSelectionHandler) {
        this.onSelectionHandler = onSelectionHandler;
    }


    // -- OnConcurrencyExceptionHandler

    private OnConcurrencyExceptionHandler onConcurrencyExceptionHandler;
    public OnConcurrencyExceptionHandler getOnConcurrencyExceptionHandler() {
        return onConcurrencyExceptionHandler;
    }

    public void setOnConcurrencyExceptionHandler(OnConcurrencyExceptionHandler onConcurrencyExceptionHandler) {
        this.onConcurrencyExceptionHandler = onConcurrencyExceptionHandler;
    }



    @Override
    public Component getHeader(String componentId) {

        final ContainedToggleboxPanel toggle = new ContainedToggleboxPanel(componentId) {
            private static final long serialVersionUID = 1L;
            @Override
            public void onSubmit(AjaxRequestTarget target) {
                for (ContainedToggleboxPanel toggle : rowToggles) {
                    toggle.toggle(target);
                    target.add(toggle);
                }
            }
        };
        toggle.add(new CssClassAppender("title-column"));
        return toggle;
    }

    private final List<ContainedToggleboxPanel> rowToggles = _Lists.newArrayList();

    @Override
    public void populateItem(final Item<ICellPopulator<ObjectAdapter>> cellItem, final String componentId, final IModel<ObjectAdapter> rowModel) {

        cellItem.add(new CssClassAppender("togglebox-column"));

        final MarkupContainer row = cellItem.getParent().getParent();
        row.setOutputMarkupId(true);
        final EntityModel entityModel = (EntityModel) rowModel;
        String concurrencyExceptionIfAny = entityModel.getAndClearConcurrencyExceptionIfAny();
        if(concurrencyExceptionIfAny != null) {
            row.add(new CssClassAppender("reloaded-after-concurrency-exception"));
        }

        final ContainedToggleboxPanel toggle = new ContainedToggleboxPanel(componentId) {
            private static final long serialVersionUID = 1L;
            @Override
            public void onSubmit(AjaxRequestTarget target) {
                final EntityModel entityModel = (EntityModel) rowModel;
                ObjectAdapter selectedAdapter = null;
                try {
                    selectedAdapter = entityModel.load(ConcurrencyChecking.CHECK);
                    if(onSelectionHandler != null) {
                        onSelectionHandler.onSelected(this, selectedAdapter, target);
                    }
                } catch(ConcurrencyException ex) {

                    // should work second time, because the previous attempt will have updated the OAM's OIDs version.
                    selectedAdapter = entityModel.load(ConcurrencyChecking.CHECK);
                    if(onConcurrencyExceptionHandler != null) {
                        onConcurrencyExceptionHandler.onConcurrencyException(this, selectedAdapter, ex, target);
                    }

                    entityModel.setException(ex);
                }
            }
        };
        rowToggles.add(toggle);
        toggle.setOutputMarkupId(true);
        cellItem.add(toggle);
    }

    public void clearToggles() {
        rowToggles.clear();
    }


}