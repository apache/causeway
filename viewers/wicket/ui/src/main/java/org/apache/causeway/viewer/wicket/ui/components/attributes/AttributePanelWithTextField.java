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

import java.util.Optional;

import org.apache.wicket.markup.html.form.AbstractTextComponent;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.model.IModel;
import org.apache.wicket.util.convert.IConverter;

import org.apache.causeway.commons.internal.assertions._Assert;
import org.apache.causeway.commons.internal.exceptions._Exceptions;
import org.apache.causeway.viewer.commons.model.components.UiString;
import org.apache.causeway.viewer.wicket.model.models.UiAttributeWkt;
import org.apache.causeway.viewer.wicket.ui.components.attributes.AttributeFragmentFactory.InputFragment;
import org.apache.causeway.viewer.wicket.ui.util.Wkt;

/**
 * This panel is the base for panels that present a textfield/text area.
 * Uses {@link UiAttributeWkt} as the backing model.
 * <p>
 * Supports the concept of being
 * COMPACT (eg within a table) or
 * REGULAR (eg within a form).
 */
public abstract class AttributePanelWithTextField<T>
extends AttributePanelWithFormField<T> {

    private static final long serialVersionUID = 1L;

    private AbstractTextComponent<T> formField;

    protected AttributePanelWithTextField(
            final String id,
            final UiAttributeWkt attributeModel,
            final Class<T> type) {
        super(id, attributeModel, type);
        guardAgainstIncompatibleScalarType();
    }

    // -- CONVERSION

    /**
     * Optionally the {@link IConverter} that is used for the either regular (editing) or compact (HTML) view of the panel.
     */
    protected abstract Optional<IConverter<T>> converter();

    protected final IConverter<T> converterElseFail() {
        return converter().orElseThrow(()->
            _Exceptions.illegalState("framework bug: %s requires a converter", this.getClass().getSimpleName()));
    }

    // --

    /**
     * TextField, with converter.
     */
    protected AbstractTextComponent<T> createTextField(final String id) {
        return getFormatModifiers().contains(FormatModifier.MULTILINE)
                ? Wkt.textAreaWithConverter(
                        id, unwrappedModel(), type, converter())
                : Wkt.textFieldWithConverter(
                        id, unwrappedModel(), type, converter());
    }

    protected final IModel<T> unwrappedModel() {
        return attributeModel().unwrapped(type);
    }

    // --

    @Override
    protected final FormComponent<T> createFormComponent(final String id, final UiAttributeWkt attributeModel) {
        this.formField = createTextField(id);
        formField.setOutputMarkupId(true);
        return applyFormComponentAttributes(formField);
    }

    @Override
    protected Optional<InputFragment> getInputFragmentType() {
        return Optional.of(getFormatModifiers().contains(FormatModifier.MULTILINE)
                ? InputFragment.TEXTAREA
                : InputFragment.TEXT);
    }

    @Override
    protected String obtainInlinePromptLinkCssIfAny() {
        return !getFormatModifiers().contains(FormatModifier.MULTILINE)
                ? super.obtainInlinePromptLinkCssIfAny()
                /* Most other components require 'form-control form-control-sm' on the owning inline prompt link.
                 * For TextArea, however, this instead appears on the TextArea itself.
                 */
                : null;
    }

    // -- CONVERSION

    @Override
    protected final UiString obtainOutputFormat() {
        // conversion does not affect the output format (usually HTML)
        return super.obtainOutputFormat();
    }

    // -- HELPER

    private void guardAgainstIncompatibleScalarType() {
        _Assert.assertTrue(attributeModel().getElementType().isAssignableFrom(type), ()->
            String.format("[%s:%s] cannot possibly unwrap model of type %s into target type %s",
                    this.getClass().getSimpleName(),
                    attributeModel().getIdentifier(),
                    attributeModel().getElementType().getCorrespondingClass(),
                    type));
    }

    <F extends FormComponent<?>> F applyFormComponentAttributes(final F formComponent) {
        var attributeModel = attributeModel();
        Wkt.setFormComponentAttributes(formComponent,
                attributeModel::multilineNumberOfLines,
                attributeModel::maxLength,
                attributeModel::typicalLength);
        return formComponent;
    }

}

