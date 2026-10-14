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
package org.apache.causeway.extensions.d3.wkt.ui.components;

import org.apache.wicket.Component;
import org.apache.wicket.model.IModel;

import org.apache.causeway.extensions.d3.metamodel.facet.D3GraphFacet;
import org.apache.causeway.viewer.commons.model.components.UiComponentType;
import org.apache.causeway.viewer.wicket.model.models.UiAttributeWkt;
import org.apache.causeway.viewer.wicket.ui.ComponentFactory;
import org.apache.causeway.viewer.wicket.ui.ComponentFactoryAbstract;

import jakarta.inject.Inject;

/**
 * Wicket {@link ComponentFactory} that renders a domain property annotated with
 * {@code @D3Graph} (as recognised through the presence of a {@link D3GraphFacet})
 * as an interactive D3.js force-layout graph (see {@link D3Panel}).
 */
@org.springframework.stereotype.Component
public class D3PanelComponentFactory extends ComponentFactoryAbstract {

    @Inject
    public D3PanelComponentFactory() {
        super(UiComponentType.ATTRIBUTE_NAME_AND_VALUE, D3Panel.class);
    }

    @Override
    public ApplicationAdvice appliesTo(final IModel<?> model) {
        if (!(model instanceof UiAttributeWkt attributeModel)) {
            return ApplicationAdvice.DOES_NOT_APPLY;
        }
        if (!attributeModel.getMetaModel().containsFacet(D3GraphFacet.class)) {
            return ApplicationAdvice.DOES_NOT_APPLY;
        }
        // graph data must be a non-blank CharSequence (a JSON string)
        var value = attributeModel.getObject();
        if (value != null
                && value.getPojo() instanceof CharSequence cs
                && !cs.toString().isBlank()) {
            return ApplicationAdvice.APPLIES;
        }
        return ApplicationAdvice.DOES_NOT_APPLY;
    }

    @Override
    public Component createComponent(final String id, final IModel<?> model) {
        return new D3Panel(id, (UiAttributeWkt) model);
    }
}
