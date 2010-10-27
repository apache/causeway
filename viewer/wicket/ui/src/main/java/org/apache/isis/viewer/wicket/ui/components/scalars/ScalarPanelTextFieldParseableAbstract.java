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

import org.apache.isis.metamodel.facets.propparam.validate.maxlength.MaxLengthFacet;
import org.apache.isis.metamodel.spec.ObjectSpecification;
import org.apache.isis.viewer.wicket.model.models.ScalarModel;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.Model;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.IValidator;
import org.apache.wicket.validation.ValidationError;
import org.apache.wicket.validation.validator.StringValidator;

/**
 * Adapter for {@link ScalarPanelTextFieldAbstract textField-based scalar panel}s where
 * moreover the scalar parameter or property is a value type that is parseable. 
 */
public abstract class ScalarPanelTextFieldParseableAbstract extends
		ScalarPanelTextFieldAbstract<String> {

	private static final long serialVersionUID = 1L;
	private String idTextField;

	public ScalarPanelTextFieldParseableAbstract(String id, String idTextField,
			final ScalarModel scalarModel) {
		super(id, scalarModel);
		this.idTextField = idTextField;
	}

	@Override
	protected TextField<String> createTextField() {
		TextField<String> textField = new TextField<String>(idTextField,
				new Model<String>() {
					private static final long serialVersionUID = 1L;

					@Override
					public String getObject() {
						return getModel().getObjectAsString();
					}

					@Override
					public void setObject(String object) {
						if (object == null) {
							getModel().setObject(null);
						} else {
							getModel().setObjectAsString(object);
						}
					}
				});
		return textField;
	}

	protected void addStandardSemantics() {
		super.addStandardSemantics();

		addMaxLengthValidator();
		addObjectAdapterValidator();
	}

	private void addMaxLengthValidator() {
		ScalarModel scalarModel = getModel();
		TextField<String> textField = getTextField();

		ObjectSpecification facetHolder = scalarModel
				.getTypeOfSpecification();

		MaxLengthFacet maxLengthFacet = facetHolder
				.getFacet(MaxLengthFacet.class);
		if (maxLengthFacet != null) {
			textField
					.add(StringValidator.maximumLength(maxLengthFacet.value()));
		}
	}

	private void addObjectAdapterValidator() {
		final ScalarModel scalarModel = getModel();
		final TextField<String> textField = getTextField();

		textField.add(new IValidator<String>() {
			private static final long serialVersionUID = 1L;

			@Override
			public void validate(IValidatable<String> validatable) {
				String proposedValue = validatable.getValue();
				String reasonIfAny = scalarModel
						.parseAndValidate(proposedValue);
				if (reasonIfAny != null) {
					ValidationError error = new ValidationError();
					error.setMessage(reasonIfAny);
					validatable.error(error);
				}
			}
		});
	}

}
