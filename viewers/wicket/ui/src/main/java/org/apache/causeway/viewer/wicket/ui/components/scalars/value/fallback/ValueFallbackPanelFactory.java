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
package org.apache.causeway.viewer.wicket.ui.components.scalars.value.fallback;


import org.apache.wicket.Component;
import org.apache.wicket.model.IModel;

import org.apache.causeway.viewer.wicket.model.models.ScalarModel;
import org.apache.causeway.viewer.wicket.ui.ComponentFactory;
import org.apache.causeway.viewer.wicket.ui.components.scalars.ComponentFactoryScalarAbstract;

import lombok.val;

/**
 * {@link ComponentFactory} for the {@link ValueFallbackPanel}.
 */
public class ValueFallbackPanelFactory
extends ComponentFactoryScalarAbstract {

    private static final long serialVersionUID = 1L;

    public ValueFallbackPanelFactory() {
        // not asking the super-type to validate types, so no value types need be provided.
        super(ValueFallbackPanel.class);
    }

    @Override
    public ApplicationAdvice appliesTo(final IModel<?> model) {
        if (!(model instanceof ScalarModel)) {
            return ApplicationAdvice.DOES_NOT_APPLY;
        }
        val scalarModel = (ScalarModel) model;
        if(!scalarModel.getScalarTypeSpec().isValue()) {
            return ApplicationAdvice.DOES_NOT_APPLY;
        }

        final boolean hasChoices = scalarModel.hasChoices();
        // autoComplete not supported for values, only for references
        // final boolean hasAutoComplete = scalarModel.hasAutoComplete();
        return appliesIf( !(hasChoices /*|| hasAutoComplete*/) );
    }

    @Override
    public Component createComponent(final String id, final ScalarModel scalarModel) {
        return new ValueFallbackPanel(id, scalarModel);
    }


}
