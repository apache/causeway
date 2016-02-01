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

package org.apache.isis.viewer.wicket.ui.components.layout.fixedcols.propsandcolls;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.viewer.wicket.model.models.EntityModel;
import org.apache.isis.viewer.wicket.ui.components.layout.fallback.EntityEditablePanel;
import org.apache.isis.viewer.wicket.ui.components.layout.fixedcols.FCGridPanel;
import org.apache.isis.viewer.wicket.ui.panels.PanelAbstract;

/**
 * {@link PanelAbstract Panel} representing the properties and collections of an entity, as per
 * the provided {@link EntityModel}.
 * 
 * <p>
 *     Used by both {@link FCGridPanel} and also {@link EntityEditablePanel}.  In the former
 *     case the collections are never shown, and edit buttons suppressed. In the latter case the
 *     collections are shown, possibly overflowing to region below.
 * </p>
 */
public class EntityPropsAndCollsPanel extends PanelAbstract<EntityModel> {

    private static final long serialVersionUID = 1L;

    private static final String ID_ENTITY_FORM = "entityForm";

    private EntityPropsAndCollsForm form;

    public EntityPropsAndCollsPanel(final String id, final EntityModel entityModel) {
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
            form = new EntityPropsAndCollsForm(ID_ENTITY_FORM, model, this);
            addOrReplace(form);
        } else {
            permanentlyHide(ID_ENTITY_FORM);
        }
    }
}
