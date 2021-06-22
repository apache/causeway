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
package org.apache.isis.persistence.jdo.metamodel.facets.prop.column;

import java.math.BigDecimal;

import javax.inject.Inject;
import javax.jdo.annotations.Column;
import javax.jdo.annotations.IdentityType;

import org.apache.isis.core.metamodel.context.MetaModelContext;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facetapi.MetaModelRefiner;
import org.apache.isis.core.metamodel.facets.FacetFactoryAbstract;
import org.apache.isis.core.metamodel.facets.FacetedMethod;
import org.apache.isis.core.metamodel.facets.value.bigdecimal.BigDecimalValueFacet;
import org.apache.isis.core.metamodel.facets.value.bigdecimal.BigDecimalValueSemanticsProvider;
import org.apache.isis.core.metamodel.progmodel.ProgrammingModel;
import org.apache.isis.core.metamodel.spec.feature.MixedIn;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.core.metamodel.specloader.validator.ValidationFailure;
import org.apache.isis.persistence.jdo.provider.metamodel.facets.object.persistencecapable.JdoPersistenceCapableFacet;
import org.apache.isis.persistence.jdo.provider.metamodel.facets.prop.notpersistent.JdoNotPersistentFacet;

import lombok.val;


public class BigDecimalInferredFromJdoColumnAnnotationFacetFactory
extends FacetFactoryAbstract
implements MetaModelRefiner {

    private static final int DEFAULT_LENGTH = BigDecimalValueSemanticsProvider.DEFAULT_LENGTH;
    private static final int DEFAULT_SCALE = BigDecimalValueSemanticsProvider.DEFAULT_SCALE;

    @Inject
    public BigDecimalInferredFromJdoColumnAnnotationFacetFactory(final MetaModelContext mmc) {
        super(mmc, FeatureType.PROPERTIES_ONLY);
    }

    @Override
    public void process(final ProcessMethodContext processMethodContext) {

        if(BigDecimal.class != processMethodContext.getMethod().getReturnType()) {
            return;
        }

        final FacetedMethod holder = processMethodContext.getFacetHolder();

        BigDecimalValueFacet existingFacet = holder.getFacet(BigDecimalValueFacet.class);

        val jdoColumnAnnotation = processMethodContext.synthesizeOnMethod(Column.class)
                .orElse(null);

        if (jdoColumnAnnotation == null) {
            if(existingFacet != null
                    && !existingFacet.getPrecedence().isFallback()) {
                // do nothing
            } else {
                addFacet(new BigDecimalFacetFallback(holder));
            }
        } else {

            // obtain the existing facet's length and scale, to use as defaults if none are specified on the @Column
            // this will mean a metamodel validation exception will only be fired later (see #refineMetaModelValidator)
            // if there was an *explicit* value defined on the @Column annotation that is incompatible with existing.
            Integer existingLength = null;
            Integer existingScale = null;
            if(existingFacet != null
                    && !existingFacet.getPrecedence().isFallback()) {
                existingLength = existingFacet.getPrecision();
                existingScale = existingFacet.getScale();
            }

            Integer length = valueElseDefaults(jdoColumnAnnotation.length(), existingLength, DEFAULT_LENGTH);
            Integer scale = valueElseDefaults(jdoColumnAnnotation.scale(), existingScale, DEFAULT_SCALE);
            addFacet(new BigDecimalFacetInferredFromJdoColumn(holder, length, scale));
        }
    }

    private static final Integer valueElseDefaults(final int value, final Integer underlying, int defaultVal) {
        return value != -1
                ? value
                        : underlying != null
                        ? underlying
                                : defaultVal;
    }

    @Override
    public void refineProgrammingModel(ProgrammingModel programmingModel) {
        programmingModel.addVisitingValidatorSkipManagedBeans(spec->{

            // only consider persistent entities
            final JdoPersistenceCapableFacet pcFacet = spec.getFacet(JdoPersistenceCapableFacet.class);
            if(pcFacet==null || pcFacet.getIdentityType() == IdentityType.NONDURABLE) {
                return;
            }

            spec.streamProperties(MixedIn.EXCLUDED)
            // skip checks if annotated with JDO @NotPersistent
            .filter(association->!association.containsNonFallbackFacet(JdoNotPersistentFacet.class))
            .forEach(association->{
                validateBigDecimalValueFacet(association);
            });

        });
    }

    private static void validateBigDecimalValueFacet(ObjectAssociation association) {

        association.lookupFacet(BigDecimalValueFacet.class)
        .map(BigDecimalValueFacet::getSharedFacetRankingElseFail)
        .ifPresent(facetRanking->facetRanking
                .visitTopRankPairsSemanticDiffering(BigDecimalValueFacet.class, (a, b)->{

                    ValidationFailure.raiseFormatted(
                            association,
                            "%s: inconsistent BigDecimalValue semantics specified in %s and %s.",
                            association.getFeatureIdentifier().toString(),
                            a.getClass().getSimpleName(),
                            b.getClass().getSimpleName());
                }));

    }


}
