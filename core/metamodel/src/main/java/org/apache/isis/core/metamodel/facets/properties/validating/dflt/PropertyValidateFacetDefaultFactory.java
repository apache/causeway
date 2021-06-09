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

package org.apache.isis.core.metamodel.facets.properties.validating.dflt;

import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facets.FacetFactory;
import org.apache.isis.core.metamodel.facets.FacetFactoryAbstract;
import org.apache.isis.core.metamodel.facets.properties.validating.PropertyValidateFacet;
import org.apache.isis.core.metamodel.specloader.facetprocessor.FacetProcessor;

import lombok.val;

/**
 * Simply installs a {@link PropertyValidateFacet} onto all properties.
 *
 * <p>
 * The idea is that this {@link FacetFactory} is included early on in the
 * {@link FacetProcessor}, but other {@link PropertyValidateFacet}
 * implementations will potentially replace these where the property is
 * annotated or otherwise provides a validation mechanism.
 */
public class PropertyValidateFacetDefaultFactory extends FacetFactoryAbstract {

    public PropertyValidateFacetDefaultFactory() {
        super(FeatureType.PROPERTIES_ONLY);
    }

    @Override
    public void process(final ProcessMethodContext processMethodContext) {
        val facetHolder = processMethodContext.getFacetHolder();
        addFacetIfPresent(
                new PropertyValidateFacetDefault(facetHolder));
    }

    @Override
    public void processParams(final ProcessParameterContext processParameterContext) {
        val facetHolder = processParameterContext.getFacetHolder();
        addFacetIfPresent(
                new PropertyValidateFacetDefault(facetHolder));
    }

}
