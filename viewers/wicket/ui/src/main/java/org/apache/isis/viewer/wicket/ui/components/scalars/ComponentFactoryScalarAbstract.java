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
package org.apache.isis.viewer.wicket.ui.components.scalars;

import org.apache.wicket.Component;
import org.apache.wicket.model.IModel;

import org.apache.isis.commons.collections.Can;
import org.apache.isis.viewer.common.model.components.ComponentType;
import org.apache.isis.viewer.wicket.model.models.ScalarModel;
import org.apache.isis.viewer.wicket.ui.ComponentFactoryAbstract;

import lombok.Getter;

public abstract class ComponentFactoryScalarAbstract
extends ComponentFactoryAbstract {

    private static final long serialVersionUID = 1L;

    @Getter
    private final Can<Class<?>> scalarTypes;

    protected ComponentFactoryScalarAbstract(
            final Class<?> componentClass,
            final Class<?>... scalarTypes) {
        this(componentClass, Can.ofArray(scalarTypes));
    }

    protected ComponentFactoryScalarAbstract(
            final Class<?> componentClass,
            final Can<Class<?>> scalarTypes) {
        super(ComponentType.SCALAR_NAME_AND_VALUE, componentClass);
        this.scalarTypes = scalarTypes;
    }

    @Override
    public ApplicationAdvice appliesTo(final IModel<?> model) {
        if (!(model instanceof ScalarModel)) {
            return ApplicationAdvice.DOES_NOT_APPLY;
        }
        final ScalarModel scalarModel = (ScalarModel) model;
        if(!scalarModel.isScalarTypeAnyOf(scalarTypes)) {
            return ApplicationAdvice.DOES_NOT_APPLY;
        }
        final boolean hasChoices = scalarModel.hasChoices();
        // autoComplete not supported on values, only references
        // final boolean hasAutoComplete = scalarModel.hasAutoComplete();
        return appliesIf( !(hasChoices /*|| hasAutoComplete*/) );
    }

    @Override
    public final Component createComponent(final String id, final IModel<?> model) {
        return createComponent(id, (ScalarModel) model);
    }

    protected abstract Component createComponent(String id, ScalarModel scalarModel);

}
