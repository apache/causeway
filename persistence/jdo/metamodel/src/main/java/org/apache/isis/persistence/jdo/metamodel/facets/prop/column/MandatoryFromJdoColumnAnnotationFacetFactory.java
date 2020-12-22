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

import java.util.stream.Stream;

import javax.inject.Inject;
import javax.jdo.annotations.Column;
import javax.jdo.annotations.IdentityType;

import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.core.metamodel.facetapi.FacetUtil;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facetapi.MetaModelRefiner;
import org.apache.isis.core.metamodel.facets.FacetFactoryAbstract;
import org.apache.isis.core.metamodel.facets.FacetedMethod;
import org.apache.isis.core.metamodel.facets.objectvalue.mandatory.MandatoryFacet;
import org.apache.isis.core.metamodel.facets.objectvalue.mandatory.MandatoryFacetDefault;
import org.apache.isis.core.metamodel.facets.properties.property.mandatory.MandatoryFacetForPropertyAnnotation;
import org.apache.isis.core.metamodel.progmodel.ProgrammingModel;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.MixedIn;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.core.metamodel.specloader.validator.MetaModelValidator;
import org.apache.isis.core.metamodel.specloader.validator.MetaModelValidatorVisiting;
import org.apache.isis.core.metamodel.specloader.validator.MetaModelValidatorVisiting.Visitor;
import org.apache.isis.persistence.jdo.metamodel.facets.object.persistencecapable.JdoPersistenceCapableFacet;
import org.apache.isis.persistence.jdo.metamodel.facets.prop.notpersistent.JdoNotPersistentFacet;
import org.apache.isis.persistence.jdo.metamodel.facets.prop.primarykey.OptionalFacetDerivedFromJdoPrimaryKeyAnnotation;
import org.apache.isis.persistence.jdo.provider.entities.JdoFacetContext;

import lombok.val;


public class MandatoryFromJdoColumnAnnotationFacetFactory 
extends FacetFactoryAbstract
implements MetaModelRefiner {
    
    @Inject private JdoFacetContext jdoFacetContext;

    public MandatoryFromJdoColumnAnnotationFacetFactory() {
        super(FeatureType.PROPERTIES_ONLY);
    }

    @Override
    public void process(final ProcessMethodContext processMethodContext) {

        // only applies to JDO entities; ignore any view models
        final Class<?> cls = processMethodContext.getCls();
        if(!jdoFacetContext.isPersistenceEnhanced(cls)) {
            return;
        }

        final FacetedMethod holder = processMethodContext.getFacetHolder();

        final MandatoryFacet existingFacet = holder.getFacet(MandatoryFacet.class);
        if(existingFacet != null) {

            if (existingFacet instanceof OptionalFacetDerivedFromJdoPrimaryKeyAnnotation) {
                // do not replace this facet;
                // we must keep an optional facet here for different reasons
                return;
            }
            if (existingFacet instanceof MandatoryFacetForPropertyAnnotation.Required) {
                // do not replace this facet;
                // an explicit @Property(optional=FALSE) annotation cannot be overridden by @Column annotation
                return;
            }
        }

        val jdoColumnAnnotation = processMethodContext.synthesizeOnMethod(Column.class)
                .orElse(null);
        boolean required = whetherRequired(processMethodContext, jdoColumnAnnotation);
        MandatoryFacet facet = jdoColumnAnnotation != null
                ? new MandatoryFacetDerivedFromJdoColumn(holder, required)
                : new MandatoryFacetInferredFromAbsenceOfJdoColumn(holder, required);


        // as a side-effect, will chain any existing facets.
        // we'll exploit this fact for meta-model validation (see #refineMetaModelValidator(), below)
        FacetUtil.addFacet(facet);

        // however, if a @Column was explicitly provided, and the underlying facet
        // was the simple MandatoryFacetDefault (from an absence of @Optional or @Mandatory),
        // then don't chain, simply replace.
        if(facet instanceof MandatoryFacetDerivedFromJdoColumn && facet.getUnderlyingFacet() instanceof MandatoryFacetDefault) {
            facet.setUnderlyingFacet(null);
        }
    }

    private static boolean whetherRequired(final ProcessMethodContext processMethodContext, final Column annotation) {

        final String allowsNull = annotation != null ? annotation.allowsNull() : null;

        if(_Strings.isNotEmpty(allowsNull)) {
            // if miss-spelled, then DN assumes is not-nullable
            return !"true".equalsIgnoreCase(allowsNull.trim());
        }

        final Class<?> returnType = processMethodContext.getMethod().getReturnType();
        // per JDO spec
        return returnType != null && returnType.isPrimitive();

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

                final JdoPersistenceCapableFacet pcFacet = objectSpec.getFacet(JdoPersistenceCapableFacet.class);
                if(pcFacet==null || pcFacet.getIdentityType() == IdentityType.NONDURABLE) {
                    return;
                }

                final Stream<ObjectAssociation> associations = objectSpec
                        .streamAssociations(MixedIn.EXCLUDED)
                        .filter(ObjectAssociation.Predicates.PROPERTIES);

                associations
                // skip checks if annotated with JDO @NotPersistent
                .filter(association->!association.containsNonFallbackFacet(JdoNotPersistentFacet.class))
                .forEach(association->validateMandatoryFacet(association, validator));
            }

            private void validateMandatoryFacet(ObjectAssociation association, MetaModelValidator validator) {
                MandatoryFacet facet = association.getFacet(MandatoryFacet.class);

                MandatoryFacet underlying = (MandatoryFacet) facet.getUnderlyingFacet();
                if(underlying == null) {
                    return;
                }

                if(facet instanceof MandatoryFacetDerivedFromJdoColumn) {

                    if(underlying.isInvertedSemantics() == facet.isInvertedSemantics()) {
                        return;
                    }

                    if(underlying.isInvertedSemantics()) {
                        // ie @Optional
                        validator.onFailure(
                                association,
                                association.getIdentifier(),
                                "%s: incompatible usage of Isis' @Optional annotation and @javax.jdo.annotations.Column; use just @javax.jdo.annotations.Column(allowsNull=\"...\")",
                                association.getIdentifier().toClassAndNameIdentityString());
                    } else {
                        validator.onFailure(
                                association,
                                association.getIdentifier(),
                                "%s: incompatible Isis' default of required/optional properties vs JDO; add @javax.jdo.annotations.Column(allowsNull=\"...\")",
                                association.getIdentifier().toClassAndNameIdentityString());
                    }
                }

                if(facet instanceof MandatoryFacetInferredFromAbsenceOfJdoColumn) {

                    if(underlying.isInvertedSemantics() == facet.isInvertedSemantics()) {
                        return;
                    }
                    if(underlying.isInvertedSemantics()) {
                        // ie @Optional
                        validator.onFailure(
                                association,
                                association.getIdentifier(),
                                "%s: incompatible usage of Isis' @Optional annotation and @javax.jdo.annotations.Column; use just @javax.jdo.annotations.Column(allowsNull=\"...\")",
                                association.getIdentifier().toClassAndNameIdentityString());
                    } else {
                        validator.onFailure(
                                association,
                                association.getIdentifier(),
                                "%s: incompatible default handling of required/optional properties between Isis and JDO; add @javax.jdo.annotations.Column(allowsNull=\"...\")",
                                association.getIdentifier().toClassAndNameIdentityString());
                    }
                }
            }
        };
    }

}
