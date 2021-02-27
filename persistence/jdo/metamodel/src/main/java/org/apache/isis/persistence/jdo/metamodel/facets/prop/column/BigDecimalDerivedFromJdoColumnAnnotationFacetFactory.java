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

import javax.jdo.annotations.Column;
import javax.jdo.annotations.IdentityType;

import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facetapi.MetaModelRefiner;
import org.apache.isis.core.metamodel.facets.FacetFactoryAbstract;
import org.apache.isis.core.metamodel.facets.FacetedMethod;
import org.apache.isis.core.metamodel.facets.properties.bigdecimal.javaxvaldigits.BigDecimalFacetOnPropertyFromJavaxValidationDigitsAnnotation;
import org.apache.isis.core.metamodel.facets.value.bigdecimal.BigDecimalValueFacet;
import org.apache.isis.core.metamodel.facets.value.bigdecimal.BigDecimalValueSemanticsProvider;
import org.apache.isis.core.metamodel.progmodel.ProgrammingModel;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.MixedIn;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.core.metamodel.specloader.validator.MetaModelValidator;
import org.apache.isis.core.metamodel.specloader.validator.MetaModelValidatorVisiting;
import org.apache.isis.core.metamodel.specloader.validator.MetaModelValidatorVisiting.Visitor;
import org.apache.isis.persistence.jdo.provider.metamodel.facets.object.persistencecapable.JdoPersistenceCapableFacet;
import org.apache.isis.persistence.jdo.provider.metamodel.facets.prop.notpersistent.JdoNotPersistentFacet;

import lombok.val;


public class BigDecimalDerivedFromJdoColumnAnnotationFacetFactory extends FacetFactoryAbstract
implements MetaModelRefiner {

    private static final int DEFAULT_LENGTH = BigDecimalValueSemanticsProvider.DEFAULT_LENGTH;
    private static final int DEFAULT_SCALE = BigDecimalValueSemanticsProvider.DEFAULT_SCALE;

    public BigDecimalDerivedFromJdoColumnAnnotationFacetFactory() {
        super(FeatureType.PROPERTIES_ONLY);
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
            if(existingFacet != null && !existingFacet.isFallback()) {
                // do nothing
            } else {
                final BigDecimalValueFacet facet = new BigDecimalFacetFallback(holder);
                super.addFacet(facet);
            }
        } else {

            // obtain the existing facet's length and scale, to use as defaults if none are specified on the @Column
            // this will mean a metamodel validation exception will only be fired later (see #refineMetaModelValidator)
            // if there was an *explicit* value defined on the @Column annotation that is incompatible with existing.
            Integer existingLength = null;
            Integer existingScale = null;
            if(existingFacet != null && !existingFacet.isFallback()) {
                existingLength = existingFacet.getPrecision();
                existingScale = existingFacet.getScale();
            }

            Integer length = valueElseDefaults(jdoColumnAnnotation.length(), existingLength, DEFAULT_LENGTH);
            Integer scale = valueElseDefaults(jdoColumnAnnotation.scale(), existingScale, DEFAULT_SCALE);
            final BigDecimalValueFacet facet = new BigDecimalFacetDerivedFromJdoColumn(holder, length, scale);
            super.addFacet(facet);
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
        programmingModel.addValidator(newValidatorVisitor());
    }

    private Visitor newValidatorVisitor() {
        return new MetaModelValidatorVisiting.Visitor() {

            @Override
            public boolean visit(ObjectSpecification objectSpec, MetaModelValidator validator) {
                validate(objectSpec, validator);
                return true;
            }

            private void validate(ObjectSpecification objectSpec, MetaModelValidator validator) {

                // only consider persistent entities
                final JdoPersistenceCapableFacet pcFacet = objectSpec.getFacet(JdoPersistenceCapableFacet.class);
                if(pcFacet==null || pcFacet.getIdentityType() == IdentityType.NONDURABLE) {
                    return;
                }

                objectSpec.streamProperties(MixedIn.EXCLUDED)
                // skip checks if annotated with JDO @NotPersistent
                .filter(association->!association.containsNonFallbackFacet(JdoNotPersistentFacet.class))
                .forEach(association->{
                    validateBigDecimalValueFacet(association, validator);
                });

            }

            private void validateBigDecimalValueFacet(ObjectAssociation association, MetaModelValidator validator) {
                BigDecimalValueFacet facet = association.getFacet(BigDecimalValueFacet.class);
                if(facet == null) {
                    return;
                }

                BigDecimalValueFacet underlying = (BigDecimalValueFacet) facet.getUnderlyingFacet();
                if(underlying == null) {
                    return;
                }

                if(facet instanceof BigDecimalFacetDerivedFromJdoColumn) {

                    if(underlying instanceof BigDecimalFacetOnPropertyFromJavaxValidationDigitsAnnotation) {

                        if(notNullButNotEqual(facet.getPrecision(), underlying.getPrecision())) {
                            validator.onFailure(
                                    association,
                                    association.getIdentifier(),
                                    "%s: @javax.jdo.annotations.Column(length=...) different from @javax.validation.constraint.Digits(...); should equal the sum of its integer and fraction attributes",
                                    association.getIdentifier().toString());
                        }

                        if(notNullButNotEqual(facet.getScale(), underlying.getScale())) {
                            validator.onFailure(
                                    association,
                                    association.getIdentifier(),
                                    "%s: @javax.jdo.annotations.Column(scale=...) different from @javax.validation.constraint.Digits(fraction=...)",
                                    association.getIdentifier().toString());
                        }
                    }
                }
            }

            private boolean notNullButNotEqual(Integer x, Integer y) {
                return x != null && y != null && !x.equals(y);
            }
        };
    }


}
