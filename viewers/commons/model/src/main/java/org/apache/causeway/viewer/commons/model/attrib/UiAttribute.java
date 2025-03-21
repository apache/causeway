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
package org.apache.causeway.viewer.commons.model.attrib;

import java.util.Optional;

import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.internal.base._NullSafe;
import org.apache.causeway.core.metamodel.context.HasMetaModelContext;
import org.apache.causeway.core.metamodel.facetapi.FeatureType;
import org.apache.causeway.core.metamodel.interactions.managed.InteractionVeto;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.ObjectFeature;
import org.apache.causeway.core.metamodel.util.Facets;
import org.apache.causeway.viewer.commons.model.UiModel;

/**
 * We refer to both method parameters and instance fields collectively
 * as "attributes" of a class or method.
 * <p>
 * The field case (aka property) is always singular.
 * {@link UiAttribute#isSingular}
 * <p>
 * The parameter case is either singular or plural.
 * {@link UiAttribute#isSingular} or {@link UiAttribute#isPlural}
 */
public interface UiAttribute extends UiModel, HasMetaModelContext {

    ObjectFeature getMetaModel();

    /** action's or property's owner */
    ManagedObject getOwner();

    String getIdentifier();
    String getCssClass();
    boolean whetherHidden();

    /**
     * Optionally the veto that prevents from editing.
     * Could be an explicit reason, or just an inferred veto originating from read-only semantics.
     */
    Optional<InteractionVeto> disabledReason();

    /** feature name */
    default String getFriendlyName() {
        return getMetaModel().getFriendlyName(this::getOwner);
    }

    default boolean isSingular() {
        return getMetaModel().getFeatureType() == FeatureType.ACTION_PARAMETER_SINGULAR
                || getMetaModel().getFeatureType() == FeatureType.PROPERTY;
    }

    default boolean isPlural() {
        return getMetaModel().getFeatureType() == FeatureType.ACTION_PARAMETER_PLURAL
                || getMetaModel().getFeatureType() == FeatureType.COLLECTION;
    }

    default boolean isProperty() {
        return getMetaModel().getFeatureType().isProperty();
    }

    default boolean isParameter() {
        return getMetaModel().getFeatureType().isActionParameter();
    }

    default Optional<String> getDescribedAs() {
        return getMetaModel().getDescription(this::getOwner);
    }

    default String getFileAccept() {
        return Facets.fileAccept(getMetaModel()).orElse(null);
    }

    int getAutoCompleteMinLength();

    default boolean isRequired() {
        return !getMetaModel().isOptional();
    }

    default ObjectSpecification getElementType() {
        return getMetaModel().getElementType();
    }

    default boolean isElementTypeAnyOf(final Can<Class<?>> requiredClasses) {
        final String fullName = getElementType().getFullIdentifier();
        return requiredClasses.stream()
                .map(Class::getName)
                .anyMatch(fullName::equals);
    }

    default boolean isElementTypeSubtypeOf(final Class<?> requiredClass) {
        final Class<?> elementType = getElementType().getCorrespondingClass();
        return _NullSafe.streamNullable(requiredClass)
                .anyMatch(x -> x.isAssignableFrom(elementType));
    }

    ManagedObject getDefault();

    boolean hasChoices();
    boolean hasAutoComplete();
    default boolean hasObjectAutoComplete() {
        return Facets.autoCompleteIsPresent(getElementType());
    }

    Can<ManagedObject> getChoices();
    Can<ManagedObject> getAutoComplete(final String searchArg);

    default ChoiceProviderSort getChoiceProviderSort() {
        return ChoiceProviderSort.valueOf(this);
    }

    public static enum ChoiceProviderSort {
        NO_CHOICES,
        CHOICES,
        AUTO_COMPLETE,
        OBJECT_AUTO_COMPLETE;
        public static ChoiceProviderSort valueOf(final UiAttribute uiAttribute) {
            if (uiAttribute.hasChoices()) {
                return ChoiceProviderSort.CHOICES;
            } else if(uiAttribute.hasAutoComplete()) {
                return ChoiceProviderSort.AUTO_COMPLETE;
            } else if(uiAttribute.hasObjectAutoComplete()) {
                return ChoiceProviderSort.OBJECT_AUTO_COMPLETE;
            }
            return NO_CHOICES;
        }
        public boolean isNoChoices() { return this == NO_CHOICES; }
        public boolean isChoicesAny() { return !isNoChoices(); }
    }

}
