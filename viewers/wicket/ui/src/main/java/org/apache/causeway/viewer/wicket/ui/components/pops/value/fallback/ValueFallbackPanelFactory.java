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
package org.apache.causeway.viewer.wicket.ui.components.pops.value.fallback;


import org.apache.wicket.Component;

import org.apache.causeway.viewer.wicket.model.models.PopModel;
import org.apache.causeway.viewer.wicket.ui.ComponentFactory;
import org.apache.causeway.viewer.wicket.ui.components.pops.ComponentFactoryPopAbstract;

/**
 * {@link ComponentFactory} for the {@link ValueFallbackPanel}.
 */
public class ValueFallbackPanelFactory
extends ComponentFactoryPopAbstract {

    private static final long serialVersionUID = 1L;

    public ValueFallbackPanelFactory() {
        // not asking the super-type to validate types, so no value types need be provided.
        super(ValueFallbackPanel.class);
    }

    @Override
    public Component createComponent(final String id, final PopModel popModel) {
        return new ValueFallbackPanel(id, popModel);
    }

    @Override
    protected ApplicationAdvice appliesTo(final PopModel popModel) {
        if(!popModel.getScalarTypeSpec().isValue()) {
            return ApplicationAdvice.DOES_NOT_APPLY;
        }

        // autoComplete not supported for values, only for references
        // final boolean hasAutoComplete = popModel.hasAutoComplete();
        return appliesIf( !(popModel.hasChoices() /*|| hasAutoComplete*/) );
    }


}
