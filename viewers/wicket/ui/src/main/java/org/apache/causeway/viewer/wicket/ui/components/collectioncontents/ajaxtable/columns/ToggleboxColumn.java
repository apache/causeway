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
package org.apache.causeway.viewer.wicket.ui.components.collectioncontents.ajaxtable.columns;

import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IModel;

import org.apache.causeway.commons.internal.collections._Lists;
import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.core.metamodel.tabular.interactive.DataRow;
import org.apache.causeway.core.metamodel.tabular.interactive.DataTableInteractive;
import org.apache.causeway.viewer.wicket.ui.components.widgets.checkbox.ContainedToggleboxPanel;
import org.apache.causeway.viewer.wicket.ui.util.Wkt;

import lombok.val;

public final class ToggleboxColumn
extends GenericColumnAbstract {

    private static final long serialVersionUID = 1L;

    public static enum BulkToggle {
        CLEAR_ALL, SET_ALL;
        static BulkToggle valueOf(final boolean b) {
            return b ? SET_ALL : CLEAR_ALL;
        }
        public boolean isSetAll() { return this == SET_ALL; }
    }

    private IModel<DataTableInteractive> dataTableModelHolder;

    public ToggleboxColumn(
            final MetaModelContext commonContext,
            final IModel<DataTableInteractive> dataTableModelHolder) {
        super(commonContext, "");
        this.dataTableModelHolder = dataTableModelHolder;
    }

    @Override
    protected Component createCellComponent(
            final String componentId, final DataRow dataRow, final IModel<Boolean> dataRowToggle) {
        val rowToggle = new ContainedToggleboxPanel(componentId, dataRowToggle);
        rowToggles.add(rowToggle);
        return rowToggle.setOutputMarkupId(true);
    }

    @Override
    public Component getHeader(final String componentId) {
        val bulkToggle = new ContainedToggleboxPanel(
                componentId,
                new BulkToggleWkt(dataTableModelHolder),
                this::onBulkUpdate);
        Wkt.cssAppend(bulkToggle, "title-column");
        return bulkToggle;
    }

    private void onBulkUpdate(final Boolean isChecked, final AjaxRequestTarget target) {
        val bulkToggle = BulkToggle.valueOf(isChecked);
        for (ContainedToggleboxPanel rowToggle : rowToggles) {
            rowToggle.set(bulkToggle, target);
        }
    }

    private final List<ContainedToggleboxPanel> rowToggles = _Lists.newArrayList();

    public void removeToggles() {
        rowToggles.clear();
    }

}