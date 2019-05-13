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
import java.util.Optional;

import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;

import org.apache.isis.applib.Identifier;
import org.apache.isis.applib.services.i18n.TranslationService;
import org.apache.isis.applib.services.inject.ServiceInjector;
import org.apache.isis.applib.services.registry.ServiceRegistry;
import org.apache.isis.core.metamodel.MetaModelContext;
import org.apache.isis.core.metamodel.adapter.ObjectAdapterProvider;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facetapi.IdentifiedHolder;
import org.apache.isis.core.metamodel.facetapi.MethodRemover;
import org.apache.isis.core.metamodel.facets.object.domainobject.autocomplete.AutoCompleteFacetForDomainObjectAnnotation;
import org.apache.isis.core.metamodel.services.events.MetamodelEventService;
import org.apache.isis.core.metamodel.services.persistsession.PersistenceSessionServiceInternal;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.isis.core.metamodel.spec.feature.OneToOneActionParameter;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;
import org.apache.isis.core.security.authentication.AuthenticationSessionProvider;
import org.apache.isis.core.unittestsupport.jmocking.JUnitRuleMockery2;
import org.apache.isis.core.unittestsupport.jmocking.JUnitRuleMockery2.Mode;

public abstract class AbstractFacetFactoryJUnit4TestCase {

    @Rule
    public JUnitRuleMockery2 context = JUnitRuleMockery2.createFor(Mode.INTERFACES_AND_CLASSES);

    @Mock
    protected SpecificationLoader mockSpecificationLoader;
    @Mock
    protected PersistenceSessionServiceInternal mockPersistenceSessionServiceInternal;
    @Mock
    protected MethodRemover mockMethodRemover;
    @Mock
    protected FacetHolder mockFacetHolder;
    @Mock
    protected ServiceInjector mockServiceInjector;
    @Mock
    protected ServiceRegistry mockServiceRegistry;
    @Mock
    protected TranslationService mockTranslationService;
    @Mock   
    protected AuthenticationSessionProvider mockAuthenticationSessionProvider;

    protected IdentifiedHolder facetHolder;

    @Mock    protected ObjectSpecification mockOnType;
    @Mock    protected ObjectSpecification mockObjSpec;
    @Mock    protected OneToOneAssociation mockOneToOneAssociation;
    @Mock    protected OneToManyAssociation mockOneToManyAssociation;
    @Mock    protected OneToOneActionParameter mockOneToOneActionParameter;
//    @Mock    protected EventBusService mockEventBusService;
    @Mock    protected ObjectAdapterProvider mockObjectAdapterProvider;
    
    @Mock    protected MetamodelEventService mockMetamodelEventService;
    
    protected FacetedMethod facetedMethod;
    protected FacetedMethodParameter facetedMethodParameter;

    public static class Customer {

        private String firstName;

        public String getFirstName() {
            return firstName;
        }

        public void setFirstName(final String firstName) {
            this.firstName = firstName;
        }
    }

    @Before
    public void setUpFacetedMethodAndParameter() throws Exception {

        // PRODUCTION
        
    	MetaModelContext.preset(MetaModelContext.builder()
    			.specificationLoader(mockSpecificationLoader)
        		.serviceInjector(mockServiceInjector)
        		.serviceRegistry(mockServiceRegistry)
        		.build());
        
        context.checking(new Expectations() {{

            allowing(mockServiceRegistry).lookupService(TranslationService.class);
            will(returnValue(Optional.of(mockTranslationService)));

            allowing(mockServiceRegistry).lookupService(AuthenticationSessionProvider.class);
            will(returnValue(Optional.of(mockAuthenticationSessionProvider)));

            allowing(mockServiceRegistry).lookupServiceElseFail(MetamodelEventService.class);
            will(returnValue(mockMetamodelEventService));

//            allowing(mockServicesInjector).lookupServiceElseFail(EventBusService.class);
//            will(returnValue(mockEventBusService));

        }});

        facetHolder = new AbstractFacetFactoryTest.IdentifiedHolderImpl(Identifier.propertyOrCollectionIdentifier(Customer.class, "firstName"));
        facetedMethod = FacetedMethod.createForProperty(AbstractFacetFactoryTest.Customer.class, "firstName");
        facetedMethodParameter = new FacetedMethodParameter(FeatureType.ACTION_PARAMETER_SCALAR, facetedMethod.getOwningType(), facetedMethod.getMethod(), String.class
        );
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

    protected AutoCompleteFacetForDomainObjectAnnotation expectNoMethodsRemoved() {
        context.never(mockMethodRemover);
        return null;
    }

}
