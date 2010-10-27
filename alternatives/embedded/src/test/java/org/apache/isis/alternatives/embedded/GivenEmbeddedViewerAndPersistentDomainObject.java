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


package org.apache.isis.alternatives.embedded;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.apache.isis.metamodel.authentication.AuthenticationSession;
import org.apache.isis.progmodel.wrapper.applib.DisabledException;
import org.apache.isis.progmodel.wrapper.applib.HiddenException;
import org.apache.isis.progmodel.wrapper.applib.InvalidException;
import org.apache.isis.progmodel.wrapper.applib.WrapperFactory;
import org.apache.isis.alternatives.embedded.EmbeddedContext;
import org.apache.isis.alternatives.embedded.IsisMetaModel;
import org.apache.isis.alternatives.embedded.dom.claim.ClaimRepositoryImpl;
import org.apache.isis.alternatives.embedded.dom.employee.Employee;
import org.apache.isis.alternatives.embedded.dom.employee.EmployeeRepositoryImpl;
import org.apache.isis.alternatives.embedded.internal.PersistenceState;


@RunWith(JMock.class)
public class GivenEmbeddedViewerAndPersistentDomainObject {
	
	private Mockery mockery = new JUnit4Mockery();
	
	private EmbeddedContext mockContext;

	private AuthenticationSession mockAuthenticationSession;

	private IsisMetaModel metaModel;

	private WrapperFactory viewer;

	private Employee employeeDO;

	private Employee employeeVO;
	
	
	@Before
	public void setUp() {
		
		employeeDO = new Employee();
		employeeDO.setName("Smith");
		
		mockContext = mockery.mock(EmbeddedContext.class);
		mockAuthenticationSession = mockery.mock(AuthenticationSession.class);


		mockery.checking(new Expectations(){{
			allowing(mockContext).getPersistenceState(with(any(Employee.class)));
			will(returnValue(PersistenceState.PERSISTENT));
			
			allowing(mockContext).getPersistenceState(with(any(String.class)));
			will(returnValue(PersistenceState.STANDALONE));
			
			allowing(mockContext).getAuthenticationSession();
			will(returnValue(mockAuthenticationSession));
		}});

		metaModel = new IsisMetaModel(mockContext, EmployeeRepositoryImpl.class, ClaimRepositoryImpl.class);
		metaModel.init();
		
		viewer = metaModel.getViewer();
	}
	
	@Test
	public void shouldBeAbleToGetViewOfDomainObject() {
		employeeVO = viewer.wrap(employeeDO);
		assertThat(employeeVO, is(notNullValue()));
	}

	
	@Test
	public void shouldBeAbleToReadVisibleProperty() {
		employeeVO = viewer.wrap(employeeDO);
		
		assertThat(employeeVO.getName(), is(employeeDO.getName()));
	}

	@Test(expected=HiddenException.class)
	public void shouldNotBeAbleToViewHiddenProperty() {
		employeeVO = viewer.wrap(employeeDO);
		
		employeeDO.whetherHideName = true;
		employeeVO.getName(); // should throw exception
	}


	@Test
	public void shouldBeAbleToModifyEnabledPropertyUsingSetter() {
		employeeVO = viewer.wrap(employeeDO);
		
		employeeVO.setName("Jones");
		assertThat(employeeDO.getName(), is("Jones"));
		assertThat(employeeVO.getName(), is(employeeDO.getName()));
	}

	@Test(expected=DisabledException.class)
	public void shouldNotBeAbleToModifyDisabledProperty() {
		employeeVO = viewer.wrap(employeeDO);
		
		employeeDO.reasonDisableName = "sorry, no change allowed";
		employeeVO.setName("Jones");
	}


	@Test(expected=UnsupportedOperationException.class)
	public void shouldNotBeAbleToModifyPropertyUsingModify() {
		employeeVO = viewer.wrap(employeeDO);
		
		employeeVO.modifyName("Jones"); // should throw exception
	}

	@Test(expected=UnsupportedOperationException.class)
	public void shouldNotBeAbleToModifyPropertyUsingClear() {
		employeeVO = viewer.wrap(employeeDO);
		
		employeeVO.clearName(); // should throw exception
	}


	@Test(expected=InvalidException.class)
	public void shouldNotBeAbleToModifyPropertyIfInvalid() {
		employeeVO = viewer.wrap(employeeDO);
		
		employeeDO.reasonValidateName = "sorry, invalid data";
		employeeVO.setName("Jones");
	}


	@Test(expected=DisabledException.class)
	public void shouldNotBeAbleToModifyPropertyForTransientOnly() {
		employeeVO = viewer.wrap(employeeDO);
		
		employeeVO.setPassword("12345678");
	}

	
	
	@Ignore("incomplete")
	@Test
	public void shouldBeAbleToInjectIntoDomainObjects() {
		
		// TODO: also ... be able to inject EmbeddedViewer as a service itself, if required.
		
		employeeVO.setPassword("12345678");
		
	}

}
