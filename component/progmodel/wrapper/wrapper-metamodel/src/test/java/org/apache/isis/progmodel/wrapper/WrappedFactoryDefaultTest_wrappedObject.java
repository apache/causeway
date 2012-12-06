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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

import org.apache.isis.core.testsupport.jmock.JUnitRuleMockery2;
import org.apache.isis.core.testsupport.jmock.JUnitRuleMockery2.Mode;
import org.apache.isis.progmodel.wrapper.applib.DisabledException;
import org.apache.isis.progmodel.wrapper.applib.HiddenException;
import org.apache.isis.progmodel.wrapper.applib.InvalidException;
import org.apache.isis.progmodel.wrapper.applib.WrapperFactory;
import org.apache.isis.progmodel.wrapper.metamodel.internal.WrapperFactoryDefault;
import org.apache.isis.tck.dom.claimapp.employees.Employee;
import org.apache.isis.tck.dom.claimapp.employees.EmployeeRepository;
import org.apache.isis.tck.dom.claimapp.employees.EmployeeRepositoryImpl;

public class WrappedFactoryDefaultTest_wrappedObject {

    @Rule
    public JUnitRuleMockery2 mockery = JUnitRuleMockery2.createFor(Mode.INTERFACES_ONLY);

    private EmployeeRepository employeeRepository;
    // private ClaimRepository claimRepository;

    private Employee employeeDO;
    private Employee employeeWO;

    private WrapperFactory wrapperFactory;

    @Before
    public void setUp() {

        employeeRepository = new EmployeeRepositoryImpl();
        // claimRepository = new ClaimRepositoryImpl();

        employeeDO = new Employee();
        employeeDO.setName("Smith");
        employeeDO.setEmployeeRepository(employeeRepository); // would be done
                                                              // by the
                                                              // EmbeddedContext
                                                              // impl

        wrapperFactory = new WrapperFactoryDefault();
        employeeWO = wrapperFactory.wrap(employeeDO);
    }

    @Ignore("TODO - moved from embedded runtime, need to re-enable")
    @Test
    public void shouldWrapDomainObject() {
        // then
        assertThat(employeeWO, is(notNullValue()));
    }

    @Ignore("TODO - moved from embedded runtime, need to re-enable")
    @Test
    public void shouldBeAbleToInjectIntoDomainObjects() {

        // given
        assertThat(employeeDO.getEmployeeRepository(), is(notNullValue()));

        // then
        assertThat(employeeWO.getEmployeeRepository(), is(notNullValue()));
    }

    @Ignore("TODO - moved from embedded runtime, need to re-enable")
    @Test
    public void shouldBeAbleToReadVisibleProperty() {
        // then
        assertThat(employeeWO.getName(), is(employeeDO.getName()));
    }

    @Ignore("TODO - moved from embedded runtime, need to re-enable")
    @Test(expected = HiddenException.class)
    public void shouldNotBeAbleToViewHiddenProperty() {
        // given
        employeeDO.whetherHideName = true;
        // when
        employeeWO.getName();
        // then should throw exception
    }

    @Ignore("TODO - moved from embedded runtime, need to re-enable")
    @Test
    public void shouldBeAbleToModifyEnabledPropertyUsingSetter() {
        // when
        employeeWO.setName("Jones");
        // then
        assertThat(employeeDO.getName(), is("Jones"));
        assertThat(employeeWO.getName(), is(employeeDO.getName()));
    }

    @Ignore("TODO - moved from embedded runtime, need to re-enable")
    @Test(expected = DisabledException.class)
    public void shouldNotBeAbleToModifyDisabledProperty() {
        // given
        employeeDO.reasonDisableName = "sorry, no change allowed";
        // when
        employeeWO.setName("Jones");
        // then should throw exception
    }

    @Ignore("TODO - moved from embedded runtime, need to re-enable")
    @Test(expected = UnsupportedOperationException.class)
    public void shouldNotBeAbleToModifyPropertyUsingModify() {
        // when
        employeeWO.modifyName("Jones");
        // then should throw exception
    }

    @Ignore("TODO - moved from embedded runtime, need to re-enable")
    @Test(expected = UnsupportedOperationException.class)
    public void shouldNotBeAbleToModifyPropertyUsingClear() {
        // when
        employeeWO.clearName();
        // then should throw exception
    }

    @Ignore("TODO - moved from embedded runtime, need to re-enable")
    @Test(expected = InvalidException.class)
    public void shouldNotBeAbleToModifyPropertyIfInvalid() {
        // given
        employeeDO.reasonValidateName = "sorry, invalid data";
        // when
        employeeWO.setName("Jones");
        // then should throw exception
    }

}
