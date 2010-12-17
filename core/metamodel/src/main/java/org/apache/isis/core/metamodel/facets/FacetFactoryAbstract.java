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


import java.lang.reflect.Method;
import java.util.List;

import com.google.common.collect.ImmutableList;

import org.apache.isis.core.metamodel.spec.feature.ObjectFeatureType;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;
import org.apache.isis.core.metamodel.specloader.SpecificationLoaderAware;


public abstract class FacetFactoryAbstract implements FacetFactory, SpecificationLoaderAware {

    private final List<ObjectFeatureType> featureTypes;
    
    private SpecificationLoader specificationLoader;

    public FacetFactoryAbstract(final List<ObjectFeatureType> featureTypes) {
        this.featureTypes = ImmutableList.copyOf(featureTypes);
    }

    @Override
    public List<ObjectFeatureType> getFeatureTypes() {
        return featureTypes;
    }

    @Override
    public boolean process(final Class<?> cls, final MethodRemover methodRemover, final FacetHolder holder) {
        return false;
    }

    @Override
    public boolean process(Class<?> cls, final Method method, final MethodRemover methodRemover, final FacetHolder holder) {
        return false;
    }

    @Override
    public boolean processParams(final Method method, final int paramNum, final FacetHolder holder) {
        return false;
    }

    
    //////////////////////////////////////////////////////////////////
    // Dependencies (injected)
    //////////////////////////////////////////////////////////////////
    
    protected SpecificationLoader getSpecificationLoader() {
        return specificationLoader;
    }

    /**
     * Injected
     */
    @Override
    public void setSpecificationLoader(final SpecificationLoader specificationLoader) {
        this.specificationLoader = specificationLoader;
    }

}
