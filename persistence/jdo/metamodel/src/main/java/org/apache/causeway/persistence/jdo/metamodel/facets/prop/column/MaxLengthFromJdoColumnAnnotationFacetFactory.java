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
package org.apache.causeway.persistence.jdo.metamodel.facets.prop.column;

import java.util.stream.Stream;

import javax.inject.Inject;
import javax.jdo.annotations.IdentityType;

import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.core.metamodel.facetapi.FacetUtil;
import org.apache.causeway.core.metamodel.facetapi.FeatureType;
import org.apache.causeway.core.metamodel.facetapi.MetaModelRefiner;
import org.apache.causeway.core.metamodel.facets.FacetFactoryAbstract;
import org.apache.causeway.core.metamodel.facets.objectvalue.maxlen.MaxLengthFacet;
import org.apache.causeway.core.metamodel.progmodel.ProgrammingModel;
import org.apache.causeway.core.metamodel.spec.feature.MixedIn;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.causeway.core.metamodel.specloader.validator.ValidationFailure;
import org.apache.causeway.persistence.jdo.provider.entities.JdoFacetContext;
import org.apache.causeway.persistence.jdo.provider.metamodel.facets.object.persistencecapable.JdoPersistenceCapableFacet;
import org.apache.causeway.persistence.jdo.provider.metamodel.facets.prop.notpersistent.JdoNotPersistentFacet;

import lombok.val;

public class MaxLengthFromJdoColumnAnnotationFacetFactory
extends FacetFactoryAbstract
implements MetaModelRefiner {

    @Inject private JdoFacetContext jdoFacetContext;

    public MaxLengthFromJdoColumnAnnotationFacetFactory(final MetaModelContext mmc) {
        super(mmc, FeatureType.PROPERTIES_ONLY);
    }

    @Override
    public void process(final ProcessMethodContext processMethodContext) {

        // only applies to JDO entities; ignore any view models
        final Class<?> cls = processMethodContext.getCls();
        if(!jdoFacetContext.isPersistenceEnhanced(cls)) {
            return;
        }

        if(String.class != processMethodContext.getMethod().getReturnType()) {
            return;
        }

        val facetHolder = processMethodContext.getFacetHolder();

        _ColumnUtil.processColumnAnnotations(processMethodContext,
                jdoColumnIfAny->{
                    FacetUtil.addFacetIfPresent(
                            MaxTotalDigitsFacetFromJdoColumnAnnotation
                            .createJdo(jdoColumnIfAny, facetHolder));
                },
                jpaColumnIfAny->{
                    FacetUtil.addFacetIfPresent(
                            MaxTotalDigitsFacetFromJdoColumnAnnotation
                            .createJpa(jpaColumnIfAny, facetHolder));
                });

    }

    @Override
    public void refineProgrammingModel(final ProgrammingModel programmingModel) {
        programmingModel.addVisitingValidatorSkipManagedBeans(objectSpec->{
            final JdoPersistenceCapableFacet pcFacet = objectSpec.getFacet(JdoPersistenceCapableFacet.class);
            if(pcFacet==null || pcFacet.getIdentityType() == IdentityType.NONDURABLE) {
                return;
            }

            final Stream<ObjectAssociation> associations = objectSpec
                    .streamAssociations(MixedIn.EXCLUDED)
                    .filter(ObjectAssociation.Predicates.PROPERTIES);

            associations.forEach(association->{
                // skip checks if annotated with JDO @NotPersistent
                if(association.containsNonFallbackFacet(JdoNotPersistentFacet.class)) {
                    return;
                }

                association.lookupFacet(MaxLengthFacet.class)
                .map(MaxLengthFacet::getSharedFacetRankingElseFail)
                .ifPresent(facetRanking->facetRanking
                        .visitTopRankPairsSemanticDiffering(MaxLengthFacet.class, (a, b)->{

                            ValidationFailure.raiseFormatted(
                                    association,
                                    "%s: inconsistent MaxLength semantics specified in %s and %s.",
                                    association.getFeatureIdentifier().toString(),
                                    a.getClass().getSimpleName(),
                                    b.getClass().getSimpleName());
                        }));

            });
        });
    }

}
