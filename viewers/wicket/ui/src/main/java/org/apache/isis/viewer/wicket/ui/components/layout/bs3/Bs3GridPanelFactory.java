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

package org.apache.isis.viewer.wicket.ui.components.layout.bs3;

import org.apache.wicket.Component;
import org.apache.wicket.model.IModel;

import org.apache.isis.applib.layout.grid.Grid;
import org.apache.isis.applib.layout.grid.bootstrap3.BS3Grid;
import org.apache.isis.core.metamodel.facets.object.grid.GridFacet;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.viewer.wicket.model.models.EntityModel;
import org.apache.isis.viewer.wicket.ui.ComponentFactory;
import org.apache.isis.viewer.wicket.ui.ComponentType;
import org.apache.isis.viewer.wicket.ui.components.entity.EntityComponentFactoryAbstract;

import lombok.val;

/**
 * {@link ComponentFactory} for {@link BS3GridPanel}.
 */
public class Bs3GridPanelFactory extends EntityComponentFactoryAbstract {

    private static final long serialVersionUID = 1L;

    public Bs3GridPanelFactory() {
        super(ComponentType.ENTITY, BS3GridPanel.class);
    }

    @Override protected ApplicationAdvice appliesTo(final IModel<?> model) {
        final EntityModel entityModel = (EntityModel) model;

        val objectAdapter = entityModel.getObject();
        final ObjectSpecification specification = entityModel.getTypeOfSpecification();
        final GridFacet facet = specification.getFacet(GridFacet.class);

        final Grid grid = facet.getGrid(objectAdapter);
        return ApplicationAdvice.appliesIf(grid instanceof BS3Grid);
    }

    @Override
    public Component createComponent(final String id, final IModel<?> model) {
        final EntityModel entityModel = (EntityModel) model;

        val objectAdapter = entityModel.getObject();
        final ObjectSpecification specification = entityModel.getTypeOfSpecification();
        final GridFacet facet = specification.getFacet(GridFacet.class);

        val grid = (BS3Grid) facet.getGrid(objectAdapter);
        return new BS3GridPanel(id, entityModel, grid);
    }
}
