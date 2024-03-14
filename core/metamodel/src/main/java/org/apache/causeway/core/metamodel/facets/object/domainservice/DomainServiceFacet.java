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
package org.apache.causeway.core.metamodel.facets.object.domainservice;

import java.util.function.Predicate;

import org.apache.causeway.core.metamodel.facetapi.Facet;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;

public interface DomainServiceFacet extends Facet {

    /**
     * Whether facetHolder represents a service that contributes actions to the UI.
     * May or may not also contribute to the Web API(s).
     */
    boolean isContributingToUi();

    /**
     * Whether facetHolder represents a service that contributes actions the Web API(s).
     * May or may not also contribute to the UI.
     */
    boolean isContributingToWebApi();

    // -- PREDICATES

    static Predicate<ObjectSpecification> contributingToUi() {
        return spec-> spec.lookupFacet(DomainServiceFacet.class)
                .map(DomainServiceFacet::isContributingToUi)
                .orElse(false);
    }

    static Predicate<ObjectSpecification> contributingToWebApi() {
        return spec-> spec.lookupFacet(DomainServiceFacet.class)
                .map(DomainServiceFacet::isContributingToWebApi)
                .orElse(false);
    }

}