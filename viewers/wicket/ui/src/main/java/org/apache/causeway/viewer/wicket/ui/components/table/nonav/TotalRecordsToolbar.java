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
package org.apache.causeway.viewer.wicket.ui.components.table.nonav;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractToolbar;
import org.apache.wicket.extensions.markup.html.repeater.data.table.NoRecordsToolbar;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LambdaModel;

import org.apache.causeway.viewer.wicket.model.models.HasCommonContext;
import org.apache.causeway.viewer.wicket.model.timetaken.TimeTakenModel;
import org.apache.causeway.viewer.wicket.ui.components.table.DataTableWithPagesAndFilter;
import org.apache.causeway.viewer.wicket.ui.components.table.nav.pagesize.PagesizeChooser;
import org.apache.causeway.viewer.wicket.ui.util.Wkt;

/**
 * Responsibility: Display 'Showing all of 123' at the bottom of data tables.
 * <p>
 * Implementation Note: this is almost a copy of {@link NoRecordsToolbar}
 *
 * @since 2.0
 */
public class TotalRecordsToolbar extends AbstractToolbar
implements HasCommonContext {
    private static final long serialVersionUID = 1L;
    private static final String ID_NAVIGATOR_CONTAINER = "navigatorContainer";
    private static final String ID_PAGESIZE_CHOOSER = "pagesizeChooser";

    /**
     * @param table data-table this tool-bar is attached to
     */
    public TotalRecordsToolbar(final DataTableWithPagesAndFilter<?, ?> table) {
        this(table, LambdaModel.of(()->
                String.format("Showing all of %d", table.getRowCount())));
    }

    /**
     * @param table
     *            data table this toolbar will be attached to
     * @param messageModel
     *            model that will be used to display the "total records" message
     */
    protected TotalRecordsToolbar(final DataTableWithPagesAndFilter<?, ?> table, final IModel<String> messageModel) {
        super(table);

        var navigatorContainer = Wkt.add(this, new WebMarkupContainer(ID_NAVIGATOR_CONTAINER));

        Wkt.add(navigatorContainer,
                new PagesizeChooser(ID_PAGESIZE_CHOOSER, table));

        navigatorContainer.add(AttributeModifier.replace("colspan", LambdaModel.of(()->
            String.valueOf(table.getColumns().size()).intern())));

        Wkt.labelAdd(navigatorContainer, "navigatorLabel", messageModel);
        Wkt.labelAdd(navigatorContainer, "prototypingLabel",
                TimeTakenModel.createForPrototypingElseBlank(getMetaModelContext()));
    }

    /**
     * only shows this toolbar when there is only one page (when page navigation not available),
     * and when there are at least 6 elements in the list
     *
     */
    @Override
    protected void onConfigure() {
        super.onConfigure();
        if(getTable().getRowCount() <= 5) {
            setVisible(false);
            return;
        }
        setVisible(getTable().getPageCount() == 1);
    }
}
