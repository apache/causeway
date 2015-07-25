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

package org.apache.isis.core.integtestsupport.persistence;

import org.jmock.Expectations;
import org.junit.Rule;
import org.junit.Test;
import org.apache.isis.core.integtestsupport.IsisSystemWithFixtures;
import org.apache.isis.core.integtestsupport.IsisSystemWithFixtures.Fixtures.Initialization;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.ResolveState;
import org.apache.isis.core.metamodel.adapter.oid.OidMarshaller;
import org.apache.isis.core.metamodel.adapter.oid.RootOid;
import org.apache.isis.core.metamodel.adapter.oid.RootOidDefault;
import org.apache.isis.core.metamodel.services.container.DomainObjectContainerDefault;
import org.apache.isis.core.metamodel.spec.ObjectSpecId;
import org.apache.isis.core.objectstore.InMemoryPersistenceMechanismInstaller;
import org.apache.isis.core.runtime.system.persistence.IdentifierGenerator;
import org.apache.isis.core.tck.dom.refs.ParentEntityRepository;
import org.apache.isis.core.tck.dom.refs.SimpleEntity;
import org.apache.isis.core.unittestsupport.jmocking.JUnitRuleMockery2;
import org.apache.isis.core.unittestsupport.jmocking.JUnitRuleMockery2.Mode;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class PersistorSessionHydratorTest {

    @Rule
    public JUnitRuleMockery2 context = JUnitRuleMockery2.createFor(Mode.INTERFACES_AND_CLASSES);

    private RootOid epvTransientOid = RootOidDefault.deString("!SMPL:-999", new OidMarshaller());

    private IdentifierGenerator mockIdentifierGenerator = context.mock(IdentifierGenerator.class);
    {
        context.checking(new Expectations() {
            {
                final ObjectSpecId docdSpecId = ObjectSpecId.of(DomainObjectContainerDefault.class.getName());
                allowing(mockIdentifierGenerator).createTransientIdentifierFor(with(equalTo(docdSpecId)), with(an(DomainObjectContainerDefault.class)));
                will(returnValue("1"));
                allowing(mockIdentifierGenerator).createPersistentIdentifierFor(with(equalTo(docdSpecId)), with(an(DomainObjectContainerDefault.class)), with(any(RootOid.class)));
                will(returnValue("1"));

                final ObjectSpecId peSpecId = ObjectSpecId.of("ParentEntities");
                allowing(mockIdentifierGenerator).createTransientIdentifierFor(with(equalTo(peSpecId)), with(an(ParentEntityRepository.class)));
                will(returnValue("1"));
                allowing(mockIdentifierGenerator).createPersistentIdentifierFor(with(equalTo(peSpecId)), with(an(ParentEntityRepository.class)), with(any(RootOid.class)));
                will(returnValue("1"));

                final ObjectSpecId smplSpecId = ObjectSpecId.of("SMPL");
                allowing(mockIdentifierGenerator).createTransientIdentifierFor(with(equalTo(smplSpecId)), with(an(SimpleEntity.class)));
                will(returnValue("-999"));
                
                allowing(mockIdentifierGenerator).createPersistentIdentifierFor(with(equalTo(smplSpecId)), with(an(SimpleEntity.class)), with(any(RootOid.class)));
                will(returnValue("1"));
            }
        });
    }
    
    @Rule
    public IsisSystemWithFixtures iswf = IsisSystemWithFixtures.builder()
        .with(Initialization.NO_INIT)
        .with(new InMemoryPersistenceMechanismInstaller())
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
        final RootOidDefault oid = RootOidDefault.deString("SMPL:1", new OidMarshaller());
        final ObjectAdapter adapter = iswf.recreateAdapter(oid);
        
        // then
        assertEquals(oid, adapter.getOid());
        assertEquals(ResolveState.GHOST, adapter.getResolveState());

        final SimpleEntity epv = (SimpleEntity)adapter.getObject();
        assertEquals("Fred", epv.getName());
        assertNotNull(adapter.getVersion());
    }
}
