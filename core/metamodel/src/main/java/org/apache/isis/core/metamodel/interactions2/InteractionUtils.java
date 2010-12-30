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


package org.apache.isis.core.metamodel.interactions2;

import org.apache.isis.core.metamodel.consent2.InteractionResult;
import org.apache.isis.core.metamodel.consent2.InteractionResultSet;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetFilters;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;


public final class InteractionUtils {

    private InteractionUtils() {}

    public static InteractionResult isVisibleResult(final FacetHolder facetHolder, final VisibilityContext<?> context) {
        final InteractionResult result = new InteractionResult(context.createInteractionEvent());
        final Facet[] facets = facetHolder.getFacets(FacetFilters.isA(HidingInteractionAdvisor.class));
        for (int i = 0; i < facets.length; i++) {
            final HidingInteractionAdvisor advisor = (HidingInteractionAdvisor) facets[i];
            result.advise(advisor.hides(context), advisor);
        }
        return result;
    }

    public static InteractionResultSet isVisibleResultSet(
            final FacetHolder facetHolder,
            final VisibilityContext<?> context,
            final InteractionResultSet resultSet) {
        return resultSet.add(isVisibleResult(facetHolder, context));
    }

    public static InteractionResult isUsableResult(final FacetHolder facetHolder, final UsabilityContext<?> context) {
        final InteractionResult result = new InteractionResult(context.createInteractionEvent());
        final Facet[] facets = facetHolder.getFacets(FacetFilters.isA(DisablingInteractionAdvisor.class));
        for (int i = 0; i < facets.length; i++) {
            final DisablingInteractionAdvisor advisor = (DisablingInteractionAdvisor) facets[i];
            final String disables = advisor.disables(context);
            result.advise(disables, advisor);
        }
        return result;
    }

    public static InteractionResultSet isUsableResultSet(
            final FacetHolder facetHolder,
            final UsabilityContext<?> context,
            final InteractionResultSet resultSet) {
        return resultSet.add(isUsableResult(facetHolder, context));
    }

    public static InteractionResult isValidResult(final FacetHolder facetHolder, final ValidityContext<?> context) {
        final InteractionResult result = new InteractionResult(context.createInteractionEvent());
        final Facet[] facets = facetHolder.getFacets(FacetFilters.isA(ValidatingInteractionAdvisor.class));
        for (int i = 0; i < facets.length; i++) {
            final ValidatingInteractionAdvisor advisor = (ValidatingInteractionAdvisor) facets[i];
            result.advise(advisor.invalidates(context), advisor);
        }
        return result;
    }

    public static InteractionResultSet isValidResultSet(
            final FacetHolder facetHolder,
            final ValidityContext<?> context,
            final InteractionResultSet resultSet) {
        return resultSet.add(isValidResult(facetHolder, context));
    }

}

