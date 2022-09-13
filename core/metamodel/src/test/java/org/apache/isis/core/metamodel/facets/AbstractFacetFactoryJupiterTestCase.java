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

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mockito;

import org.apache.isis.applib.Identifier;
import org.apache.isis.applib.id.LogicalType;
import org.apache.isis.commons.collections.ImmutableEnumSet;
import org.apache.isis.core.metamodel._testing.MetaModelContext_forTesting;
import org.apache.isis.core.metamodel.context.HasMetaModelContext;
import org.apache.isis.core.metamodel.context.MetaModelContext;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facetapi.MethodRemover;
import org.apache.isis.core.metamodel.facets.object.domainobject.autocomplete.AutoCompleteFacetForDomainObjectAnnotation;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.isis.core.metamodel.spec.feature.OneToOneActionParameter;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;
import org.apache.isis.core.metamodel.valuesemantics.IntValueSemantics;

import lombok.Getter;
import lombok.Setter;

public abstract class AbstractFacetFactoryJupiterTestCase
implements HasMetaModelContext {

    protected MethodRemover mockMethodRemover;
    protected FacetHolder mockFacetHolder;
    protected ObjectSpecification mockOnType;
    protected ObjectSpecification mockObjSpec;
    protected OneToOneAssociation mockOneToOneAssociation;
    protected OneToManyAssociation mockOneToManyAssociation;
    protected OneToOneActionParameter mockOneToOneActionParameter;

    @Getter(onMethod_ = {@Override})
    protected MetaModelContext metaModelContext;
    protected SpecificationLoader specificationLoader;
    protected FacetHolder facetHolder;
    protected FacetedMethod facetedMethod;
    protected FacetedMethodParameter facetedMethodParameter;

    public static class Customer {
        @Getter @Setter private String firstName;
    }

    @BeforeEach
    protected void setUpFacetedMethodAndParameter() throws Exception {

        mockMethodRemover = Mockito.mock(MethodRemover.class);
        mockFacetHolder = Mockito.mock(FacetHolder.class);
        mockOnType = Mockito.mock(ObjectSpecification.class);
        mockObjSpec = Mockito.mock(ObjectSpecification.class);
        mockOneToOneAssociation = Mockito.mock(OneToOneAssociation.class);
        mockOneToManyAssociation = Mockito.mock(OneToManyAssociation.class);
        mockOneToOneActionParameter = Mockito.mock(OneToOneActionParameter.class);

        metaModelContext = MetaModelContext_forTesting.builder()
                        .valueSemantic(new IntValueSemantics())
                        .build();

        specificationLoader = metaModelContext.getSpecificationLoader();

        facetHolder = FacetHolder.simple(
                metaModelContext,
                Identifier.propertyIdentifier(LogicalType.fqcn(Customer.class), "firstName"));
        facetedMethod = FacetedMethod.createSetterForProperty(metaModelContext,
                AbstractFacetFactoryTest.Customer.class, "firstName");
        facetedMethodParameter = new FacetedMethodParameter(metaModelContext,
                FeatureType.ACTION_PARAMETER_SCALAR, facetedMethod.getOwningType(),
                facetedMethod.getMethod(), 0);

    }

    @AfterEach
    protected void tearDown() throws Exception {
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
        //context.never(mockMethodRemover);
        Mockito.verifyNoInteractions(mockMethodRemover);
        return null;
    }

}
