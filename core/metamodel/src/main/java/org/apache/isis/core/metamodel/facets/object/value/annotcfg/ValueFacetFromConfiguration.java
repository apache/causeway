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
package org.apache.isis.core.metamodel.facets.object.value.annotcfg;

import java.util.Optional;

import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.object.value.ValueFacetAbstract;
import org.apache.isis.core.metamodel.facets.object.value.vsp.ValueSemanticsProviderUtil;

import lombok.val;

public class ValueFacetFromConfiguration
extends ValueFacetAbstract {

    // -- FACTORY

    public static Optional<ValueFacetFromConfiguration> create(
            final String candidateSemanticsProviderName,
            final FacetHolder holder) {

        val valueFacet = new ValueFacetFromConfiguration(candidateSemanticsProviderName, holder);

        return valueFacet.hasSemanticsProvider()
                ? Optional.of(valueFacet)
                : Optional.empty();
    }

    // -- CONSTRUCTOR

    private ValueFacetFromConfiguration(
            final String candidateSemanticsProviderName,
            final FacetHolder holder) {

        super(
                ValueSemanticsProviderUtil.valueSemanticsProviderOrNull(null, candidateSemanticsProviderName),
                AddFacetsIfInvalidStrategy.DONT_ADD,
                holder,
                Precedence.LOW);
    }

}
