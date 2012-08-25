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

package org.apache.isis.viewer.wicket.ui;

import org.apache.wicket.Component;
import org.apache.wicket.model.IModel;

/**
 * Adapter implementation for {@link ComponentFactory}.
 */
public abstract class ComponentFactoryAbstract implements ComponentFactory {

    private static final long serialVersionUID = 1L;

    private final ComponentType componentType;
    private final String name;

    public ComponentFactoryAbstract(final ComponentType componentType) {
        this.componentType = componentType;
        this.name = getClass().getSimpleName();
    }

    public ComponentFactoryAbstract(final ComponentType componentType, final String name) {
        this.componentType = componentType;
        this.name = name;
    }

    @Override
    public ComponentType getComponentType() {
        return componentType;
    }

    /**
     * Applies if {@link #getComponentType()} matches; disregards the provided
     * {@link IModel}.
     * 
     * @see #appliesTo(IModel)
     */
    @Override
    public final ApplicationAdvice appliesTo(final ComponentType componentType, final IModel<?> model) {
        if (componentType != getComponentType()) {
            return ApplicationAdvice.DOES_NOT_APPLY;
        }
        return appliesTo(model);
    }

    /**
     * Hook for subclasses to check the {@link IModel}.
     */
    protected abstract ApplicationAdvice appliesTo(IModel<?> model);

    /**
     * Convenenience for subclasses to call from {@link #appliesTo(IModel)}
     */
    protected final ApplicationAdvice appliesIf(final boolean b) {
        return b ? ApplicationAdvice.APPLIES : ApplicationAdvice.DOES_NOT_APPLY;
    }

    /**
     * Convenenience for subclasses to call from {@link #appliesTo(IModel)}
     */
    protected final ApplicationAdvice appliesExclusivelyIf(final boolean b) {
        return b ? ApplicationAdvice.APPLIES_EXCLUSIVELY : ApplicationAdvice.DOES_NOT_APPLY;
    }

    @Override
    public final Component createComponent(final IModel<?> model) {
        return createComponent(getComponentType().toString(), model);
    }

    @Override
    public abstract Component createComponent(String id, IModel<?> model);

    @Override
    public String getName() {
        return name;
    }

}
