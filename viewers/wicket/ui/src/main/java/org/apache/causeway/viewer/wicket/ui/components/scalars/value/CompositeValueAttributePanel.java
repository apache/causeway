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
package org.apache.causeway.viewer.wicket.ui.components.scalars.value;

import java.util.Optional;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.form.AbstractTextComponent;
import org.apache.wicket.markup.html.form.FormComponent;

import org.apache.causeway.viewer.wicket.model.models.UiAttributeWkt;
import org.apache.causeway.viewer.wicket.ui.components.scalars.AttributeFragmentFactory.FieldFrame;
import org.apache.causeway.viewer.wicket.ui.components.scalars.AttributeFragmentFactory.InputFragment;
import org.apache.causeway.viewer.wicket.ui.components.scalars.AttributePanelWithFormField;
import org.apache.causeway.viewer.wicket.ui.components.scalars.markup.MarkupComponent;
import org.apache.causeway.viewer.wicket.ui.components.widgets.bootstrap.FormGroup;

public class CompositeValueAttributePanel<T>
extends AttributePanelWithFormField<T> {

    private static final long serialVersionUID = 1L;

    public CompositeValueAttributePanel(
            final String id,
            final UiAttributeWkt attributeModel,
            final Class<T> valueType) {
        super(id, attributeModel, valueType);
    }

    @Override
    protected Component createComponentForOutput(final String id) {
        return new MarkupComponent(id, attributeModel());
    }

    @Override
    protected Optional<InputFragment> getInputFragmentType() {
        return Optional.empty();
    }

    @Override
    protected void onFormGroupCreated(final FormGroup formGroup) {
        super.onFormGroupCreated(formGroup);
        fieldFrame.addOrReplace(FieldFrame.SCALAR_VALUE_CONTAINER
                .createComponent(this::createComponentForOutput));
    }

    @Override
    protected FormComponent<T> createFormComponent(final String id, final UiAttributeWkt attributeModel) {
        // read-only FormComponent, to receive the param/property name label
        return new AbstractTextComponent<T>(id) {
            private static final long serialVersionUID = 1L;
            @Override
            public void validate() {
                // this is a nested form component,
                // the parent form does validation
            }
            @Override
            public boolean checkRequired() {
                if (attributeModel().isRequired()){
                    return !attributeModel().isEmpty();
                }
                return true;
            }
            @Override
            public void updateModel() {
                // update not allowed; the CompositeValueUpdaterForParameter updates
                // the underlying attribute model on nested dialog submission
            }

        };
    }

}
