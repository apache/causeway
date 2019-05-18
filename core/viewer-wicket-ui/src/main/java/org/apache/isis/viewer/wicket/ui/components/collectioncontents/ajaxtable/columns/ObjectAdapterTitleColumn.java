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
import org.apache.isis.core.runtime.memento.ObjectAdapterMemento;
import org.apache.isis.viewer.wicket.model.models.EntityModel;
import org.apache.isis.viewer.wicket.model.models.EntityModel.RenderingHint;
import org.apache.isis.viewer.wicket.ui.ComponentFactory;
import org.apache.isis.viewer.wicket.ui.ComponentType;
import org.apache.isis.viewer.wicket.ui.util.CssClassAppender;

public class ObjectAdapterTitleColumn extends ColumnAbstract<ObjectAdapter> {

    private static final long serialVersionUID = 1L;
    private final ObjectAdapterMemento parentAdapterMementoIfAny;

    private static String columnName(final ObjectAdapterMemento parentAdapterMementoIfAny, final int maxTitleLength) {
        if(maxTitleLength == 0) {
            return "";
        }
        return (parentAdapterMementoIfAny != null? "Related ":"") + "Object";
    }

    public ObjectAdapterTitleColumn(final ObjectAdapterMemento parentAdapterMementoIfAny, final int maxTitleLength) {
        super(columnName(parentAdapterMementoIfAny, maxTitleLength)); // i18n
        this.parentAdapterMementoIfAny = parentAdapterMementoIfAny;
    }

    @Override
    public void populateItem(final Item<ICellPopulator<ObjectAdapter>> cellItem, final String componentId, final IModel<ObjectAdapter> rowModel) {
        final Component component = createComponent(componentId, rowModel);
        cellItem.add(component);
        cellItem.add(new CssClassAppender("title-column"));
    }

    private Component createComponent(final String id, final IModel<ObjectAdapter> rowModel) {
        final ObjectAdapter adapter = rowModel.getObject();
        final EntityModel model = new EntityModel(adapter);
        model.setRenderingHint(parentAdapterMementoIfAny != null? RenderingHint.PARENTED_TITLE_COLUMN: RenderingHint.STANDALONE_TITLE_COLUMN);
        model.setContextAdapterIfAny(parentAdapterMementoIfAny);
        // will use EntityLinkSimplePanelFactory as model is an EntityModel
        final ComponentFactory componentFactory = findComponentFactory(ComponentType.ENTITY_LINK, model);
        return componentFactory.createComponent(id, model);
    }



}
