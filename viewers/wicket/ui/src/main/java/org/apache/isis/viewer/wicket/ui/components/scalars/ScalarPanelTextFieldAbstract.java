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

import java.io.Serializable;
import java.util.Optional;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.AbstractTextComponent;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.convert.IConverter;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.IValidator;
import org.apache.wicket.validation.ValidationError;

import org.apache.isis.commons.internal.base._Casts;
import org.apache.isis.core.metamodel.facets.objectvalue.maxlen.MaxLengthFacet;
import org.apache.isis.core.metamodel.facets.objectvalue.typicallen.TypicalLengthFacet;
import org.apache.isis.core.metamodel.objectmanager.ObjectManager;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ManagedObjects;
import org.apache.isis.core.runtime.context.IsisAppCommonContext;
import org.apache.isis.viewer.wicket.model.models.ScalarModel;
import org.apache.isis.viewer.wicket.model.util.CommonContextUtils;
import org.apache.isis.viewer.wicket.ui.components.widgets.bootstrap.FormGroup;
import org.apache.isis.viewer.wicket.ui.panels.PanelAbstract;
import org.apache.isis.viewer.wicket.ui.util.Tooltips;
import org.apache.isis.viewer.wicket.ui.util.Wkt;

import lombok.NonNull;
import lombok.val;

/**
 * Adapter for {@link PanelAbstract panel}s that use a {@link ScalarModel} as
 * their backing model.
 *
 * <p>
 * Supports the concept of being
 * {@link org.apache.isis.viewer.wicket.ui.components.scalars.ScalarPanelAbstract.Rendering#COMPACT}
 * (eg within a table) or
 * {@link org.apache.isis.viewer.wicket.ui.components.scalars.ScalarPanelAbstract.Rendering#REGULAR regular}
 * (eg within a form).
 * </p>
 *
 * <p>
 * This implementation is for panels that use a textfield/text area.
 * </p>
 */
public abstract class ScalarPanelTextFieldAbstract<T extends Serializable>
extends ScalarPanelAbstract
implements TextFieldValueModel.ScalarModelProvider {

    private static final long serialVersionUID = 1L;

    protected final Class<T> cls;

    protected static class ReplaceDisabledTagWithReadonlyTagBehaviour extends Behavior {
        private static final long serialVersionUID = 1L;

        @Override public void onComponentTag(final Component component, final ComponentTag tag) {
            super.onComponentTag(component, tag);
            if(component.isEnabled()) {
                return;
            }
            tag.remove("disabled");
            tag.put("readonly","readonly");
        }
    }

    private AbstractTextComponent<T> textField;


    public ScalarPanelTextFieldAbstract(final String id, final ScalarModel scalarModel, final Class<T> cls) {
        super(id, scalarModel);
        this.cls = cls;
    }

    // ///////////////////////////////////////////////////////////////////


    protected AbstractTextComponent<T> getTextField() {
        return textField;
    }

    /**
     * Optional hook for subclasses to override
     */
    protected AbstractTextComponent<T> createTextFieldForRegular(final String id) {
        return createTextField(id);
    }

    protected TextField<T> createTextField(final String id) {
        return new TextField<>(id, newTextFieldValueModel(), cls);
    }

    TextFieldValueModel<T> newTextFieldValueModel() {
        return new TextFieldValueModel<>(this);
    }

    // ///////////////////////////////////////////////////////////////////


    @Override
    protected MarkupContainer createComponentForRegular() {

        // even though only one of textField and scalarValueEditInlineContainer will ever be visible,
        // am instantiating both to avoid NPEs
        // elsewhere can use Component#isVisibilityAllowed or ScalarModel.getEditStyle() to check whichis visible.

        textField = createTextFieldForRegular(ID_SCALAR_VALUE);
        textField.setOutputMarkupId(true);

        addStandardSemantics();

        //
        // read-only/dialog edit
        //

        final MarkupContainer scalarIfRegularFormGroup = createScalarIfRegularFormGroup();
        return scalarIfRegularFormGroup;
    }

    @Override
    protected Component getScalarValueComponent() {
        return textField;
    }

    private void addReplaceDisabledTagWithReadonlyTagBehaviourIfRequired(final Component component) {
        if(!getWicketViewerSettings().isReplaceDisabledTagWithReadonlyTag()) {
            return;
        }
        if (component == null) {
            return;
        }
        if (!component.getBehaviors(ReplaceDisabledTagWithReadonlyTagBehaviour.class).isEmpty()) {
            return;
        }
        component.add(new ReplaceDisabledTagWithReadonlyTagBehaviour());
    }

    protected MarkupContainer createScalarIfRegularFormGroup() {
        Fragment textFieldFragment = createTextFieldFragment("scalarValueContainer");
        final String name = getModel().getFriendlyName();
        textField.setLabel(Model.of(name));

        final FormGroup formGroup = new FormGroup(ID_SCALAR_IF_REGULAR, this.textField);
        textFieldFragment.add(this.textField);
        formGroup.add(textFieldFragment);

        final String labelCaption = getRendering().getLabelCaption(textField);
        final Label scalarName = createScalarName(ID_SCALAR_NAME, labelCaption);

        getModel()
        .getDescribedAs()
        .ifPresent(describedAs->Tooltips.addTooltip(scalarName, describedAs));

        formGroup.add(scalarName);

        return formGroup;
    }

    private Fragment createTextFieldFragment(final String id) {
        return new Fragment(id, createTextFieldFragmentId(), this);
    }

    protected String createTextFieldFragmentId() {
        return "text";
    }

    /**
     * Overrides default to use a fragment, allowing the inner rendering to switch between a simple span
     * or a textarea
     */
    @Override
    protected Component createInlinePromptComponent(
            final String id,
            final IModel<String> inlinePromptModel) {
        final Fragment fragment = new Fragment(id, "textInlinePrompt", this) {
            private static final long serialVersionUID = 1L;

            @Override protected void onComponentTag(final ComponentTag tag) {
                super.onComponentTag(tag);
                tag.put("tabindex","-1");
            }
        };
        Wkt.labelAdd(fragment, "scalarValue", inlinePromptModel);
        return fragment;
    }

    protected void addStandardSemantics() {
        textField.setRequired(getModel().isRequired());
        setTextFieldSizeAndMaxLengthIfSpecified();

        addValidatorForIsisValidation();
    }

    private void addValidatorForIsisValidation() {
        val scalarModel = getModel();

        textField.add(new IValidator<T>() {
            private static final long serialVersionUID = 1L;
            private transient IsisAppCommonContext commonContext;

            @Override
            public void validate(final IValidatable<T> validatable) {
                final T proposedValue = validatable.getValue();
                final ManagedObject proposedAdapter = objectManager().adapt(proposedValue);
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

        });
    }

    private void setTextFieldSizeAndMaxLengthIfSpecified() {

        final Integer maxLength = getMaxLengthOf(getModel());
        Integer typicalLength = getTypicalLenghtOf(getModel());

        // doesn't make sense for typical length to be > maxLength
        if(typicalLength != null && maxLength != null && typicalLength > maxLength) {
            typicalLength = maxLength;
        }

        if (typicalLength != null) {
            textField.add(new AttributeModifier("size", Model.of("" + typicalLength)));
        }

        if(maxLength != null) {
            textField.add(new AttributeModifier("maxlength", Model.of("" + maxLength)));
        }
    }


    // //////////////////////////////////////

    /**
     * Mandatory hook method to build the component to render the model when in
     * {@link org.apache.isis.viewer.wicket.ui.components.scalars.ScalarPanelAbstract.Rendering#COMPACT}
     * format.
     * <p>
     * This default implementation uses a {@link Label}, however it may be overridden if required.
     */
    @Override
    protected Component createComponentForCompact() {
        return Wkt.labelAdd(
                getCompactFragment(CompactType.SPAN), ID_SCALAR_IF_COMPACT,
                ()->getModel().getObjectAsString());
    }

    public enum CompactType {
        INPUT_CHECKBOX,
        SPAN
    }


    Fragment getCompactFragment(final CompactType type) {
        Fragment compactFragment;
        switch (type) {
        case INPUT_CHECKBOX:
            compactFragment = new Fragment(ID_SCALAR_IF_COMPACT, "compactAsInputCheckbox", ScalarPanelTextFieldAbstract.this);
            break;
        case SPAN:
        default:
            compactFragment = new Fragment(ID_SCALAR_IF_COMPACT, "compactAsSpan", ScalarPanelTextFieldAbstract.this);
            break;
        }
        return compactFragment;
    }


    // //////////////////////////////////////

    @Override
    protected InlinePromptConfig getInlinePromptConfig() {
        return InlinePromptConfig.supportedAndHide(textField);
    }

    @Override
    protected IModel<String> obtainInlinePromptModel() {
        IModel<T> model = textField.getModel();
        // must be "live", for ajax updates.
        return _Casts.uncheckedCast(model);
    }

    protected class ToStringConvertingModel<X> extends Model<String> {
        private static final long serialVersionUID = 1L;

        @NonNull private final IConverter<X> converter;

        private ToStringConvertingModel(final @NonNull IConverter<X> converter) {
            this.converter = converter;
        }

        @Override public String getObject() {
            val adapter = scalarModel.getObject();
            val value = ManagedObjects.UnwrapUtil.single(adapter);
            final String str = value != null
                    ? converter.convertToString(_Casts.uncheckedCast(value), getLocaleProvider().getLocale())
                    : null;
            return str;
        }
    }

    protected ToStringConvertingModel<T> toStringConvertingModelOf(final IConverter<T> converter) {
        return new ToStringConvertingModel<>(converter);
    }

    // //////////////////////////////////////


    @Override
    protected void onInitializeNotEditable() {
        super.onInitializeNotEditable();

        textField.setEnabled(false);
        addReplaceDisabledTagWithReadonlyTagBehaviourIfRequired(textField);

        clearTooltip();
    }

    @Override
    protected void onInitializeReadonly(final String disableReason) {
        super.onInitializeReadonly(disableReason);

        textField.setEnabled(false);
        addReplaceDisabledTagWithReadonlyTagBehaviourIfRequired(textField);

        inlinePromptLink.setEnabled(false);

        setTooltip(disableReason);
    }

    @Override
    protected void onInitializeEditable() {
        super.onInitializeEditable();
        textField.setEnabled(true);
        inlinePromptLink.setEnabled(true);
        clearTooltip();
    }

    @Override
    protected void onNotEditable(final String disableReason, final Optional<AjaxRequestTarget> target) {
        textField.setEnabled(false);
        inlinePromptLink.setEnabled(false);
        setTooltip(disableReason);
        target.ifPresent(ajax->{
            ajax.add(textField);
            ajax.add(inlinePromptLink);
        });
    }

    @Override
    protected void onEditable(final Optional<AjaxRequestTarget> target) {
        textField.setEnabled(true);
        inlinePromptLink.setEnabled(true);
        clearTooltip();
        target.ifPresent(ajax->{
            ajax.add(textField);
            ajax.add(inlinePromptLink);
        });
    }

    private void setTooltip(final String tooltip) {
        Tooltips.addTooltip(textField, tooltip);
        Tooltips.addTooltip(inlinePromptLink, tooltip);
    }

    private void clearTooltip() {
        Tooltips.clearTooltip(textField);
        Tooltips.clearTooltip(inlinePromptLink);
    }

    // //////////////////////////////////////

    private static Integer getMaxLengthOf(final @NonNull ScalarModel model) {
        return model.getScalarTypeSpec()
                .lookupFacet(MaxLengthFacet.class)
                .map(MaxLengthFacet::value)
                .orElse(null);
    }

    private static Integer getTypicalLenghtOf(final @NonNull ScalarModel model) {
        return model.getScalarTypeSpec()
                .lookupFacet(TypicalLengthFacet.class)
                .map(TypicalLengthFacet::value)
                .orElse(null);
    }


}

