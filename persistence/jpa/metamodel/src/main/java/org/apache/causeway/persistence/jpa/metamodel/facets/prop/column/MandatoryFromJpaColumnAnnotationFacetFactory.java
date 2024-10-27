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

import java.util.Optional;

import javax.inject.Inject;
import javax.persistence.Column;
import javax.persistence.JoinColumn;

import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.core.metamodel.facetapi.FacetUtil;
import org.apache.causeway.core.metamodel.facetapi.FeatureType;
import org.apache.causeway.core.metamodel.facetapi.MetaModelRefiner;
import org.apache.causeway.core.metamodel.facets.FacetFactoryAbstract;
import org.apache.causeway.core.metamodel.facets.objectvalue.mandatory.MandatoryFacet.Semantics;
import org.apache.causeway.core.metamodel.progmodel.ProgrammingModel;
import org.apache.causeway.core.metamodel.spec.feature.MixedIn;
import org.apache.causeway.persistence.commons.metamodel.facets.prop.column.MandatoryFromXxxColumnAnnotationMetaModelRefinerUtil;

public class MandatoryFromJpaColumnAnnotationFacetFactory
extends FacetFactoryAbstract
implements MetaModelRefiner {

    @Inject
    public MandatoryFromJpaColumnAnnotationFacetFactory(final MetaModelContext mmc) {
        super(mmc, FeatureType.PROPERTIES_ONLY);
    }

    @Override
    public void process(final ProcessMethodContext processMethodContext) {

        final Optional<Boolean> nullable1 = processMethodContext.synthesizeOnMethod(JoinColumn.class)
                .map(JoinColumn::nullable);

        final Optional<Boolean> nullable2 = processMethodContext.synthesizeOnMethod(Column.class)
                .map(Column::nullable);

        if(!nullable1.isPresent()
                && !nullable2.isPresent()) {
            return;
        }

        var nullable = nullable1.orElseGet(nullable2::get);
        var semantics = Semantics.required(!nullable);

        var facetHolder = processMethodContext.getFacetHolder();
        FacetUtil.addFacet(
                new MandatoryFacetFromJpaColumnAnnotation(semantics, facetHolder));
    }

    @Override
    public void refineProgrammingModel(final ProgrammingModel programmingModel) {
        programmingModel.addValidatorSkipManagedBeans(objectSpec->{

            objectSpec
                    .streamProperties(MixedIn.EXCLUDED)
                    .forEach(MandatoryFromXxxColumnAnnotationMetaModelRefinerUtil::validateMandatoryFacet);

        });
    }

}
