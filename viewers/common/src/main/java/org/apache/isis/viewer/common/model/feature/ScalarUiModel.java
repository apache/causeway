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
package org.apache.isis.viewer.common.model.feature;

import java.math.BigDecimal;
import java.util.Optional;

import org.apache.isis.commons.collections.Can;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facets.objectvalue.digits.MaxTotalDigitsFacet;
import org.apache.isis.core.metamodel.facets.objectvalue.fileaccept.FileAcceptFacet;
import org.apache.isis.core.metamodel.facets.objectvalue.typicallen.TypicalLengthFacet;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.feature.ObjectFeature;

public interface ScalarUiModel {

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

    /**
     * for {@link BigDecimal}s only.
     *
     * @see #getLength()
     */
    default Integer getLength() {
        return getMetaModel().lookupFacet(MaxTotalDigitsFacet.class)
                .map(MaxTotalDigitsFacet::getMaxTotalDigits)
                .orElse(null);
    }

    default Integer getTypicalLength() {
        final TypicalLengthFacet facet = getMetaModel().getFacet(TypicalLengthFacet.class);
        return facet != null? facet.value() : null;
    }

    default String getFileAccept() {
        final FileAcceptFacet facet = getMetaModel().getFacet(FileAcceptFacet.class);
        return facet != null? facet.value(): null;
    }

    int getAutoCompleteMinLength();
    boolean hasChoices();
    boolean hasAutoComplete();
    ManagedObject getDefault();
    Can<ManagedObject> getChoices();
    Can<ManagedObject> getAutoComplete(final String searchArg);

}
