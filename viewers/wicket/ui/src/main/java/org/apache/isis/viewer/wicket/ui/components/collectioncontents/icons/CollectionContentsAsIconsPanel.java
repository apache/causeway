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
package org.apache.isis.viewer.wicket.ui.components.collectioncontents.icons;

import org.apache.wicket.markup.repeater.RepeatingView;

import org.apache.isis.core.metamodel.interactions.managed.nonscalar.DataRow;
import org.apache.isis.core.metamodel.interactions.managed.nonscalar.DataTableModel;
import org.apache.isis.viewer.wicket.model.models.EntityCollectionModel;
import org.apache.isis.viewer.wicket.model.models.EntityModel;
import org.apache.isis.viewer.wicket.ui.components.entity.header.EntityHeaderPanel;
import org.apache.isis.viewer.wicket.ui.panels.PanelAbstract;

import lombok.val;

/**
 * {@link PanelAbstract Panel} that represents a {@link EntityCollectionModel
 * collection of entity}s rendered using a simple list of icons.
 */
public class CollectionContentsAsIconsPanel
extends PanelAbstract<DataTableModel, EntityCollectionModel> {

    private static final long serialVersionUID = 1L;

    private static final String ID_ENTITY_INSTANCE = "entityInstance";

    public CollectionContentsAsIconsPanel(final String id, final EntityCollectionModel model) {
        super(id, model);
        buildGui();
    }

    private void buildGui() {
        final EntityCollectionModel model = getModel();

        val visibleAdapters = model.getDataTableModel().getDataRowsFiltered()
                .getValue()
                .map(DataRow::getRowElement)
                .toList();

        final RepeatingView entityInstances = new RepeatingView(ID_ENTITY_INSTANCE);

        add(entityInstances);
        for (val adapter : visibleAdapters) {
            final String childId = entityInstances.newChildId();
            final EntityModel entityModel = EntityModel.ofAdapter(super.getCommonContext(), adapter);
            final EntityHeaderPanel entitySummaryPanel = new EntityHeaderPanel(childId, entityModel);
            entityInstances.add(entitySummaryPanel);
        }
    }

}
