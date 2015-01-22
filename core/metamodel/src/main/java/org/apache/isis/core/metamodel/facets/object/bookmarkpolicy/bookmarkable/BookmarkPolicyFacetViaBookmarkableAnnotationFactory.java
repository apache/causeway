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

import java.util.List;

import org.apache.isis.applib.annotation.ActionSemantics.Of;
import org.apache.isis.applib.annotation.BookmarkPolicy;
import org.apache.isis.applib.annotation.Bookmarkable;
import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facetapi.FacetUtil;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facetapi.MetaModelValidatorRefiner;
import org.apache.isis.core.metamodel.facets.Annotations;
import org.apache.isis.core.metamodel.facets.FacetFactoryAbstract;
import org.apache.isis.core.metamodel.facets.actions.semantics.ActionSemanticsFacet;
import org.apache.isis.core.metamodel.facets.object.bookmarkpolicy.BookmarkPolicyFacet;
import org.apache.isis.core.metamodel.facets.object.bookmarkpolicy.BookmarkPolicyFacetFallback;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.Contributed;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.specloader.validator.MetaModelValidatorComposite;
import org.apache.isis.core.metamodel.specloader.validator.MetaModelValidatorVisiting;
import org.apache.isis.core.metamodel.specloader.validator.ValidationFailures;

public class BookmarkPolicyFacetViaBookmarkableAnnotationFactory extends FacetFactoryAbstract implements MetaModelValidatorRefiner {

    public BookmarkPolicyFacetViaBookmarkableAnnotationFactory() {
        super(FeatureType.OBJECTS_AND_ACTIONS);
    }

    @Override
    public void process(final ProcessClassContext processClassContext) {
        final Bookmarkable annotation = Annotations.getAnnotation(processClassContext.getCls(), Bookmarkable.class);
        FacetUtil.addFacet(create(annotation, processClassContext.getFacetHolder()));
    }

    @Override
    public void process(final ProcessMethodContext processMethodContext) {
        final Bookmarkable annotation = Annotations.getAnnotation(processMethodContext.getMethod(), Bookmarkable.class);
        FacetUtil.addFacet(create(annotation, processMethodContext.getFacetHolder()));
    }

    private BookmarkPolicyFacet create(final Bookmarkable annotation, final FacetHolder holder) {
        return annotation == null ? new BookmarkPolicyFacetFallback(holder) : new BookmarkPolicyFacetViaBookmarkableAnnotation(annotation.value(), holder);
    }


    /**
     * Violation if there is an action that is bookmarkable but does not have safe action semantics.
     */
    @Override
    public void refineMetaModelValidator(MetaModelValidatorComposite metaModelValidator, IsisConfiguration configuration) {
        metaModelValidator.add(new MetaModelValidatorVisiting(new MetaModelValidatorVisiting.Visitor() {

            @Override
            public boolean visit(ObjectSpecification objectSpec, ValidationFailures validationFailures) {
                final Class<?> cls = objectSpec.getCorrespondingClass();
                
                final List<ObjectAction> objectActions = objectSpec.getObjectActions(Contributed.EXCLUDED);
                for (final ObjectAction objectAction : objectActions) {
                    final BookmarkPolicyFacet bookmarkFacet = objectAction.getFacet(BookmarkPolicyFacet.class);
                    if(bookmarkFacet == null || bookmarkFacet.isNoop() || bookmarkFacet.value() == BookmarkPolicy.NEVER) {
                        continue;
                    }
                    final ActionSemanticsFacet semanticsFacet = objectAction.getFacet(ActionSemanticsFacet.class);
                    if(semanticsFacet == null || semanticsFacet.isNoop() || semanticsFacet.value() != Of.SAFE) {
                      validationFailures.add(
                          "Action %s is bookmarkable but action semantics are not explicitly indicated as being safe.  " +
                          "Either add @Action(semantics=SemanticsOf.SAFE), or remove @ActionLayout(bookmarking=...).",
                      objectAction.getIdentifier().toClassAndNameIdentityString());
                    }
                }
                return true;
            }
        }));
    }

}
