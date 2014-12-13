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

import org.jmock.auto.Mock;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;

import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facetapi.FacetHolderImpl;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facetapi.MethodRemover;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.SpecificationLoaderSpi;
import org.apache.isis.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.isis.core.metamodel.spec.feature.OneToOneActionParameter;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.isis.core.unittestsupport.jmocking.JUnitRuleMockery2;
import org.apache.isis.core.unittestsupport.jmocking.JUnitRuleMockery2.Mode;

public abstract class AbstractFacetFactoryJUnit4TestCase extends Assert {

    @Rule
    public JUnitRuleMockery2 context = JUnitRuleMockery2.createFor(Mode.INTERFACES_AND_CLASSES);

    @Mock
    protected SpecificationLoaderSpi mockSpecificationLoaderSpi;
    @Mock
    protected MethodRemover mockMethodRemover;
    @Mock
    protected FacetHolder mockFacetHolder;

    @Mock
    protected IsisConfiguration mockConfiguration;

    protected FacetHolder facetHolderImpl;

    @Mock
    protected ObjectSpecification mockObjSpec;
    @Mock
    protected OneToOneAssociation mockOneToOneAssociation;
    @Mock
    protected OneToManyAssociation mockOneToManyAssociation;
    @Mock
    protected OneToOneActionParameter mockOneToOneActionParameter;

    protected FacetedMethod facetedMethod;
    protected FacetedMethodParameter facetedMethodParameter;

    @Before
    public void setUpFacetedMethodAndParameter() throws Exception {
        facetHolderImpl = new FacetHolderImpl();
        facetedMethod = FacetedMethod.createForProperty(AbstractFacetFactoryTest.Customer.class, "firstName");
        facetedMethodParameter = new FacetedMethodParameter(String.class);
    }
    
    @After
    public void tearDown() throws Exception {
        facetHolderImpl = null;
        facetedMethod = null;
        facetedMethodParameter = null;
    }
    
    protected boolean contains(final Class<?>[] types, final Class<?> type) {
        return Utils.contains(types, type);
    }

    protected boolean contains(final FeatureType[] featureTypes, final FeatureType featureType) {
        return Utils.contains(featureTypes, featureType);
    }

    protected static boolean contains(final List<FeatureType> featureTypes, final FeatureType featureType) {
        return Utils.contains(featureTypes, featureType);
    }

    protected Method findMethod(final Class<?> type, final String methodName, final Class<?>[] methodTypes) {
        return Utils.findMethod(type, methodName, methodTypes);
    }

    protected Method findMethod(final Class<?> type, final String methodName) {
        return Utils.findMethod(type, methodName);
    }

    protected void expectNoMethodsRemoved() {
        context.never(mockMethodRemover);
    }

}
