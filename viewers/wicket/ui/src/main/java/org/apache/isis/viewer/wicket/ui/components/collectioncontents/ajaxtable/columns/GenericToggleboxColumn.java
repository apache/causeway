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
import org.apache.isis.core.metamodel.interactions.managed.nonscalar.DataTableModel;
import org.apache.isis.core.runtime.context.IsisAppCommonContext;
import org.apache.isis.viewer.wicket.model.models.interaction.coll.DataRowWkt;
import org.apache.isis.viewer.wicket.ui.components.widgets.checkbox.ContainedToggleboxPanel;
import org.apache.isis.viewer.wicket.ui.util.Wkt;

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

    private IModel<DataTableModel> dataTableModelHolder;

    public GenericToggleboxColumn(
            final IsisAppCommonContext commonContext,
            final IModel<DataTableModel> dataTableModelHolder
            ) {
        super(commonContext, "");
        this.dataTableModelHolder = dataTableModelHolder;
    }

    @Override
    public Component getHeader(final String componentId) {

        val bulkToggle = new ContainedToggleboxPanel(componentId, new BulkToggleWkt(dataTableModelHolder)) {
            private static final long serialVersionUID = 1L;
            @Override public void onUpdate(final AjaxRequestTarget target) {
                val bulkToggle = BulkToggle.valueOf(this.isChecked());
                for (ContainedToggleboxPanel rowToggle : rowToggles) {
                    rowToggle.set(bulkToggle, target);
                }
            }
        };
        Wkt.cssAppend(bulkToggle, "title-column");
        return bulkToggle;
    }

    private final List<ContainedToggleboxPanel> rowToggles = _Lists.newArrayList();

    @Override
    public void populateItem(
            final Item<ICellPopulator<DataRow>> cellItem,
            final String componentId,
            final IModel<DataRow> rowModel) {

        Wkt.cssAppend(cellItem, "togglebox-column");

        final MarkupContainer row = cellItem.getParent().getParent();
        row.setOutputMarkupId(true);
        val rowToggle = new ContainedToggleboxPanel(componentId, ((DataRowWkt)rowModel).getDataRowToggle());
        rowToggles.add(rowToggle);
        rowToggle.setOutputMarkupId(true);
        cellItem.add(rowToggle);
    }

    public void removeToggles() {
        rowToggles.clear();
    }


}