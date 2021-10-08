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

import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.core.metamodel.interactions.managed.nonscalar.DataRow;
import org.apache.isis.core.runtime.context.IsisAppCommonContext;
import org.apache.isis.viewer.wicket.ui.components.widgets.checkbox.ContainedToggleboxPanel;
import org.apache.isis.viewer.wicket.ui.util.CssClassAppender;

import lombok.val;

public final class GenericToggleboxColumn
extends GenericColumnAbstract {

    private static final long serialVersionUID = 1L;

    public static enum BulkToggle {
        CLEAR_ALL, SET_ALL;
        static BulkToggle valueOf(final boolean b) {
            return b ? SET_ALL : CLEAR_ALL;
        }
        public boolean isSetAll() { return this == SET_ALL; }
    }


    public GenericToggleboxColumn(
            final IsisAppCommonContext commonContext) {
        super(commonContext, "");
    }

    @Override
    public Component getHeader(final String componentId) {

        val bulkToggle = new ContainedToggleboxPanel(componentId) {
            private static final long serialVersionUID = 1L;
            @Override
            public void onSubmit(final AjaxRequestTarget target) {
                val bulkToggle = BulkToggle.valueOf(!this.isChecked());
                //System.err.printf("bulkToggle: %s%n", bulkToggle);
                for (ContainedToggleboxPanel rowToggle : rowToggles) {
                    if(rowToggle.smartSet(bulkToggle, target)) {
                        target.add(rowToggle);
                    }
                }
            }
        };
        bulkToggle.add(new CssClassAppender("title-column"));
        return bulkToggle;
    }

    private final List<ContainedToggleboxPanel> rowToggles = _Lists.newArrayList();

    @Override
    public void populateItem(
            final Item<ICellPopulator<DataRow>> cellItem,
            final String componentId,
            final IModel<DataRow> rowModel) {

        cellItem.add(new CssClassAppender("togglebox-column"));

        final MarkupContainer row = cellItem.getParent().getParent();
        row.setOutputMarkupId(true);

        val rowToggle = new ContainedToggleboxPanel(componentId) {
            private static final long serialVersionUID = 1L;
            @Override
            public void onSubmit(final AjaxRequestTarget target) {
                val isChecked = rowModel.getObject().getSelectToggle().toggleThenGet();
                // no matter what, the underlying backend model must by reflect by the UI
                setModel(isChecked);
            }
        };
        rowToggles.add(rowToggle);
        rowToggle.setOutputMarkupId(true);
        cellItem.add(rowToggle);
    }

    public void removeToggles() {
        rowToggles.clear();
    }


}