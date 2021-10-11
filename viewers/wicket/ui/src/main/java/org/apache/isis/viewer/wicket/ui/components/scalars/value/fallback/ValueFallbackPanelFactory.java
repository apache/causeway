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
package org.apache.isis.viewer.wicket.ui.components.scalars.value.fallback;

import org.apache.wicket.Component;
import org.apache.wicket.model.IModel;

import org.apache.isis.core.metamodel.facets.object.value.ValueFacet;
import org.apache.isis.viewer.wicket.model.models.ScalarModel;
import org.apache.isis.viewer.wicket.ui.ComponentFactory;
import org.apache.isis.viewer.wicket.ui.components.scalars.ComponentFactoryScalarAbstract;

/**
 * {@link ComponentFactory} for the {@link ValueFallbackPanel}.
 */
public class ValueFallbackPanelFactory
extends ComponentFactoryScalarAbstract {

    private static final long serialVersionUID = 1L;

    public ValueFallbackPanelFactory() {
        super(ValueFallbackPanel.class); // not asking the supertype to validate types, so no value types need be provided.
    }

    @Override
    public ApplicationAdvice appliesTo(final IModel<?> model) {
        if (!(model instanceof ScalarModel)) {
            return ApplicationAdvice.DOES_NOT_APPLY;
        }
        final ScalarModel scalarModel = (ScalarModel) model;
        final ValueFacet<?> valueFacet = scalarModel.getScalarTypeSpec().getFacet(ValueFacet.class);
        if(valueFacet == null) {
            return ApplicationAdvice.DOES_NOT_APPLY;
        }
        final boolean hasChoices = scalarModel.hasChoices();
        // autoComplete not supported on values, only references
        // final boolean hasAutoComplete = scalarModel.hasAutoComplete();
        return appliesIf( !(hasChoices /*|| hasAutoComplete*/) );
    }

    @Override
    public Component createComponent(final String id, final ScalarModel scalarModel) {
        return new ValueFallbackPanel(id, scalarModel);
    }


}
