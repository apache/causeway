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

package org.apache.isis.runtimes.embedded;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.progmodel.wrapper.applib.DisabledException;
import org.apache.isis.progmodel.wrapper.applib.HiddenException;
import org.apache.isis.progmodel.wrapper.applib.InvalidException;
import org.apache.isis.progmodel.wrapper.applib.WrapperFactory;
import org.apache.isis.runtimes.embedded.dom.claim.ClaimRepository;
import org.apache.isis.runtimes.embedded.dom.claim.ClaimRepositoryImpl;
import org.apache.isis.runtimes.embedded.dom.employee.Employee;
import org.apache.isis.runtimes.embedded.dom.employee.EmployeeRepository;
import org.apache.isis.runtimes.embedded.dom.employee.EmployeeRepositoryImpl;
import org.apache.isis.runtimes.embedded.internal.PersistenceState;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JMock.class)
public class WrappedFactoryDefaultTest_wrappedObject {

//    @Rule
//    public JMockRule rule = new JMockRule();
    
    private final Mockery mockery = new JUnit4Mockery();

    // @Mock
    private EmbeddedContext mockContext;
    // @Mock
    private AuthenticationSession mockAuthenticationSession;

    private EmployeeRepository employeeRepository;
    private ClaimRepository claimRepository;
    
    private Employee employeeDO;
    private Employee employeeWO;

    private IsisMetaModel metaModel;
    private WrapperFactory wrapperFactory;

    
    @Before
    public void setUp() {

        employeeRepository = new EmployeeRepositoryImpl();
        claimRepository = new ClaimRepositoryImpl();

        employeeDO = new Employee();
        employeeDO.setName("Smith");
        employeeDO.setEmployeeRepository(employeeRepository); // would be done by the EmbeddedContext impl

        mockContext = mockery.mock(EmbeddedContext.class);
        mockAuthenticationSession = mockery.mock(AuthenticationSession.class);

        mockery.checking(new Expectations() {
            {
                allowing(mockContext).getPersistenceState(with(any(Employee.class)));
                will(returnValue(PersistenceState.PERSISTENT));

                allowing(mockContext).getPersistenceState(with(any(String.class)));
                will(returnValue(PersistenceState.STANDALONE));

                allowing(mockContext).getAuthenticationSession();
                will(returnValue(mockAuthenticationSession));
            }
        });

        metaModel = new IsisMetaModel(mockContext, employeeRepository, claimRepository);
        metaModel.init();

        //employeeDO.setEmployeeRepository(employeeRepository);
        wrapperFactory = metaModel.getWrapperFactory();
        employeeWO = wrapperFactory.wrap(employeeDO);
    }

    @Test
    public void shouldWrapDomainObject() {
        // then
        assertThat(employeeWO, is(notNullValue()));
    }

    @Test
    public void shouldBeAbleToInjectIntoDomainObjects() {

        // given
        assertThat(employeeDO.getEmployeeRepository(), is(notNullValue()));
        
        // then
        assertThat(employeeWO.getEmployeeRepository(), is(notNullValue()));
    }


    @Test
    public void shouldBeAbleToReadVisibleProperty() {
        // then
        assertThat(employeeWO.getName(), is(employeeDO.getName()));
    }

    @Test(expected = HiddenException.class)
    public void shouldNotBeAbleToViewHiddenProperty() {
        // given
        employeeDO.whetherHideName = true;
        // when
        employeeWO.getName(); 
        // then should throw exception
    }

    @Test
    public void shouldBeAbleToModifyEnabledPropertyUsingSetter() {
        // when
        employeeWO.setName("Jones");
        // then
        assertThat(employeeDO.getName(), is("Jones"));
        assertThat(employeeWO.getName(), is(employeeDO.getName()));
    }

    @Test(expected = DisabledException.class)
    public void shouldNotBeAbleToModifyDisabledProperty() {
        // given
        employeeDO.reasonDisableName = "sorry, no change allowed";
        // when
        employeeWO.setName("Jones");
        // then should throw exception
    }

    @Test(expected = UnsupportedOperationException.class)
    public void shouldNotBeAbleToModifyPropertyUsingModify() {
        // when
        employeeWO.modifyName("Jones");
        // then should throw exception
    }

    @Test(expected = UnsupportedOperationException.class)
    public void shouldNotBeAbleToModifyPropertyUsingClear() {
        // when
        employeeWO.clearName();
        // then should throw exception
    }

    @Test(expected = InvalidException.class)
    public void shouldNotBeAbleToModifyPropertyIfInvalid() {
        // given
        employeeDO.reasonValidateName = "sorry, invalid data";
        // when
        employeeWO.setName("Jones");
        // then should throw exception
    }

}
