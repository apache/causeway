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

package org.apache.isis.viewer.wicket.ui.components.scalars.noapplib;

import org.apache.wicket.datetime.markup.html.form.DateTextField;
import org.apache.wicket.extensions.yui.calendar.DatePicker;
import org.apache.wicket.markup.html.form.AbstractTextComponent;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.Model;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.IValidator;
import org.apache.wicket.validation.ValidationError;

import org.apache.isis.applib.value.Date;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.viewer.wicket.model.models.ScalarModel;
import org.apache.isis.viewer.wicket.ui.components.scalars.ScalarPanelTextFieldAbstract;

/**
 * Panel for rendering scalars of type {@link Date Isis' applib.Date}.
 */
public class IsisDatePanel extends ScalarPanelTextFieldAbstract<java.util.Date> {

    private static final long serialVersionUID = 1L;

    private static final String ID_SCALAR_VALUE = "scalarValue";
    private static final String DATE_PATTERN = "MM/dd/yyyy"; // TODO: i18n, see for example http://apache-wicket.1842946.n4.nabble.com/DatePicker-format-td1847739.html

    public IsisDatePanel(final String id, final ScalarModel scalarModel) {
        super(id, scalarModel);
    }

    @Override
    protected AbstractTextComponent<java.util.Date> createTextField() {
        final TextField<java.util.Date> textField = DateTextField.forDatePattern(ID_SCALAR_VALUE, new Model<java.util.Date>() {
            private static final long serialVersionUID = 1L;

            @Override
            public java.util.Date getObject() {
                final ObjectAdapter adapter = getModel().getObject();
                if (adapter == null) {
                    return null;
                }
                final Date noDate = (Date) adapter.getObject();
                return noDate.dateValue();
            }

            @Override
            public void setObject(final java.util.Date date) {
                final Date noDate = new Date(date);
                final ObjectAdapter adapter = adapterFor(noDate);
                getModel().setObject(adapter);
            }
        }, DATE_PATTERN);
        return textField;
    }

    @Override
    protected void addSemantics() {
        super.addSemantics();

        final DatePicker datePicker = new DatePicker();
        getTextField().add(datePicker);

        addObjectAdapterValidator();
    }

    private void addObjectAdapterValidator() {
        final ScalarModel scalarModel = getModel();
        final AbstractTextComponent<java.util.Date> textField = getTextField();

        textField.add(new IValidator<java.util.Date>() {
            private static final long serialVersionUID = 1L;

            @Override
            public void validate(final IValidatable<java.util.Date> validatable) {
                final java.util.Date proposedValue = validatable.getValue();
                final Date proposed = new Date(proposedValue);
                final ObjectAdapter proposedAdapter = adapterFor(proposed);
                final String reasonIfAny = scalarModel.validate(proposedAdapter);
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
