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
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.model.Model;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.IValidator;
import org.apache.wicket.validation.ValidationError;

import org.apache.isis.core.metamodel.objectmanager.ObjectManager;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.runtime.context.IsisAppCommonContext;
import org.apache.isis.viewer.wicket.model.models.ScalarModel;
import org.apache.isis.viewer.wicket.model.util.CommonContextUtils;
import org.apache.isis.viewer.wicket.ui.components.widgets.bootstrap.FormGroup;
import org.apache.isis.viewer.wicket.ui.util.Tooltips;
import org.apache.isis.viewer.wicket.ui.util.Wkt;

import lombok.val;

public abstract class ScalarPanelWithFormFieldAbstract
extends ScalarPanelAbstract {

    private static final long serialVersionUID = 1L;

    protected ScalarPanelWithFormFieldAbstract(final String id, final ScalarModel scalarModel) {
        super(id, scalarModel);
    }

    @Override
    protected final Component getValidationFeedbackReceiver() {
        return getFormComponent();
    }

    // -- FORM COMPONENT

    private FormComponent<?> formComponent;
    protected final FormComponent<?> getFormComponent() { return formComponent; }
    /**
     * Builds the component to render the form field.
     */
    protected abstract FormComponent<?> createFormComponent(ScalarModel scalarModel);

    // -- REGULAR

    @Override
    protected final MarkupContainer createComponentForRegular() {
        val scalarModel = scalarModel();

        formComponent = createFormComponent(scalarModel);
        formComponent.setLabel(Model.of(scalarModel.getFriendlyName()));

        final FormGroup formGroup = new FormGroup(ID_SCALAR_IF_REGULAR, formComponent);
        formGroup.add(formComponent);

        formComponent.setRequired(scalarModel.isRequired());
        if(scalarModel.isRequired()
                && scalarModel.isEnabled()) {
            Wkt.cssAppend(formGroup, "mandatory");
        }

        final String labelCaption = getRendering().getLabelCaption(formComponent);
        final Label scalarName = createScalarName(ID_SCALAR_NAME, labelCaption);
        formGroup.add(scalarName);

        scalarModel.getDescribedAs()
            .ifPresent(describedAs->Tooltips.addTooltip(scalarName, describedAs));

        formComponent.add(createValidator(scalarModel));

        onFormGroupCreated(formGroup);

        return formGroup;
    }

    // -- HOOKS

    protected void onFormGroupCreated(final FormGroup formGroup) {
    }

    protected IValidator<Object> createValidator(final ScalarModel scalarModel) {
        return new IValidator<Object>() {
            private static final long serialVersionUID = 1L;
            private transient IsisAppCommonContext commonContext;

            @Override
            public void validate(final IValidatable<Object> validatable) {
                final ManagedObject proposedAdapter = objectManager().adapt(validatable.getValue());
                final String reasonIfAny = scalarModel.validate(proposedAdapter);
                if (reasonIfAny != null) {
                    final ValidationError error = new ValidationError();
                    error.setMessage(reasonIfAny);
                    validatable.error(error);
                }
            }

            private ObjectManager objectManager() {
                return getCommonContext().getObjectManager();
            }

            private IsisAppCommonContext getCommonContext() {
                return commonContext = CommonContextUtils.computeIfAbsent(commonContext);
            }

        };
    }

}
