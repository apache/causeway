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
package org.apache.isis.metamodel.facets.object.navparent.annotation;

import java.lang.reflect.Method;
import java.util.Optional;

import org.apache.isis.commons.internal._Constants;
import org.apache.isis.metamodel.adapter.ObjectAdapter;
import org.apache.isis.metamodel.facetapi.Facet;
import org.apache.isis.metamodel.facets.AbstractFacetFactoryJUnit4TestCase;
import org.apache.isis.metamodel.facets.FacetFactory.ProcessClassContext;
import org.apache.isis.metamodel.facets.object.navparent.NavigableParentFacet;
import org.apache.isis.metamodel.facets.object.navparent.annotation.NavigableParentTestSamples.DomainObjectA;
import org.apache.isis.metamodel.facets.object.navparent.method.NavigableParentFacetMethod;
import org.apache.isis.security.authentication.AuthenticationSession;
import org.apache.isis.security.authentication.AuthenticationSessionProvider;
import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class NavigableParentAnnotationFacetFactoryTest extends AbstractFacetFactoryJUnit4TestCase {

	private NavigableParentAnnotationFacetFactory facetFactory;

	@Mock
	private ObjectAdapter mockObjectAdapter;
	@Mock
	private AuthenticationSession mockAuthenticationSession;

	@Before
	public void setUp() throws Exception {

		// PRODUCTION

		context.allowing(mockSpecificationLoader);

		facetFactory = new NavigableParentAnnotationFacetFactory();

		context.checking(new Expectations() {
			{
				allowing(mockServiceRegistry).lookupService(AuthenticationSessionProvider.class);
				will(returnValue(Optional.of(mockAuthenticationSessionProvider)));

				allowing(mockAuthenticationSessionProvider).getAuthenticationSession();
				will(returnValue(mockAuthenticationSession));

				//                allowing(mockServicesInjector).getSpecificationLoader();
				//                will(returnValue(mockSpecificationLoader));
				//
				//                allowing(mockServicesInjector).getPersistenceSessionServiceInternal();
				//                will(returnValue(mockPersistenceSessionServiceInternal));
			}
		});

	}

	@After
	@Override
	public void tearDown() throws Exception {
		facetFactory = null;
		super.tearDown();
	}

	@Test
	public void testParentAnnotatedMethod() throws Exception {
		testParentMethod(new DomainObjectA(), "root");
	}

	// -- HELPER

	private void testParentMethod(Object domainObject, String parentMethodName) throws Exception {

		final Class<?> domainClass = domainObject.getClass();

		facetFactory.process(new ProcessClassContext(domainClass, mockMethodRemover, facetedMethod));

		final Facet facet = facetedMethod.getFacet(NavigableParentFacet.class);
		Assert.assertNotNull(facet);
		Assert.assertTrue(facet instanceof NavigableParentFacetMethod);

		final NavigableParentFacetMethod navigableParentFacetMethod = (NavigableParentFacetMethod) facet;
		final Method parentMethod = domainClass.getMethod(parentMethodName);

		Assert.assertEquals(
				parentMethod.invoke(domainObject, _Constants.emptyObjects), 
				navigableParentFacetMethod.navigableParent(domainObject)	);

	}



}
