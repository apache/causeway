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
package org.apache.isis.core.metamodel.facets.actions.layout;

import javax.inject.Inject;

import org.apache.isis.applib.annotations.ActionLayout;
import org.apache.isis.core.metamodel.context.MetaModelContext;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facets.FacetFactoryAbstract;
import org.apache.isis.core.metamodel.facets.actions.position.ActionPositionFacetFallback;
import org.apache.isis.core.metamodel.facets.actions.redirect.RedirectFacetFallback;
import org.apache.isis.core.metamodel.facets.members.layout.group.LayoutGroupFacetFromActionLayoutAnnotation;
import org.apache.isis.core.metamodel.facets.members.layout.order.LayoutOrderFacetFromActionLayoutAnnotation;
import org.apache.isis.core.metamodel.specloader.validator.MetaModelValidatorForAmbiguousMixinAnnotations;

import lombok.val;

public class ActionLayoutFacetFactory
extends FacetFactoryAbstract {

    @Inject
    public ActionLayoutFacetFactory(final MetaModelContext mmc) {
        super(mmc, FeatureType.ACTIONS_ONLY);
    }

    @Override
    public void process(final ProcessMethodContext processMethodContext) {

        val facetHolder = processMethodContext.getFacetHolder();
        val actionLayoutIfAny = processMethodContext
                .synthesizeOnMethodOrMixinType(
                        ActionLayout.class,
                        () -> MetaModelValidatorForAmbiguousMixinAnnotations
                        .addValidationFailure(processMethodContext.getFacetHolder(), ActionLayout.class));

        // bookmarkable
        addFacetIfPresent(
                BookmarkPolicyFacetForActionLayoutAnnotation
                .create(actionLayoutIfAny, facetHolder));

        // cssClass
        addFacetIfPresent(
                CssClassFacetForActionLayoutAnnotation
                .create(actionLayoutIfAny, facetHolder));

        // cssClassFa
        addFacetIfPresent(
                CssClassFaFacetForActionLayoutAnnotation
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
        addFacetIfPresent(PromptStyleFacetForActionLayoutAnnotation
                .create(actionLayoutIfAny, getConfiguration(), facetHolder));

        // position
        val actionPositionFacet = ActionPositionFacetForActionLayoutAnnotation
                .create(actionLayoutIfAny, facetHolder)
                .orElseGet(()->new ActionPositionFacetFallback(facetHolder));

        addFacet(actionPositionFacet);

        // redirectPolicy
        val redirectFacet = RedirectFacetFromActionLayoutAnnotation
                .create(actionLayoutIfAny, facetHolder)
                .orElseGet(()->new RedirectFacetFallback(facetHolder));
        addFacet(redirectFacet);

        // sequence (layout)
        addFacetIfPresent(
                LayoutOrderFacetFromActionLayoutAnnotation
                .create(actionLayoutIfAny, facetHolder));

    }


}
