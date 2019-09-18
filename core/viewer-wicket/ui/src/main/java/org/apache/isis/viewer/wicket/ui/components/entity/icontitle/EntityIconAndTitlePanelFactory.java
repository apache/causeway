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

package org.apache.isis.viewer.wicket.ui.components.entity.icontitle;

import org.apache.wicket.Component;
import org.apache.wicket.model.IModel;

import org.apache.isis.metamodel.facets.object.value.ValueFacet;
import org.apache.isis.metamodel.spec.ObjectSpecification;
import org.apache.isis.viewer.wicket.model.models.ObjectAdapterModel;
import org.apache.isis.viewer.wicket.ui.ComponentFactory;
import org.apache.isis.viewer.wicket.ui.ComponentFactoryAbstract;
import org.apache.isis.viewer.wicket.ui.ComponentType;

/**
 * {@link ComponentFactory} for {@link EntityIconAndTitlePanel}.
 */
public class EntityIconAndTitlePanelFactory extends ComponentFactoryAbstract {

    private static final long serialVersionUID = 1L;

    public EntityIconAndTitlePanelFactory(final ComponentType componentType, final @SuppressWarnings("rawtypes") Class componentClass) {
        super(componentType, componentClass);
    }

    public EntityIconAndTitlePanelFactory(final ComponentType componentType, final String name, final @SuppressWarnings("rawtypes") Class componentClass) {
        super(componentType, name, componentClass);
    }

    public EntityIconAndTitlePanelFactory() {
        this(ComponentType.ENTITY_ICON_AND_TITLE, EntityIconAndTitlePanel.class);
    }

    @Override
    protected ApplicationAdvice appliesTo(final IModel<?> model) {
        if (!(model instanceof ObjectAdapterModel)) {
            return ApplicationAdvice.DOES_NOT_APPLY;
        }
        final ObjectAdapterModel entityModel = (ObjectAdapterModel) model;
        final ObjectSpecification specification = entityModel.getTypeOfSpecification();
        final boolean isObject = specification.isNotCollection();
        final boolean isValue = specification.containsFacet(ValueFacet.class);
        boolean b = isObject && !isValue;
        if (!b) {
            return ApplicationAdvice.DOES_NOT_APPLY;
        }

        return ApplicationAdvice.APPLIES;
    }

    @Override
    public Component createComponent(final String id, final IModel<?> model) {
        final ObjectAdapterModel entityModel = (ObjectAdapterModel) model;
        return new EntityIconAndTitlePanel(id, entityModel);
    }
}
