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

package org.apache.isis.viewer.wicket.ui.components.entity.selector.links;

import org.apache.wicket.Component;
import org.apache.wicket.model.IModel;

import org.apache.isis.applib.layout.grid.Grid;
import org.apache.isis.applib.layout.grid.bootstrap3.BS3Grid;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.facets.object.grid.GridFacet;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.viewer.wicket.model.models.EntityModel;
import org.apache.isis.viewer.wicket.ui.ComponentFactory;
import org.apache.isis.viewer.wicket.ui.ComponentType;
import org.apache.isis.viewer.wicket.ui.components.entity.EntityComponentFactoryAbstract;
import org.apache.isis.viewer.wicket.ui.components.layout.bs3.BS3GridPanel;

/**
 * {@link ComponentFactory} for {@link EntityLinksSelectorPanel}.
 */
public class EntityLinksSelectorPanelFactory extends EntityComponentFactoryAbstract {

    private static final long serialVersionUID = 1L;

    public EntityLinksSelectorPanelFactory() {
        super(ComponentType.ENTITY, EntityLinksSelectorPanel.class);
    }

    @Override
    public Component createComponent(final String id, final IModel<?> model) {
        final EntityModel entityModel = (EntityModel) model;

        final ObjectAdapter objectAdapter = entityModel.getObject();
        final ObjectSpecification specification = entityModel.getTypeOfSpecification();
        final GridFacet facet = specification.getFacet(GridFacet.class);

        final Grid grid = facet.getGrid(objectAdapter);
        if (grid != null) {
            if(grid instanceof BS3Grid) {
                final BS3Grid bs3Grid = (BS3Grid) grid;
                return new BS3GridPanel(id, entityModel, bs3Grid);
            }
        }
        return new EntityLinksSelectorPanel(id, entityModel, this);
    }
}
