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

package org.apache.isis.viewer.wicket.ui.components.entity;

import org.apache.wicket.Component;
import org.apache.wicket.model.IModel;

import org.apache.isis.applib.layout.v1_0.ObjectLayoutMetadata;
import org.apache.isis.core.metamodel.facets.object.layoutmetadata.ObjectLayoutMetadataFacet;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.viewer.wicket.model.models.EntityModel;
import org.apache.isis.viewer.wicket.ui.ComponentFactory;
import org.apache.isis.viewer.wicket.ui.ComponentType;
import org.apache.isis.viewer.wicket.ui.components.entity.combined.EntityCombinedPanel;
import org.apache.isis.viewer.wicket.ui.components.entity.tabbed.ColumnTabGroupListColumnPanel;

/**
 * {@link ComponentFactory} for {@link ColumnTabGroupListColumnPanel}.
 */
public class EntityPanelFactory extends EntityComponentFactoryAbstract {

    private static final long serialVersionUID = 1L;

    private static final String NAME = "tabbed";

    public EntityPanelFactory() {
        super(ComponentType.ENTITY, NAME, ColumnTabGroupListColumnPanel.class);
    }

    @Override
    public Component createComponent(final String id, final IModel<?> model) {

        final EntityModel entityModel = (EntityModel) model;

        final ObjectSpecification specification = entityModel.getTypeOfSpecification();
        final ObjectLayoutMetadataFacet facet = specification.getFacet(ObjectLayoutMetadataFacet.class);
        final ObjectLayoutMetadata layoutMetadata = facet.getMetadata();
        final boolean hasLayout = layoutMetadata != null;
        return hasLayout
                ? new ColumnTabGroupListColumnPanel(id, entityModel)
                : new EntityCombinedPanel(id, entityModel);
    }
}
