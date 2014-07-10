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

import static org.hamcrest.CoreMatchers.equalTo;

import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.junit.Rule;
import org.junit.Test;

import org.apache.isis.applib.annotation.Where;
import org.apache.isis.applib.filter.Filter;
import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.facets.all.hide.HiddenFacet;
import org.apache.isis.core.unittestsupport.jmocking.JUnitRuleMockery2;
import org.apache.isis.core.unittestsupport.jmocking.JUnitRuleMockery2.Mode;

public class ObjectAssociationFiltersTests {

    @Rule
    public JUnitRuleMockery2 context = JUnitRuleMockery2.createFor(Mode.INTERFACES_AND_CLASSES);

    @Mock
    private AuthenticationSession mockSession;
    @Mock
    private ObjectAdapter mockTarget;
    @Mock
    private ObjectAssociation mockAssociation;

    @Test
    public void shouldNotJustCheckIfAssociationContainsHiddenFacet() {
        context.checking(new Expectations() {
            {
                never(mockAssociation).containsFacet(HiddenFacet.class);
                allowing(mockAssociation).isVisible(with(any(AuthenticationSession.class)), with(any(ObjectAdapter.class)), with(equalTo(Where.ANYWHERE)));
            }
        });
        final Filter<ObjectAssociation> filter = ObjectAssociation.Filters.dynamicallyVisible(mockSession, mockTarget, Where.ANYWHERE);
        filter.accept(mockAssociation);
    }

}
