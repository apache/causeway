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
package org.apache.causeway.viewer.wicket.ui.components.pops.composite;

import java.util.Optional;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.form.AbstractTextComponent;
import org.apache.wicket.markup.html.form.FormComponent;

import org.apache.causeway.viewer.wicket.model.models.PopModel;
import org.apache.causeway.viewer.wicket.ui.components.pops.PopPanelFormFieldAbstract;
import org.apache.causeway.viewer.wicket.ui.components.pops.PopFragmentFactory.FieldFrame;
import org.apache.causeway.viewer.wicket.ui.components.pops.PopFragmentFactory.InputFragment;
import org.apache.causeway.viewer.wicket.ui.components.pops.markup.MarkupComponent;
import org.apache.causeway.viewer.wicket.ui.components.widgets.bootstrap.FormGroup;

public class CompositeValuePanel<T>
extends PopPanelFormFieldAbstract<T> {

    private static final long serialVersionUID = 1L;

    public CompositeValuePanel(
            final String id,
            final PopModel popModel,
            final Class<T> valueType) {
        super(id, popModel, valueType);
    }

    @Override
    protected Component createComponentForOutput(final String id) {
        return new MarkupComponent(id, popModel());
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
    protected FormComponent<T> createFormComponent(final String id, final PopModel popModel) {
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
                if (popModel().isRequired()){
                    return !popModel().isEmpty();
                }
                return true;
            }
            @Override
            public void updateModel() {
                // update not allowed; the CompositeValueUpdaterForParameter updates
                // the underlying PopModel on nested dialog submission
            }

        };
    }



}
