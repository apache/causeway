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

package org.apache.isis.core.metamodel.facets.actions.action;

import java.lang.reflect.Method;
import java.util.UUID;

import org.apache.isis.applib.services.HasUniqueId;
import org.apache.isis.config.internal._Config;
import org.apache.isis.core.metamodel.facets.AbstractFacetFactoryJUnit4TestCase;
import org.apache.isis.core.metamodel.facets.object.domainobject.domainevents.ActionDomainEventDefaultFacetForDomainObjectAnnotation;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.security.authentication.AuthenticationSessionProvider;
import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.junit.After;
import org.junit.Before;

public class ActionAnnotationFacetFactoryTest extends AbstractFacetFactoryJUnit4TestCase {

    ActionAnnotationFacetFactory facetFactory;
    Method actionMethod;

    @Mock ObjectSpecification mockTypeSpec;
    @Mock ObjectSpecification mockReturnTypeSpec;

    void expectRemoveMethod(final Method actionMethod) {
        context.checking(new Expectations() {{
            oneOf(mockMethodRemover).removeMethod(actionMethod);
        }});
    }

    void allowingLoadSpecificationRequestsFor(final Class<?> cls, final Class<?> returnType) {
        context.checking(new Expectations() {{
            allowing(mockSpecificationLoader).loadSpecification(cls);
            will(returnValue(mockTypeSpec));

            allowing(mockSpecificationLoader).loadSpecification(returnType);
            will(returnValue(mockReturnTypeSpec));
        }});
    }

    @Before
    public void setUp() throws Exception {
        
        // PRODUCTION
        
        _Config.clear();
        
        facetFactory = new ActionAnnotationFacetFactory();

        context.checking(new Expectations() {{
            allowing(mockServiceRegistry).lookupServiceElseFail(AuthenticationSessionProvider.class);
            will(returnValue(mockAuthenticationSessionProvider));
            
            allowing(mockTypeSpec).getFacet(ActionDomainEventDefaultFacetForDomainObjectAnnotation.class);
            will(returnValue(null));

        }});

        actionMethod = findMethod(Customer.class, "someAction");
    }

    @Override
	@After
    public void tearDown() throws Exception {
        facetFactory = null;
    }

    class Customer {
        public void someAction() {
        }
    }

    class SomeTransactionalId implements HasUniqueId {
        public void someAction() {
        }

        @Override
        public UUID getUniqueId() {
            return null;
        }

        
    }

    void allowingCommandConfigurationToReturn(final String value) {
        _Config.put("isis.services.command.actions", value);
    }

    void allowingPublishingConfigurationToReturn(final String value) {
        _Config.put("isis.services.publish.actions", value);
    }

}
