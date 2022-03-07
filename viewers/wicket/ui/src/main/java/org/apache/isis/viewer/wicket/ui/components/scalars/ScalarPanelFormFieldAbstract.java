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

import java.util.Optional;

import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.model.Model;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.IValidator;
import org.apache.wicket.validation.ValidationError;
import org.springframework.lang.Nullable;

import org.apache.isis.core.metamodel.objectmanager.ObjectManager;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.runtime.context.IsisAppCommonContext;
import org.apache.isis.viewer.wicket.model.models.ScalarModel;
import org.apache.isis.viewer.wicket.model.util.CommonContextUtils;
import org.apache.isis.viewer.wicket.ui.components.scalars._FragmentFactory.CompactFragment;
import org.apache.isis.viewer.wicket.ui.components.widgets.bootstrap.FormGroup;
import org.apache.isis.viewer.wicket.ui.util.Tooltips;
import org.apache.isis.viewer.wicket.ui.util.Wkt;

import lombok.val;

public abstract class ScalarPanelFormFieldAbstract<T>
extends ScalarPanelAbstract {

    private static final long serialVersionUID = 1L;

    protected final Class<T> type;

    protected ScalarPanelFormFieldAbstract(
            final String id,
            final ScalarModel scalarModel,
            final Class<T> type) {
        super(id, scalarModel);
        this.type = type;
    }

    @Override
    protected final Component getValidationFeedbackReceiver() {
        return getFormComponent();
    }

    // -- FORM COMPONENT

    private FormComponent<T> formComponent;
    @Nullable
    protected final FormComponent<T> getFormComponent() { return formComponent; }

    /**
     * Builds the component to render the form field.
     */
    protected abstract FormComponent<T> createFormComponent(String id, ScalarModel scalarModel);

    // -- REGULAR

    @Override
    protected final MarkupContainer createComponentForRegular() {
        val scalarModel = scalarModel();

        val friendlyNameModel = Model.of(scalarModel.getFriendlyName());

        formComponent = createFormComponent(ID_SCALAR_VALUE, scalarModel);
        formComponent.setLabel(friendlyNameModel);

        val formGroup = new FormGroup(ID_SCALAR_IF_REGULAR, formComponent);
        formGroup.add(formComponent);

        formComponent.setRequired(scalarModel.isRequired());
        if(scalarModel.isRequired()
                && scalarModel.isEnabled()) {
            Wkt.cssAppend(formGroup, "mandatory");
        }

        formGroup.add(createScalarNameLabel(ID_SCALAR_NAME, friendlyNameModel));

        formComponent.add(createValidator(scalarModel));

        onFormGroupCreated(formGroup);

        return formGroup;
    }

    // -- COMPACT

    @Override
    protected final Component createComponentForCompact() {
        return createComponentForCompact(ID_SCALAR_IF_COMPACT);
    }

    /**
     * Builds the component to render the model when in COMPACT format.
     * <p>
     * The (textual) default implementation uses a {@link Label}.
     * However, it may be overridden if required.
     */
    protected Component createComponentForCompact(final String id) {
        return Wkt.labelAdd(
                _FragmentFactory.createCompactFragment(CompactFragment.SPAN, this),
                id,
                ()->{
                    val scalarModel = scalarModel();
                    return scalarModel.isCurrentValueAbsent()
                            ? ""
                            : scalarModel.proposedValue()
                                .getValueAsHtml().getValue();
                                //.getValueAsParsableText().getValue();
                });
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

    @Override
    protected InlinePromptConfig getInlinePromptConfig() {
        return getFormComponent()!=null
                ? InlinePromptConfig.supportedAndHide(getFormComponent())
                : InlinePromptConfig.notSupported();
    }

    @Override
    protected void onInitializeNotEditable() {
        if(getFormComponent()!=null) {
            //keep inlinePromptLink (if any) enabled
            getFormComponent().setEnabled(false);
        }
        if(getWicketViewerSettings().isReplaceDisabledTagWithReadonlyTag()) {
            Wkt.behaviorAddReplaceDisabledTagWithReadonlyTag(getFormComponent());
        }
        clearTooltip();
    }

    @Override
    protected void onInitializeReadonly(final String disableReason) {
        formComponentEnable(false);
        if(getWicketViewerSettings().isReplaceDisabledTagWithReadonlyTag()) {
            Wkt.behaviorAddReplaceDisabledTagWithReadonlyTag(getFormComponent());
        }
        setTooltip(disableReason);
    }

    @Override
    protected void onInitializeEditable() {
        formComponentEnable(true);
        clearTooltip();
    }

    @Override
    protected void onNotEditable(final String disableReason, final Optional<AjaxRequestTarget> target) {
        formComponentEnable(false);
        setTooltip(disableReason);
        target.ifPresent(this::formComponentAddTo);
    }

    @Override
    protected void onEditable(final Optional<AjaxRequestTarget> target) {
        formComponentEnable(true);
        clearTooltip();
        target.ifPresent(this::formComponentAddTo);
    }

    // -- HELPER

    private void formComponentEnable(final boolean b) {
        if(getFormComponent()!=null) {
            getFormComponent().setEnabled(b);
        }
        if(inlinePromptLink!=null) {
            inlinePromptLink.setEnabled(b);
        }
    }

    private void formComponentAddTo(final AjaxRequestTarget ajax) {
        if(getFormComponent()!=null) {
            ajax.add(getFormComponent());
        }
        if(inlinePromptLink!=null) {
            ajax.add(inlinePromptLink);
        }
    }

    private void setTooltip(final String tooltip) {
        Tooltips.addTooltip(getFormComponent(), tooltip);
        Tooltips.addTooltip(inlinePromptLink, tooltip);
    }

    private void clearTooltip() {
        Tooltips.clearTooltip(getFormComponent());
        Tooltips.clearTooltip(inlinePromptLink);
    }

}
