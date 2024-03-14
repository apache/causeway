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
package org.apache.causeway.core.metamodel.facets.object.domainservice.annotation;

import org.apache.causeway.applib.id.LogicalType;
import org.apache.causeway.applib.services.scope.ActionContributionFilterService;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;
import org.apache.causeway.core.metamodel.facets.object.domainservice.DomainServiceFacetAbstract;

public class DomainServiceFacetForAnnotation
extends DomainServiceFacetAbstract {

    public DomainServiceFacetForAnnotation(
            final LogicalType logicalType,
            final Can<ActionContributionFilterService> filterServices,
            final FacetHolder holder) {
        super(evaluateIsContributingToUi(logicalType, filterServices),
                evaluateIsContributingToWebApi(logicalType, filterServices),
                holder);
    }

    // -- HELPER

    private static boolean evaluateIsContributingToUi(
            final LogicalType logicalType,
            final Can<ActionContributionFilterService> filterServices) {
        var veto = filterServices.stream()
            .anyMatch(spi->!spi.isContributingToUi(logicalType));
        return !veto;
    }

    private static boolean evaluateIsContributingToWebApi(
            final LogicalType logicalType,
            final Can<ActionContributionFilterService> filterServices) {
        var veto = filterServices.stream()
            .anyMatch(spi->!spi.isContributingToWebApi(logicalType));
        return !veto;
    }

}
