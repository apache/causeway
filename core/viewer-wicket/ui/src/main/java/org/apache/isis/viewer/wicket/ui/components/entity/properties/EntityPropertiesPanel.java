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

package org.apache.isis.viewer.wicket.ui.components.entity.properties;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.viewer.wicket.model.models.EntityModel;
import org.apache.isis.viewer.wicket.ui.panels.PanelAbstract;

/**
 * {@link PanelAbstract Panel} representing the properties of an entity, as per
 * the provided {@link EntityModel}.
 */
public class EntityPropertiesPanel extends PanelAbstract<EntityModel> {

    private static final long serialVersionUID = 1L;

    private static final String ID_ENTITY_PROPERTIES = "entityProperties";

    private EntityPropertiesForm form;

    public EntityPropertiesPanel(final String id, final EntityModel entityModel) {
        super(id, entityModel);
        buildGui();
        form.toViewMode(null);
    }


    private void buildGui() {
        buildEntityPropertiesAndOrCollectionsGui();
        setOutputMarkupId(true);
    }

    private void buildEntityPropertiesAndOrCollectionsGui() {
        final EntityModel model = getModel();
        final ObjectAdapter adapter = model.getObject();
        if (adapter != null) {
            form = new EntityPropertiesForm(ID_ENTITY_PROPERTIES, model, this);
            addOrReplace(form);
        } else {
            permanentlyHide(ID_ENTITY_PROPERTIES);
        }
    }
}
