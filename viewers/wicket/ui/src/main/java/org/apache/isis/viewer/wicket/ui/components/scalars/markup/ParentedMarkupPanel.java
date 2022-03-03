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
package org.apache.isis.viewer.wicket.ui.components.scalars.markup;

import java.io.Serializable;

import org.apache.wicket.Component;

import org.apache.isis.viewer.wicket.model.models.ScalarModel;
import org.apache.isis.viewer.wicket.ui.components.scalars.ScalarPanelTextFieldWithValueSemantics;
import org.apache.isis.viewer.wicket.ui.components.scalars.TextFieldVariant;
import org.apache.isis.viewer.wicket.ui.components.widgets.bootstrap.FormGroup;

/**
 * Panel for rendering scalars of type {@link org.apache.isis.applib.value.Markup}.
 */
public class ParentedMarkupPanel<T extends Serializable>
extends ScalarPanelTextFieldWithValueSemantics<T> {

    private static final long serialVersionUID = 1L;
    private final transient MarkupComponentFactory<ScalarModel> markupComponentFactory;

    public ParentedMarkupPanel(
            final String id,
            final ScalarModel scalarModel,
            final Class<T> valueType,
            final MarkupComponentFactory<ScalarModel> markupComponentFactory) {

        super(id, scalarModel, valueType, TextFieldVariant.MULTI_LINE);
        this.markupComponentFactory = markupComponentFactory;
    }

    @Override
    protected void onFormGroupCreated(final FormGroup formGroup) {
        if(scalarModel().isEditMode()) {
            // fallback to text area
            super.onFormGroupCreated(formGroup);
        } else {
            formGroup.add(createMarkupComponent(ID_SCALAR_VALUE_CONTAINER));
        }
    }

    @Override
    protected Component createComponentForCompact() {
        return createMarkupComponent(ID_SCALAR_IF_COMPACT);
    }

    protected final MarkupComponent createMarkupComponent(final String id) {
        return markupComponentFactory.newMarkupComponent(id, scalarModel());
    }
}
