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
package org.apache.isis.viewer.wicket.ui.components.unknown;

import org.apache.wicket.Component;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.resource.CssResourceReference;

import org.apache.isis.viewer.common.model.components.ComponentType;
import org.apache.isis.viewer.wicket.ui.ComponentFactory;
import org.apache.isis.viewer.wicket.ui.panels.PanelUtil;

public class UnknownModelPanelFactory implements ComponentFactory {

    private static final long serialVersionUID = 1L;

    @Override
    public ApplicationAdvice appliesTo(final ComponentType componentType, final IModel<?> model) {
        return ApplicationAdvice.APPLIES;
    }

    @Override
    public Component createComponent(final String id, final IModel<?> model) {
        return new UnknownModelPanel(id, model);
    }

    @Override
    public ComponentType getComponentType() {
        return ComponentType.UNKNOWN;
    }

    @Override
    public Component createComponent(final IModel<?> model) {
        return createComponent("unknown", model);
    }

    @Override
    public String getName() {
        return getClass().getSimpleName();
    }

    @Override
    public CssResourceReference getCssResourceReference() {
        return PanelUtil.cssResourceReferenceFor(UnknownModelPanel.class);
    }

    @Override
    public Class<?> getComponentTypeClass() {
        return UnknownModelPanel.class;
    }

}
