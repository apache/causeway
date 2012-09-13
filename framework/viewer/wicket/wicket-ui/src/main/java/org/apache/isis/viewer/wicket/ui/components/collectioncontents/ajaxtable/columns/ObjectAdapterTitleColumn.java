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

import org.apache.wicket.Component;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.viewer.wicket.model.models.EntityModel;
import org.apache.isis.viewer.wicket.model.models.EntityModel.RenderingHint;
import org.apache.isis.viewer.wicket.ui.ComponentFactory;
import org.apache.isis.viewer.wicket.ui.ComponentType;

public class ObjectAdapterTitleColumn extends ColumnAbstract<ObjectAdapter> {

    private static final long serialVersionUID = 1L;

    public ObjectAdapterTitleColumn() {
        super("Title"); // i18n
    }

    @Override
    public void populateItem(final Item<ICellPopulator<ObjectAdapter>> cellItem, final String componentId, final IModel<ObjectAdapter> rowModel) {
        final Component component = createComponent(componentId, rowModel);
        cellItem.add(component);
    }

    private Component createComponent(final String id, final IModel<ObjectAdapter> rowModel) {
        final ObjectAdapter adapter = rowModel.getObject();
        final EntityModel model = new EntityModel(adapter);
        model.setRenderingHint(RenderingHint.COMPACT);
        final ComponentFactory componentFactory = findComponentFactory(ComponentType.ENTITY_LINK, model);
        return componentFactory.createComponent(id, model);
    }

}
