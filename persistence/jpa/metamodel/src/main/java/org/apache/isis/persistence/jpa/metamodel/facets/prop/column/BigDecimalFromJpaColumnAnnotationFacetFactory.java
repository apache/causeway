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
package org.apache.isis.persistence.jpa.metamodel.facets.prop.column;

import java.math.BigDecimal;

import javax.inject.Inject;
import javax.persistence.Column;

import org.apache.isis.core.metamodel.context.MetaModelContext;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facets.FacetFactoryAbstract;
import org.apache.isis.core.metamodel.facets.FacetedMethod;

import lombok.val;

public class BigDecimalFromJpaColumnAnnotationFacetFactory
extends FacetFactoryAbstract {

    @Inject
    public BigDecimalFromJpaColumnAnnotationFacetFactory(final MetaModelContext mmc) {
        super(mmc, FeatureType.PROPERTIES_ONLY);
    }

    @Override
    public void process(final ProcessMethodContext processMethodContext) {

        if(BigDecimal.class != processMethodContext.getMethod().getReturnType()) {
            return;
        }

        final FacetedMethod holder = processMethodContext.getFacetHolder();

        val jpaColumnIfAny = processMethodContext.synthesizeOnMethod(Column.class);

        addFacetIfPresent(
                MaxTotalDigitsFacetFromJpaColumnAnnotation
                .create(jpaColumnIfAny, holder));

        addFacetIfPresent(
                MaxFractionDigitsFacetFromJpaColumnAnnotation
                .create(jpaColumnIfAny, holder));
    }

}
