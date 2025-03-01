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
package org.apache.causeway.core.metamodel.facets.actions.layout;

import java.util.Optional;

import jakarta.inject.Inject;

import org.apache.causeway.applib.annotation.ActionLayout;
import org.apache.causeway.applib.annotation.PromptStyle;
import org.apache.causeway.core.config.CausewayConfiguration;
import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;
import org.apache.causeway.core.metamodel.facetapi.FeatureType;
import org.apache.causeway.core.metamodel.facets.FacetFactoryAbstract;
import org.apache.causeway.core.metamodel.facets.actions.position.ActionPositionFacetFallback;
import org.apache.causeway.core.metamodel.facets.actions.redirect.RedirectFacetFallback;
import org.apache.causeway.core.metamodel.facets.members.layout.group.LayoutGroupFacetFromActionLayoutAnnotation;
import org.apache.causeway.core.metamodel.facets.members.layout.order.LayoutOrderFacetFromActionLayoutAnnotation;
import org.apache.causeway.core.metamodel.facets.object.promptStyle.PromptStyleFacet;
import org.apache.causeway.core.metamodel.specloader.validator.ValidationFailureUtils;

public class ActionLayoutFacetFactory
extends FacetFactoryAbstract {

    @Inject
    public ActionLayoutFacetFactory(final MetaModelContext mmc) {
        super(mmc, FeatureType.ACTIONS_ONLY);
    }

    @Override
    public void process(final ProcessMethodContext processMethodContext) {

        var facetHolder = processMethodContext.getFacetHolder();
        var actionLayoutIfAny = processMethodContext
                .synthesizeOnMethodOrMixinType(
                        ActionLayout.class,
                        () -> ValidationFailureUtils
                        .raiseAmbiguousMixinAnnotations(processMethodContext.getFacetHolder(), ActionLayout.class));

        // cssClass
        addFacetIfPresent(
                CssClassFacetForActionLayoutAnnotation
                .create(actionLayoutIfAny, facetHolder));

        // cssClassFa
        addFacetIfPresent(
                FaFacetForActionLayoutAnnotation
                .create(actionLayoutIfAny, facetHolder));

        // describedAs
        addFacetIfPresent(
                MemberDescribedFacetForActionLayoutAnnotation
                .create(actionLayoutIfAny, facetHolder));

        // hidden
        addFacetIfPresent(
                HiddenFacetForActionLayoutAnnotation
                .create(actionLayoutIfAny, facetHolder));

        // layoutGroup (explicit via field set, or implicit via associated collection)
        addFacetIfPresent(
                LayoutGroupFacetFromActionLayoutAnnotation
                .create(actionLayoutIfAny, facetHolder));

        // named
        addFacetIfPresent(
                NamedFacetForActionLayoutAnnotation
                .create(actionLayoutIfAny, facetHolder));

        // promptStyle
        addFacetIfPresent(
            createPromptStyleFacetForActionLayoutAnnotation(actionLayoutIfAny, getConfiguration(), facetHolder));

        // position
        var actionPositionFacet = ActionPositionFacetForActionLayoutAnnotation
                .create(actionLayoutIfAny, facetHolder)
                .orElseGet(()->new ActionPositionFacetFallback(facetHolder));

        addFacet(actionPositionFacet);

        // redirectPolicy
        var redirectFacet = RedirectFacetFromActionLayoutAnnotation
                .create(actionLayoutIfAny, facetHolder)
                .orElseGet(()->new RedirectFacetFallback(facetHolder));
        addFacet(redirectFacet);

        // sequence (layout)
        addFacetIfPresent(
                LayoutOrderFacetFromActionLayoutAnnotation
                .create(actionLayoutIfAny, facetHolder));

    }

    // -- HELPER

    private static Optional<PromptStyleFacet> createPromptStyleFacetForActionLayoutAnnotation(
        final Optional<ActionLayout> actionLayoutIfAny,
        final CausewayConfiguration configuration,
        final FacetHolder holder) {

        return Optional.ofNullable(
            actionLayoutIfAny
                .map(ActionLayout::promptStyle)
                .filter(promptStyle -> promptStyle != PromptStyle.NOT_SPECIFIED)
                .map(promptStyle -> switch (promptStyle) {
                    case DIALOG, DIALOG_MODAL, DIALOG_SIDEBAR, INLINE, INLINE_AS_IF_EDIT->
                        new PromptStyleFacet("ActionLayoutAnnotation", promptStyle, holder);
                    case AS_CONFIGURED->
                        holder.containsNonFallbackFacet(PromptStyleFacet.class)
                            ? null // do not replace
                            : PromptStyleFacet.asConfgured(configuration, holder);
                    case NOT_SPECIFIED -> null; // unexpected code reach
                })
                .orElseGet(() ->
                    // do not replace
                    holder.containsNonFallbackFacet(PromptStyleFacet.class)
                        ? null
                        : PromptStyleFacet.asConfgured(configuration, holder))
        );
    }

}
