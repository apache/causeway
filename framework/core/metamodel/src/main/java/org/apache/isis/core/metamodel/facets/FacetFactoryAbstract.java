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

package org.apache.isis.core.metamodel.facets;

import java.util.List;

import com.google.common.collect.ImmutableList;

import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.spec.SpecificationLoader;
import org.apache.isis.core.metamodel.spec.SpecificationLoaderAware;

public abstract class FacetFactoryAbstract implements FacetFactory, SpecificationLoaderAware {

    private final List<FeatureType> featureTypes;

    private SpecificationLoader specificationLoader;

    public FacetFactoryAbstract(final List<FeatureType> featureTypes) {
        this.featureTypes = ImmutableList.copyOf(featureTypes);
    }

    @Override
    public List<FeatureType> getFeatureTypes() {
        return featureTypes;
    }

    @Override
    public void process(final ProcessClassContext processClassContext) {
    }

    @Override
    public void process(final ProcessMethodContext processMethodContext) {
    }

    @Override
    public void processParams(final ProcessParameterContext processParameterContext) {
    }

    // ////////////////////////////////////////////////////////////////
    // Dependencies (injected)
    // ////////////////////////////////////////////////////////////////

    protected SpecificationLoader getSpecificationLoader() {
        return specificationLoader;
    }

    /**
     * Injected
     */
    @Override
    public void setSpecificationLookup(final SpecificationLoader specificationLookup) {
        this.specificationLoader = specificationLookup;
    }

}
