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

    public ScalarPanelTextFieldParseableAbstract(final String id, final ScalarModel scalarModel) {
        super(id, scalarModel, String.class);
    }

    @Override
    protected AbstractTextComponent<String> createTextFieldForRegular(final String id) {
        final TextFieldStringModel model = new TextFieldStringModel(this);
        return new TextField<>(id, model);
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
