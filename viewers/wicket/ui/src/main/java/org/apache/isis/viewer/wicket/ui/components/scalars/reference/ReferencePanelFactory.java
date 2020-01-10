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

package org.apache.isis.viewer.wicket.ui.components.scalars.reference;

import org.apache.wicket.Component;
import org.apache.wicket.model.IModel;

import org.apache.isis.core.metamodel.facets.object.value.ValueFacet;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.viewer.wicket.model.models.ScalarModel;
import org.apache.isis.viewer.wicket.ui.ComponentFactory;
import org.apache.isis.viewer.wicket.ui.ComponentFactoryAbstract;
import org.apache.isis.viewer.wicket.ui.ComponentType;

/**
 * {@link ComponentFactory} for {@link ReferencePanel}.
 */
public class ReferencePanelFactory extends ComponentFactoryAbstract {

    private static final long serialVersionUID = 1L;

    public ReferencePanelFactory() {
        super(ComponentType.SCALAR_NAME_AND_VALUE, ReferencePanel.class);
    }

    @Override
    public ApplicationAdvice appliesTo(final IModel<?> model) {
        if (!(model instanceof ScalarModel)) {
            return ApplicationAdvice.DOES_NOT_APPLY;
        }
        final ScalarModel scalarModel = (ScalarModel) model;
        final ObjectSpecification specification = scalarModel.getTypeOfSpecification();
        return appliesIf(!specification.containsFacet(ValueFacet.class));
    }

    @Override
    public Component createComponent(final String id, final IModel<?> model) {
        final ScalarModel scalarModel = (ScalarModel) model;
        return new ReferencePanel(id, scalarModel);
    }
}
