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

package org.apache.isis.viewer.wicket.ui.components.widgets.formcomponent;

import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.FormComponentPanel;
import org.apache.wicket.model.IModel;

import org.apache.isis.viewer.wicket.model.hints.UiHintContainer;
import org.apache.isis.viewer.wicket.ui.ComponentType;
import org.apache.isis.viewer.wicket.ui.app.registry.ComponentFactoryRegistry;
import org.apache.isis.viewer.wicket.ui.app.registry.ComponentFactoryRegistryAccessor;
import org.apache.isis.viewer.wicket.ui.util.Components;

/**
 * Convenience adapter for {@link FormComponent}s that are implemented using the
 * Wicket {@link FormComponentPanel}, providing the ability to build up the
 * panel using other {@link ComponentType}s.
 */
public abstract class FormComponentPanelAbstract<T> extends FormComponentPanel<T>  {

    private static final long serialVersionUID = 1L;

    private ComponentType componentType;

    public FormComponentPanelAbstract(final String id, final IModel<T> model) {
        super(id, model);
        this.componentType = ComponentType.lookup(id);
    }

    public ComponentType getComponentType() {
        return componentType;
    }

    /**
     * For subclasses
     */
    protected Component addOrReplace(final ComponentType componentType, final IModel<?> model) {
        return getComponentFactoryRegistry().addOrReplaceComponent(this, componentType, model);
    }

    /**
     * For subclasses
     */
    protected void permanentlyHide(final ComponentType... componentIds) {
        permanentlyHide(this, componentIds);
    }

    /**
     * For subclasses
     */
    public void permanentlyHide(final String... ids) {
        permanentlyHide(this, ids);
    }

    /**
     * For subclasses
     */
    protected void permanentlyHide(final MarkupContainer container, final ComponentType... componentIds) {
        Components.permanentlyHide(container, componentIds);
    }

    /**
     * For subclasses
     */
    public void permanentlyHide(final MarkupContainer container, final String... ids) {
        Components.permanentlyHide(container, ids);
    }


    // ///////////////////////////////////////////////////////////////////
    // Hint support
    // ///////////////////////////////////////////////////////////////////

    public UiHintContainer getHintContainer() {
        return hintContainerOf(this);
    }

    private UiHintContainer hintContainerOf(Component component) {
        if(component == null) {
            return null;
        }
        IModel<?> model = component.getDefaultModel();
        if(model instanceof UiHintContainer) {
            return (UiHintContainer) model;
        }
        // otherwise, go up the UI component hierarchy
        return hintContainerOf(getParent());
    }

    // ///////////////////////////////////////////////////////////////////
    // Convenience
    // ///////////////////////////////////////////////////////////////////

    protected ComponentFactoryRegistry getComponentFactoryRegistry() {
        final ComponentFactoryRegistryAccessor cfra = (ComponentFactoryRegistryAccessor) getApplication();
        return cfra.getComponentFactoryRegistry();
    }



}
