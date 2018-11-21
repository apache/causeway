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

package org.apache.isis.core.runtime.system;

import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import org.apache.isis.applib.Identifier;
import org.apache.isis.applib.services.message.MessageService;
import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.core.commons.authentication.AuthenticationSessionProvider;
import org.apache.isis.core.metamodel.facets.FacetedMethod;
import org.apache.isis.core.metamodel.facets.all.named.NamedFacet;
import org.apache.isis.core.metamodel.facets.all.named.NamedFacetAbstract;
import org.apache.isis.core.metamodel.services.ServicesInjector;
import org.apache.isis.core.metamodel.services.persistsession.PersistenceSessionServiceInternal;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;
import org.apache.isis.core.metamodel.specloader.specimpl.ObjectActionDefault;
import org.apache.isis.core.unittestsupport.jmocking.JUnitRuleMockery2;
import org.apache.isis.core.unittestsupport.jmocking.JUnitRuleMockery2.Mode;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class ObjectActionLayoutXmlDefaultTest {

    @Rule
    public JUnitRuleMockery2 context = JUnitRuleMockery2.createFor(Mode.INTERFACES_AND_CLASSES);
    
    
    private ObjectActionDefault action;
    
    @Mock
    private FacetedMethod mockFacetedMethod;

    @Mock
    private AuthenticationSessionProvider mockAuthenticationSessionProvider;
    @Mock
    private SpecificationLoader mockSpecificationLoader;
    @Mock
    private MessageService mockMessageService;
    @Mock
    private PersistenceSessionServiceInternal mockPersistenceSessionServiceInternal;

    private ServicesInjector stubServicesInjector;

    @Before
    public void setUp() throws Exception {

        stubServicesInjector = ServicesInjector.builderForTesting()
                .addServices(_Lists.of(
                        mockAuthenticationSessionProvider,
                        mockSpecificationLoader,
                        mockPersistenceSessionServiceInternal,
                        mockMessageService))
                .build();

        context.checking(new Expectations() {
            {
                oneOf(mockFacetedMethod).getIdentifier();
                will(returnValue(Identifier.actionIdentifier("Customer", "reduceheadcount")));
            }
        });

        action = new ObjectActionDefault(mockFacetedMethod, stubServicesInjector);
    }


    @Test
    public void testNameDefaultsToActionsMethodName() {
        final String name = "Reduceheadcount";
        final NamedFacet facet = new NamedFacetAbstract(name, true, mockFacetedMethod) {
        };
        context.checking(new Expectations() {
            {
            	oneOf(mockFacetedMethod).getFacet(NamedFacet.class);
                will(returnValue(facet));
            }
        });
        assertThat(action.getName(), is(equalTo(name)));
    }

    @Test
    public void testId() {
        assertEquals("reduceheadcount", action.getId());
    }

}
