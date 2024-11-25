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

import org.apache.causeway.commons.internal.collections._Lists;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.viewer.wicket.model.models.interaction.coll.DataRowToggleWkt;
import org.apache.causeway.viewer.wicket.model.models.interaction.coll.DataRowWkt;
import org.apache.causeway.viewer.wicket.ui.components.table.DataTableWithPagesAndFilter;
import org.apache.causeway.viewer.wicket.ui.components.table.nav.pageact.PageActionChooser;
import org.apache.causeway.viewer.wicket.ui.components.widgets.checkbox.ContainedToggleboxPanel;
import org.apache.causeway.viewer.wicket.ui.util.Wkt;

public final class ToggleboxColumn
extends GenericColumnAbstract {

    private static final long serialVersionUID = 1L;

    private final List<ContainedToggleboxPanel> rowToggles = _Lists.newArrayList();
    private DataTableWithPagesAndFilter<?, ?> table;

    public ToggleboxColumn(
            final ObjectSpecification elementType,
            final DataTableWithPagesAndFilter<?, ?> table) {
        super(elementType, "");
        this.table = table;
    }

    public void removeToggles() {
        rowToggles.clear();
    }

    @Override
    protected Component createCellComponent(final String componentId, final DataRowWkt dataRowWkt) {
        var dataRowToggle = new DataRowToggleWkt(dataRowWkt);
        var rowToggle = new ContainedToggleboxPanel(componentId, dataRowToggle);
        rowToggles.add(rowToggle);
        return rowToggle;
    }

    @Override
    public Component getHeader(final String componentId) {
        var pageActionChooser = new PageActionChooser(componentId, table);
        Wkt.cssAppend(pageActionChooser, "togglebox-column");
        return pageActionChooser;
    }

}