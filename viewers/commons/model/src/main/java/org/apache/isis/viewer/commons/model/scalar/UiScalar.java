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
package org.apache.isis.viewer.commons.model.scalar;

import java.util.Optional;

import org.apache.isis.commons.collections.Can;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.object.ManagedObject;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectFeature;
import org.apache.isis.core.metamodel.util.Facets;
import org.apache.isis.viewer.commons.model.UiModel;

public interface UiScalar extends UiModel {

    ObjectFeature getMetaModel();

    /** action's or property's owner */
    ManagedObject getOwner();

    /** feature name */
    default String getFriendlyName() {
        return getMetaModel().getFriendlyName(this::getOwner);
    }

    default boolean isCollection() {
        return getMetaModel().getFeatureType() == FeatureType.ACTION_PARAMETER_COLLECTION
                || getMetaModel().getFeatureType() == FeatureType.COLLECTION;
    }

    default boolean isScalar() {
        return !isCollection();
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

    default ObjectSpecification getScalarTypeSpec() {
        return getMetaModel().getElementType();
    }

    ManagedObject getDefault();

    boolean hasChoices();
    boolean hasAutoComplete();
    default boolean hasObjectAutoComplete() {
        return Facets.autoCompleteIsPresent(getScalarTypeSpec());
    }

    Can<ManagedObject> getChoices();
    Can<ManagedObject> getAutoComplete(final String searchArg);

    default ChoiceProviderSort getChoiceProviderSort() {
        return ChoiceProviderSort.valueOf(this);
    }

    static enum ChoiceProviderSort {
        NO_CHOICES,
        CHOICES,
        AUTO_COMPLETE,
        OBJECT_AUTO_COMPLETE;
        static ChoiceProviderSort valueOf(final UiScalar scalarModel) {
            if (scalarModel.hasChoices()) {
                return ChoiceProviderSort.CHOICES;
            } else if(scalarModel.hasAutoComplete()) {
                return ChoiceProviderSort.AUTO_COMPLETE;
            } else if(scalarModel.hasObjectAutoComplete()) {
                return ChoiceProviderSort.OBJECT_AUTO_COMPLETE;
            }
            return NO_CHOICES;
        }
        public boolean isNoChoices() { return this == NO_CHOICES; }
        public boolean isChoicesAny() { return !isNoChoices(); }
    }

}
