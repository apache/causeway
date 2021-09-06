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

import org.jmock.auto.Mock;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;

import org.apache.isis.applib.Identifier;
import org.apache.isis.applib.id.LogicalType;
import org.apache.isis.commons.collections.ImmutableEnumSet;
import org.apache.isis.core.internaltestsupport.jmocking.JUnitRuleMockery2;
import org.apache.isis.core.internaltestsupport.jmocking.JUnitRuleMockery2.Mode;
import org.apache.isis.core.metamodel._testing.MetaModelContext_forTesting;
import org.apache.isis.core.metamodel.context.HasMetaModelContext;
import org.apache.isis.core.metamodel.context.MetaModelContext;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facetapi.FacetHolderAbstract;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facetapi.MethodRemover;
import org.apache.isis.core.metamodel.facets.object.domainobject.autocomplete.AutoCompleteFacetForDomainObjectAnnotation;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.isis.core.metamodel.spec.feature.OneToOneActionParameter;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;

import lombok.Getter;
import lombok.Setter;

public abstract class AbstractFacetFactoryJUnit4TestCase
implements HasMetaModelContext {

    @Rule
    public JUnitRuleMockery2 context = JUnitRuleMockery2.createFor(Mode.INTERFACES_AND_CLASSES);

    @Mock protected MethodRemover mockMethodRemover;
    @Mock protected FacetHolder mockFacetHolder;
    @Mock protected SpecificationLoader mockSpecificationLoader;

    @Mock protected ObjectSpecification mockOnType;
    @Mock protected ObjectSpecification mockObjSpec;
    @Mock protected OneToOneAssociation mockOneToOneAssociation;
    @Mock protected OneToManyAssociation mockOneToManyAssociation;
    @Mock protected OneToOneActionParameter mockOneToOneActionParameter;

    @Getter(onMethod_ = {@Override})
    protected MetaModelContext metaModelContext;
    protected FacetHolder facetHolder;
    protected FacetedMethod facetedMethod;
    protected FacetedMethodParameter facetedMethodParameter;

    public static class Customer {
        @Getter @Setter private String firstName;
    }

    @Before
    public void setUpFacetedMethodAndParameter() throws Exception {

        metaModelContext = MetaModelContext_forTesting.builder()
                        .specificationLoader(mockSpecificationLoader)
                        .build();

        facetHolder = FacetHolderAbstract.simple(
                metaModelContext,
                Identifier.propertyOrCollectionIdentifier(LogicalType.fqcn(Customer.class), "firstName"));
        facetedMethod = FacetedMethod.createForProperty(metaModelContext, AbstractFacetFactoryTest.Customer.class, "firstName");
        facetedMethodParameter = new FacetedMethodParameter(metaModelContext, FeatureType.ACTION_PARAMETER_SCALAR, facetedMethod.getOwningType(), facetedMethod.getMethod(), String.class);

    }

    @After
    public void tearDown() throws Exception {
        facetHolder = null;
        facetedMethod = null;
        facetedMethodParameter = null;
    }

    protected boolean contains(final Class<?>[] types, final Class<?> type) {
        return Utils.contains(types, type);
    }

    protected static boolean contains(final ImmutableEnumSet<FeatureType> featureTypes, final FeatureType featureType) {
        return Utils.contains(featureTypes, featureType);
    }

    protected Method findMethod(final Class<?> type, final String methodName, final Class<?>[] methodTypes) {
        return Utils.findMethod(type, methodName, methodTypes);
    }

    protected Method findMethod(final Class<?> type, final String methodName) {
        return Utils.findMethod(type, methodName);
    }

    protected AutoCompleteFacetForDomainObjectAnnotation expectNoMethodsRemoved() {
        //Mockito.verifyNoMoreInteractions(mockMethodRemover);
        context.never(mockMethodRemover);
        return null;
    }

}
