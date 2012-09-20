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

import java.util.Date;

import org.apache.wicket.datetime.PatternDateConverter;
import org.apache.wicket.datetime.markup.html.form.DateTextField;
import org.apache.wicket.extensions.yui.calendar.DatePicker;
import org.apache.wicket.markup.html.form.AbstractTextComponent;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.Model;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.IValidator;
import org.apache.wicket.validation.ValidationError;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.viewer.wicket.model.models.ScalarModel;

/**
 * Panel for rendering scalars representing dates, along with a date picker.
 */
public abstract class ScalarPanelTextFieldDatePickerAbstract<T> extends ScalarPanelTextFieldAbstract<java.util.Date> {

    private static final long serialVersionUID = 1L;

    private static final String DATE_PATTERN = "dd-MM-yyyy"; // TODO: yui calendar does not seem to understand 'dd-MMM-yyyy' (interprets as dd-MM-yyyy)
    
    private final String idScalarValue;

    public ScalarPanelTextFieldDatePickerAbstract(final String id, String idScalarValue, final ScalarModel scalarModel) {
        super(id, scalarModel, java.util.Date.class);
        this.idScalarValue = idScalarValue;
    }

    protected abstract java.util.Date asDate(final T pojo);
    protected abstract T asPojo(final java.util.Date date);

    @Override
    protected AbstractTextComponent<java.util.Date> createTextField() {
        final TextField<java.util.Date> textField = DateTextField.withConverter(idScalarValue, new Model<java.util.Date>() {
            private static final long serialVersionUID = 1L;

            @Override
            public java.util.Date getObject() {
                final ObjectAdapter adapter = getModel().getObject();
                if (adapter == null) {
                    return null;
                }
                @SuppressWarnings("unchecked")
                final T pojo = (T) adapter.getObject();
                final java.util.Date date = asDate(pojo);
                return date;
            }

            @Override
            public void setObject(final java.util.Date date) {
                if(date == null) {
                    getModel().setObject(null);
                    return;
                }
                final T pojo = asPojo(date);
                final ObjectAdapter adapter = adapterFor(pojo);
                getModel().setObject(adapter);
            }
        }, new PatternDateConverter(DATE_PATTERN, true));
        return textField;
    }

    @Override
    protected void addSemantics() {
        super.addSemantics();

        final DatePicker datePicker = new DatePicker(){
            private static final long serialVersionUID = 1L;

            @Override
            protected String getAdditionalJavaScript()
            {
                return "${calendar}.cfg.setProperty(\"navigator\",true,false); ${calendar}.render();";
            }
        };
        datePicker.setShowOnFieldClick(true);
        datePicker.setAutoHide(true);
        getTextField().add(datePicker);

        addObjectAdapterValidator();
    }

    private void addObjectAdapterValidator() {
        final AbstractTextComponent<Date> textField = getTextField();

        textField.add(new IValidator<java.util.Date>() {
            private static final long serialVersionUID = 1L;

            @Override
            public void validate(final IValidatable<java.util.Date> validatable) {
                final java.util.Date proposedValue = validatable.getValue();
                final T proposed = asPojo(proposedValue);
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

    private ObjectAdapter adapterFor(final Object pojo) {
        return getAdapterManager().adapterFor(pojo);
    }
}
