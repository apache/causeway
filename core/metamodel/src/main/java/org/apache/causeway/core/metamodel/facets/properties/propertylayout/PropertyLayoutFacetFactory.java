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

import jakarta.inject.Inject;

import org.springframework.util.StringUtils;

import org.apache.causeway.applib.annotation.PromptStyle;
import org.apache.causeway.applib.annotation.PropertyLayout;
import org.apache.causeway.core.config.CausewayConfiguration;
import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;
import org.apache.causeway.core.metamodel.facetapi.FeatureType;
import org.apache.causeway.core.metamodel.facets.FacetFactoryAbstract;
import org.apache.causeway.core.metamodel.facets.FacetedMethod;
import org.apache.causeway.core.metamodel.facets.members.layout.group.LayoutGroupFacetFromPropertyLayoutAnnotation;
import org.apache.causeway.core.metamodel.facets.members.layout.order.LayoutOrderFacetFromPropertyLayoutAnnotation;
import org.apache.causeway.core.metamodel.facets.object.navchild.NavigableSubtreeSequenceFacet;
import org.apache.causeway.core.metamodel.facets.object.promptStyle.PromptStyleFacet;
import org.apache.causeway.core.metamodel.specloader.validator.ValidationFailureUtils;

public class PropertyLayoutFacetFactory
extends FacetFactoryAbstract {

    @Inject
    public PropertyLayoutFacetFactory(final MetaModelContext mmc) {
        super(mmc, FeatureType.PROPERTIES_AND_ACTIONS);
    }

    @Override
    public void process(final ProcessMethodContext processMethodContext) {

        var facetHolder = processMethodContext.getFacetHolder();
        var propertyLayoutIfAny = processMethodContext
                .synthesizeOnMethodOrMixinType(
                        PropertyLayout.class,
                        () -> ValidationFailureUtils
                        .raiseAmbiguousMixinAnnotations(processMethodContext.getFacetHolder(), PropertyLayout.class));

        addFacetIfPresent(CssClassFacetForPropertyLayoutAnnotation
            .create(propertyLayoutIfAny, facetHolder));

        addFacetIfPresent(MemberDescribedFacetForPropertyLayoutAnnotation
            .create(propertyLayoutIfAny, facetHolder));

        addFacetIfPresent(HiddenFacetForPropertyLayoutAnnotation
            .create(propertyLayoutIfAny, facetHolder));

        addFacetIfPresent(LabelAtFacetForPropertyLayoutAnnotation
            .create(propertyLayoutIfAny, facetHolder));

        addFacetIfPresent(LayoutGroupFacetFromPropertyLayoutAnnotation
            .create(propertyLayoutIfAny, facetHolder));

        addFacetIfPresent(LayoutOrderFacetFromPropertyLayoutAnnotation
            .create(propertyLayoutIfAny, facetHolder));

        addFacetIfPresent(MultiLineFacetForPropertyLayoutAnnotation
            .create(propertyLayoutIfAny, facetHolder));

        addFacetIfPresent(NamedFacetForPropertyLayoutAnnotation
            .create(propertyLayoutIfAny, facetHolder));

        addFacetIfPresent(
            createPromptStyleFacetForPropertyLayoutAnnotation(propertyLayoutIfAny, getConfiguration(), facetHolder));

        addFacetIfPresent(TypicalLengthFacetForPropertyLayoutAnnotation
            .create(propertyLayoutIfAny, facetHolder));

        addFacetIfPresent(UnchangingFacetForPropertyLayoutAnnotation
            .create(propertyLayoutIfAny, facetHolder));

        addFacetIfPresent(
            propertyLayoutIfAny
                .map(PropertyLayout::navigableSubtree)
                .filter(StringUtils::hasLength)
                .flatMap(sequence->NavigableSubtreeSequenceFacet.create("PropertyLayout annotation",
                    processMethodContext.getCls(), processMethodContext.getMethod().asMethod(), sequence, facetHolder)));
    }

    // -- HELPER

    static Optional<PromptStyleFacet> createPromptStyleFacetForPropertyLayoutAnnotation(
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
                        new PromptStyleFacet("PropertyLayoutAnnotation", promptStyle, holder);
                    case INLINE_AS_IF_EDIT->
                        new PromptStyleFacet("PropertyLayoutAnnotation", PromptStyle.INLINE, holder);
                    case AS_CONFIGURED->
                        holder.containsNonFallbackFacet(PromptStyleFacet.class)
                            ? null // do not replace
                            : PromptStyleFacet.asConfgured(configuration, holder);
                    case NOT_SPECIFIED -> null; // unexpected code reach
                })
                .orElseGet(() ->
                    holder.containsNonFallbackFacet(PromptStyleFacet.class)
                        ? null // do not replace
                        : PromptStyleFacet.asConfgured(configuration, holder))
        );
    }

}
