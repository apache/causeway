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
package org.apache.causeway.core.metamodel.facets.properties.propertylayout;

import java.util.Optional;

import org.apache.causeway.applib.annotation.PromptStyle;
import org.apache.causeway.applib.annotation.PropertyLayout;
import org.apache.causeway.core.config.CausewayConfiguration;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;
import org.apache.causeway.core.metamodel.facetapi.FeatureType;
import org.apache.causeway.core.metamodel.facets.FacetedMethod;
import org.apache.causeway.core.metamodel.facets.object.promptStyle.PromptStyleFacet;
import org.apache.causeway.core.metamodel.facets.object.promptStyle.PromptStyleFacetAbstract;
import org.apache.causeway.core.metamodel.facets.object.promptStyle.PromptStyleFacetAsConfigured;

public final class PromptStyleFacetForPropertyLayoutAnnotation
extends PromptStyleFacetAbstract {

    public static Optional<PromptStyleFacet> create(
            final Optional<PropertyLayout> propertyLayoutIfAny,
            final CausewayConfiguration configuration,
            final FacetHolder holder) {

        // guard against member not being a property
        if(holder instanceof FacetedMethod facetedMethod
                && facetedMethod.featureType() != FeatureType.PROPERTY) {
            return Optional.empty();
        }

        return Optional.ofNullable(

            propertyLayoutIfAny
                .map(PropertyLayout::promptStyle)
                .filter(promptStyle -> promptStyle != PromptStyle.NOT_SPECIFIED)
                .map(promptStyle -> switch (promptStyle) {
                    case DIALOG, DIALOG_MODAL, DIALOG_SIDEBAR, INLINE->
                        new PromptStyleFacetForPropertyLayoutAnnotation(promptStyle, holder);
                    case INLINE_AS_IF_EDIT->
                        new PromptStyleFacetForPropertyLayoutAnnotation(PromptStyle.INLINE, holder);
                    case AS_CONFIGURED->
                        holder.containsNonFallbackFacet(PromptStyleFacet.class)
                            ? null // do not replace
                            : new PromptStyleFacetAsConfigured(configuration, holder);
                    case NOT_SPECIFIED -> null; // unexpected code reach
                })
                .orElseGet(() ->
                    holder.containsNonFallbackFacet(PromptStyleFacet.class)
                        ? null // do not replace
                        : new PromptStyleFacetAsConfigured(configuration, holder))
        );
    }

    private final PromptStyle promptStyle;

    private PromptStyleFacetForPropertyLayoutAnnotation(final PromptStyle promptStyle, final FacetHolder holder) {
        super( holder );
        this.promptStyle = promptStyle;
    }

    @Override
    public PromptStyle value() {
        return promptStyle;
    }

}
