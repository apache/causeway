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

package org.apache.isis.core.metamodel.facets.actions.homepage.annotation;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.apache.isis.applib.Identifier;
import org.apache.isis.applib.annotation.HomePage;
import org.apache.isis.commons.internal.collections._Maps;
import org.apache.isis.core.metamodel.context.MetaModelContext;
import org.apache.isis.core.metamodel.facetapi.FacetUtil;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facetapi.MetaModelRefiner;
import org.apache.isis.core.metamodel.facets.FacetFactoryAbstract;
import org.apache.isis.core.metamodel.facets.FacetedMethod;
import org.apache.isis.core.metamodel.facets.actions.homepage.HomePageFacet;
import org.apache.isis.core.metamodel.progmodel.ProgrammingModel;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.MixedIn;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.specloader.validator.MetaModelValidator;
import org.apache.isis.core.metamodel.specloader.validator.MetaModelVisitingValidatorAbstract;
import org.apache.isis.core.metamodel.specloader.validator.ValidationFailure;

import static org.apache.isis.commons.internal.functions._Predicates.not;

import lombok.NonNull;
import lombok.val;

public class HomePageFacetAnnotationFactory
extends FacetFactoryAbstract
implements MetaModelRefiner {

    @Inject
    public HomePageFacetAnnotationFactory(final MetaModelContext mmc) {
        super(mmc, FeatureType.ACTIONS_ONLY);
    }

    @Override
    public void process(final ProcessMethodContext processMethodContext) {
        final HomePage homepageAnnot = processMethodContext.synthesizeOnMethod(HomePage.class)
                .orElse(null);

//        _Assert.assertEquals("expected same", homepageAnnot,
//                Annotations.getAnnotation(processMethodContext.getMethod(), HomePage.class));

        if (homepageAnnot == null) {
            return;
        }
        final FacetedMethod facetHolder = processMethodContext.getFacetHolder();
        FacetUtil.addFacet(new HomePageFacetAnnotation(facetHolder));
    }

    @Override
    public void refineProgrammingModel(final ProgrammingModel programmingModel) {
        programmingModel.addValidator(newValidatorVisitor(programmingModel.getMetaModelContext()));
    }

    private MetaModelValidator newValidatorVisitor(final MetaModelContext mmc) {
        return new MetaModelVisitingValidatorAbstract(mmc) {

            private final Map<String, ObjectAction> actionsHavingHomePageFacet = _Maps.newHashMap();

            @Override
            public void validate(final @NonNull ObjectSpecification spec) {
                if(spec.isManagedBean()) {
                    return;
                }

                // as an optimization only checking declared members (skipping inherited ones)
                spec.streamDeclaredActions(MixedIn.EXCLUDED)
                .filter(objectAction->objectAction.containsFacet(HomePageFacet.class))
                .forEach(objectAction->{

                    actionsHavingHomePageFacet.put(objectAction.getId(), objectAction);

                    // TODO: it would be good to flag if the facet is found on any non-services, however
                    // ObjectSpecification.isService(...) can only be trusted once a PersistenceSession
                    // exists.
                    // this ought to be improved upon at some point...

                    // TODO might collide with type level annotations as well

                });
            }

            @Override
            public void summarize() {
                if(actionsHavingHomePageFacet.size()>1) {

                    final Set<String> homepageActionIdSet = actionsHavingHomePageFacet.values().stream()
                            .map(ObjectAction::getFeatureIdentifier)
                            .map(Identifier::getFullIdentityString)
                            .collect(Collectors.toCollection(HashSet::new));

                    for (val objectAction : actionsHavingHomePageFacet.values()) {
                        val actionId = objectAction.getFeatureIdentifier().getFullIdentityString();
                        val colission = homepageActionIdSet.stream()
                                .filter(not(actionId::equals))
                                .collect(Collectors.joining(", "));

                        ValidationFailure.raise(
                                objectAction,
                                String.format(
                                        "%s: other actions also specified as home page: %s ",
                                        actionId,
                                        colission));

                    }
                }
            }

        };

    }
}
