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
package org.apache.causeway.core.metamodel.facets.properties.disabled.inferred;

import jakarta.inject.Inject;

import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.core.metamodel.facetapi.FacetUtil;
import org.apache.causeway.core.metamodel.facetapi.FeatureType;
import org.apache.causeway.core.metamodel.facets.FacetFactoryAbstract;
import org.apache.causeway.core.metamodel.facets.FacetedMethod;
import org.apache.causeway.core.metamodel.facets.members.disabled.DisabledFacet;
import org.apache.causeway.core.metamodel.facets.properties.update.modify.PropertySetterFacet;

/**
 * Run "near the end"
 */
public class DisabledFacetOnPropertyInferredFactory
extends FacetFactoryAbstract {

    @Inject
    public DisabledFacetOnPropertyInferredFactory(final MetaModelContext mmc) {
        super(mmc, FeatureType.PROPERTIES_ONLY);
    }

    @Override
    public void process(final ProcessMethodContext processMethodContext) {

        final FacetedMethod property = processMethodContext.getFacetHolder();

        if(property.containsNonFallbackFacet(DisabledFacet.class)) {
            // already disabled
            return;
        }
        if(property.containsNonFallbackFacet(PropertySetterFacet.class)) {
            // already known to be modifiable
            return;
        }

        // else, infer that this is not modifiable
        FacetUtil.addFacet(new DisabledFacetOnPropertyFromMissingSetter(property));
    }

}
