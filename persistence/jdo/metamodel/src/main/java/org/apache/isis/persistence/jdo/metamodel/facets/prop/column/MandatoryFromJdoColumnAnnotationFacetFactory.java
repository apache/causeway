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

import java.util.Optional;
import java.util.stream.Stream;

import javax.inject.Inject;
import javax.jdo.annotations.Column;
import javax.jdo.annotations.IdentityType;

import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.core.metamodel.context.MetaModelContext;
import org.apache.isis.core.metamodel.facetapi.Facet.Precedence;
import org.apache.isis.core.metamodel.facetapi.FacetUtil;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facetapi.MetaModelRefiner;
import org.apache.isis.core.metamodel.facets.FacetFactoryAbstract;
import org.apache.isis.core.metamodel.facets.FacetedMethod;
import org.apache.isis.core.metamodel.facets.objectvalue.mandatory.MandatoryFacet;
import org.apache.isis.core.metamodel.facets.objectvalue.mandatory.MandatoryFacet.Semantics;
import org.apache.isis.core.metamodel.facets.properties.property.mandatory.MandatoryFacetForPropertyAnnotation;
import org.apache.isis.core.metamodel.progmodel.ProgrammingModel;
import org.apache.isis.core.metamodel.spec.feature.MixedIn;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.core.metamodel.specloader.validator.ValidationFailure;
import org.apache.isis.persistence.jdo.metamodel.facets.prop.primarykey.OptionalFacetInferredFromJdoPrimaryKeyAnnotation;
import org.apache.isis.persistence.jdo.provider.entities.JdoFacetContext;
import org.apache.isis.persistence.jdo.provider.metamodel.facets.object.persistencecapable.JdoPersistenceCapableFacet;
import org.apache.isis.persistence.jdo.provider.metamodel.facets.prop.notpersistent.JdoNotPersistentFacet;

import lombok.val;


public class MandatoryFromJdoColumnAnnotationFacetFactory
extends FacetFactoryAbstract
implements MetaModelRefiner {

    private final JdoFacetContext jdoFacetContext;

    @Inject
    public MandatoryFromJdoColumnAnnotationFacetFactory(
            final MetaModelContext mmc,
            final JdoFacetContext jdoFacetContext) {
        super(mmc, FeatureType.PROPERTIES_ONLY);
        this.jdoFacetContext = jdoFacetContext;
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

            if (existingFacet instanceof OptionalFacetInferredFromJdoPrimaryKeyAnnotation) {
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

        val columnIfAny = processMethodContext.synthesizeOnMethod(Column.class);

        val semantics = inferSemantics(processMethodContext, columnIfAny);

        FacetUtil.addFacet(
            columnIfAny.isPresent()
                    ? new MandatoryFacetFromJdoColumnAnnotation(holder, semantics)
                    : new MandatoryFacetInferredFromAbsenceOfJdoColumnAnnotation(
                            holder,
                            semantics,
                            semantics.isRequired()
                                ? Precedence.DEFAULT
                                : Precedence.INFERRED)
        );

    }

    private static Semantics inferSemantics(
            final ProcessMethodContext processMethodContext,
            final Optional<Column> columnIfAny) {

        final String allowsNull = columnIfAny.isPresent()
                ? columnIfAny.get().allowsNull()
                : null;

        if(_Strings.isNotEmpty(allowsNull)) {
            // if miss-spelled, then DN assumes is not-nullable
            return Semantics.of(!"true".equalsIgnoreCase(allowsNull.trim()));
        }

        final Class<?> returnType = processMethodContext.getMethod().getReturnType();
        // per JDO spec
        return returnType != null
                && returnType.isPrimitive()
            ? Semantics.REQUIRED
            : Semantics.OPTIONAL;

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

            associations
            // skip checks if annotated with JDO @NotPersistent
            .filter(association->!association.containsNonFallbackFacet(JdoNotPersistentFacet.class))
            .forEach(association->validateMandatoryFacet(association));

        });
    }

    private static void validateMandatoryFacet(final ObjectAssociation association) {

        association.lookupFacet(MandatoryFacet.class)
        .map(MandatoryFacet::getSharedFacetRankingElseFail)
        .ifPresent(facetRanking->facetRanking
                .visitTopRankPairsSemanticDiffering(MandatoryFacet.class, (a, b)->{

                    ValidationFailure.raiseFormatted(
                            association,
                            "%s: inconsistent Mandatory/Optional semantics specified in %s and %s.",
                            association.getFeatureIdentifier().toString(),
                            a.getClass().getSimpleName(),
                            b.getClass().getSimpleName());
                }));

    }

}
