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

package org.apache.isis.viewer.wicket.ui.panels;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.viewer.wicket.ui.ComponentType;
import org.apache.isis.viewer.wicket.ui.util.Components;

/**
 * Convenience adapter for {@link Panel}s built up using {@link ComponentType}s.
 * @param <M>
 */
public abstract class PanelAbstract<T extends IModel<?>> extends PanelBase/*<IModel<X>>*/ {

    private static final long serialVersionUID = 1L;

    private ComponentType componentType;

    public PanelAbstract(final ComponentType componentType) {
        this(componentType, null);
    }

    public PanelAbstract(final String id) {
        this(id, null);
    }

    public PanelAbstract(final ComponentType componentType, final IModel<?> model) {
        this(componentType.getWicketId(), model);
    }

    public PanelAbstract(final String id, final IModel<?> model) {
        super(id, model);
        this.componentType = ComponentType.lookup(id);
    }


    /**
     * Will be null if created using {@link #PanelAbstract(String, IModel)}.
     */
    public ComponentType getComponentType() {
        return componentType;
    }

    @SuppressWarnings("unchecked")
    @Override
    public T getModel() {
        return (T) getDefaultModel();
    }

    /**
     * For subclasses
     *
     * @return
     */
    protected Component addOrReplace(final ComponentType componentType, final IModel<?> model) {
        return getComponentFactoryRegistry().addOrReplaceComponent(this, componentType, model);
    }

    /**
     * For subclasses
     */
    protected void permanentlyHide(final ComponentType... componentIds) {
        Components.permanentlyHide(this, componentIds);
    }

    /**
     * For subclasses
     */
    public void permanentlyHide(final String... ids) {
        Components.permanentlyHide(this, ids);
    }
    
    protected void addConfirmationDialogIfAreYouSureSemantics(
            Component component, 
            SemanticsOf semanticsOf) {
        
        PanelUtil.addConfirmationDialogIfAreYouSureSemantics(super.getTranslationService(), component, semanticsOf);
    }


}
