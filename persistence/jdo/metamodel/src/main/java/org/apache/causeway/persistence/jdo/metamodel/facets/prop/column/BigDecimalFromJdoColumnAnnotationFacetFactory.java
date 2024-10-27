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

import java.math.BigDecimal;

import javax.inject.Inject;
import javax.jdo.annotations.Column;
import javax.jdo.annotations.IdentityType;

import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.core.metamodel.facetapi.FeatureType;
import org.apache.causeway.core.metamodel.facetapi.MetaModelRefiner;
import org.apache.causeway.core.metamodel.facets.FacetFactoryAbstract;
import org.apache.causeway.core.metamodel.facets.FacetedMethod;
import org.apache.causeway.core.metamodel.progmodel.ProgrammingModel;
import org.apache.causeway.core.metamodel.spec.feature.MixedIn;
import org.apache.causeway.persistence.commons.metamodel.facets.prop.column.BigDecimalFromXxxColumnAnnotationMetaModelRefinerUtil;
import org.apache.causeway.persistence.jdo.provider.metamodel.facets.object.persistencecapable.JdoPersistenceCapableFacet;
import org.apache.causeway.persistence.jdo.provider.metamodel.facets.prop.notpersistent.JdoNotPersistentFacet;

public class BigDecimalFromJdoColumnAnnotationFacetFactory
extends FacetFactoryAbstract
implements MetaModelRefiner {

    @Inject
    public BigDecimalFromJdoColumnAnnotationFacetFactory(final MetaModelContext mmc) {
        super(mmc, FeatureType.PROPERTIES_ONLY);
    }

    @Override
    public void process(final ProcessMethodContext processMethodContext) {

        if(BigDecimal.class != processMethodContext.getMethod().getReturnType()) {
            return;
        }

        final FacetedMethod holder = processMethodContext.getFacetHolder();

        var jdoColumnIfAny = processMethodContext.synthesizeOnMethod(Column.class);

        addFacetIfPresent(
                MaxTotalDigitsFacetFromJdoColumnAnnotation.create(jdoColumnIfAny, holder));

        addFacetIfPresent(
                MaxFractionalDigitsFacetFromJdoColumnAnnotation.create(jdoColumnIfAny, holder));

        if (getConfiguration().getValueTypes().getBigDecimal().isUseScaleForMinFractionalFacet()) {
            addFacetIfPresent(
                    MinFractionalDigitsFacetFromJdoColumnAnnotation.create(jdoColumnIfAny, holder));
        }

    }

    @Override
    public void refineProgrammingModel(final ProgrammingModel programmingModel) {
        programmingModel.addValidatorSkipManagedBeans(objectSpec->{

            // only consider persistent entities
            final JdoPersistenceCapableFacet pcFacet = objectSpec.getFacet(JdoPersistenceCapableFacet.class);
            if(pcFacet==null || pcFacet.getIdentityType() == IdentityType.NONDURABLE) {
                return;
            }

            objectSpec
                    .streamProperties(MixedIn.EXCLUDED)
                    // skip checks if annotated with JDO @NotPersistent
                    .filter(association->!association.containsNonFallbackFacet(JdoNotPersistentFacet.class))
                    .forEach(BigDecimalFromXxxColumnAnnotationMetaModelRefinerUtil::validateBigDecimalValueFacet);

        });
    }

}
