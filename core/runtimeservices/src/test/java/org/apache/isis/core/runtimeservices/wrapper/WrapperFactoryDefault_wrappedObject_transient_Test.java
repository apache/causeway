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

package org.apache.isis.core.runtimeservices.wrapper;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.apache.isis.applib.Identifier;
import org.apache.isis.applib.annotation.Where;
import org.apache.isis.applib.services.factory.FactoryService;
import org.apache.isis.applib.services.inject.ServiceInjector;
import org.apache.isis.applib.services.registry.ServiceRegistry;
import org.apache.isis.applib.services.wrapper.DisabledException;
import org.apache.isis.applib.services.wrapper.events.PropertyModifyEvent;
import org.apache.isis.applib.services.wrapper.events.PropertyUsabilityEvent;
import org.apache.isis.applib.services.wrapper.events.PropertyVisibilityEvent;
import org.apache.isis.applib.services.xactn.TransactionService;
import org.apache.isis.core.codegen.bytebuddy.services.ProxyFactoryServiceByteBuddy;
import org.apache.isis.core.commons.internal.plugins.codegen.ProxyFactoryService;
import org.apache.isis.core.metamodel.MetaModelContext_forTesting;
import org.apache.isis.core.metamodel.consent.Allow;
import org.apache.isis.core.metamodel.consent.Consent;
import org.apache.isis.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.core.metamodel.consent.InteractionResult;
import org.apache.isis.core.metamodel.consent.Veto;
import org.apache.isis.core.metamodel.context.MetaModelContext;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facets.members.disabled.DisabledFacet;
import org.apache.isis.core.metamodel.facets.members.disabled.DisabledFacetAbstractAlwaysEverywhere;
import org.apache.isis.core.metamodel.facets.object.entity.EntityFacet;
import org.apache.isis.core.metamodel.facets.properties.accessor.PropertyAccessorFacetViaAccessor;
import org.apache.isis.core.metamodel.facets.properties.update.modify.PropertySetterFacetViaSetterMethod;
import org.apache.isis.core.metamodel.objectmanager.ObjectManager;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;
import org.apache.isis.core.metamodel.specloader.specimpl.dflt.ObjectSpecificationDefault;
import org.apache.isis.core.runtime.session.IsisSessionFactory;
import org.apache.isis.core.runtimeservices.wrapper.dom.employees.Employee;
import org.apache.isis.core.security.authentication.AuthenticationSessionProvider;
import org.apache.isis.core.security.authentication.standard.SimpleSession;
import org.apache.isis.core.unittestsupport.jmocking.JUnitRuleMockery2;
import org.apache.isis.core.unittestsupport.jmocking.JUnitRuleMockery2.Mode;

import static org.apache.isis.core.unittestsupport.jmocking.PostponedAction.returnValuePostponed;

import lombok.val;

/**
 * Contract test.
 */
public class WrapperFactoryDefault_wrappedObject_transient_Test {

    @Rule
    public final JUnitRuleMockery2 context = JUnitRuleMockery2.createFor(Mode.INTERFACES_AND_CLASSES);

    @Mock private AuthenticationSessionProvider mockAuthenticationSessionProvider;
    @Mock private SpecificationLoader mockSpecificationLoader;
    @Mock private IsisSessionFactory mockIsisSessionFactory;
    @Mock private FactoryService mockFactoryService;
    @Mock private TransactionService mockTransactionService;
    
    
    @Mock private ManagedObject mockEmployeeAdapter;
    @Mock private ObjectSpecificationDefault mockOnType;
    @Mock private ObjectSpecificationDefault mockEmployeeSpec;
    @Mock private OneToOneAssociation mockPasswordMember;
    @Mock private Identifier mockPasswordIdentifier;
    @Mock private ServiceInjector mockServiceInjector;
    @Mock private ServiceRegistry mockServiceRegistry;
    @Mock protected ManagedObject mockPasswordAdapter;
    @Mock protected ObjectManager mockObjectManager;

    private Employee employeeDO;

    private final String passwordValue = "12345678";

    private final SimpleSession session = new SimpleSession("tester", Collections.<String>emptyList());

    private Method getPasswordMethod;
    private Method setPasswordMethod;

    private WrapperFactoryDefault wrapperFactory;
    private Employee employeeWO;
    private List<Facet> facets;
    
    protected MetaModelContext metaModelContext;

    @Before
    public void setUp() throws Exception {

        // PRODUCTION
        
        val proxyFactoryService = (ProxyFactoryService) new ProxyFactoryServiceByteBuddy();
        
        metaModelContext = MetaModelContext_forTesting.builder()
                .specificationLoader(mockSpecificationLoader)
                .objectManager(mockObjectManager)
                .authenticationSessionProvider(mockAuthenticationSessionProvider)
                .singleton(proxyFactoryService)
                .singleton(wrapperFactory = createWrapperFactory(proxyFactoryService))
                .singleton(mockFactoryService)
                .singleton(mockIsisSessionFactory)
                .singleton(mockTransactionService)
                .build();
        
        metaModelContext.getServiceInjector().injectServicesInto(wrapperFactory);

        employeeDO = new Employee();
        employeeDO.setName("Smith");

        getPasswordMethod = Employee.class.getMethod("getPassword");
        setPasswordMethod = Employee.class.getMethod("setPassword", String.class);

        context.checking(new Expectations() {
            {

                allowing(mockObjectManager).adapt(employeeDO);
                will(returnValue(mockEmployeeAdapter));

                allowing(mockObjectManager).adapt(passwordValue);
                will(returnValue(mockPasswordAdapter));

                allowing(mockEmployeeAdapter).getSpecification();
                will(returnValue(mockEmployeeSpec));

                allowing(mockEmployeeAdapter).getPojo();
                will(returnValue(employeeDO));

                allowing(mockPasswordAdapter).getPojo();
                will(returnValue(passwordValue));

                allowing(mockPasswordMember).getIdentifier();
                will(returnValue(mockPasswordIdentifier));

                allowing(mockPasswordIdentifier).toClassAndNameIdentityString();
                will(returnValue("mocked-class#member"));
                
                allowing(mockSpecificationLoader).loadSpecification(Employee.class);
                will(returnValue(mockEmployeeSpec));

                allowing(mockEmployeeSpec).getMember(with(setPasswordMethod));
                will(returnValue(mockPasswordMember));

                allowing(mockEmployeeSpec).getMember(with(getPasswordMethod));
                will(returnValue(mockPasswordMember));
                
                allowing(mockEmployeeSpec).getFacet(EntityFacet.class);
                will(returnValue(null));

                allowing(mockPasswordMember).getName();
                will(returnValue("password"));

                allowing(mockAuthenticationSessionProvider).getAuthenticationSession();
                will(returnValue(session));

                allowing(mockPasswordMember).isOneToOneAssociation();
                will(returnValue(true));

                allowing(mockPasswordMember).isOneToManyAssociation();
                will(returnValue(false));

            }
        });

        employeeWO = wrapperFactory.wrap(employeeDO);
    }

    protected WrapperFactoryDefault createWrapperFactory(ProxyFactoryService proxyFactoryService) {
        val wrapperFactory = new WrapperFactoryDefault();
        wrapperFactory.proxyFactoryService = proxyFactoryService;
        wrapperFactory.init();
        return wrapperFactory;
    }

    @Test(expected = DisabledException.class)
    public void shouldNotBeAbleToModifyProperty() {

        // given
        final DisabledFacet disabledFacet = new DisabledFacetAbstractAlwaysEverywhere(mockPasswordMember){};
        facets = Arrays.asList(disabledFacet, new PropertySetterFacetViaSetterMethod(setPasswordMethod, mockPasswordMember));

        final Consent visibilityConsent = new Allow(new InteractionResult(new PropertyVisibilityEvent(employeeDO, null)));

        final InteractionResult usabilityInteractionResult = new InteractionResult(new PropertyUsabilityEvent(employeeDO, null));
        usabilityInteractionResult.advise("disabled", disabledFacet);
        final Consent usabilityConsent = new Veto(usabilityInteractionResult);

        context.checking(new Expectations() {
            {
                allowing(mockPasswordMember).streamFacets();
                will(returnValuePostponed(facets::stream));

                allowing(mockPasswordMember).isVisible(mockEmployeeAdapter, InteractionInitiatedBy.USER, Where.ANYWHERE);
                will(returnValue(visibilityConsent));

                allowing(mockPasswordMember).isUsable(mockEmployeeAdapter, InteractionInitiatedBy.USER, Where.ANYWHERE);
                will(returnValue(usabilityConsent));
            }
        });

        // when
        employeeWO.setPassword(passwordValue);

        // then should throw exception
    }

    @Test
    public void canModifyProperty() {
        // given

        final Consent visibilityConsent = new Allow(new InteractionResult(new PropertyVisibilityEvent(employeeDO, mockPasswordIdentifier)));
        final Consent usabilityConsent = new Allow(new InteractionResult(new PropertyUsabilityEvent(employeeDO, mockPasswordIdentifier)));
        final Consent validityConsent = new Allow(new InteractionResult(new PropertyModifyEvent(employeeDO, mockPasswordIdentifier, passwordValue)));

        context.checking(new Expectations() {
            {
                allowing(mockPasswordMember).isVisible(mockEmployeeAdapter, InteractionInitiatedBy.USER, Where.ANYWHERE);
                will(returnValue(visibilityConsent));

                allowing(mockPasswordMember).isUsable(mockEmployeeAdapter, InteractionInitiatedBy.USER, Where.ANYWHERE);
                will(returnValue(usabilityConsent));

                allowing(mockPasswordMember).isAssociationValid(mockEmployeeAdapter, mockPasswordAdapter,
                        InteractionInitiatedBy.USER);
                will(returnValue(validityConsent));
            }
        });

        facets = Arrays.asList((Facet)new PropertySetterFacetViaSetterMethod(
                setPasswordMethod, mockPasswordMember));
        
        context.checking(new Expectations() {
            {
                allowing(mockPasswordMember).streamFacets();
                will(returnValuePostponed(()->facets.stream()));

                oneOf(mockPasswordMember)
                .set(mockEmployeeAdapter, mockPasswordAdapter, InteractionInitiatedBy.USER);
                
                oneOf(mockPasswordMember).get(mockEmployeeAdapter, InteractionInitiatedBy.USER);
                will(returnValue(mockPasswordAdapter));
            }
        });

        // when
        employeeWO.setPassword(passwordValue);

        // and given
        facets = Arrays.asList((Facet)new PropertyAccessorFacetViaAccessor(
                mockOnType, getPasswordMethod, mockPasswordMember));

        // then be allowed
        assertThat(employeeWO.getPassword(), is(passwordValue));
    }
}
