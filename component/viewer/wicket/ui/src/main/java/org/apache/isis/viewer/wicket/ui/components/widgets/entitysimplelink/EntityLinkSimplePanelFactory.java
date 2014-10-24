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

package org.apache.isis.viewer.wicket.ui.components.widgets.entitysimplelink;

import org.apache.wicket.Component;
import org.apache.wicket.model.IModel;

import org.apache.isis.core.metamodel.facets.object.value.ValueFacet;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.viewer.wicket.model.models.EntityModel;
import org.apache.isis.viewer.wicket.ui.ComponentFactoryAbstract;
import org.apache.isis.viewer.wicket.ui.ComponentType;

public class EntityLinkSimplePanelFactory extends ComponentFactoryAbstract {

    private static final long serialVersionUID = 1L;

    public EntityLinkSimplePanelFactory() {
        super(ComponentType.ENTITY_LINK, EntityLinkSimplePanel.class);
    }

    @Override
    public ApplicationAdvice appliesTo(final IModel<?> model) {
        if (!(model instanceof EntityModel)) {
            return ApplicationAdvice.DOES_NOT_APPLY;
        }
        final EntityModel entityModel = (EntityModel) model;
        final ObjectSpecification specification = entityModel.getTypeOfSpecification();
        return appliesIf(specification != null && !specification.containsFacet(ValueFacet.class));
    }

    @Override
    public Component createComponent(final String id, final IModel<?> model) {
        final EntityModel scalarModel = (EntityModel) model;
        return new EntityLinkSimplePanel(id, scalarModel);
    }
}
