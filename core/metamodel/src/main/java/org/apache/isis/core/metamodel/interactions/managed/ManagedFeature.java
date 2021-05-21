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
package org.apache.isis.core.metamodel.interactions.managed;

import java.util.Optional;

import javax.annotation.Nullable;

import org.apache.isis.applib.Identifier;
import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;

public interface ManagedFeature {

    Identifier getIdentifier();

    /**
     * @return The feature's display name, as rendered with the UI.
     */
    String getDisplayLabel();

    /**
     * @return The specification of the feature's underlying type.
     * For actions this is the specification of the action's return type.
     */
    ObjectSpecification getSpecification();

    /**
     * @return The feature's underlying type.
     * For actions this is the action's return type.
     */
    default Class<?> getCorrespondingClass() {
        return getSpecification().getCorrespondingClass();
    }

    FacetHolder getMetaModel();

    /**
     * @param facetType
     * @return Optionally the feature's facet of the specified {@code facetType}
     * (as per the type it reports from {@link Facet#facetType()}), based on existence.
     */
    default <T extends Facet> Optional<T> getFacet(@Nullable Class<T> facetType) {
        return facetType!=null
                ? Optional.ofNullable(getMetaModel().getFacet(facetType))
                : Optional.empty();
    }

    /**
     * @param facetType
     * @return Whether there exists a facet for this feature, that is of the
     * specified {@code facetType} (as per the type it reports from {@link Facet#facetType()}).
     */
    default <T extends Facet> boolean hasFacet(@Nullable Class<T> facetType) {
        return facetType!=null
                ? getMetaModel().getFacet(facetType)!=null
                : false;
    }

    default <T extends Facet> T getFacetElseFail(@Nullable Class<T> facetType) {
        return getFacet(facetType)
                .orElseThrow(()->_Exceptions
                        .noSuchElement("Feature %s has no such facet %s",
                                getIdentifier(),
                                facetType.getName()));
    }


}
