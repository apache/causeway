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

import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.viewer.wicket.model.common.SelectionHandler;
import org.apache.isis.viewer.wicket.ui.components.widgets.buttons.ContainedButton;

public final class ObjectAdapterSelectColumn extends ColumnAbstract<ObjectAdapter> {

    private static final long serialVersionUID = 1L;

    private final SelectionHandler handler;

    public ObjectAdapterSelectColumn(final IModel<String> columnNameModel, final SelectionHandler handler) {
        super(columnNameModel);
        this.handler = handler;
    }

    @Override
    public void populateItem(final Item<ICellPopulator<ObjectAdapter>> cellItem, final String componentId, final IModel<ObjectAdapter> rowModel) {

        // TODO: i18n
        cellItem.add(new ContainedButton(componentId, "select") {

            private static final long serialVersionUID = 1L;

            @Override
            public void onSubmit() {
                final IModel<ObjectAdapter> o = rowModel;
                final ObjectAdapter selectedAdapter = o.getObject();
                handler.onSelected(this, selectedAdapter);
            }
        });
    }
}