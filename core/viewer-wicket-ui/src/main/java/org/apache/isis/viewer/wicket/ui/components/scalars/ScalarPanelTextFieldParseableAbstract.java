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

import org.apache.wicket.markup.html.form.AbstractTextComponent;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.Model;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.IValidator;
import org.apache.wicket.validation.ValidationError;
import org.apache.wicket.validation.validator.StringValidator;

import org.apache.isis.core.metamodel.facets.objectvalue.maxlen.MaxLengthFacet;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.viewer.wicket.model.models.ScalarModel;

/**
 * Adapter for {@link ScalarPanelTextFieldAbstract textField-based scalar panel}
 * s where moreover the scalar parameter or property is a value type that is
 * parseable.
 */
public abstract class ScalarPanelTextFieldParseableAbstract extends ScalarPanelTextFieldAbstract<String> {

    private static final long serialVersionUID = 1L;
    protected final String idTextField;

    public ScalarPanelTextFieldParseableAbstract(final String id, final String idTextField, final ScalarModel scalarModel) {
        super(id, scalarModel, String.class);
        this.idTextField = idTextField;
    }

    @Override
    protected AbstractTextComponent<String> createTextFieldForRegular() {
        final AbstractTextComponent<String> textField = new TextField<String>(idTextField, new Model<String>() {
            private static final long serialVersionUID = 1L;

            @Override
            public String getObject() {
                return getModel().getObjectAsString();
            }

            @Override
            public void setObject(final String object) {
                if (object == null) {
                    getModel().setObject(null);
                } else {
                    getModel().setObjectAsString(object);
                }
            }
        });
        return textField;
    }

    @Override
    protected void addStandardSemantics() {
        super.addStandardSemantics();

        addMaxLengthValidator();
    }

    private void addMaxLengthValidator() {
        final ScalarModel scalarModel = getModel();
        final AbstractTextComponent<String> textField = getTextField();

        final ObjectSpecification facetHolder = scalarModel.getTypeOfSpecification();

        final MaxLengthFacet maxLengthFacet = facetHolder.getFacet(MaxLengthFacet.class);
        if (maxLengthFacet != null) {
            textField.add(StringValidator.maximumLength(maxLengthFacet.value()));
        }
    }

}
