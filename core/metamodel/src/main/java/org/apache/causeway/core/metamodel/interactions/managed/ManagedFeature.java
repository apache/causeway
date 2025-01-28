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
package org.apache.causeway.core.metamodel.interactions.managed;

import java.util.Optional;

import org.jspecify.annotations.Nullable;

import org.apache.causeway.applib.Identifier;
import org.apache.causeway.commons.internal.exceptions._Exceptions;
import org.apache.causeway.core.metamodel.facetapi.Facet;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.ObjectFeature;

public interface ManagedFeature {

    Identifier getIdentifier();

    /**
     * @return The feature's translated friendly name, as rendered with the UI.
     */
    String getFriendlyName();

    /**
     * @return Optionally the feature's translated description, as rendered with the UI (eg. tooltips).
     */
    Optional<String> getDescription();

    /**
     * @return The specification of the feature's underlying type.
     * For actions this is the specification of the action's return type.
     */
    ObjectSpecification getElementType();

    /**
     * @return The feature's underlying type.
     * For actions this is the action's return type.
     */
    default Class<?> getElementClass() {
        return getElementType().getCorrespondingClass();
    }

    ObjectFeature getMetaModel();

    /**
     * @param facetType
     * @return Optionally the feature's facet of the specified {@code facetType}
     * (as per the type it reports from {@link Facet#facetType()}), based on existence.
     */
    default <T extends Facet> Optional<T> getFacet(final @Nullable Class<T> facetType) {
        return facetType!=null
                ? Optional.ofNullable(getMetaModel().getFacet(facetType))
                : Optional.empty();
    }

    /**
     * @param facetType
     * @return Whether there exists a facet for this feature, that is of the
     * specified {@code facetType} (as per the type it reports from {@link Facet#facetType()}).
     */
    default <T extends Facet> boolean hasFacet(final @Nullable Class<T> facetType) {
        return facetType!=null
                ? getMetaModel().getFacet(facetType)!=null
                : false;
    }

    default <T extends Facet> T getFacetElseFail(final @Nullable Class<T> facetType) {
        return getFacet(facetType)
                .orElseThrow(()->_Exceptions
                        .noSuchElement("Feature %s has no such facet %s",
                                getIdentifier(),
                                facetType.getName()));
    }

}
