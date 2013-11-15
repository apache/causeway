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

import com.google.inject.Inject;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.markup.html.form.AbstractTextComponent;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.IValidator;
import org.apache.wicket.validation.ValidationError;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.facets.renderedadjusted.RenderedAdjustedFacet;
import org.apache.isis.viewer.wicket.model.isis.WicketViewerSettings;
import org.apache.isis.viewer.wicket.model.models.ScalarModel;

/**
 * Panel for rendering scalars representing dates, along with a date picker.
 */
public abstract class ScalarPanelTextFieldDatePickerAbstract<T extends Serializable> extends ScalarPanelTextFieldAbstract<T>  {

    private static final long serialVersionUID = 1L;

    private DateConverter<T> converter;

    public ScalarPanelTextFieldDatePickerAbstract(final String id, final ScalarModel scalarModel, final Class<T> cls) {
        super(id, scalarModel, cls);
    }

    /**
     * Expected to be in subclasses' constructor.
     * 
     * <p>
     * Is not passed into constructor only to allow subclass to read from injected {@link #getSettings()}.
     */
    protected void init(DateConverter<T> converter) {
        this.converter = converter;
    }

    protected int getAdjustBy() {
        final RenderedAdjustedFacet facet = getModel().getFacet(RenderedAdjustedFacet.class);
        return facet != null? facet.value(): 0;
    }

    protected TextField<T> createTextField(final String id) {
        return new TextFieldWithDatePicker<T>(id, new TextFieldValueModel<T>(this), cls, converter);
    }

    @Override
    protected void addSemantics() {
        super.addSemantics();

        addObjectAdapterValidator();
    }

    
    protected Component addComponentForCompact() {
        final AbstractTextComponent<T> textField = createTextField(ID_SCALAR_IF_COMPACT);
        final IModel<T> model = textField.getModel();
        final T object = (T) model.getObject();
        model.setObject(object);
        
        textField.setEnabled(false);
        
        final String dateTimePattern = converter.getDateTimePattern(getLocale());
        final int length = dateTimePattern.length() + 1; // adding 1 because seemed to truncate in tables in certain circumstances
        textField.add(new AttributeModifier("size", Model.of("" + length)));
        
        addOrReplace(textField);
        return textField;
    }

    private void addObjectAdapterValidator() {
        final AbstractTextComponent<T> textField = getTextField();

        textField.add(new IValidator<T>() {
            private static final long serialVersionUID = 1L;

            @Override
            public void validate(final IValidatable<T> validatable) {
                final T proposed = validatable.getValue();
                final ObjectAdapter proposedAdapter = adapterFor(proposed);
                String reasonIfAny = scalarModel.validate(proposedAdapter);
                if (reasonIfAny != null) {
                    final ValidationError error = new ValidationError();
                    error.setMessage(reasonIfAny);
                    validatable.error(error);
                }
            }
        });
    }

    
    protected String getDatePattern() {
        return getSettings().getDatePattern();
    }
    protected String getDateTimePattern() {
        return getSettings().getDateTimePattern();
    }
    protected String getDatePickerPattern() {
        return getSettings().getDatePickerPattern();
    }
    
    @Inject
    private WicketViewerSettings settings;
    protected WicketViewerSettings getSettings() {
        return settings;
    }

    private ObjectAdapter adapterFor(final Object pojo) {
        return getAdapterManager().adapterFor(pojo);
    }
    
    
}
