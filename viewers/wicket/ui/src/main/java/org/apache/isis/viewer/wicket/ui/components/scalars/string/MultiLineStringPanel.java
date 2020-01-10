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

package org.apache.isis.viewer.wicket.ui.components.scalars.string;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.form.AbstractTextComponent;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.IModel;

import org.apache.isis.metamodel.facets.SingleIntValueFacet;
import org.apache.isis.metamodel.facets.objectvalue.maxlen.MaxLengthFacet;
import org.apache.isis.metamodel.facets.objectvalue.multiline.MultiLineFacet;
import org.apache.isis.viewer.wicket.model.models.ScalarModel;
import org.apache.isis.viewer.wicket.ui.components.scalars.ScalarPanelTextFieldParseableAbstract;
import org.apache.isis.viewer.wicket.ui.components.scalars.TextFieldStringModel;

/**
 * Panel for rendering MultiLine scalars of type String
 */
public class MultiLineStringPanel extends ScalarPanelTextFieldParseableAbstract {

    private static final long serialVersionUID = 1L;

    public MultiLineStringPanel(final String id, final ScalarModel scalarModel) {
        super(id, scalarModel);
    }

    @Override
    protected AbstractTextComponent<String> createTextFieldForRegular(final String id) {
        TextFieldStringModel model = new TextFieldStringModel(this);
        final TextArea<String> textArea = new TextArea<String>(id, model);
        setRowsAndMaxLengthAttributesOn(textArea);
        return textArea;
    }

    private void setRowsAndMaxLengthAttributesOn(final TextArea<String> textField) {
        final MultiLineFacet multiLineFacet = getModel().getFacet(MultiLineFacet.class);
        setAttribute(textField, "rows", multiLineFacet.numberOfLines());

        final Integer maxLength = getValueOf(getModel(), MaxLengthFacet.class);
        if(maxLength != null) {
            // in conjunction with javascript in jquery.isis.wicket.viewer.js
            // see http://stackoverflow.com/questions/4459610/set-maxlength-in-html-textarea
            setAttribute(textField, "maxlength", maxLength);
        }
    }

    @Override
    protected String createTextFieldFragmentId() {
        return "textarea";
    }

    @Override
    protected Component createInlinePromptComponent(
            final String id,
            final IModel<String> inlinePromptModel) {
        final Fragment fragment = new Fragment(id, "textareaInlinePrompt", this);
        final TextArea<String> inlinePromptTextArea = new TextArea<String>("scalarValue", inlinePromptModel) {

            @Override protected void onComponentTag(final ComponentTag tag) {
                super.onComponentTag(tag);
                tag.put("tabindex","-1");
            }
        };
        setRowsAndMaxLengthAttributesOn(inlinePromptTextArea);
        fragment.add(inlinePromptTextArea);
        return fragment;
    }

    /**
     * Most other components require 'form-control input-sm' on the owning inline prompt link.
     * For this component, however, which uses a textarea, this instead appears on the textarea itself.
     */
    @Override
    protected String obtainInlinePromptLinkCssIfAny() {
        return null;
    }

    @Override
    protected String getScalarPanelType() {
        return "multiLineStringPanel";
    }

    private Component setAttribute(final TextArea<String> textField, final String attributeName, final int i) {
        return textField.add(AttributeModifier.replace(attributeName, ""+i));
    }

    private static Integer getValueOf(ScalarModel model, Class<? extends SingleIntValueFacet> facetType) {
        final SingleIntValueFacet facet = model.getFacet(facetType);
        return facet != null ? facet.value() : null;
    }

}
