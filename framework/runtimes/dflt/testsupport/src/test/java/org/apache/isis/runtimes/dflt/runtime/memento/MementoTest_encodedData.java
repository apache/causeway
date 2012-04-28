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

package org.apache.isis.runtimes.dflt.runtime.memento;

import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import org.apache.isis.core.commons.encoding.DataOutputStreamExtended;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.testsupport.jmock.JUnitRuleMockery2;
import org.apache.isis.core.testsupport.jmock.JUnitRuleMockery2.Mode;
import org.apache.isis.runtimes.dflt.runtime.system.context.IsisContext;
import org.apache.isis.runtimes.dflt.testsupport.IsisSystemWithFixtures;

public class MementoTest_encodedData {

    @Rule
    public JUnitRuleMockery2 context = JUnitRuleMockery2.createFor(Mode.INTERFACES_AND_CLASSES);

    @Rule
    public IsisSystemWithFixtures iswf = IsisSystemWithFixtures.builder().build();

    private ObjectAdapter rootAdapter;

    private Memento memento;

    @Mock
    private DataOutputStreamExtended mockOutputImpl;

    @Before
    public void setUpSystem() throws Exception {
        iswf.fixtures.epv1.setName("Fred");
        iswf.fixtures.epv2.setName("Harry");
        
        iswf.fixtures.epr1.setReference(iswf.fixtures.epv1);
        
        iswf.fixtures.epc1.getHomogeneousCollection().add(iswf.fixtures.epv1);
        iswf.fixtures.epc1.getHomogeneousCollection().add(iswf.fixtures.epv2);
        
        iswf.fixtures.epc1.getHeterogeneousCollection().add(iswf.fixtures.epv1);
        iswf.fixtures.epc1.getHeterogeneousCollection().add(iswf.fixtures.epr1);

        
        rootAdapter = IsisContext.getPersistenceSession().getAdapterManager().adapterFor(iswf.fixtures.epv1);

        memento = new Memento(rootAdapter);
    }

    
    @Test
    public void encodedData() throws Exception {
        context.checking(new Expectations() {
            {
                one(mockOutputImpl).writeEncodable(memento.getData());
            }
        });
        memento.encodedData(mockOutputImpl);
    }

}
