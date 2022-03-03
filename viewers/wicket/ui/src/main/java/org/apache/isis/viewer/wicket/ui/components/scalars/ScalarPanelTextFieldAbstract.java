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
import java.util.Locale;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.AbstractTextComponent;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.convert.IConverter;
import org.apache.wicket.validation.validator.StringValidator;

import org.apache.isis.commons.internal.base._Casts;
import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.core.metamodel.commons.ScalarRepresentation;
import org.apache.isis.core.metamodel.facets.objectvalue.maxlen.MaxLengthFacet;
import org.apache.isis.core.metamodel.facets.objectvalue.multiline.MultiLineFacet;
import org.apache.isis.core.metamodel.facets.objectvalue.typicallen.TypicalLengthFacet;
import org.apache.isis.core.metamodel.spec.ManagedObjects;
import org.apache.isis.core.metamodel.spec.feature.ObjectFeature;
import org.apache.isis.viewer.wicket.model.models.ScalarModel;
import org.apache.isis.viewer.wicket.ui.components.widgets.bootstrap.FormGroup;
import org.apache.isis.viewer.wicket.ui.panels.PanelAbstract;
import org.apache.isis.viewer.wicket.ui.util.Wkt;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.val;

/**
 * Adapter for {@link PanelAbstract panel}s that use a {@link ScalarModel} as
 * their backing model.
 * <p>
 * Supports the concept of being
 * COMPACT (eg within a table) or
 * REGULAR (eg within a form).
 * <p>
 * This implementation is for panels that use a textfield/text area.
 */
public abstract class ScalarPanelTextFieldAbstract<T extends Serializable>
extends ScalarPanelWithFormFieldAbstract<T>
implements TextFieldValueModel.ScalarModelProvider {

    private static final long serialVersionUID = 1L;

    private AbstractTextComponent<T> formField;

    @Getter(value = AccessLevel.PROTECTED)
    private TextFieldVariant textFieldVariant;

    protected ScalarPanelTextFieldAbstract(
            final String id,
            final ScalarModel scalarModel,
            final Class<T> type) {
        this(id, scalarModel, type, TextFieldVariant.SINGLE_LINE);
    }

    protected ScalarPanelTextFieldAbstract(
            final String id,
            final ScalarModel scalarModel,
            final Class<T> type,
            final TextFieldVariant textFieldVariant) {
        super(id, scalarModel, type);
        this.textFieldVariant = textFieldVariant;
    }

    // -- CONVERSION

    protected final IConverter<T> getConverter(final ScalarModel scalarModel) {
        return getConverter(scalarModel.getMetaModel(), scalarModel.isEditMode()
                ? ScalarRepresentation.EDITING
                : ScalarRepresentation.VIEWING);
    }

    /**
     * Converter that is used for the either regular (editing) or compact (HTML) view of the panel,
     * based on argument {@code scalarRepresentation}.
     */
    protected abstract IConverter<T> getConverter(
            @NonNull ObjectFeature propOrParam,
            @NonNull ScalarRepresentation scalarRepresentation);

    // --

    /**
     * TextField, with converter.
     */
    protected AbstractTextComponent<T> createTextField(final String id) {
        val converter = getConverter(scalarModel());
        return getTextFieldVariant().isSingleLine()
                ? Wkt.textFieldWithConverter(
                        id, newTextFieldValueModel(), type, converter)
                : setRowsAndMaxLengthAttributesOn(Wkt.textAreaWithConverter(
                        id, newTextFieldValueModel(), type, converter));
    }

    protected final TextFieldValueModel<T> newTextFieldValueModel() {
        return new TextFieldValueModel<>(this);
    }

    // --

    @Override
    protected FormComponent<T> createFormComponent(final ScalarModel scalarModel) {

        // even though only one of textField and scalarValueEditInlineContainer will ever be visible,
        // am instantiating both to avoid NPEs
        // elsewhere can use Component#isVisibilityAllowed or ScalarModel.getEditStyle() to check which is visible.

        formField = createTextField(ID_SCALAR_VALUE);
        formField.setOutputMarkupId(true);

        return formField;
    }

    @Override
    protected void onFormGroupCreated(final FormGroup formGroup) {
        super.onFormGroupCreated(formGroup);
        val textFieldFragment = new Fragment(ID_SCALAR_VALUE_CONTAINER, getTextFieldFragmentId(), this);
        textFieldFragment.add(getFormComponent());
        formGroup.add(textFieldFragment);
        setTextFieldSizeAndMaxLengthIfSpecified();
    }

    protected String getTextFieldFragmentId() {
        return getTextFieldVariant().isSingleLine()
                ? "text"
                : "textarea";
    }

    @Override
    protected String obtainInlinePromptLinkCssIfAny() {
        return getTextFieldVariant().isSingleLine()
                ? super.obtainInlinePromptLinkCssIfAny()
                /* Most other components require 'form-control form-control-sm' on the owning inline prompt link.
                 * For TextArea, however, this instead appears on the TextArea itself.
                 */
                : null;
    }

    /**
     * Overrides default to use a fragment, allowing the inner rendering to switch between a simple span
     * or a text-area.
     */
    @Override
    protected Component createInlinePromptComponent(
            final String id,
            final IModel<String> inlinePromptModel) {

        switch(getTextFieldVariant()) {
        case SINGLE_LINE:{
            val fragment = Wkt.fragmentAddNoTab(this, id, "textInlinePrompt");
            Wkt.labelAdd(fragment, "scalarValue", inlinePromptModel);
            return fragment;
        }
        case MULTI_LINE:{
            val fragment = new Fragment(id, "textareaInlinePrompt", this);
            val inlinePromptTextArea = Wkt.textAreaAddNoTab(fragment, "scalarValue", inlinePromptModel);
            setRowsAndMaxLengthAttributesOn(inlinePromptTextArea);
            return fragment;
        }
        default:
            throw _Exceptions.unmatchedCase(getTextFieldVariant());
        }
    }

    private void setTextFieldSizeAndMaxLengthIfSpecified() {

        val formComponent = getFormComponent();

        final Integer maxLength = getMaxLengthOf(getModel());
        Integer typicalLength = getTypicalLenghtOf(getModel());

        // doesn't make sense for typical length to be > maxLength
        if(typicalLength != null && maxLength != null && typicalLength > maxLength) {
            typicalLength = maxLength;
        }

        if (typicalLength != null) {
            formComponent.add(new AttributeModifier("size", Model.of("" + typicalLength)));
        }

        if(maxLength != null) {
            formComponent.add(new AttributeModifier("maxlength", Model.of("" + maxLength)));
            if(type.equals(String.class)) {
                formComponent.add(StringValidator.maximumLength(maxLength));
            }
        }
    }

    // --

    /**
     * Builds the component to render the model when in COMPACT format.
     * <p>
     * The (textual) default implementation uses a {@link Label}.
     * However, it may be overridden if required.
     */
    @Override
    protected Component createComponentForCompact() {
        return Wkt.labelAdd(
                getCompactFragment(CompactType.SPAN),
                ID_SCALAR_IF_COMPACT,
                ()->{
                    val scalarModel = scalarModel();
                    return scalarModel.isCurrentValueAbsent()
                            ? ""
                            : scalarModel.proposedValue().getValueAsParsableText().getValue();
                });
    }

    public enum CompactType {
        INPUT_CHECKBOX,
        SPAN
    }

    protected Fragment getCompactFragment(final CompactType type) {
        switch (type) {
        case INPUT_CHECKBOX:
            return new Fragment(ID_SCALAR_IF_COMPACT, "compactAsInputCheckbox", ScalarPanelTextFieldAbstract.this);
        case SPAN:
        default:
            return new Fragment(ID_SCALAR_IF_COMPACT, "compactAsSpan", ScalarPanelTextFieldAbstract.this);
        }
    }

    // -- CONVERSION

    @Override
    protected final IModel<String> obtainInlinePromptModel() {
        val converter = getConverter(scalarModel());
        return converter!=null
                ? new ToStringConvertingModel<>(converter)
                :  _Casts.uncheckedCast(getFormComponent().getModel());
    }

    protected class ToStringConvertingModel<X> extends Model<String> {
        private static final long serialVersionUID = 1L;

        @NonNull private final IConverter<X> converter;

        private ToStringConvertingModel(final @NonNull IConverter<X> converter) {
            this.converter = converter;
        }

        @Override public String getObject() {
            val adapter = scalarModel().getObject();
            val value = ManagedObjects.UnwrapUtil.single(adapter);
            final String str = value != null
                    ? converter.convertToString(
                            _Casts.uncheckedCast(value),
                            getLanguageProvider().getPreferredLanguage().orElseGet(Locale::getDefault))
                    : null;
            return str;
        }
    }

    // -- HELPER

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

    private Component setAttribute(final TextArea<?> textField, final String attributeName, final int i) {
        return textField.add(AttributeModifier.replace(attributeName, ""+i));
    }

    protected <X> TextArea<X> setRowsAndMaxLengthAttributesOn(final TextArea<X> textArea) {
        val multiLineFacet = getModel().getFacet(MultiLineFacet.class);
        if(multiLineFacet != null) {
            setAttribute(textArea, "rows", multiLineFacet.numberOfLines());
        }

        val maxLength = getMaxLengthOf(getModel());
        if(maxLength != null) {
            // in conjunction with javascript in jquery.isis.wicket.viewer.js
            // see http://stackoverflow.com/questions/4459610/set-maxlength-in-html-textarea
            setAttribute(textArea, "maxlength", maxLength);
        }
        return textArea;
    }


}

