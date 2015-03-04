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

package org.apache.isis.progmodel.wrapper;

import java.lang.reflect.Method;
import java.util.Collections;
import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.apache.isis.applib.services.wrapper.DisabledException;
import org.apache.isis.applib.services.wrapper.HiddenException;
import org.apache.isis.applib.services.wrapper.InvalidException;
import org.apache.isis.core.commons.authentication.AuthenticationSessionProvider;
import org.apache.isis.core.metamodel.adapter.*;
import org.apache.isis.core.metamodel.adapter.mgr.AdapterManager;
import org.apache.isis.core.metamodel.deployment.DeploymentCategory;
import org.apache.isis.core.metamodel.facetapi.FacetUtil;
import org.apache.isis.core.metamodel.facets.FacetedMethod;
import org.apache.isis.core.metamodel.facets.all.named.NamedFacetInferred;
import org.apache.isis.core.metamodel.facets.members.disabled.method.DisableForContextFacetViaMethod;
import org.apache.isis.core.metamodel.facets.members.hidden.method.HideForContextFacetViaMethod;
import org.apache.isis.core.metamodel.facets.properties.accessor.PropertyAccessorFacetViaAccessor;
import org.apache.isis.core.metamodel.facets.properties.update.clear.PropertyClearFacetViaClearMethod;
import org.apache.isis.core.metamodel.facets.properties.update.init.PropertyInitializationFacetViaSetterMethod;
import org.apache.isis.core.metamodel.facets.properties.update.modify.PropertySetterFacetViaModifyMethod;
import org.apache.isis.core.metamodel.facets.properties.validating.method.PropertyValidateFacetViaMethod;
import org.apache.isis.core.metamodel.spec.SpecificationLoader;
import org.apache.isis.core.metamodel.spec.feature.ObjectMember;
import org.apache.isis.core.metamodel.spec.feature.ObjectMemberContext;
import org.apache.isis.core.metamodel.specloader.specimpl.OneToOneAssociationImpl;
import org.apache.isis.core.metamodel.specloader.specimpl.dflt.ObjectSpecificationDefault;
import org.apache.isis.core.runtime.authentication.standard.SimpleSession;
import org.apache.isis.core.unittestsupport.jmocking.JUnitRuleMockery2;
import org.apache.isis.core.unittestsupport.jmocking.JUnitRuleMockery2.Mode;
import org.apache.isis.core.wrapper.WrapperFactoryAbstract;
import org.apache.isis.progmodel.wrapper.dom.employees.Employee;
import org.apache.isis.progmodel.wrapper.dom.employees.EmployeeRepository;
import org.apache.isis.progmodel.wrapper.dom.employees.EmployeeRepositoryImpl;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

public abstract class WrapperFactoryAbstractTest_wrappedObject {

    @Rule
    public JUnitRuleMockery2 context = JUnitRuleMockery2.createFor(Mode.INTERFACES_AND_CLASSES);

    @Rule
    public ExpectedException expectedException = ExpectedException.none();
    
    @Mock
    private AdapterManager mockAdapterManager;
    @Mock
    private AuthenticationSessionProvider mockAuthenticationSessionProvider;
    @Mock
    private ObjectPersistor mockObjectPersistor;
    @Mock
    private QuerySubmitter mockQuerySubmitter;
    @Mock
    private ServicesProvider mockServicesProvider;
    @Mock
    private SpecificationLoader mockSpecificationLookup;

    private ObjectMemberContext objectMemberContext;

    @Mock
    private ObjectSpecificationDefault mockEmployeeSpec;
    private ObjectMember employeeNameMember;

    @Mock
    private ObjectSpecificationDefault mockStringSpec;

    @Mock
    private ObjectAdapter mockEmployeeAdapter;

    @Mock
    private ObjectAdapter mockAdapterForStringSmith;
    @Mock
    private ObjectAdapter mockAdapterForStringJones;

    private final SimpleSession session = new SimpleSession("tester", Collections.<String>emptyList());

    private EmployeeRepository employeeRepository;

    private Employee employeeDO;
    private Employee employeeWO;

    private WrapperFactoryAbstract wrapperFactory;


    @Before
    public void setUp() {

        objectMemberContext = new ObjectMemberContext(DeploymentCategory.PRODUCTION, mockAuthenticationSessionProvider, mockSpecificationLookup, mockAdapterManager, mockQuerySubmitter, mockServicesProvider);
        
        employeeRepository = new EmployeeRepositoryImpl();

        employeeDO = new Employee();
        employeeDO.setName("Smith");
        employeeDO.setEmployeeRepository(employeeRepository); 

        wrapperFactory = createWrapperFactory();
        wrapperFactory.setAdapterManager(mockAdapterManager);
        wrapperFactory.setAuthenticationSessionProvider(mockAuthenticationSessionProvider);
        wrapperFactory.setObjectPersistor(mockObjectPersistor);
        wrapperFactory.setSpecificationLookup(mockSpecificationLookup);

        context.checking(new Expectations() {
            {
                allowing(mockSpecificationLookup).loadSpecification(String.class);
                will(returnValue(mockStringSpec));

                allowing(mockStringSpec).getShortIdentifier();
                will(returnValue(String.class.getName()));
            }
        });
        
        
        final Method employeeGetNameMethod = methodOf(Employee.class, "getName");
        final Method employeeSetNameMethod = methodOf(Employee.class, "setName", String.class);
        final Method employeeModifyNameMethod = methodOf(Employee.class, "modifyName", String.class);
        final Method employeeHideNameMethod = methodOf(Employee.class, "hideName");
        final Method employeeDisableNameMethod = methodOf(Employee.class, "disableName");
        final Method employeeValidateNameMethod = methodOf(Employee.class, "validateName", String.class);
        final Method employeeClearNameMethod = methodOf(Employee.class, "clearName");
        employeeNameMember = new OneToOneAssociationImpl(
                facetedMethodForProperty(
                        employeeSetNameMethod, employeeGetNameMethod, employeeModifyNameMethod, employeeClearNameMethod, employeeHideNameMethod, employeeDisableNameMethod, employeeValidateNameMethod), objectMemberContext);
        
        context.checking(new Expectations() {
            {
                allowing(mockAuthenticationSessionProvider).getAuthenticationSession();
                will(returnValue(session));

                allowing(mockAdapterManager).getAdapterFor(employeeDO);
                will(returnValue(mockEmployeeAdapter));

                allowing(mockEmployeeAdapter).getSpecification();
                will(returnValue(mockEmployeeSpec));

                allowing(mockSpecificationLookup).loadSpecification(Employee.class);
                will(returnValue(mockEmployeeSpec));

                allowing(mockEmployeeSpec).getMember(methodOf(Employee.class, "getEmployeeRepository"));
                will(returnValue(null));

                allowing(mockEmployeeSpec).getMember(employeeGetNameMethod);
                will(returnValue(employeeNameMember));

                allowing(mockEmployeeSpec).getMember(employeeSetNameMethod);
                will(returnValue(employeeNameMember));

                allowing(mockEmployeeSpec).getMember(employeeModifyNameMethod);
                will(returnValue(employeeNameMember));
                
                allowing(mockEmployeeSpec).getMember(employeeClearNameMethod);
                will(returnValue(employeeNameMember));
                
                allowing(mockEmployeeAdapter).getObject();
                will(returnValue(employeeDO));
                
                allowing(mockEmployeeAdapter).representsPersistent();
                will(returnValue(true));
            }
        });


        employeeWO = wrapperFactory.wrap(employeeDO);
    }

    /**
     * Mandatory hook.
     */
    protected abstract WrapperFactoryAbstract createWrapperFactory();

    @Test
    public void shouldWrapDomainObject() {
        // then
        assertThat(employeeWO, is(notNullValue()));
    }

    @Test
    public void shouldBeAbleToInjectIntoDomainObjects() {
        assertThat(employeeDO.getEmployeeRepository(), is(notNullValue()));
    }

    @Test
    public void cannotAccessMethodNotCorrespondingToMember() {

        expectedException.expectMessage("Method 'getEmployeeRepository' being invoked does not correspond to any of the object's fields or actions.");
        
        // then
        assertThat(employeeWO.getEmployeeRepository(), is(notNullValue()));
    }
    
    @Test
    public void shouldBeAbleToReadVisibleProperty() {

        allowingEmployeeHasSmithAdapter();
        

        // then
        assertThat(employeeWO.getName(), is(employeeDO.getName()));
    }

    @Test
    public void shouldNotBeAbleToViewHiddenProperty() {

        expectedException.expect(HiddenException.class);

        allowingEmployeeHasSmithAdapter();

        // given
        employeeDO.whetherHideName = true;
        // when
        employeeWO.getName();
        // then should throw exception
    }

    @Test
    public void shouldBeAbleToModifyEnabledPropertyUsingSetter() {

        allowingJonesStringValueAdapter();

        context.checking(new Expectations() {
            {

                oneOf(mockAdapterManager).adapterFor("Jones", mockEmployeeAdapter);
                will(returnValue(mockAdapterForStringJones));
            }
        });

        // when
        employeeWO.setName("Jones");
        // then
        assertThat(employeeDO.getName(), is("Jones"));
        assertThat(employeeWO.getName(), is(employeeDO.getName()));
    }

    @Test
    public void shouldNotBeAbleToModifyDisabledProperty() {

        expectedException.expect(DisabledException.class);

        // given
        employeeDO.reasonDisableName = "sorry, no change allowed";
        // when
        employeeWO.setName("Jones");
        // then should throw exception
    }

    @Test
    public void shouldNotBeAbleToModifyPropertyUsingModify() {

        allowingJonesStringValueAdapter();

        expectedException.expect(UnsupportedOperationException.class);

        // when
        employeeWO.modifyName("Jones");
        // then should throw exception
    }

    @Test
    public void shouldNotBeAbleToModifyPropertyUsingClear() {

        expectedException.expect(UnsupportedOperationException.class);

        // when
        employeeWO.clearName();
        // then should throw exception
    }

    @Test
    public void shouldNotBeAbleToModifyPropertyIfInvalid() {

        allowingJonesStringValueAdapter();

        expectedException.expect(InvalidException.class);

        // given
        employeeDO.reasonValidateName = "sorry, invalid data";
        // when
        employeeWO.setName("Jones");
        // then should throw exception
    }

    
    // //////////////////////////////////////

    private static FacetedMethod facetedMethodForProperty(
            Method init, Method accessor, Method modify, Method clear, Method hide, Method disable, Method validate) {
        FacetedMethod facetedMethod = FacetedMethod.createForProperty(accessor.getDeclaringClass(), accessor);
        FacetUtil.addFacet(new PropertyAccessorFacetViaAccessor(accessor, facetedMethod));
        FacetUtil.addFacet(new PropertyInitializationFacetViaSetterMethod(init, facetedMethod));
        FacetUtil.addFacet(new PropertySetterFacetViaModifyMethod(modify, facetedMethod, null));
        FacetUtil.addFacet(new PropertyClearFacetViaClearMethod(clear, facetedMethod));
        FacetUtil.addFacet(new HideForContextFacetViaMethod(hide, facetedMethod));
        FacetUtil.addFacet(new DisableForContextFacetViaMethod(disable, null, null, facetedMethod));
        FacetUtil.addFacet(new PropertyValidateFacetViaMethod(validate, null, null, facetedMethod));
        FacetUtil.addFacet(new NamedFacetInferred(accessor.getName(), facetedMethod));
        return facetedMethod;
    }

    private static Method methodOf(Class<?> cls, String methodName) {
        return methodOf(cls, methodName, new Class<?>[]{});
    }

    private static Method methodOf(Class<?> cls, String methodName, Class<?>... args) {
        try {
            return cls.getMethod(methodName, args);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // //////////////////////////////////////
    
    private void allowingEmployeeHasSmithAdapter() {
        context.checking(new Expectations() {
            {
                allowing(mockAdapterManager).adapterFor("Smith", mockEmployeeAdapter);
                will(returnValue(mockAdapterForStringSmith));

                allowing(mockAdapterForStringSmith).getObject();
                will(returnValue("Smith"));
            }
        });
    }

    private void allowingJonesStringValueAdapter() {
        context.checking(new Expectations() {
            {
                allowing(mockAdapterManager).adapterFor("Jones");
                will(returnValue(mockAdapterForStringJones));
                
                allowing(mockAdapterForStringJones).getObject();
                will(returnValue("Jones"));
                
                allowing(mockAdapterForStringJones).isTransient();
                will(returnValue(ResolveState.VALUE.isTransient()));
            }
        });
    }



}
