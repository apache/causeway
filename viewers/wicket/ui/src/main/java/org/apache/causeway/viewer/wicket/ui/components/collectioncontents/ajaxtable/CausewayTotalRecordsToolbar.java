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

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractToolbar;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.NoRecordsToolbar;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LambdaModel;
import org.apache.wicket.model.Model;

import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.viewer.wicket.model.models.HasCommonContext;
import org.apache.causeway.viewer.wicket.model.util.WktContext;
import org.apache.causeway.viewer.wicket.ui.util.Wkt;

/**
 * Responsibility: Display 'Showing all of 123' at the bottom of data tables.
 * <p>
 * Implementation Note: this is almost a copy of {@link NoRecordsToolbar}
 *
 * @since 2.0
 */
public class CausewayTotalRecordsToolbar extends AbstractToolbar
implements HasCommonContext {

    private static final long serialVersionUID = 1L;
    private static final String navigatorContainerId = "navigatorContainer";

    public CausewayTotalRecordsToolbar(final DataTable<?, ?> table) {

        this(table, new Model<String>() {

            private static final long serialVersionUID = 1L;

            @Override
            public String getObject() {
                return String.format("Showing all of %d",
                        table.getRowCount());
            }

        });

    }

    /**
     * @param table
     *            data table this toolbar will be attached to
     * @param messageModel
     *            model that will be used to display the "total records" message
     */
    protected CausewayTotalRecordsToolbar(final DataTable<?, ?> table, final IModel<String> messageModel) {
        super(table);

        WebMarkupContainer container = new WebMarkupContainer(navigatorContainerId);
        add(container);

        container.add(AttributeModifier.replace("colspan", LambdaModel.of(()->
            String.valueOf(table.getColumns().size()).intern())));

        Wkt.labelAdd(container, "navigatorLabel", messageModel);
        Wkt.labelAdd(container, "prototypingLabel", new PrototypingMessageProvider(getMetaModelContext())
                .getTookTimingMessageModel());
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

    private transient MetaModelContext mmc;
    @Override
    public MetaModelContext getMetaModelContext() {
        return mmc = WktContext.computeIfAbsent(mmc);
    }

}
