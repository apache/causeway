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


package org.apache.isis.extensions.headless.junit;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.apache.isis.commons.matchers.NofMatchers.classEqualTo;

import org.junit.Test;
import org.apache.isis.metamodel.facets.object.validate.ValidateObjectFacetViaValidateMethod;
import org.apache.isis.extensions.headless.applib.InvalidException;
import org.apache.isis.extensions.headless.applib.ViewObject;
import org.apache.isis.extensions.headless.junit.sample.domain.Customer;

public class SaveObjectsTest extends AbstractTest {

	@SuppressWarnings("unchecked")
	private ViewObject<Customer> asViewObject(final Customer proxiedNewCustomer) {
		return (ViewObject<Customer>) proxiedNewCustomer;
	}

	@Test
	public void invokingSaveThroughProxyMakesTransientObjectPersistent() {
		final Customer newCustomer = getDomainObjectContainer().newTransientInstance(Customer.class);
		assertThat(getDomainObjectContainer().isPersistent(newCustomer),is(false));
		final Customer newCustomerViewObject = getHeadlessViewer().view(newCustomer);
		newCustomerViewObject.setCustomerNumber(123);
		newCustomerViewObject.setLastName("Smith");
		newCustomerViewObject.setMandatoryAssociation(countryGbrDO);
		newCustomerViewObject.setMandatoryValue("foo");
		newCustomerViewObject.setMaxLengthField("abc");
		newCustomerViewObject.setRegExCaseInsensitiveField("ABCd");
		newCustomerViewObject.setRegExCaseSensitiveField("abcd");
		final ViewObject<Customer> proxyNewCustomer = asViewObject(newCustomerViewObject);
		proxyNewCustomer.save();
		assertThat(getDomainObjectContainer().isPersistent(newCustomer),
				is(true));
	}

	@Test
	public void invokingSaveOnThroughProxyOnAlreadyPersistedObjectJustUpdatesIt() {
		// just to get into valid state
		custJsDO.setCustomerNumber(123);
		custJsDO.setLastName("Smith");
		custJsDO.setMandatoryAssociation(countryGbrDO);
		custJsDO.setMandatoryValue("foo");
		custJsDO.setMaxLengthField("abc");
		custJsDO.setRegExCaseInsensitiveField("ABCd");
		custJsDO.setRegExCaseSensitiveField("abcd");
		
		assertThat(getDomainObjectContainer().isPersistent(custJsDO), is(true));
		
		final ViewObject<Customer> proxyNewCustomer = asViewObject(custJsVO);
		proxyNewCustomer.save();
		
		assertThat(getDomainObjectContainer().isPersistent(custJsDO), is(true));
	}

	@Test
	public void whenValidateMethodThenCanVetoSave() {
		final Customer newCustomer = getDomainObjectContainer().newTransientInstance(Customer.class);
		
		// just to get into valid state
		newCustomer.setCustomerNumber(123);
		newCustomer.setLastName("Smith");
		newCustomer.setMandatoryAssociation(countryGbrDO);
		newCustomer.setMandatoryValue("foo");
		newCustomer.setMaxLengthField("abc");
		newCustomer.setRegExCaseInsensitiveField("ABCd");
		newCustomer.setRegExCaseSensitiveField("abcd");

		final Customer newCustomerViewObject = getHeadlessViewer().view(newCustomer);
		newCustomer.validate = "No shakes";
		
		final ViewObject<Customer> proxyNewCustomer = asViewObject(newCustomerViewObject);
		try {
			assertThat(getDomainObjectContainer().isPersistent(newCustomer),
					is(false));
			proxyNewCustomer.save();
			fail("An InvalidImperativelyException should have been thrown");
		} catch (final InvalidException ex) {

			assertThat(ex.getAdvisorClass(), classEqualTo(ValidateObjectFacetViaValidateMethod.class));
			assertThat(getDomainObjectContainer().isPersistent(newCustomer),
					is(false)); // not saved
			assertThat(ex.getMessage(), equalTo("No shakes"));
		}
	}
		     
}
