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

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.AbstractTextComponent;
import org.apache.wicket.markup.html.form.FormComponentLabel;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.ComponentFeedbackPanel;
import org.apache.wicket.model.Model;

import org.apache.isis.core.metamodel.facets.maxlen.MaxLengthFacet;
import org.apache.isis.core.metamodel.facets.typicallen.TypicalLengthFacet;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.viewer.wicket.model.models.ScalarModel;

/**
 * Adapter for {@link ScalarPanelAbstract scalar panel}s that are implemented
 * using a simple {@link TextField}.
 */
public abstract class ScalarPanelTextFieldAbstract<T> extends ScalarPanelAbstract {

    private static final long serialVersionUID = 1L;

    private static final String ID_SCALAR_IF_REGULAR = "scalarIfRegular";
    private static final String ID_SCALAR_NAME = "scalarName";
    private static final String ID_FEEDBACK = "feedback";

    private static final String ID_SCALAR_IF_COMPACT = "scalarIfCompact";

    private AbstractTextComponent<T> textField;

    public ScalarPanelTextFieldAbstract(final String id, final ScalarModel scalarModel) {
        super(id, scalarModel);
    }

    protected AbstractTextComponent<T> getTextField() {
        return textField;
    }

    @Override
    protected FormComponentLabel addComponentForRegular() {
        textField = createTextField();
        textField.setOutputMarkupId(true);

        addStandardSemantics();
        addSemantics();

        final FormComponentLabel labelIfRegular = createFormComponentLabel();
        addOrReplace(labelIfRegular);

        addOrReplace(new ComponentFeedbackPanel(ID_FEEDBACK, textField));
        return labelIfRegular;
    }

    /**
     * Optional hook method
     */
    protected void addSemantics() {
        // we don't call textField.setType(), since in most cases NO does the
        // parsing, not wicket
    }

    protected abstract AbstractTextComponent<T> createTextField();

    private FormComponentLabel createFormComponentLabel() {
        final AbstractTextComponent<T> textField = getTextField();
        final String name = getModel().getName();
        textField.setLabel(Model.of(name));

        final FormComponentLabel scalarNameAndValue = new FormComponentLabel(ID_SCALAR_IF_REGULAR, textField);

        scalarNameAndValue.add(textField);

        final Label scalarName = new Label(ID_SCALAR_NAME, getFormat().getLabelCaption(textField));
        scalarNameAndValue.add(scalarName);

        return scalarNameAndValue;
    }

    protected void addStandardSemantics() {
        setRequiredIfSpecified();
        setTextFieldSizeIfSpecified();
    }

    private void setRequiredIfSpecified() {
        final ScalarModel scalarModel = getModel();
        final boolean required = scalarModel.isRequired();
        textField.setRequired(required);
    }

    private void setTextFieldSizeIfSpecified() {
        final int size = determineSize();
        if (size != -1) {
            textField.add(new AttributeModifier("size", true, new Model<String>("" + size)));
        }
    }

    private int determineSize() {
        final ScalarModel scalarModel = getModel();
        final ObjectSpecification noSpec = scalarModel.getTypeOfSpecification();

        final TypicalLengthFacet typicalLengthFacet = noSpec.getFacet(TypicalLengthFacet.class);
        if (typicalLengthFacet != null) {
            return typicalLengthFacet.value();
        }
        final MaxLengthFacet maxLengthFacet = noSpec.getFacet(MaxLengthFacet.class);
        if (maxLengthFacet != null) {
            return maxLengthFacet.value();
        }
        return -1;
    }

    /**
     * Mandatory hook method to build the component to render the model when in
     * {@link Format#COMPACT compact} format.
     */
    @Override
    protected Component addComponentForCompact() {
        final Label labelIfCompact = new Label(ID_SCALAR_IF_COMPACT, getModel().getObjectAsString());
        addOrReplace(labelIfCompact);
        return labelIfCompact;
    }

    @Override
    protected void onBeforeRenderWhenViewMode() {
        super.onBeforeRenderWhenViewMode();
        textField.setEnabled(false);
        setTitleAttribute("");
    }

    @Override
    protected void onBeforeRenderWhenDisabled(final String disableReason) {
        super.onBeforeRenderWhenDisabled(disableReason);
        textField.setEnabled(false);
        setTitleAttribute(disableReason);
    }

    @Override
    protected void onBeforeRenderWhenEnabled() {
        super.onBeforeRenderWhenEnabled();
        textField.setEnabled(true);
        setTitleAttribute("");
    }

    private void setTitleAttribute(final String titleAttribute) {
        textField.add(new AttributeModifier("title", true, Model.of(titleAttribute)));
    }

}
