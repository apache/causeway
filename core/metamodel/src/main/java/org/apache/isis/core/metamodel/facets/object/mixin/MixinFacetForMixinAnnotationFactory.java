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

package org.apache.isis.core.metamodel.facets.object.mixin;

import org.apache.isis.applib.annotation.Mixin;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facetapi.MetaModelValidatorRefiner;
import org.apache.isis.core.metamodel.facets.FacetFactoryAbstract;
import org.apache.isis.core.metamodel.specloader.validator.MetaModelValidatorComposite;

public class MixinFacetForMixinAnnotationFactory extends FacetFactoryAbstract implements MetaModelValidatorRefiner {

    private final MetaModelValidatorForMixinTypes mixinTypeValidator = new MetaModelValidatorForMixinTypes("@Mixin");

    public MixinFacetForMixinAnnotationFactory() {
        super(FeatureType.OBJECTS_ONLY);
    }

    @Override
    public void process(final ProcessClassContext processClassContext) {

        final Class<?> candidateMixinType = processClassContext.getCls();

        final Mixin mixinAnnotation = candidateMixinType.getAnnotation(Mixin.class);
        if(mixinAnnotation == null) {
            return;
        }

        if (!mixinTypeValidator.ensureMixinType(candidateMixinType)) {
            return;
        }
        final FacetHolder facetHolder = processClassContext.getFacetHolder();

        final MixinFacet mixinFacet = MixinFacetForMixinAnnotation.create(candidateMixinType, facetHolder,
                servicesInjector);
        facetHolder.addFacet(mixinFacet);
    }


    @Override
    public void refineMetaModelValidator(final MetaModelValidatorComposite metaModelValidator) {
        metaModelValidator.add(mixinTypeValidator);
    }

}
