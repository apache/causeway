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
import org.apache.causeway.viewer.wicket.model.models.UiObjectWkt;
import org.apache.causeway.viewer.wicket.ui.components.object.ObjectComponentFactoryAbstract;

import jakarta.inject.Inject;

/**
 * Wicket {@link org.apache.causeway.viewer.wicket.ui.ComponentFactory} that renders a
 * domain object whose <b>type</b> is annotated with {@code @D3Graph} (as recognised
 * through the presence of a {@link D3GraphFacet} on its
 * {@link org.apache.causeway.core.metamodel.spec.ObjectSpecification}) as an
 * interactive D3.js force-layout graph (see {@link D3ObjectPanel}).
 * <p>
 * Applies exclusively, so that the object page is rendered as a graph rather than
 * falling back to the default grid.
 * </p>
 */
@org.springframework.stereotype.Component
public class D3ObjectPanelComponentFactory extends ObjectComponentFactoryAbstract {

    @Inject
    public D3ObjectPanelComponentFactory() {
        super(UiComponentType.DOMAIN_OBJECT, D3ObjectPanel.class);
    }

    @Override
    protected ApplicationAdvice doAppliesTo(final UiObjectWkt objectModel) {
        return ApplicationAdvice.appliesExclusivelyIf(
                objectModel.getTypeOfSpecification().containsFacet(D3GraphFacet.class));
    }

    @Override
    public Component createComponent(final String id, final IModel<?> model) {
        return new D3ObjectPanel(id, (UiObjectWkt) model);
    }

}
