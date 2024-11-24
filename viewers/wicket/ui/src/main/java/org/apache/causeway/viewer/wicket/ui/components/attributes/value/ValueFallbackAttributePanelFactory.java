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
package org.apache.causeway.viewer.wicket.ui.components.attributes.value;

import org.apache.wicket.Component;

import org.apache.causeway.viewer.wicket.model.models.UiAttributeWkt;
import org.apache.causeway.viewer.wicket.ui.ComponentFactory;
import org.apache.causeway.viewer.wicket.ui.components.attributes.AttributeComponentFactoryAbstract;

/**
 * {@link ComponentFactory} for the {@link ValueFallbackAttributePanel}.
 */
public class ValueFallbackAttributePanelFactory
extends AttributeComponentFactoryAbstract {

    public ValueFallbackAttributePanelFactory() {
        // not asking the super-type to validate types, so no value types need be provided.
        super(ValueFallbackAttributePanel.class);
    }

    @Override
    public Component createComponent(final String id, final UiAttributeWkt attributeModel) {
        return new ValueFallbackAttributePanel(id, attributeModel);
    }

    @Override
    protected ApplicationAdvice appliesTo(final UiAttributeWkt attributeModel) {
        if(!attributeModel.getElementType().isValue()) {
            return ApplicationAdvice.DOES_NOT_APPLY;
        }
        return appliesIf(
                !attributeModel.hasChoices()
                && !attributeModel.hasAutoComplete());
    }

}
