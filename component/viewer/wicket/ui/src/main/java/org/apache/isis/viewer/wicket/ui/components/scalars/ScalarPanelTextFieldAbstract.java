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
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.AbstractTextComponent;
import org.apache.wicket.markup.html.form.FormComponentLabel;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.Model;

import org.apache.isis.core.metamodel.facets.SingleIntValueFacet;
import org.apache.isis.core.metamodel.facets.propparam.maxlen.MaxLengthFacet;
import org.apache.isis.core.metamodel.facets.objpropparam.typicallen.TypicalLengthFacet;
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
    
    protected static final String ID_SCALAR_VALUE = "scalarValue";

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

    protected AbstractTextComponent<T> createTextFieldForRegular() {
        return createTextField(ID_SCALAR_VALUE);
    }

    protected TextField<T> createTextField(final String id) {
        return new TextField<>(id, newTextFieldValueModel(), cls);
    }

    protected TextFieldValueModel<T> newTextFieldValueModel() {
        return new TextFieldValueModel<>(this);
    }

    @Override
    protected FormComponentLabel addComponentForRegular() {
        textField = createTextFieldForRegular();
        textField.setOutputMarkupId(true);

        addStandardSemantics();
        addSemantics();

        final FormComponentLabel labelIfRegular = createFormComponentLabel();
        addOrReplace(labelIfRegular);

        final Label scalarName = new Label(ID_SCALAR_NAME, getRendering().getLabelCaption(textField));

        if(getModel().isRequired()) {
            scalarName.add(new CssClassAppender("mandatory"));
        }

        addOrReplace(scalarName);

        final String describedAs = getModel().getDescribedAs();
        if(describedAs != null) {
            labelIfRegular.add(new AttributeModifier("title", Model.of(describedAs)));
        }
        
        addFeedbackTo(labelIfRegular, textField);
        addAdditionalLinksTo(labelIfRegular);
        
        return labelIfRegular;
    }
    
    

    /**
     * Optional hook method
     */
    protected void addSemantics() {
        // we don't call textField.setType(), since we want more control 
        // over the parsing (using custom subclasses of TextField etc)
    }



    private FormComponentLabel createFormComponentLabel() {
        final AbstractTextComponent<T> textField = getTextField();
        final String name = getModel().getName();
        textField.setLabel(Model.of(name));
        
        final FormComponentLabel scalarNameAndValue = new FormComponentLabel(ID_SCALAR_IF_REGULAR, textField);
        
        scalarNameAndValue.add(textField);

        return scalarNameAndValue;
    }

    protected void addStandardSemantics() {
         textField.setRequired(getModel().isRequired());
         setTextFieldSizeAndMaxLengthIfSpecified(textField);
    }

    protected void setTextFieldSizeAndMaxLengthIfSpecified(AbstractTextComponent<T> textField) {

        final Integer maxLength = getValueOf(getModel(), MaxLengthFacet.class);
        Integer typicalLength = getValueOf(getModel(), TypicalLengthFacet.class);

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

    private static Integer getValueOf(ScalarModel model, Class<? extends SingleIntValueFacet> facetType) {
        final SingleIntValueFacet facet = model.getFacet(facetType);
        return facet != null ? facet.value() : null;
    }
    
    /**
     * Mandatory hook method to build the component to render the model when in
     * {@link Rendering#COMPACT compact} format.
     * 
     * <p>
     * This default implementation uses a {@link Label}, however it may be overridden if required.
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

    @Override
    protected void addFormComponentBehavior(Behavior behavior) {
        textField.add(behavior);
    }

}

