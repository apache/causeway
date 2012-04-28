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
import org.apache.isis.core.testsupport.jmock.InjectIntoJMockAction;
import org.apache.isis.core.testsupport.jmock.JUnitRuleMockery2;
import org.apache.isis.core.testsupport.jmock.JUnitRuleMockery2.Mode;
import org.apache.isis.runtimes.dflt.objectstores.dflt.InMemoryPersistenceMechanismInstaller;
import org.apache.isis.runtimes.dflt.runtime.system.persistence.OidGenerator;
import org.apache.isis.runtimes.dflt.testsupport.IsisSystemWithFixtures;
import org.apache.isis.runtimes.dflt.testsupport.IsisSystemWithFixtures.Fixtures.Initialization;
import org.apache.isis.tck.dom.eg.ExamplePojoRepository;
import org.apache.isis.tck.dom.eg.ExamplePojoWithValues;
import org.apache.isis.tck.dom.eg.TestPojoRepository;

public class PersistorSessionHydratorTest {

    @Rule
    public JUnitRuleMockery2 context = JUnitRuleMockery2.createFor(Mode.INTERFACES_AND_CLASSES);

    private RootOid testPojoRepoRootOid = RootOidDefault.create(TestPojoRepository.class.getName(), "1");
    private RootOid examplePojoRepoRootOid = RootOidDefault.create(ExamplePojoRepository.class.getName(), "1");
    private RootOid epvTransientOid = RootOidDefault.createTransient("EPV|-999");
    private RootOid epvPersistentOid = RootOidDefault.create("EPV|1");
    
    private OidGenerator mockOidGenerator = context.mock(OidGenerator.class); // a bit nasty...
    {
        context.checking(new Expectations() {
            {
                allowing(mockOidGenerator).injectInto(with(any(Object.class)));
                will(InjectIntoJMockAction.injectInto());
                
                allowing(mockOidGenerator).open();
                allowing(mockOidGenerator).close();
                
                allowing(mockOidGenerator).createTransientOid(with(a(TestPojoRepository.class)));
                will(returnValue(testPojoRepoRootOid));

                allowing(mockOidGenerator).createTransientOid(with(an(ExamplePojoRepository.class)));
                will(returnValue(examplePojoRepoRootOid));
                
                allowing(mockOidGenerator).createTransientOid(with(an(ExamplePojoWithValues.class)));
                will(returnValue(epvTransientOid));
                
                allowing(mockOidGenerator).asPersistent(epvTransientOid);
                will(returnValue(epvPersistentOid));
            }
        });
    }
    

    @Rule
    public IsisSystemWithFixtures iswf = IsisSystemWithFixtures.builder()
        .with(Initialization.NO_INIT)
        .with(new InMemoryPersistenceMechanismInstaller() {
            protected OidGenerator createOidGenerator(IsisConfiguration configuration) {
                return mockOidGenerator;
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
        final RootOidDefault oid = RootOidDefault.create("EPV|1");
        final ObjectAdapter adapter = iswf.recreateAdapter(oid);
        
        // then
        assertEquals(oid, adapter.getOid());
        assertEquals(ResolveState.GHOST, adapter.getResolveState());

        final ExamplePojoWithValues epv = (ExamplePojoWithValues)adapter.getObject();
        assertEquals("Fred", epv.getName());
        assertNotNull(adapter.getVersion());
    }
}
