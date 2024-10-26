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
package org.apache.causeway.viewer.wicket.ui.components.table;

import java.util.List;

import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.NoRecordsToolbar;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.model.IModel;

import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.core.metamodel.object.ManagedObjects;
import org.apache.causeway.core.metamodel.tabular.DataRow;
import org.apache.causeway.viewer.wicket.model.itemreuse.ReuseIfRowIndexEqualsStrategy;
import org.apache.causeway.viewer.wicket.ui.components.collectioncontents.ajaxtable.CollectionContentsSortableDataProvider;
import org.apache.causeway.viewer.wicket.ui.components.table.head.HeadersToolbar;
import org.apache.causeway.viewer.wicket.ui.components.table.nav.NavigationToolbar;
import org.apache.causeway.viewer.wicket.ui.components.table.nonav.TotalRecordsToolbar;
import org.apache.causeway.viewer.wicket.ui.util.Wkt;

public class CausewayAjaxDataTable extends DataTableWithPagesAndFilter<DataRow, String> {

    private static final long serialVersionUID = 1L;

    private final CollectionContentsSortableDataProvider dataProvider;

    public CausewayAjaxDataTable(
            final String id,
            final List<? extends IColumn<DataRow, String>> columns,
            final CollectionContentsSortableDataProvider dataProvider,
            final int rowsPerPage) {
        super(id, columns, dataProvider, rowsPerPage);
        this.dataProvider = dataProvider;
        // optimization
        setItemReuseStrategy(ReuseIfRowIndexEqualsStrategy.getInstance());
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();
        buildGui();
    }

    private void buildGui() {
        var wicketConfig = MetaModelContext.instanceElseFail().getConfiguration().getViewer().getWicket();

        addTopToolbar(new HeadersToolbar(this, this.dataProvider, wicketConfig));

        if (!isDecoratedWithDataTablesNet()) {
            // toolbars do decide for themselves, whether they are visible
            addBottomToolbar(new NavigationToolbar(this));
            addBottomToolbar(new NoRecordsToolbar(this));
            addBottomToolbar(new TotalRecordsToolbar(this));
        }
    }

    public boolean isDecoratedWithDataTablesNet() {
        IDataProvider<?> dataProvider = getDataProvider();
        return dataProvider instanceof CollectionContentsSortableDataProvider &&
                ((CollectionContentsSortableDataProvider) dataProvider).isDecoratedWithDataTablesNet();
    }

    @Override
    protected Item<DataRow> newRowItem(final String id, final int index, final IModel<DataRow> model) {
        return Wkt.oddEvenItem(id, index, model, CausewayAjaxDataTable::cssClassForRow);
    }

    // -- HELPER

    private static String cssClassForRow(final DataRow model) {
        if(model==null
                || ManagedObjects.isNullOrUnspecifiedOrEmpty(model.getRowElement())) {
            return null;
        }
        var rowElement = model.getRowElement();
        return rowElement.getSpecification().getCssClass(rowElement);
    }

}
