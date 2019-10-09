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

package org.apache.isis.metamodel.facets.object.mixin;

import org.apache.isis.applib.annotation.Mixin;
import org.apache.isis.metamodel.facetapi.FeatureType;
import org.apache.isis.metamodel.facetapi.MetaModelRefiner;
import org.apache.isis.metamodel.facets.FacetFactoryAbstract;
import org.apache.isis.metamodel.progmodel.ProgrammingModel;

import lombok.val;

public class MixinFacetForMixinAnnotationFactory 
extends FacetFactoryAbstract implements MetaModelRefiner {

    private final MetaModelValidatorForMixinTypes mixinTypeValidator = 
            new MetaModelValidatorForMixinTypes("@Mixin");

    public MixinFacetForMixinAnnotationFactory() {
        super(FeatureType.OBJECTS_ONLY);
    }

    @Override
    public void process(final ProcessClassContext processClassContext) {

        val mixinIfAny = processClassContext.synthesizeOnType(Mixin.class);
        if(!mixinIfAny.isPresent()) {
            return;
        }

        val facetHolder = processClassContext.getFacetHolder();
        val candidateMixinType = processClassContext.getCls();
        if (!mixinTypeValidator.ensureMixinType(facetHolder, candidateMixinType)) {
            return;
        }
        
        val mixinFacet = MixinFacetForMixinAnnotation
                .create(mixinIfAny.get(), candidateMixinType, facetHolder, getServiceInjector());
        facetHolder.addFacet(mixinFacet);
    }

    @Override
    public void refineProgrammingModel(ProgrammingModel programmingModel) {
        programmingModel.addValidator(mixinTypeValidator);
    }

}
