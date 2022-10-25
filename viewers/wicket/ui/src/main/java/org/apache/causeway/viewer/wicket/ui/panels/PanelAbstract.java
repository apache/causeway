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
package org.apache.causeway.viewer.wicket.ui.panels;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.springframework.lang.Nullable;

import org.apache.causeway.commons.internal.base._Casts;
import org.apache.causeway.viewer.commons.model.components.UiComponentType;
import org.apache.causeway.viewer.wicket.ui.util.WktComponents;

/**
 * Convenience adapter for {@link Panel}s built up using {@link UiComponentType}s.
 *
 * @apiNote using raw-types here, to not further complicate generic type constraints on PanelAbstract
 */
public abstract class PanelAbstract<T, M extends IModel<T>>
extends PanelBase<T> {

    private static final long serialVersionUID = 1L;

    private UiComponentType uiComponentType;

    public PanelAbstract(final UiComponentType uiComponentType) {
        this(uiComponentType, null);
    }

    public PanelAbstract(final String id) {
        this(id, null);
    }

    public PanelAbstract(final UiComponentType uiComponentType, final M model) {
        this(uiComponentType.getId(), model);
    }

    public PanelAbstract(final String id, final M model) {
        super(id, model);
        this.uiComponentType = UiComponentType.lookup(id);
    }

    /**
     * Will be null if created using {@link #PanelAbstract(String, IModel)}.
     */
    public UiComponentType getComponentType() {
        return uiComponentType;
    }

    @Override
    public M getModel() {
        return _Casts.uncheckedCast(getDefaultModel());
    }

    /**
     * For subclasses
     */
    protected Component addOrReplace(final UiComponentType uiComponentType, final IModel<?> model) {
        return getComponentFactoryRegistry().addOrReplaceComponent(this, uiComponentType, model);
    }

    /**
     * For subclasses
     */
    protected void permanentlyHide(final UiComponentType... componentIds) {
        WktComponents.permanentlyHide(this, componentIds);
    }

    /**
     * For subclasses
     */
    public void permanentlyHide(final String... ids) {
        WktComponents.permanentlyHide(this, ids);
    }

    protected static void setVisible(@Nullable final Component component, final boolean visible) {
        if(component == null) {
            return;
        }
        component.setVisible(visible);
    }

}
