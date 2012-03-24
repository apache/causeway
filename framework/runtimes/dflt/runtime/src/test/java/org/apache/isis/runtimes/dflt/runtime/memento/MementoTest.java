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

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertThat;

import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.oid.OidMatchers;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.SpecificationLoader;
import org.apache.isis.core.testsupport.jmock.JUnitRuleMockery2;
import org.apache.isis.core.testsupport.jmock.JUnitRuleMockery2.Mode;
import org.apache.isis.runtimes.dflt.runtime.persistence.adapterfactory.pojo.PojoAdapter;
import org.apache.isis.runtimes.dflt.runtime.persistence.objectstore.transaction.PojoAdapterBuilder;
import org.apache.isis.runtimes.dflt.runtime.persistence.objectstore.transaction.PojoAdapterBuilder.Persistence;
import org.apache.isis.runtimes.dflt.runtime.persistence.oidgenerator.serial.RootOidDefault;
import org.apache.isis.runtimes.dflt.runtime.system.persistence.PersistenceSession;
import org.apache.isis.runtimes.embedded.EmbeddedContext;
import org.apache.isis.runtimes.embedded.IsisMetaModel;
import org.apache.isis.runtimes.embedded.PersistenceState;

public class MementoTest {

    @Rule
    public JUnitRuleMockery2 context = JUnitRuleMockery2.createFor(Mode.INTERFACES_AND_CLASSES);

    @Mock
    private EmbeddedContext mockEmbeddedContext;
    @Mock
    private PersistenceSession mockPersistenceSession;
    
    private ObjectAdapter originalAdapter;
    private ObjectAdapter returnedAdapter;

    private Memento memento;

    private IsisMetaModel isisMetaModel;

    private Customer transientPojo;


    public static class Customer {
        // {{ FirstName (property)
        private String firstName;

        @MemberOrder(sequence = "1")
        public String getFirstName() {
            return firstName;
        }

        public void setFirstName(final String firstName) {
            this.firstName = firstName;
        }
        // }}
    }
    public static class CustomerRepository {
        public Customer aCustomer() { return null; }
    }

    @Before
    public void setUpSystem() throws Exception {
    
        isisMetaModel = new IsisMetaModel(mockEmbeddedContext, new CustomerRepository());
        isisMetaModel.init();
        
        transientPojo = new Customer();
        transientPojo.setFirstName("Fred");
        
        context.checking(new Expectations() {
            {
                allowing(mockEmbeddedContext).getPersistenceState(with("Fred"));
                will(returnValue(PersistenceState.STANDALONE));
            }
        });

        originalAdapter = PojoAdapterBuilder.create().withPojo(transientPojo).with(Persistence.TRANSIENT).with(isisMetaModel.getSpecificationLoader()) .build();
    }

    @Test
    public void testDifferentAdapterReturned() throws Exception {
        
        memento = createMementoFor(originalAdapter);
        final Customer recreatedPojo = expectRecreateAdapter();
        
        returnedAdapter = memento.recreateObject();

        assertNotSame(originalAdapter, returnedAdapter);
        assertEquals(transientPojo.getFirstName(), recreatedPojo.getFirstName());
    }

    @Test
    public void testHaveEqualOids() throws Exception {

        memento = createMementoFor(originalAdapter);
        expectRecreateAdapter();
        returnedAdapter = memento.recreateObject();

        assertEquals(originalAdapter.getOid(), returnedAdapter.getOid());
    }

    @Test
    public void testHaveSameSpecification() throws Exception {
        
        memento = createMementoFor(originalAdapter);
        expectRecreateAdapter();
        returnedAdapter = memento.recreateObject();

        assertEquals(originalAdapter.getSpecification(), returnedAdapter.getSpecification());
    }
    
    private Memento createMementoFor(final ObjectAdapter adapter) {
        return new Memento(adapter) {
            private static final long serialVersionUID = 1L;

            @Override
            protected SpecificationLoader getSpecificationLoader() {
                return isisMetaModel.getSpecificationLoader();
            }
            
            @Override
            protected PersistenceSession getPersistenceSession() {
                return mockPersistenceSession;
            }
        };
    }

    private Customer expectRecreateAdapter() {
        final ObjectSpecification customerSpec = isisMetaModel.getSpecificationLoader().loadSpecification(Customer.class);
        final Customer recreatedPojo = new Customer();
        final PojoAdapter recreatedAdapter = PojoAdapterBuilder.create().withPojo(recreatedPojo).with(Persistence.TRANSIENT).with(isisMetaModel.getSpecificationLoader()).build();
        context.checking(new Expectations() {
            {
                one(mockPersistenceSession).recreateAdapter(with(OidMatchers.matching("CUS", "1")), with(equalTo(customerSpec)));
                will(returnValue(recreatedAdapter));
            }
        });
        return recreatedPojo;
    }



}
