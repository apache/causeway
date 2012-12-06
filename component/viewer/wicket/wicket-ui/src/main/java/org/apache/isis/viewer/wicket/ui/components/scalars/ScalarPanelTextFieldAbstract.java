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

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.AbstractTextComponent;
import org.apache.wicket.markup.html.form.FormComponentLabel;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.ComponentFeedbackPanel;
import org.apache.wicket.model.Model;

import org.apache.isis.core.metamodel.facets.SingleIntValueFacet;
import org.apache.isis.core.metamodel.facets.maxlen.MaxLengthFacet;
import org.apache.isis.core.metamodel.facets.typicallen.TypicalLengthFacet;
import org.apache.isis.viewer.wicket.model.models.ScalarModel;
import org.apache.isis.viewer.wicket.ui.util.CssClassAppender;

/**
 * Adapter for {@link ScalarPanelAbstract scalar panel}s that are implemented
 * using a simple {@link TextField}.
 */
public abstract class ScalarPanelTextFieldAbstract<T extends Serializable> extends ScalarPanelAbstract {

    private static final long serialVersionUID = 1L;

    private static final String ID_SCALAR_IF_REGULAR = "scalarIfRegular";
    private static final String ID_SCALAR_NAME = "scalarName";
    private static final String ID_FEEDBACK = "feedback";

    protected static final String ID_SCALAR_IF_COMPACT = "scalarIfCompact";

    protected final Class<T> cls;
    
    private AbstractTextComponent<T> textField;

    public ScalarPanelTextFieldAbstract(final String id, final ScalarModel scalarModel, final Class<T> cls) {
        super(id, scalarModel);
        this.cls = cls;
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
        if(getModel().isRequired()) {
            labelIfRegular.add(new CssClassAppender("mandatory"));
        }

        final String describedAs = getModel().getDescribedAs();
        if(describedAs != null) {
            labelIfRegular.add(new AttributeModifier("title", Model.of(describedAs)));
        }
        
        addFeedbackTo(labelIfRegular);
        return labelIfRegular;
    }

    protected void addFeedbackTo(MarkupContainer markupContainer) {
        // 6.0.0
        markupContainer.addOrReplace(new ComponentFeedbackPanel(ID_FEEDBACK, textField));
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

        final Label scalarName = new Label(ID_SCALAR_NAME, getRendering().getLabelCaption(textField));
        
        scalarNameAndValue.add(scalarName);

        return scalarNameAndValue;
    }

    protected void addStandardSemantics() {
        setRequiredIfSpecified();
        setTextFieldSizeIfSpecified(textField);
    }

    private void setRequiredIfSpecified() {
        final ScalarModel scalarModel = getModel();
        final boolean required = scalarModel.isRequired();
        textField.setRequired(required);
    }

    protected void setTextFieldSizeIfSpecified(AbstractTextComponent<T> textField) {
        final Integer size = determineSize();
        if (size != null) {
            textField.add(new AttributeModifier("size", Model.of("" + size)));
        }
    }

    @SuppressWarnings("unchecked")
    protected Integer determineSize() {
        return firstValueOf(getModel(), TypicalLengthFacet.class, MaxLengthFacet.class);
    }
    
    private Integer firstValueOf(ScalarModel model, Class<? extends SingleIntValueFacet>... facetTypes) {
        for(Class<? extends SingleIntValueFacet> facetType: facetTypes) {
        final SingleIntValueFacet facet = model.getFacet(facetType);
            if (facet != null) {
                return facet.value();
            }
        }
        return null;
    }
    
    /**
     * Mandatory hook method to build the component to render the model when in
     * {@link Rendering#COMPACT compact} format.
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
        textField.add(new AttributeModifier("title", Model.of(titleAttribute)));
    }

}
