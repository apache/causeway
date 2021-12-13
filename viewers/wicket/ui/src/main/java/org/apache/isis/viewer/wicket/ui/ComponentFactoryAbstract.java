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
import org.apache.wicket.request.resource.CssResourceReference;

import org.apache.isis.core.runtime.context.IsisAppCommonContext;
import org.apache.isis.viewer.common.model.components.ComponentType;
import org.apache.isis.viewer.wicket.ui.panels.PanelUtil;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.log4j.Log4j2;

/**
 * Adapter implementation for {@link ComponentFactory}.
 */
@ToString
@Log4j2
public abstract class ComponentFactoryAbstract implements ComponentFactory {

    private static final long serialVersionUID = 1L;

    @ToString.Exclude
    @Getter @Setter private transient IsisAppCommonContext commonContext;

    @Getter(onMethod_ = {@Override}) private final ComponentType componentType;
    @Getter(onMethod_ = {@Override}) private final String name;

    @Getter(onMethod_ = {@Override}) private final Class<?> componentTypeClass;

    protected ComponentFactoryAbstract(final ComponentType componentType) {
        this(componentType, null, null);
    }

    protected ComponentFactoryAbstract(final ComponentType componentType, final String name) {
        this(componentType, name, null);
    }

    protected ComponentFactoryAbstract(final ComponentType componentType, final Class<?> componentClass) {
        this(componentType, null, componentClass);
    }

    protected ComponentFactoryAbstract(
            final ComponentType componentType,
            final String name,
            final Class<?> componentTypeClass) {

        this.componentType = componentType;
        this.name = name != null ? name : getClass().getSimpleName();
        if(componentTypeClass != null && ComponentFactory.class.isAssignableFrom(componentTypeClass)) {
            throw new IllegalArgumentException("specified a ComponentFactory as a componentTypeClass... "
                    + "you probably meant the component instead? componentClass = "
                    + componentTypeClass.getName());
        }
        this.componentTypeClass = componentTypeClass;
        log.debug("new factory {}", this::toString);
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

    protected final ApplicationAdvice appliesIf(final boolean b) {
        return ApplicationAdvice.appliesIf(b);
    }

    @Override
    public final Component createComponent(final IModel<?> model) {
        log.debug("creating component {}", getComponentType()::toString);
        return createComponent(getComponentType().toString(), model);
    }

    @Override
    public abstract Component createComponent(String id, IModel<?> model);

    @Override
    public CssResourceReference getCssResourceReference() {
        return PanelUtil.cssResourceReferenceFor(componentTypeClass);
    }

}
