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

package org.apache.isis.core.metamodel.facets.properties.disabled.fromimmutable;

import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facets.FacetFactoryAbstract;
import org.apache.isis.core.metamodel.facets.FacetedMethod;
import org.apache.isis.core.metamodel.facets.members.disabled.DisabledFacet;
import org.apache.isis.core.metamodel.facets.object.immutable.ImmutableFacet;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;

public class DisabledFacetOnPropertyDerivedFromImmutableFactory extends FacetFactoryAbstract {

    public DisabledFacetOnPropertyDerivedFromImmutableFactory() {
        super(FeatureType.PROPERTIES_ONLY);
    }

    @Override
    public void process(final ProcessMethodContext processMethodContext) {
        final Class<?> declaringClass = processMethodContext.getMethod().getDeclaringClass();
        final ObjectSpecification spec = getSpecificationLoader().loadSpecification(
                declaringClass);
        if (spec.containsNonFallbackFacet(ImmutableFacet.class)) {
            //final ImmutableFacet immutableFacet = spec.getFacet(ImmutableFacet.class);
            final FacetedMethod facetHolder = processMethodContext.getFacetHolder();
            DisabledFacet facet = facetHolder.getFacet(DisabledFacet.class);
            if(facet != null && facet.isInvertedSemantics()) {
                // @Property(editing=ENABLED)
                return;
            }
            super.addFacet(new DisabledFacetOnPropertyDerivedFromImmutable(facetHolder));
        }
    }

}
