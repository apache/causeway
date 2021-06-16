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

package org.apache.isis.core.metamodel.facets.object.bookmarkpolicy.bookmarkable;

import java.util.function.Predicate;

import javax.inject.Inject;

import org.apache.isis.applib.annotation.BookmarkPolicy;
import org.apache.isis.core.metamodel.context.MetaModelContext;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facetapi.MetaModelRefiner;
import org.apache.isis.core.metamodel.facets.FacetFactoryAbstract;
import org.apache.isis.core.metamodel.facets.actions.semantics.ActionSemanticsFacet;
import org.apache.isis.core.metamodel.facets.object.bookmarkpolicy.BookmarkPolicyFacet;
import org.apache.isis.core.metamodel.facets.object.bookmarkpolicy.BookmarkPolicyFacetFallback;
import org.apache.isis.core.metamodel.progmodel.ProgrammingModel;
import org.apache.isis.core.metamodel.spec.feature.MixedIn;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.specloader.validator.ValidationFailure;

import lombok.val;

public class BookmarkPolicyFacetFallbackFactory
extends FacetFactoryAbstract
implements MetaModelRefiner {

    @Inject
    public BookmarkPolicyFacetFallbackFactory(final MetaModelContext mmc) {
        super(mmc, FeatureType.OBJECTS_AND_ACTIONS);
    }

    @Override
    public void process(final ProcessClassContext processClassContext) {
        addFacet(new BookmarkPolicyFacetFallback(processClassContext.getFacetHolder()));
    }

    @Override
    public void process(final ProcessMethodContext processMethodContext) {
        addFacet(new BookmarkPolicyFacetFallback(processMethodContext.getFacetHolder()));
    }

    /**
     * Violation if there is an action that is bookmarkable but does not have safe action semantics.
     */
    @Override
    public void refineProgrammingModel(final ProgrammingModel programmingModel) {

        programmingModel.addVisitingValidatorSkipManagedBeans(objectSpec -> {

            // as an optimization only checking 'declared' members (skipping inherited ones)
            // otherwise inherited would be checked more than once
            objectSpec.streamDeclaredActions(MixedIn.EXCLUDED)
            .filter(isBookmarkable())
            .forEach(objectAction->{
                val actionSemanticsFacet = objectAction.getFacet(ActionSemanticsFacet.class);
                if(actionSemanticsFacet == null
                        || actionSemanticsFacet.getPrecedence().isFallback()
                        || !actionSemanticsFacet.value().isSafeInNature()) {
                    ValidationFailure.raiseFormatted(
                            objectAction,
                            "%s: action is bookmarkable but action semantics are not explicitly "
                            + "indicated as being safe. "
                            + "Either add @Action(semantics=SemanticsOf.SAFE) "
                            + "or @Action(semantics=SemanticsOf.SAFE_AND_REQUEST_CACHEABLE), "
                            + "or remove @ActionLayout(bookmarking=...).",
                            objectAction.getFeatureIdentifier().toString());
                }
            });

        });
    }

    private static Predicate<ObjectAction> isBookmarkable() {
        return objectAction->{
            val bookmarkPolicyFacet = objectAction.getFacet(BookmarkPolicyFacet.class);
            if(bookmarkPolicyFacet == null
                    || bookmarkPolicyFacet.getPrecedence().isFallback()
                    || bookmarkPolicyFacet.value() == BookmarkPolicy.NEVER) {
                return false;
            }
            return true;
        };
    }


}
