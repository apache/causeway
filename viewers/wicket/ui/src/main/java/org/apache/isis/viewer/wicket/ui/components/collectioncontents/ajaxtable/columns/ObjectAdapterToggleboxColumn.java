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

import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;

import org.apache.isis.core.commons.internal.collections._Lists;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.webapp.context.IsisWebAppCommonContext;
import org.apache.isis.viewer.wicket.model.common.OnSelectionHandler;
import org.apache.isis.viewer.wicket.model.models.EntityModel;
import org.apache.isis.viewer.wicket.ui.components.widgets.checkbox.ContainedToggleboxPanel;
import org.apache.isis.viewer.wicket.ui.util.CssClassAppender;

import lombok.val;

public final class ObjectAdapterToggleboxColumn extends ColumnAbstract<ManagedObject> {

    private static final long serialVersionUID = 1L;


    public ObjectAdapterToggleboxColumn(IsisWebAppCommonContext commonContext) {
        this(commonContext, null);
    }

    public ObjectAdapterToggleboxColumn(
            IsisWebAppCommonContext commonContext,
            OnSelectionHandler onSelectionHandler) {
        
        super(commonContext, "");
        this.onSelectionHandler = onSelectionHandler;
    }

    // -- OnSelectionHandler
    private OnSelectionHandler onSelectionHandler;
    public OnSelectionHandler getOnSelectionHandler() {
        return onSelectionHandler;
    }

    public void setOnSelectionHandler(OnSelectionHandler onSelectionHandler) {
        this.onSelectionHandler = onSelectionHandler;
    }


    @Override
    public Component getHeader(String componentId) {

        val bulkToggle = new ContainedToggleboxPanel(componentId) {
            private static final long serialVersionUID = 1L;
            @Override
            public void onSubmit(AjaxRequestTarget target) {
                val setToChecked = !this.isChecked();
                
                for (ContainedToggleboxPanel rowToggle : rowToggles) {
                    
                    // smart update idiom: only toggle when needed
                    if(setToChecked == rowToggle.isChecked()) {
                        continue;
                    }
                    
                    rowToggle.toggle(target);
                    target.add(rowToggle);
                }
            }
        };
        bulkToggle.add(new CssClassAppender("title-column"));
        return bulkToggle;
    }

    private final List<ContainedToggleboxPanel> rowToggles = _Lists.newArrayList();

    @Override
    public void populateItem(
            final Item<ICellPopulator<ManagedObject>> cellItem, 
            final String componentId, 
            final IModel<ManagedObject> rowModel) {

        cellItem.add(new CssClassAppender("togglebox-column"));

        final MarkupContainer row = cellItem.getParent().getParent();
        row.setOutputMarkupId(true);

        val rowToggle = new ContainedToggleboxPanel(componentId) {
            private static final long serialVersionUID = 1L;
            @Override
            public void onSubmit(AjaxRequestTarget target) {
                
                val entityModel = (EntityModel) rowModel;
                val selectedAdapter = entityModel.getManagedObject();
                
                if(onSelectionHandler != null) {
                    onSelectionHandler.onSelected(this, selectedAdapter, target);
                }
            }
        };
        rowToggles.add(rowToggle);
        rowToggle.setOutputMarkupId(true);
        cellItem.add(rowToggle);
    }

    public void clearToggles() {
        rowToggles.clear();
    }


}