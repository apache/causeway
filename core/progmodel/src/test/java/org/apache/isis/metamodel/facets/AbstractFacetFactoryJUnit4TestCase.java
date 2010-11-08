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


package org.apache.isis.metamodel.facets;

import java.lang.reflect.Method;

import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectFeatureType;
import org.apache.isis.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.isis.core.metamodel.spec.feature.OneToOneActionParameter;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.isis.metamodel.specloader.ObjectReflector;


@RunWith(JMock.class)
public abstract class AbstractFacetFactoryJUnit4TestCase {

	protected Mockery context = new JUnit4Mockery();
	
    protected ObjectReflector reflector;
    protected MethodRemover methodRemover;
    protected FacetHolder facetHolder;
    
    protected ObjectSpecification noSpec;
    protected OneToOneAssociation oneToOneAssociation;
    protected OneToManyAssociation oneToManyAssociation;
    protected OneToOneActionParameter actionParameter;

    @Before
    public void setUp() throws Exception {
        reflector = context.mock(ObjectReflector.class);
        methodRemover = context.mock(MethodRemover.class);
        facetHolder = context.mock(FacetHolder.class);
        
        noSpec = context.mock(ObjectSpecification.class);
        oneToOneAssociation = context.mock(OneToOneAssociation.class);
        oneToManyAssociation = context.mock(OneToManyAssociation.class);
        actionParameter = context.mock(OneToOneActionParameter.class);
    }

    @After
    public void tearDown() throws Exception {
        reflector = null;
        methodRemover = null;
        facetHolder = null;
        
        noSpec = null;
        oneToOneAssociation = null;
        oneToManyAssociation = null;
        actionParameter = null;
    }


    protected boolean contains(final Class<?>[] types, final Class<?> type) {
    	return Utils.contains(types, type);
    }

    protected boolean contains(final ObjectFeatureType[] featureTypes, final ObjectFeatureType featureType) {
    	return Utils.contains(featureTypes, featureType);
    }

    protected Method findMethod(final Class<?> type, final String methodName, final Class<?>[] methodTypes) {
    	return Utils.findMethod(type, methodName, methodTypes);
    }

    protected Method findMethod(final Class<?> type, final String methodName) {
        return Utils.findMethod(type, methodName);
    }

}

