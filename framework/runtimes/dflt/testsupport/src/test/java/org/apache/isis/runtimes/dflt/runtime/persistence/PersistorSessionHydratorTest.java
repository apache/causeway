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

package org.apache.isis.runtimes.dflt.runtime.persistence;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.junit.Rule;
import org.junit.Test;

import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.ResolveState;
import org.apache.isis.core.metamodel.adapter.oid.RootOid;
import org.apache.isis.core.metamodel.adapter.oid.RootOidDefault;
import org.apache.isis.core.metamodel.spec.ObjectSpecId;
import org.apache.isis.core.testsupport.jmock.JUnitRuleMockery2;
import org.apache.isis.core.testsupport.jmock.JUnitRuleMockery2.Mode;
import org.apache.isis.runtimes.dflt.objectstores.dflt.InMemoryPersistenceMechanismInstaller;
import org.apache.isis.runtimes.dflt.runtime.system.persistence.IdentifierGenerator;
import org.apache.isis.runtimes.dflt.testsupport.IsisSystemWithFixtures;
import org.apache.isis.runtimes.dflt.testsupport.IsisSystemWithFixtures.Fixtures.Initialization;
import org.apache.isis.tck.dom.eg.ExamplePojoRepository;
import org.apache.isis.tck.dom.eg.ExamplePojoWithValues;
import org.apache.isis.tck.dom.eg.TestPojoRepository;

public class PersistorSessionHydratorTest {

    @Rule
    public JUnitRuleMockery2 context = JUnitRuleMockery2.createFor(Mode.INTERFACES_AND_CLASSES);

    private RootOid epvTransientOid = RootOidDefault.deString("!EPV:-999");

    private IdentifierGenerator mockIdentifierGenerator = context.mock(IdentifierGenerator.class);
    {
        context.checking(new Expectations() {
            {
                allowing(mockIdentifierGenerator).createTransientIdentifierFor(with(equalTo(ObjectSpecId.of("TestPojoRepository"))), with(any(Object.class)));
                will(returnValue("1"));

                allowing(mockIdentifierGenerator).createPersistentIdentifierFor(with(equalTo(ObjectSpecId.of("TestPojoRepository"))), with(an(TestPojoRepository.class)), with(any(RootOid.class)));
                will(returnValue("1"));

                allowing(mockIdentifierGenerator).createTransientIdentifierFor(with(equalTo(ObjectSpecId.of("ExamplePojoRepository"))), with(an(ExamplePojoRepository.class)));
                will(returnValue("1"));
                allowing(mockIdentifierGenerator).createPersistentIdentifierFor(with(equalTo(ObjectSpecId.of("ExamplePojoRepository"))), with(an(ExamplePojoRepository.class)), with(any(RootOid.class)));
                will(returnValue("1"));
                
                allowing(mockIdentifierGenerator).createTransientIdentifierFor(with(equalTo(ObjectSpecId.of("EPV"))), with(an(ExamplePojoWithValues.class)));
                will(returnValue("-999"));
                
                allowing(mockIdentifierGenerator).createPersistentIdentifierFor(with(equalTo(ObjectSpecId.of("EPV"))), with(an(ExamplePojoWithValues.class)), with(any(RootOid.class)));
                will(returnValue("1"));
            }
        });
    }
    
    @Rule
    public IsisSystemWithFixtures iswf = IsisSystemWithFixtures.builder()
        .with(Initialization.NO_INIT)
        .with(new InMemoryPersistenceMechanismInstaller() {
            protected IdentifierGenerator createIdentifierGenerator(IsisConfiguration configuration) {
                return mockIdentifierGenerator;
            };
        })
        .build();

    
    @Test
    public void adaptorFor_whenTransient() {
        // given
        iswf.fixtures.epv1 = iswf.container.newTransientInstance(ExamplePojoWithValues.class);
        
        // when
        final ObjectAdapter adapter = iswf.adapterFor(iswf.fixtures.epv1);

        // then
        assertEquals(epvTransientOid, adapter.getOid());
        assertEquals(iswf.fixtures.epv1, adapter.getObject());
        assertEquals(ResolveState.TRANSIENT, adapter.getResolveState());
        assertEquals(null, adapter.getVersion());
    }

    @Test
    public void recreateAdapter_whenPersistent() throws Exception {
        
        // given persisted object
        iswf.fixtures.epv1 = iswf.container.newTransientInstance(ExamplePojoWithValues.class);
        iswf.fixtures.epv1.setName("Fred");
        iswf.persist(iswf.fixtures.epv1);
        iswf.tearDownSystem();
        iswf.setUpSystem();
        
        // when
        final RootOidDefault oid = RootOidDefault.deString("EPV:1");
        final ObjectAdapter adapter = iswf.recreateAdapter(oid);
        
        // then
        assertEquals(oid, adapter.getOid());
        assertEquals(ResolveState.GHOST, adapter.getResolveState());

        final ExamplePojoWithValues epv = (ExamplePojoWithValues)adapter.getObject();
        assertEquals("Fred", epv.getName());
        assertNotNull(adapter.getVersion());
    }
}
