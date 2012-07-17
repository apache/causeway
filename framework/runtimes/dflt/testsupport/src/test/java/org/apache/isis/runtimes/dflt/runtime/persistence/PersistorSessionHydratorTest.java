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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.jmock.Expectations;
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
import org.apache.isis.tck.dom.refs.ParentEntityRepository;
import org.apache.isis.tck.dom.refs.SimpleEntity;

public class PersistorSessionHydratorTest {

    @Rule
    public JUnitRuleMockery2 context = JUnitRuleMockery2.createFor(Mode.INTERFACES_AND_CLASSES);

    private RootOid epvTransientOid = RootOidDefault.deString("!SMPL:-999");

    private IdentifierGenerator mockIdentifierGenerator = context.mock(IdentifierGenerator.class);
    {
        context.checking(new Expectations() {
            {
                allowing(mockIdentifierGenerator).createTransientIdentifierFor(with(equalTo(ObjectSpecId.of("ParentEntities"))), with(an(ParentEntityRepository.class)));
                will(returnValue("1"));
                allowing(mockIdentifierGenerator).createPersistentIdentifierFor(with(equalTo(ObjectSpecId.of("ParentEntities"))), with(an(ParentEntityRepository.class)), with(any(RootOid.class)));
                will(returnValue("1"));
                
                allowing(mockIdentifierGenerator).createTransientIdentifierFor(with(equalTo(ObjectSpecId.of("SMPL"))), with(an(SimpleEntity.class)));
                will(returnValue("-999"));
                
                allowing(mockIdentifierGenerator).createPersistentIdentifierFor(with(equalTo(ObjectSpecId.of("SMPL"))), with(an(SimpleEntity.class)), with(any(RootOid.class)));
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
        iswf.fixtures.smpl1 = iswf.container.newTransientInstance(SimpleEntity.class);
        
        // when
        final ObjectAdapter adapter = iswf.adapterFor(iswf.fixtures.smpl1);

        // then
        assertEquals(epvTransientOid, adapter.getOid());
        assertEquals(iswf.fixtures.smpl1, adapter.getObject());
        assertEquals(ResolveState.TRANSIENT, adapter.getResolveState());
        assertEquals(null, adapter.getVersion());
    }

    @Test
    public void recreateAdapter_whenPersistent() throws Exception {
        
        // given persisted object
        iswf.fixtures.smpl1 = iswf.container.newTransientInstance(SimpleEntity.class);
        iswf.fixtures.smpl1.setName("Fred");
        iswf.persist(iswf.fixtures.smpl1);
        iswf.tearDownSystem();
        iswf.setUpSystem();
        
        // when
        final RootOidDefault oid = RootOidDefault.deString("SMPL:1");
        final ObjectAdapter adapter = iswf.recreateAdapter(oid);
        
        // then
        assertEquals(oid, adapter.getOid());
        assertEquals(ResolveState.GHOST, adapter.getResolveState());

        final SimpleEntity epv = (SimpleEntity)adapter.getObject();
        assertEquals("Fred", epv.getName());
        assertNotNull(adapter.getVersion());
    }
}
