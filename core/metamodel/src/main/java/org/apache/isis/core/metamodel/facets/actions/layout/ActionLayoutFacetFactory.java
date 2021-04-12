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

import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facetapi.MetaModelRefiner;
import org.apache.isis.core.metamodel.facets.FacetFactoryAbstract;
import org.apache.isis.core.metamodel.facets.actions.position.ActionPositionFacet;
import org.apache.isis.core.metamodel.facets.actions.position.ActionPositionFacetFallback;
import org.apache.isis.core.metamodel.facets.actions.redirect.RedirectFacet;
import org.apache.isis.core.metamodel.facets.actions.redirect.RedirectFacetFallback;
import org.apache.isis.core.metamodel.facets.all.describedas.DescribedAsFacet;
import org.apache.isis.core.metamodel.facets.all.hide.HiddenFacet;
import org.apache.isis.core.metamodel.facets.all.named.NamedFacet;
import org.apache.isis.core.metamodel.facets.members.cssclass.CssClassFacet;
import org.apache.isis.core.metamodel.facets.members.cssclassfa.CssClassFaFacet;
import org.apache.isis.core.metamodel.facets.members.layout.group.LayoutGroupFacet;
import org.apache.isis.core.metamodel.facets.members.layout.group.LayoutGroupFacetFromActionLayoutAnnotation;
import org.apache.isis.core.metamodel.facets.members.layout.order.LayoutOrderFacet;
import org.apache.isis.core.metamodel.facets.members.layout.order.LayoutOrderFacetFromActionLayoutAnnotation;
import org.apache.isis.core.metamodel.facets.object.bookmarkpolicy.BookmarkPolicyFacet;
import org.apache.isis.core.metamodel.facets.object.promptStyle.PromptStyleFacet;
import org.apache.isis.core.metamodel.progmodel.ProgrammingModel;
import org.apache.isis.core.metamodel.specloader.validator.MetaModelValidatorForAmbiguousMixinAnnotations;

import lombok.val;

public class ActionLayoutFacetFactory 
extends FacetFactoryAbstract
implements MetaModelRefiner {
    
    private final MetaModelValidatorForAmbiguousMixinAnnotations ambiguousMixinAnnotationsValidator
            = new MetaModelValidatorForAmbiguousMixinAnnotations();

    public ActionLayoutFacetFactory() {
        super(FeatureType.ACTIONS_ONLY);
    }

    @Override
    public void process(final ProcessMethodContext processMethodContext) {

        val facetHolder = processMethodContext.getFacetHolder();
        val actionLayoutIfAny = processMethodContext
                .synthesizeOnMethodOrMixinType(
                        ActionLayout.class,
                        () -> ambiguousMixinAnnotationsValidator
                        .addValidationFailure(processMethodContext.getFacetHolder(), ActionLayout.class));
        
        // bookmarkable
        BookmarkPolicyFacet bookmarkableFacet = BookmarkPolicyFacetForActionLayoutAnnotation
                .create(actionLayoutIfAny, facetHolder);
        super.addFacet(bookmarkableFacet);

        // cssClass
        CssClassFacet cssClassFacet = CssClassFacetForActionLayoutAnnotation.create(actionLayoutIfAny, facetHolder);
        super.addFacet(cssClassFacet);

        // cssClassFa
        CssClassFaFacet cssClassFaFacet = CssClassFaFacetForActionLayoutAnnotation.create(actionLayoutIfAny, facetHolder);
        super.addFacet(cssClassFaFacet);

        // describedAs
        DescribedAsFacet describedAsFacet = DescribedAsFacetForActionLayoutAnnotation.create(actionLayoutIfAny, facetHolder);
        super.addFacet(describedAsFacet);

        // hidden
        HiddenFacet hiddenFacet = HiddenFacetForActionLayoutAnnotation.create(actionLayoutIfAny, facetHolder);
        super.addFacet(hiddenFacet);
        
        // layoutGroup
        LayoutGroupFacet layoutGroupFacet = LayoutGroupFacetFromActionLayoutAnnotation.create(actionLayoutIfAny, facetHolder);
        super.addFacet(layoutGroupFacet);

        // named
        NamedFacet namedFacet = NamedFacetForActionLayoutAnnotation.create(actionLayoutIfAny, facetHolder);
        super.addFacet(namedFacet);

        // promptStyle
        PromptStyleFacet promptStyleFacet = PromptStyleFacetForActionLayoutAnnotation
                .create(actionLayoutIfAny, getConfiguration(), facetHolder);

        super.addFacet(promptStyleFacet);

        // position
        ActionPositionFacet actionPositionFacet = ActionPositionFacetForActionLayoutAnnotation
                .create(actionLayoutIfAny, facetHolder);
        if(actionPositionFacet == null) {
            actionPositionFacet = new ActionPositionFacetFallback(facetHolder);
        }
        super.addFacet(actionPositionFacet);

        // redirectPolicy
        RedirectFacet redirectFacet = RedirectFacetFromActionLayoutAnnotation.create(actionLayoutIfAny, facetHolder);
        if(redirectFacet == null) {
            redirectFacet = new RedirectFacetFallback(facetHolder);
        }
        super.addFacet(redirectFacet);
        
        // sequence (layout)
        LayoutOrderFacet layoutOrderFacet = LayoutOrderFacetFromActionLayoutAnnotation.create(actionLayoutIfAny, facetHolder); 
        super.addFacet(layoutOrderFacet);

    }

    // -- METAMODEL REFINER

    @Override
    public void refineProgrammingModel(ProgrammingModel programmingModel) {
        programmingModel.addValidator(ambiguousMixinAnnotationsValidator);
    }

}
