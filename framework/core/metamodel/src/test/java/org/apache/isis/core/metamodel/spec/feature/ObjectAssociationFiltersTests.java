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

package org.apache.isis.core.metamodel.spec.feature;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.apache.isis.applib.filter.Filter;
import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.facets.hide.HiddenFacet;

@RunWith(JMock.class)
public class ObjectAssociationFiltersTests {

    private final Mockery mockery = new JUnit4Mockery();

    private AuthenticationSession mockSession;
    private ObjectAdapter mockTarget;
    private ObjectAssociation mockAssociation;

    @Before
    public void setUp() throws Exception {
        mockSession = mockery.mock(AuthenticationSession.class);
        mockTarget = mockery.mock(ObjectAdapter.class, "target");
        mockAssociation = mockery.mock(ObjectAssociation.class);
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void shouldNotJustCheckIfAssociationContainsHiddenFacet() {
        mockery.checking(new Expectations() {
            {
                never(mockAssociation).containsFacet(HiddenFacet.class);
                allowing(mockAssociation).isVisible(with(any(AuthenticationSession.class)), with(any(ObjectAdapter.class)));
            }
        });
        final Filter<ObjectAssociation> filter = ObjectAssociationFilters.dynamicallyVisible(mockSession, mockTarget);
        filter.accept(mockAssociation);
    }

}
