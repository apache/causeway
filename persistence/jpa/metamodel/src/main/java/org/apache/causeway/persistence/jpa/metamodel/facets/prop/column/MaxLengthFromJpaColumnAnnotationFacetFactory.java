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
package org.apache.causeway.persistence.jpa.metamodel.facets.prop.column;

import javax.persistence.Column;

import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.core.metamodel.facetapi.FacetUtil;
import org.apache.causeway.core.metamodel.facetapi.FeatureType;
import org.apache.causeway.core.metamodel.facetapi.MetaModelRefiner;
import org.apache.causeway.core.metamodel.facets.FacetFactoryAbstract;
import org.apache.causeway.core.metamodel.progmodel.ProgrammingModel;
import org.apache.causeway.core.metamodel.spec.feature.MixedIn;
import org.apache.causeway.persistence.commons.metamodel.facets.prop.column.MaxLengthFromXxxColumnAnnotationMetaModelRefinerUtil;

public class MaxLengthFromJpaColumnAnnotationFacetFactory
extends FacetFactoryAbstract
implements MetaModelRefiner {

    public MaxLengthFromJpaColumnAnnotationFacetFactory(final MetaModelContext mmc) {
        super(mmc, FeatureType.PROPERTIES_ONLY);
    }

    @Override
    public void process(final ProcessMethodContext processMethodContext) {

        // only applies to JDO entities; ignore any view models
        final Class<?> cls = processMethodContext.getCls();

        if(String.class != processMethodContext.getMethod().getReturnType()) {
            return;
        }

        var facetHolder = processMethodContext.getFacetHolder();

        var jdoColumnIfAny = processMethodContext.synthesizeOnMethod(Column.class);

        FacetUtil.addFacetIfPresent(
                MaxLengthFacetFromJpaColumnAnnotation
                .create(jdoColumnIfAny, facetHolder));
    }

    @Override
    public void refineProgrammingModel(final ProgrammingModel programmingModel) {
        programmingModel.addValidatorSkipManagedBeans(objectSpec->{

            objectSpec
                    .streamProperties(MixedIn.EXCLUDED)
                    .forEach(MaxLengthFromXxxColumnAnnotationMetaModelRefinerUtil::validateMaxLengthFacet);
        });
    }

}
