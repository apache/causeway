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
package org.apache.causeway.viewer.wicket.ui.components.attributes;

import org.apache.wicket.Component;
import org.apache.wicket.model.IModel;

import org.apache.causeway.viewer.commons.model.components.UiComponentType;
import org.apache.causeway.viewer.wicket.model.models.UiAttributeWkt;
import org.apache.causeway.viewer.wicket.ui.ComponentFactoryAbstract;

public abstract class AttributeComponentFactory
extends ComponentFactoryAbstract {

    protected AttributeComponentFactory(
            final Class<?> componentClass) {
        super(UiComponentType.ATTRIBUTE_NAME_AND_VALUE, componentClass);
    }

    @Override
    public final Component createComponent(final String id, final IModel<?> model) {
        return createComponent(id, (UiAttributeWkt) model);
    }

    @Override
    public final ApplicationAdvice appliesTo(final IModel<?> model) {
        if (!(model instanceof UiAttributeWkt)) {
            return ApplicationAdvice.DOES_NOT_APPLY;
        }
        return appliesTo((UiAttributeWkt)model);
    }

    protected abstract Component createComponent(String id, UiAttributeWkt attributeModel);
    protected abstract ApplicationAdvice appliesTo(UiAttributeWkt attributeModel);

}
