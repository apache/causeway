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


package org.apache.isis.core.metamodel.examples.facets.jsr303;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import org.apache.isis.applib.Identifier;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.examples.facets.jsr303.Jsr303PropertyValidationFacet;
import org.apache.isis.core.metamodel.interactions.PropertyModifyContext;
import org.apache.isis.core.metamodel.spec.identifier.Identified;


@RunWith(JMock.class)
public class Jsr303FacetValidatingInteraction {

    private final Mockery mockery = new JUnit4Mockery() {
        {
            setImposteriser(ClassImposteriser.INSTANCE);
        }
    };

    private Jsr303PropertyValidationFacet facet;
    private Identified mockHolder;

    private PropertyModifyContext mockContext;
    private ObjectAdapter mockTargetObjectAdapter;
    private ObjectAdapter mockProposedObjectAdapter;

    private DomainObjectWithCustomValidation domainObjectWithCustomValidation;

    private DomainObjectWithBuiltInValidation domainObjectWithBuiltInValidation;
    
    @Before
    public void setUp() throws Exception {
        mockHolder = mockery.mock(Identified.class);
        facet = new Jsr303PropertyValidationFacet(mockHolder);
        mockContext = mockery.mock(PropertyModifyContext.class);
        mockTargetObjectAdapter = mockery.mock(ObjectAdapter.class, "target");
        mockProposedObjectAdapter = mockery.mock(ObjectAdapter.class, "proposed");

        domainObjectWithBuiltInValidation = new DomainObjectWithBuiltInValidation();
        domainObjectWithCustomValidation = new DomainObjectWithCustomValidation();

        mockery.checking(new Expectations() {
            {
            	oneOf(mockHolder).getIdentifier();
                will(returnValue(Identifier.propertyOrCollectionIdentifier(DomainObjectWithBuiltInValidation.class, "serialNumber")));

                oneOf(mockContext).getTarget();
                will(returnValue(mockTargetObjectAdapter));

                oneOf(mockContext).getProposed();
                will(returnValue(mockProposedObjectAdapter));
            }
        });
    }

    @After
    public void tearDown() throws Exception {
        mockHolder = null;
        facet = null;
        mockContext = null;
    }

    @Test
    public void invalidatesWhenBuiltInConstraintVetoes() {
        mockery.checking(new Expectations() {
            {
            	oneOf(mockTargetObjectAdapter).getObject();
                will(returnValue(domainObjectWithBuiltInValidation));

                oneOf(mockProposedObjectAdapter).getObject();
                will(returnValue("NONSENSE"));
            }
        });

        final String reason = facet.invalidates(mockContext);
        assertThat(reason, is(not(nullValue())));
        assertThat(reason, is("serialNumber is invalid: must match ....-....-...."));
    }

    @Test
    public void validatesWhenBuiltInConstraintIsMet() {
        mockery.checking(new Expectations() {
            {
            	oneOf(mockTargetObjectAdapter).getObject();
                will(returnValue(domainObjectWithBuiltInValidation));

                oneOf(mockProposedObjectAdapter).getObject();
                will(returnValue("1234-5678-9012"));
            }
        });

        final String reason = facet.invalidates(mockContext);
        assertThat(reason, is(nullValue()));
    }

    @Test
    public void invalidatesWhenFailsCustomConstraint() {
        mockery.checking(new Expectations() {
            {
            	oneOf(mockTargetObjectAdapter).getObject();
                will(returnValue(domainObjectWithCustomValidation));

                oneOf(mockProposedObjectAdapter).getObject();
                will(returnValue("NONSENSE"));
            }
        });

        final String reason = facet.invalidates(mockContext);
        assertThat(reason, is(not(nullValue())));
        assertThat(reason, is("serialNumber is invalid: must match ....-....-...."));
    }

    @Test
    public void validatesWhenFailsCustomConstraint() {
        mockery.checking(new Expectations() {
            {
            	oneOf(mockTargetObjectAdapter).getObject();
                will(returnValue(domainObjectWithCustomValidation));

                oneOf(mockProposedObjectAdapter).getObject();
                will(returnValue("1234-5678-9012"));
            }
        });

        final String reason = facet.invalidates(mockContext);
        assertThat(reason, is(nullValue()));
    }

}
