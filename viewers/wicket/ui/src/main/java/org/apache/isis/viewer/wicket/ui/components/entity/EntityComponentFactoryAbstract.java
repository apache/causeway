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

import org.apache.isis.core.metamodel.facets.object.value.ValueFacet;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.viewer.common.model.components.ComponentType;
import org.apache.isis.viewer.wicket.model.models.EntityModel;
import org.apache.isis.viewer.wicket.ui.ComponentFactoryAbstract;

import lombok.val;

/**
 * Convenience adapter for a number of {@link ComponentFactoryAbstract component
 * factory}s that where the created {@link Component} are backed by an
 * {@link EntityModel}.
 */
public abstract class EntityComponentFactoryAbstract extends ComponentFactoryAbstract {

    private static final long serialVersionUID = 1L;

    public EntityComponentFactoryAbstract(
            final ComponentType componentType,
            final Class<?> componentClass) {

        super(componentType, componentClass);
    }

    public EntityComponentFactoryAbstract(
            final ComponentType componentType,
            final String name,
            final Class<?> componentClass) {

        super(componentType, name, componentClass);
    }

    @Override
    protected ApplicationAdvice appliesTo(final IModel<?> model) {
        if (!(model instanceof EntityModel)) {
            return ApplicationAdvice.DOES_NOT_APPLY;
        }
        final EntityModel entityModel = (EntityModel) model;
        // hit a scenario on a redirect-and-post strategy where the component is rendered not on an
        // EntityPage but instead using a custom home page.  The hacky override in entity page (in EntityPage#onBeforeRender)
        // is therefore not called. resulting in a concurrency exception.
        //
        // Therefore, we do the same processing here instead.
        val adapter = entityModel.getManagedObject();
        if (adapter == null) {
            // is ok;
        }
        final ObjectSpecification specification = entityModel.getTypeOfSpecification();
        final boolean isScalar = specification.isScalar();
        final boolean isValue = specification.containsFacet(ValueFacet.class);
        if (isScalar && !isValue) {
            return doAppliesTo(entityModel);
        }
        return ApplicationAdvice.DOES_NOT_APPLY;
    }

    /**
     * optional hook.
     */
    protected ApplicationAdvice doAppliesTo(final EntityModel entityModel) {
        return ApplicationAdvice.APPLIES;
    }

}
