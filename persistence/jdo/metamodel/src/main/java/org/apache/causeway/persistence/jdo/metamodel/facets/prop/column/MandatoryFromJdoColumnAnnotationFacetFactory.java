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

import java.util.Optional;

import javax.inject.Inject;
import javax.jdo.annotations.Column;
import javax.jdo.annotations.IdentityType;

import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.core.metamodel.facetapi.Facet.Precedence;
import org.apache.causeway.core.metamodel.facetapi.FacetUtil;
import org.apache.causeway.core.metamodel.facetapi.FeatureType;
import org.apache.causeway.core.metamodel.facetapi.MetaModelRefiner;
import org.apache.causeway.core.metamodel.facets.FacetFactoryAbstract;
import org.apache.causeway.core.metamodel.facets.FacetedMethod;
import org.apache.causeway.core.metamodel.facets.objectvalue.mandatory.MandatoryFacet;
import org.apache.causeway.core.metamodel.facets.properties.property.mandatory.MandatoryFacetForPropertyAnnotation;
import org.apache.causeway.core.metamodel.progmodel.ProgrammingModel;
import org.apache.causeway.core.metamodel.spec.feature.MixedIn;
import org.apache.causeway.persistence.commons.metamodel.facets.prop.column.MandatoryFromXxxColumnAnnotationMetaModelRefinerUtil;
import org.apache.causeway.persistence.jdo.metamodel.facets.prop.primarykey.MandatoryFacetFromJdoPrimaryKeyAnnotation;
import org.apache.causeway.persistence.jdo.provider.entities.JdoFacetContext;
import org.apache.causeway.persistence.jdo.provider.metamodel.facets.object.persistencecapable.JdoPersistenceCapableFacet;
import org.apache.causeway.persistence.jdo.provider.metamodel.facets.prop.notpersistent.JdoNotPersistentFacet;

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

            if (existingFacet instanceof MandatoryFacetFromJdoPrimaryKeyAnnotation) {
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

        var jdoColumnIfAny = processMethodContext.synthesizeOnMethod(javax.jdo.annotations.Column.class);
        MandatoryFacet.Semantics semantics = inferSemantics(processMethodContext, jdoColumnIfAny);
        if(jdoColumnIfAny.isPresent()) {
            FacetUtil.addFacet(
                    new MandatoryFacetFromJdoColumnAnnotation(semantics, holder));
        } else {
            FacetUtil.addFacet(
                    new MandatoryFacetFromAbsenceOfJdoColumnAnnotation(
                            semantics,
                            holder,
                            semantics.isRequired()
                                    ? Precedence.DEFAULT
                                    : Precedence.INFERRED));
        }

    }

    static MandatoryFacet.Semantics inferSemantics(
            final ProcessMethodContext processMethodContext,
            final Optional<Column> columnIfAny) {

        final String allowsNull = columnIfAny.isPresent()
                ? columnIfAny.get().allowsNull()
                : null;

        if(_Strings.isNotEmpty(allowsNull)) {
            // if miss-spelled, then DN assumes is not-nullable
            return MandatoryFacet.Semantics.required(!"true".equalsIgnoreCase(allowsNull.trim()));
        }

        final Class<?> returnType = processMethodContext.getMethod().getReturnType();
        // per JDO spec
        return returnType != null
                && returnType.isPrimitive()
                ? MandatoryFacet.Semantics.REQUIRED
                : MandatoryFacet.Semantics.OPTIONAL;

    }

    @Override
    public void refineProgrammingModel(final ProgrammingModel programmingModel) {
        programmingModel.addValidatorSkipManagedBeans(objectSpec->{

            final JdoPersistenceCapableFacet pcFacet = objectSpec.getFacet(JdoPersistenceCapableFacet.class);
            if(pcFacet==null || pcFacet.getIdentityType() == IdentityType.NONDURABLE) {
                return;
            }

            objectSpec
                    .streamProperties(MixedIn.EXCLUDED)
                    // skip checks if annotated with JDO @NotPersistent
                    .filter(association->!association.containsNonFallbackFacet(JdoNotPersistentFacet.class))
                    .forEach(MandatoryFromXxxColumnAnnotationMetaModelRefinerUtil::validateMandatoryFacet);

        });
    }

}
