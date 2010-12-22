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


package org.apache.isis.core.progmodel.facets;

import java.lang.reflect.Method;
import java.util.List;

import junit.framework.TestCase;

import org.apache.log4j.BasicConfigurator;

import org.apache.isis.core.metamodel.facets.FacetHolderImpl;
import org.apache.isis.core.metamodel.feature.FeatureType;


public abstract class AbstractFacetFactoryTest extends TestCase {

    protected ProgrammableReflector reflector;
    protected ProgrammableMethodRemover methodRemover;

    protected FacetHolderImpl facetHolder;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        BasicConfigurator.configure();
        reflector = new ProgrammableReflector();
        facetHolder = new FacetHolderImpl();
        methodRemover = new ProgrammableMethodRemover();
    }

    @Override
    protected void tearDown() throws Exception {
        reflector = null;
        methodRemover = null;
        facetHolder = null;
        super.tearDown();
    }

    protected boolean contains(final Class<?>[] types, final Class<?> type) {
    	return Utils.contains(types, type);
    }

    protected boolean contains(final List<FeatureType> featureTypes, final FeatureType featureType) {
    	return Utils.contains(featureTypes, featureType);
    }

    protected Method findMethod(final Class<?> type, final String methodName, final Class<?>[] methodTypes) {
    	return Utils.findMethod(type, methodName, methodTypes);
    }

    protected Method findMethod(final Class<?> type, final String methodName) {
        return Utils.findMethod(type, methodName);
    }

    protected void assertNoMethodsRemoved() {
        assertTrue(methodRemover.getRemoveMethodMethodCalls().isEmpty());
        assertTrue(methodRemover.getRemoveMethodArgsCalls().isEmpty());
    }

    /**
     * Use {@link #contains(FeatureType[], FeatureType)
     */
    public abstract void testFeatureTypes();

}

