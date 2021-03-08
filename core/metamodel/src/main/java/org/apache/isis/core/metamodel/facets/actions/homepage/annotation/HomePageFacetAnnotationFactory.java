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

import org.apache.isis.applib.Identifier;
import org.apache.isis.applib.annotation.HomePage;
import org.apache.isis.commons.internal.collections._Maps;
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
import org.apache.isis.core.metamodel.specloader.validator.MetaModelValidatorVisiting;
import org.apache.isis.core.metamodel.specloader.validator.MetaModelValidatorVisiting.Visitor;

import static org.apache.isis.commons.internal.functions._Predicates.not;

import lombok.val;

public class HomePageFacetAnnotationFactory extends FacetFactoryAbstract
implements MetaModelRefiner {

    public HomePageFacetAnnotationFactory() {
        super(FeatureType.ACTIONS_ONLY);
    }

    @Override
    public void process(ProcessMethodContext processMethodContext) {
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
    public void refineProgrammingModel(ProgrammingModel programmingModel) {
        programmingModel.addValidatorSkipManagedBeans(newValidatorVisitor());
    }

    private Visitor newValidatorVisitor() {
        return new MetaModelValidatorVisiting.SummarizingVisitor() {

            private final Map<String, ObjectAction> actionsHavingHomePageFacet = _Maps.newHashMap();

            @Override
            public boolean visit(ObjectSpecification objectSpec, MetaModelValidator validator) {
                
                // as an optimization only checking declared members (skipping inherited ones)                 
                objectSpec.streamDeclaredActions(MixedIn.EXCLUDED)
                .filter(objectAction->objectAction.containsFacet(HomePageFacet.class))
                .forEach(objectAction->{
                    
                    actionsHavingHomePageFacet.put(objectAction.getId(), objectAction);

                    // TODO: it would be good to flag if the facet is found on any non-services, however
                    // ObjectSpecification.isService(...) can only be trusted once a PersistenceSession 
                    // exists.
                    // this ought to be improved upon at some point...

                    // TODO might collide with type level annotations as well 
                    
                });

                return true; // keep searching
            }

            @Override
            public void summarize(MetaModelValidator validator) {
                if(actionsHavingHomePageFacet.size()>1) {
                    
                    final Set<String> homepageActionIdSet = actionsHavingHomePageFacet.values().stream()
                            .map(ObjectAction::getIdentifier)
                            .map(Identifier::getFullIdentityString)
                            .collect(Collectors.toCollection(HashSet::new));
                    
                    for (val objectAction : actionsHavingHomePageFacet.values()) {
                        val actionIdentifier = objectAction.getIdentifier(); 
                        val actionId = actionIdentifier.getFullIdentityString();
                        val colission = homepageActionIdSet.stream()
                                .filter(not(actionId::equals))
                                .collect(Collectors.joining(", "));

                        validator.onFailure(
                                objectAction,
                                actionIdentifier,
                                "%s: other actions also specified as home page: %s ",
                                actionId, colission);
                    }
                }
            }
        };
    }
}
