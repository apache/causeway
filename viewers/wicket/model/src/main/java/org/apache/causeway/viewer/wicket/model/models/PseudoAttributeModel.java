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
package org.apache.causeway.viewer.wicket.model.models;

import java.util.Optional;
import java.util.function.Supplier;

import org.jspecify.annotations.NonNull;

import org.apache.causeway.commons.binding.Bindable;
import org.apache.causeway.commons.binding.Observable;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.internal.binding._Bindables;
import org.apache.causeway.commons.internal.binding._Observables;
import org.apache.causeway.core.metamodel.commons.ViewOrEditMode;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;
import org.apache.causeway.core.metamodel.facetapi.FeatureType;
import org.apache.causeway.core.metamodel.interactions.managed.InteractionVeto;
import org.apache.causeway.core.metamodel.interactions.managed.ManagedValue;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;
import org.apache.causeway.core.metamodel.spec.feature.ObjectFeature;
import org.apache.causeway.viewer.commons.model.hints.RenderingHint;

/**
 * Wraps a {@link ValueModel}. Used for rendering standalone values.
 */
class PseudoAttributeModel extends UiAttributeWkt {

	private static final long serialVersionUID = 1L;

	final ValueModel valueModel;

	//TODO don't serialize the value object twice
	protected PseudoAttributeModel(final ValueModel valueModel) {
		super(UiObjectWkt.ofAdapter(valueModel.getObject()), ViewOrEditMode.VIEWING, RenderingHint.STANDALONE_PROPERTY_COLUMN);
		this.valueModel = valueModel;
	}

	record PseudoFeature(ValueModel valueModel) implements ObjectFeature {
		@Override public FeatureType getFeatureType() {
			return FeatureType.OBJECT;
		}
		@Override public FacetHolder getFacetHolder() {
			return valueModel.elementType();
		}
		@Override public String getId() {
			return "$value";
		}
		@Override public String getFriendlyName(final Supplier<ManagedObject> domainObjectProvider) {
			return "value";
		}
		@Override public Optional<String> getStaticFriendlyName() {
			return Optional.of("value");
		}
		@Override public String getCanonicalFriendlyName() {
			return "value";
		}
		@Override public Optional<String> getDescription(final Supplier<ManagedObject> domainObjectProvider) {
			return Optional.empty();
		}
		@Override public Optional<String> getStaticDescription() {
			return Optional.empty();
		}
		@Override public Optional<String> getCanonicalDescription() {
			return Optional.empty();
		}
		@Override public ObjectSpecification getElementType() {
			return valueModel.elementType();
		}
		@Override public String asciiId() {
			return "$value";
		}
	}

	record PseudoManagedValue(ValueModel valueModel) implements ManagedValue {

		@Override public ObjectSpecification getElementType() {
			return valueModel.elementType();
		}
		@Override public Bindable<ManagedObject> getValue() {
			return _Bindables.forValue(valueModel.getObject());
		}
		@Override public Observable<String> getValueAsTitle() {
			// TODO Auto-generated method stub
			return null;
		}
		@Override public Observable<String> getValueAsHtml() {
			// TODO Auto-generated method stub
			return null;
		}
		@Override public boolean isValueAsParsableTextSupported() {
			return false;
		}
		@Override
		public Bindable<String> getValueAsParsableText() {
			// TODO Auto-generated method stub
			return null;
		}
		@Override public Observable<String> getValidationMessage() {
			// TODO Auto-generated method stub
			return null;
		}
		@Override public Bindable<String> getSearchArgument() {
			// TODO Auto-generated method stub
			return null;
		}
		@Override public Observable<Can<ManagedObject>> getChoices() {
			return _Observables.lazy(Can::empty);
		}
	}

	@Override
	public ObjectFeature getMetaModel() {
		return new PseudoFeature(valueModel);
	}

	@Override
	public ManagedObject getOwner() {
		return valueModel.getObject();
	}

	@Override
	public String getIdentifier() {
		return "$value";
	}

	@Override
	public ManagedValue proposedValue() {
		return new PseudoManagedValue(valueModel);
	}

	@Override
	protected String toStringOf() {
		return "%s[%s]".formatted(getClass().getName(), getIdentifier());
	}

	@Override public boolean isSingular() { return true; }
	@Override public boolean isPlural() { return false; }
	@Override public boolean isProperty() { return false; }
	@Override public boolean isParameter() { return false; }

	@Override public String getCssClass() { return null; }
	@Override public boolean whetherHidden() { return false; }
	@Override public Optional<InteractionVeto> disabledReason() { return Optional.empty(); }
	@Override public int getAutoCompleteMinLength() { return 0; }
	@Override public ManagedObject getDefault() { return valueModel.getObject(); }
	@Override public boolean hasChoices() {  return false; }
	@Override public boolean hasAutoComplete() { return false; }
	@Override public Can<ManagedObject> getChoices() { return Can.empty(); }
	@Override public Can<ManagedObject> getAutoComplete(final String searchArg) { return Can.empty(); }
	@Override public String validate(@NonNull final ManagedObject proposedAdapter) {  return null; }
	@Override protected Can<ObjectAction> calcAssociatedActions() { return Can.empty(); }

}
