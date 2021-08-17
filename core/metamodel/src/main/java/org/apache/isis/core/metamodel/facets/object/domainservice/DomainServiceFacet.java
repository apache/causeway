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
package org.apache.isis.core.metamodel.facets.object.domainservice;

import java.util.Optional;

import org.springframework.lang.Nullable;

import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.commons.internal.base._NullSafe;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;

/**
 * Corresponds to annotating the class with the {@link org.apache.isis.applib.annotation.DomainService} annotation.
 */
public interface DomainServiceFacet extends Facet {

    /**
     * Corresponds to {@link org.apache.isis.applib.annotation.DomainService#nature()}.
     *
     */
    public NatureOfService getNatureOfService();

    // -- UTILITY

    public static Optional<NatureOfService> getNatureOfService(final @Nullable FacetHolder facetHolderIfAny) {
        return Optional.ofNullable(facetHolderIfAny)
        .map(facetHolder->facetHolder.getFacet(DomainServiceFacet.class))
        .filter(_NullSafe::isPresent)
        .map(DomainServiceFacet::getNatureOfService);
    }

    /**
     * @param facetHolderIfAny - null-able
     * @return whether facetHolder represents a service that contributes actions to at least REST
     * (and potentially the UI)
     */
    public static boolean isContributing(final @Nullable FacetHolder facetHolderIfAny) {
        return getNatureOfService(facetHolderIfAny)
                .isPresent();
    }

    /**
     * @param facetHolderIfAny - null-able
     * @return whether facetHolder represents a service that contributes actions to the UI (and REST implied)
     */
    public static boolean isContributingToUiAndRest(final @Nullable FacetHolder facetHolderIfAny) {
        return getNatureOfService(facetHolderIfAny)
                .filter(NatureOfService::isView)
                .isPresent();
    }

    /**
     * @param facetHolderIfAny - null-able
     * @return whether facetHolder represents a service that contributes actions only to REST
     */
    public static boolean isContributingOnlyToRest(final @Nullable FacetHolder facetHolderIfAny) {
        return getNatureOfService(facetHolderIfAny)
                .filter(NatureOfService::isRestOnly)
                .isPresent();
    }

}