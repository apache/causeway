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

package org.apache.isis.core.metamodel.specloader.traverser;

import static org.apache.isis.core.commons.ensure.Ensure.ensureThatState;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;

import java.lang.reflect.Method;
import java.util.List;

import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.SpecificationLoaderSpi;
import org.apache.isis.core.metamodel.spec.SpecificationLoaderSpiAware;

public class SpecificationTraverserDefault implements SpecificationTraverser, SpecificationLoaderSpiAware {

    private SpecificationLoaderSpi specificationLoaderSpi;

    // ////////////////////////////////////////////////////////////////////
    // init, shutdown
    // ////////////////////////////////////////////////////////////////////

    @Override
    public void init() {
        ensureThatState(specificationLoaderSpi, is(notNullValue()));
    }

    @Override
    public void shutdown() {
    }

    // ////////////////////////////////////////////////////////////////////
    // Traverse API
    // ////////////////////////////////////////////////////////////////////

    /**
     * Traverses the return types of each method.
     * 
     * <p>
     * It's possible for there to be multiple return types: the generic type,
     * and the parameterized type.
     */
    @Override
    public void traverseTypes(final Method method, final List<Class<?>> discoveredTypes) {
        final TypeExtractorMethodReturn returnTypes = new TypeExtractorMethodReturn(method);
        for (final Class<?> returnType : returnTypes) {
            discoveredTypes.add(returnType);
        }
    }

    /**
     * Does nothing.
     */
    @Override
    public void traverseReferencedClasses(final ObjectSpecification noSpec, final List<Class<?>> discoveredTypes) throws ClassNotFoundException {
    }

    // ////////////////////////////////////////////////////////////////////
    // Dependencies (due to *Aware)
    // ////////////////////////////////////////////////////////////////////

    public SpecificationLoaderSpi getSpecificationLoaderSpi() {
        return specificationLoaderSpi;
    }

    @Override
    public void setSpecificationLoaderSpi(final SpecificationLoaderSpi specificationLoader) {
        this.specificationLoaderSpi = specificationLoader;
    }

}
