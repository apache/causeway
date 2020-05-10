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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import org.apache.isis.applib.Identifier;
import org.apache.isis.applib.services.bookmark.BookmarkService;
import org.apache.isis.applib.services.command.Command;
import org.apache.isis.applib.services.command.Command.Executor;
import org.apache.isis.applib.services.command.CommandContext;
import org.apache.isis.applib.services.command.CommandExecutorService;
import org.apache.isis.applib.services.factory.FactoryService;
import org.apache.isis.applib.services.message.MessageService;
import org.apache.isis.applib.services.metamodel.BeanSort;
import org.apache.isis.applib.services.wrapper.DisabledException;
import org.apache.isis.applib.services.wrapper.HiddenException;
import org.apache.isis.applib.services.wrapper.InvalidException;
import org.apache.isis.applib.services.wrapper.control.AsyncControlService;
import org.apache.isis.applib.services.xactn.TransactionService;
import org.apache.isis.core.codegen.bytebuddy.services.ProxyFactoryServiceByteBuddy;
import org.apache.isis.core.commons.internal.plugins.codegen.ProxyFactoryService;
import org.apache.isis.core.internaltestsupport.jmocking.JUnitRuleMockery2;
import org.apache.isis.core.internaltestsupport.jmocking.JUnitRuleMockery2.Mode;
import org.apache.isis.core.metamodel.MetaModelContext_forTesting;
import org.apache.isis.core.metamodel.context.MetaModelContext;
import org.apache.isis.core.metamodel.facetapi.FacetUtil;
import org.apache.isis.core.metamodel.facets.FacetedMethod;
import org.apache.isis.core.metamodel.facets.all.named.NamedFacetInferred;
import org.apache.isis.core.metamodel.facets.members.disabled.method.DisableForContextFacetViaMethod;
import org.apache.isis.core.metamodel.facets.members.hidden.method.HideForContextFacetViaMethod;
import org.apache.isis.core.metamodel.facets.object.entity.EntityFacet;
import org.apache.isis.core.metamodel.facets.properties.accessor.PropertyAccessorFacetViaAccessor;
import org.apache.isis.core.metamodel.facets.properties.update.clear.PropertyClearFacetViaClearMethod;
import org.apache.isis.core.metamodel.facets.properties.update.init.PropertyInitializationFacetViaSetterMethod;
import org.apache.isis.core.metamodel.facets.properties.update.modify.PropertySetterFacetViaModifyMethod;
import org.apache.isis.core.metamodel.facets.properties.validating.method.PropertyValidateFacetViaMethod;
import org.apache.isis.core.metamodel.interactions.HidingInteractionAdvisor;
import org.apache.isis.core.metamodel.objectmanager.ObjectManager;
import org.apache.isis.core.metamodel.services.command.CommandDtoServiceInternal;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectMember;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;
import org.apache.isis.core.metamodel.specloader.specimpl.OneToOneAssociationDefault;
import org.apache.isis.core.metamodel.specloader.specimpl.dflt.ObjectSpecificationDefault;
import org.apache.isis.core.runtime.iactn.IsisInteractionFactory;
import org.apache.isis.core.runtime.iactn.IsisInteractionTracker;
import org.apache.isis.core.runtimeservices.wrapper.dom.employees.Employee;
import org.apache.isis.core.runtimeservices.wrapper.dom.employees.EmployeeRepository;
import org.apache.isis.core.runtimeservices.wrapper.dom.employees.EmployeeRepositoryImpl;
import org.apache.isis.core.security.authentication.AuthenticationSessionTracker;
import org.apache.isis.core.security.authentication.standard.SimpleSession;
import org.apache.isis.schema.cmd.v2.CommandDto;

import lombok.val;

public class WrapperFactoryDefault_wrappedObject_Test {

    @Rule
    public JUnitRuleMockery2 context = JUnitRuleMockery2.createFor(Mode.INTERFACES_AND_CLASSES);

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Mock private AuthenticationSessionTracker mockAuthenticationSessionTracker;
    @Mock private MessageService mockMessageService;
    @Mock private CommandContext mockCommandContext;
    @Mock private Command mockCommand;
    @Mock private CommandDtoServiceInternal mockCommandDtoServiceInternal;
    @Mock private ObjectSpecification mockOnType;
    @Mock private SpecificationLoader mockSpecificationLoader;
    @Mock private IsisInteractionFactory mockIsisInteractionFactory;
    @Mock private IsisInteractionTracker mockIsisInteractionTracker;
    @Mock private CommandExecutorService mockCommandExecutorService;
    private AsyncControlService asyncControlService = new AsyncControlService();
    @Mock private ObjectSpecificationDefault mockEmployeeSpec;
    @Mock private FactoryService mockFactoryService;
    @Mock private TransactionService mockTransactionService;
    @Mock private BookmarkService mockBookmarkService;
    @Mock protected ObjectManager mockObjectManager;
    
    private ObjectMember employeeNameMember;

    @Mock private ObjectSpecificationDefault mockStringSpec;
    @Mock private ManagedObject mockEmployeeAdapter;
    @Mock private ManagedObject mockAdapterForStringSmith;
    @Mock private ManagedObject mockAdapterForStringJones;
    @Mock private Identifier mockId;

    private final SimpleSession session = new SimpleSession("tester", Collections.<String>emptyList());

    private EmployeeRepository employeeRepository;

    private Employee employeeDO;
    private Employee employeeWO;

    private WrapperFactoryDefault wrapperFactory;
    
    protected MetaModelContext metaModelContext;

    @SuppressWarnings("unchecked")
    @Before
    public void setUp() {

        // PRODUCTION
        
        val proxyFactoryService = (ProxyFactoryService) new ProxyFactoryServiceByteBuddy();

        metaModelContext = MetaModelContext_forTesting.builder()
                .specificationLoader(mockSpecificationLoader)
                .objectManager(mockObjectManager)
                .authenticationSessionTracker(mockAuthenticationSessionTracker)
                .singleton(proxyFactoryService)
                .singleton(wrapperFactory = createWrapperFactory(proxyFactoryService))
                .singleton(mockCommandContext)
                .singleton(mockCommandDtoServiceInternal)
                .singleton(mockFactoryService)
                .singleton(mockIsisInteractionFactory)
                .singleton(mockIsisInteractionTracker)
                .singleton(mockTransactionService)
                .singleton(mockCommandExecutorService)
                .singleton(mockCommandDtoServiceInternal)
                .singleton(asyncControlService)
                .singleton(mockBookmarkService)
                .build();
        
        metaModelContext.getServiceInjector().injectServicesInto(wrapperFactory);

        employeeRepository = new EmployeeRepositoryImpl();

        employeeDO = new Employee();
        employeeDO.setName("Smith");
        employeeDO.setEmployeeRepository(employeeRepository);

        context.checking(new Expectations() {
            {
                
                allowing(mockObjectManager).adapt(employeeDO);
                will(returnValue(mockEmployeeAdapter));

                allowing(mockEmployeeSpec).isManagedBean();
                will(returnValue(true));
                
                allowing(mockEmployeeSpec).getBeanSort();
                will(returnValue(BeanSort.ENTITY));
                
                allowing(mockEmployeeSpec).isIdentifiable();
                will(returnValue(true));
                
                allowing(mockEmployeeSpec).getCorrespondingClass();
                will(returnValue(Employee.class));

                allowing(mockStringSpec).getCorrespondingClass();
                will(returnValue(String.class));

                allowing(mockCommandDtoServiceInternal).asCommandDto(with(any(List.class)), with(any(OneToOneAssociation.class)), with(any(ManagedObject.class)));
                will(returnValue(new CommandDto()));
                
                allowing(mockCommandContext).getCommand();
                will(returnValue(mockCommand));
                
                allowing(mockCommandContext).getCurrentExecutor();
                will(returnValue(Optional.of(Executor.USER)));

                allowing(mockSpecificationLoader).loadSpecification(String.class);
                will(returnValue(mockStringSpec));

                allowing(mockStringSpec).getShortIdentifier();
                will(returnValue(String.class.getName()));

                allowing(mockAuthenticationSessionTracker).currentAuthenticationSession();
                will(returnValue(Optional.of(session)));

                allowing(mockEmployeeAdapter).titleString(null);
                will(returnValue("titleOf[mockEmployeeAdapter]"));
                
                allowing(mockEmployeeAdapter).getSpecification();
                will(returnValue(mockEmployeeSpec));

                allowing(mockSpecificationLoader).loadSpecification(Employee.class);
                will(returnValue(mockEmployeeSpec));

                allowing(mockEmployeeSpec).getMember(methodOf(Employee.class, "getEmployeeRepository"));
                will(returnValue(null));
                
                allowing(mockEmployeeSpec).getFacet(EntityFacet.class);
                will(returnValue(null));


//                allowing(mockAdapterForStringJones).isDestroyed();
//                will(returnValue(false));

                allowing(mockAdapterForStringJones).getSpecification();
                will(returnValue(mockStringSpec));

                allowing(mockObjectManager).adapt("Jones");
                will(returnValue(mockAdapterForStringJones));

            }
        });


        final Method employeeGetNameMethod = methodOf(Employee.class, "getName");
        final Method employeeSetNameMethod = methodOf(Employee.class, "setName", String.class);
        final Method employeeModifyNameMethod = methodOf(Employee.class, "modifyName", String.class);
        final Method employeeHideNameMethod = methodOf(Employee.class, "hideName");
        final Method employeeDisableNameMethod = methodOf(Employee.class, "disableName");
        final Method employeeValidateNameMethod = methodOf(Employee.class, "validateName", String.class);
        final Method employeeClearNameMethod = methodOf(Employee.class, "clearName");
        employeeNameMember = new OneToOneAssociationDefault(
                facetedMethodForProperty(
                        metaModelContext,
                        employeeSetNameMethod, employeeGetNameMethod, employeeModifyNameMethod, employeeClearNameMethod, employeeHideNameMethod, employeeDisableNameMethod, employeeValidateNameMethod));

        context.checking(new Expectations() {
            {
                //                allowing(mockServicesInjector).lookupServiceElseFail(WrapperFactory.class);
                //                will(returnValue(wrapperFactory));

                allowing(mockEmployeeSpec).getMember(employeeGetNameMethod);
                will(returnValue(employeeNameMember));

                allowing(mockEmployeeSpec).getMember(employeeSetNameMethod);
                will(returnValue(employeeNameMember));

                allowing(mockEmployeeSpec).getMember(employeeModifyNameMethod);
                will(returnValue(employeeNameMember));

                allowing(mockEmployeeSpec).getMember(employeeClearNameMethod);
                will(returnValue(employeeNameMember));

                allowing(mockEmployeeAdapter).getPojo();
                will(returnValue(employeeDO));

//                allowing(mockEmployeeAdapter).isRepresentingPersistent();
//                will(returnValue(true));
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

        expectedException.expectMessage(
                "Method 'getEmployeeRepository' being invoked does not correspond to any of the object's fields or actions.");

        // then
        assertThat(employeeWO.getEmployeeRepository(), is(notNullValue()));
    }

    @Test
    public void shouldBeAbleToReadVisibleProperty() {

        allowingEmployeeHasSmithAdapter();

        assertTrue(
                metaModelContext.getConfiguration().getCore().getMetaModel().isFilterVisibility());

        context.checking(new Expectations() {{

            allowing(mockAdapterForStringSmith).getSpecification();
            will(returnValue(mockStringSpec));

            allowing(mockStringSpec).isEntity();
            will(returnValue(false));
            
            allowing(mockStringSpec).getIdentifier();
            will(returnValue(mockId));
            
            allowing(mockStringSpec).getBeanSort();
            will(returnValue(BeanSort.VIEW_MODEL));
            
            allowing(mockStringSpec).streamFacets(HidingInteractionAdvisor.class);
            will(returnValue(Stream.empty()));
            
            allowing(mockObjectManager).adapt("Smith");
            will(returnValue(mockAdapterForStringSmith));
        }});

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

        assertTrue(
                metaModelContext.getConfiguration().getCore().getMetaModel().isFilterVisibility());

        context.checking(new Expectations() {
            {
                allowing(mockAdapterForStringJones).titleString(null);

                ignoring(mockCommand);

                //ignoring(mockStringSpec);
                
                allowing(mockStringSpec).isParented();
                will(returnValue(false));
                
                allowing(mockStringSpec).isEntity();
                will(returnValue(false));
                
                allowing(mockStringSpec).getIdentifier();
                will(returnValue(mockId));
                
                allowing(mockStringSpec).getBeanSort();
                will(returnValue(BeanSort.VIEW_MODEL));
                
                allowing(mockStringSpec).streamFacets(HidingInteractionAdvisor.class);
                will(returnValue(Stream.empty()));
                
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

    private FacetedMethod facetedMethodForProperty(
            MetaModelContext mmc,
            Method init, Method accessor, Method modify, Method clear, Method hide, Method disable, Method validate) {
        FacetedMethod facetedMethod = FacetedMethod.createForProperty(accessor.getDeclaringClass(), accessor);
        facetedMethod.setMetaModelContext(mmc);
        FacetUtil.addFacet(new PropertyAccessorFacetViaAccessor(mockOnType, accessor, facetedMethod));
        FacetUtil.addFacet(new PropertyInitializationFacetViaSetterMethod(init, facetedMethod));
        FacetUtil.addFacet(new PropertySetterFacetViaModifyMethod(modify, facetedMethod));
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
//                allowing(mockAdapterManager).adapterFor("Smith");
//                will(returnValue(mockAdapterForStringSmith));

                allowing(mockAdapterForStringSmith).getPojo();
                will(returnValue("Smith"));
            }
        });
    }

    private void allowingJonesStringValueAdapter() {
        context.checking(new Expectations() {
            {
//                allowing(mockAdapterManager).adapterFor("Jones");
//                will(returnValue(mockAdapterForStringJones));

                allowing(mockAdapterForStringJones).getPojo();
                will(returnValue("Jones"));

//                allowing(mockAdapterForStringJones).isTransient();
//                will(returnValue(false));
            }
        });
    }



}
