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

package org.apache.isis.metamodel.facets.object.bookmarkpolicy.bookmarkable;

import java.util.stream.Stream;

import org.apache.isis.applib.annotation.BookmarkPolicy;
import org.apache.isis.metamodel.facetapi.FacetUtil;
import org.apache.isis.metamodel.facetapi.FeatureType;
import org.apache.isis.metamodel.facetapi.MetaModelValidatorRefiner;
import org.apache.isis.metamodel.facets.FacetFactoryAbstract;
import org.apache.isis.metamodel.facets.actions.semantics.ActionSemanticsFacet;
import org.apache.isis.metamodel.facets.object.bookmarkpolicy.BookmarkPolicyFacet;
import org.apache.isis.metamodel.facets.object.bookmarkpolicy.BookmarkPolicyFacetFallback;
import org.apache.isis.metamodel.spec.ObjectSpecification;
import org.apache.isis.metamodel.spec.feature.Contributed;
import org.apache.isis.metamodel.spec.feature.ObjectAction;
import org.apache.isis.metamodel.specloader.validator.MetaModelValidatorComposite;
import org.apache.isis.metamodel.specloader.validator.MetaModelValidatorVisiting;
import org.apache.isis.metamodel.specloader.validator.ValidationFailures;

public class BookmarkPolicyFacetFallbackFactory extends FacetFactoryAbstract implements  MetaModelValidatorRefiner {

    public BookmarkPolicyFacetFallbackFactory() {
        super(FeatureType.OBJECTS_AND_ACTIONS);
    }

    @Override
    public void process(final ProcessClassContext processClassContext) {
        FacetUtil.addFacet(new BookmarkPolicyFacetFallback(processClassContext.getFacetHolder()));
    }

    @Override
    public void process(final ProcessMethodContext processMethodContext) {
        FacetUtil.addFacet(new BookmarkPolicyFacetFallback(processMethodContext.getFacetHolder()));
    }

    /**
     * Violation if there is an action that is bookmarkable but does not have safe action semantics.
     */
    @Override
    public void refineMetaModelValidator(MetaModelValidatorComposite metaModelValidator) {
        metaModelValidator.add(new MetaModelValidatorVisiting(new MetaModelValidatorVisiting.Visitor() {

            @Override
            public boolean visit(ObjectSpecification objectSpec, ValidationFailures validationFailures) {

                final Stream<ObjectAction> objectActions = objectSpec.streamObjectActions(Contributed.EXCLUDED);

                objectActions
                .filter(objectAction->{
                    final BookmarkPolicyFacet bookmarkFacet = objectAction.getFacet(BookmarkPolicyFacet.class);
                    if(bookmarkFacet == null || bookmarkFacet.isNoop() || 
                            bookmarkFacet.value() == BookmarkPolicy.NEVER) {
                        return false;
                    }
                    return true;
                })
                .forEach(objectAction->{
                    final ActionSemanticsFacet semanticsFacet = objectAction.getFacet(ActionSemanticsFacet.class);
                    if(semanticsFacet == null || semanticsFacet.isNoop() || !semanticsFacet.value().isSafeInNature()) {
                        validationFailures.add(
                                "%s: action is bookmarkable but action semantics are not explicitly indicated as being safe.  " +
                                        "Either add @Action(semantics=SemanticsOf.SAFE) or @Action(semantics=SemanticsOf.SAFE_AND_REQUEST_CACHEABLE), or remove @ActionLayout(bookmarking=...).",
                                        objectAction.getIdentifier().toClassAndNameIdentityString());
                    }
                });

                return true;
            }
        }));
    }



}
