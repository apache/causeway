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

package org.apache.isis.metamodel.facets.actions.homepage.annotation;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.isis.applib.Identifier;
import org.apache.isis.applib.annotation.HomePage;
import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.metamodel.facetapi.FacetUtil;
import org.apache.isis.metamodel.facetapi.FeatureType;
import org.apache.isis.metamodel.facetapi.MetaModelValidatorRefiner;
import org.apache.isis.metamodel.facets.Annotations;
import org.apache.isis.metamodel.facets.FacetFactoryAbstract;
import org.apache.isis.metamodel.facets.FacetedMethod;
import org.apache.isis.metamodel.facets.actions.homepage.HomePageFacet;
import org.apache.isis.metamodel.spec.ObjectSpecification;
import org.apache.isis.metamodel.spec.feature.Contributed;
import org.apache.isis.metamodel.spec.feature.ObjectAction;
import org.apache.isis.metamodel.specloader.validator.MetaModelValidatorComposite;
import org.apache.isis.metamodel.specloader.validator.MetaModelValidatorVisiting;
import org.apache.isis.metamodel.specloader.validator.MetaModelValidatorVisiting.Visitor;
import org.apache.isis.metamodel.specloader.validator.ValidationFailures;

import static org.apache.isis.commons.internal.functions._Predicates.not;

import lombok.val;

public class HomePageFacetAnnotationFactory extends FacetFactoryAbstract implements MetaModelValidatorRefiner{

    public HomePageFacetAnnotationFactory() {
        super(FeatureType.ACTIONS_ONLY);
    }

    @Override
    public void process(ProcessMethodContext processMethodContext) {
        final HomePage annotation = Annotations.getAnnotation(processMethodContext.getMethod(), HomePage.class);
        if (annotation == null) {
            return;
        }
        final FacetedMethod facetHolder = processMethodContext.getFacetHolder();
        FacetUtil.addFacet(new HomePageFacetAnnotation(facetHolder));
    }

    /* (non-Javadoc)
     * @see org.apache.isis.metamodel.facetapi.MetaModelValidatorRefiner#refineMetaModelValidator(org.apache.isis.metamodel.specloader.validator.MetaModelValidatorComposite, org.apache.isis.commons.config.IsisConfiguration)
     */
    @Override
    public void refineMetaModelValidator(MetaModelValidatorComposite metaModelValidator) {
        metaModelValidator.add(new MetaModelValidatorVisiting(newValidatorVisitor()));
    }

    private Visitor newValidatorVisitor() {
        return new MetaModelValidatorVisiting.SummarizingVisitor() {

            private final List<Identifier> annotated = _Lists.newArrayList();

            @Override
            public boolean visit(ObjectSpecification objectSpec, ValidationFailures validationFailures) {
                final Stream<ObjectAction> objectActions = objectSpec.streamObjectActions(Contributed.EXCLUDED);

                objectActions
                .filter(objectAction->objectAction.containsFacet(HomePageFacet.class))
                .forEach(objectAction->{

                    val identifier = objectAction.getIdentifier();

                    // TODO: it would good to flag if the facet is found on any non-services, however
                    // ObjectSpecification.isService(...) can only be trusted once a PersistenceSession exists.
                    // this ought to be improved upon at some point...
                    annotated.add(identifier);
                });

                return true; // keep searching
            }

            @Override
            public void summarize(ValidationFailures validationFailures) {
                if(annotated.size()>1) {
                    
                    final Set<String> annotatedIdSet = annotated.stream()
                            .map(Identifier::toClassAndNameIdentityString)
                            .collect(Collectors.toCollection(HashSet::new));
                    
                    for (val actionIdentifier : annotated) {
                        val actionId = actionIdentifier.toClassAndNameIdentityString();
                        val nonServiceNamesStr = annotatedIdSet.stream()
                                .filter(not(actionId::equals))
                                .collect(Collectors.joining(", "));

                        validationFailures.add(
                                actionIdentifier,
                                "%s: other actions also specified as home page: %s ",
                                actionId, nonServiceNamesStr);
                    }
                }
            }
        };
    }
}
