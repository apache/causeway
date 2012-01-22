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

package org.apache.isis.runtimes.dflt.objectstores.nosql;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.commons.exceptions.UnexpectedCallException;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.ResolveState;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.runtimes.dflt.objectstores.dflt.testsystem.TestProxySystemII;
import org.apache.isis.runtimes.dflt.runtime.persistence.objectstore.transaction.PersistenceCommand;
import org.apache.isis.runtimes.dflt.runtime.persistence.oidgenerator.simple.SerialOid;
import org.apache.isis.runtimes.dflt.runtime.system.context.IsisContext;
import org.apache.isis.runtimes.dflt.runtime.system.persistence.PersistenceQuery;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.junit.Before;
import org.junit.Test;

public class NoSqlObjectStoreTest {

    private Mockery context;
    private NoSqlDataDatabase db;
    private ObjectSpecification specification;
    private ObjectAdapter object;
    private NoSqlObjectStore store;
    private KeyCreator keyCreator;
    private VersionCreator versionCreator;

    @Before
    public void setup() {
        Logger.getRootLogger().setLevel(Level.OFF);
        final TestProxySystemII system = new TestProxySystemII();
        system.init();

        specification = IsisContext.getSpecificationLoader().loadSpecification(ExampleReferencePojo.class);
        object = IsisContext.getPersistenceSession().createInstance(specification);
        ((SerialOid) object.getOid()).setId(3);
        object.getOid().makePersistent();

        context = new Mockery();
        db = context.mock(NoSqlDataDatabase.class);
        context.checking(new Expectations() {
            {
                one(db).open();
                one(db).containsData();
                will(returnValue(false));
                one(db).close();
            }
        });
        keyCreator = context.mock(KeyCreator.class);
        versionCreator = context.mock(VersionCreator.class);

        final Map<String, DataEncryption> dataEncrypter = new HashMap<String, DataEncryption>();
        final DataEncryption dataEncrypter1 = new DataEncryption() {
            @Override
            public String getType() {
                return "etc";
            }

            @Override
            public void init(final IsisConfiguration configuration) {
            }

            @Override
            public String encrypt(final String plainText) {
                throw new UnexpectedCallException();
            }

            @Override
            public String decrypt(final String encryptedText) {
                return encryptedText.substring(3);
            }
        };
        dataEncrypter.put(dataEncrypter1.getType(), dataEncrypter1);

        store = new NoSqlObjectStore(db, new NoSqlOidGenerator(db), keyCreator, versionCreator, null, dataEncrypter);
    }

    @Test
    public void open() throws Exception {
        context.checking(new Expectations() {
            {
                one(db).open();
            }
        });
        store.open();
    }

    @Test
    public void close() throws Exception {
        context.checking(new Expectations() {
            {
                one(db).close();
            }
        });
        store.close();
    }

    @Test
    public void isFixtureInstalledOnFirstStartup() throws Exception {
        assertFalse(store.isFixturesInstalled());
    }

    @Test
    public void isFixtureInstalledOnSubsequentStartup() throws Exception {
        context.checking(new Expectations() {
            {
                one(db).open();
                one(db).containsData();
                will(returnValue(true));
                one(db).close();
            }
        });
        store = new NoSqlObjectStore(db, new NoSqlOidGenerator(db), null, null, null, new HashMap<String, DataEncryption>());
        assertTrue(store.isFixturesInstalled());
    }

    @Test
    public void registerService() throws Exception {
        context.checking(new Expectations() {
            {
                one(keyCreator).key(SerialOid.createPersistent(4));
                will(returnValue("4"));
                one(db).addService("service", "4");
            }
        });
        store.registerService("service", SerialOid.createPersistent(4));
    }

    @Test
    public void oidForService() throws Exception {
        context.checking(new Expectations() {
            {
                one(db).getService("service");
                will(returnValue("4"));
                one(keyCreator).oid("4");
            }
        });
        store.getOidForService("service");
    }

    @Test
    public void hasInstances() throws Exception {
        context.checking(new Expectations() {
            {
                one(db).hasInstances(specification.getFullIdentifier());
                will(returnValue(true));
            }
        });
        store.hasInstances(specification);
    }

    @Test
    public void execute() throws Exception {
        final PersistenceCommand command = context.mock(PersistenceCommand.class);
        final List<PersistenceCommand> commands = new ArrayList<PersistenceCommand>();
        commands.add(command);

        context.checking(new Expectations() {
            {
                one(command).execute(null);
                one(db).write(commands);
            }
        });

        store.execute(commands);
    }

    @Test
    public void instances() throws Exception {
        final PersistenceQuery persistenceQuery = context.mock(PersistenceQuery.class);
        context.checking(new Expectations() {
            {
                one(persistenceQuery).getSpecification();
                will(returnValue(specification));

                one(db).instancesOf(specification.getFullIdentifier());
                will(returnIterator());
            }
        });

        store.getInstances(persistenceQuery);
    }

    @Test
    public void resolve() throws Exception {
        specification = IsisContext.getSpecificationLoader().loadSpecification(ExamplePojo.class);

        context.checking(new Expectations() {
            {
                one(keyCreator).key(SerialOid.createPersistent(3));
                will(returnValue("3"));
                one(db).getInstance("3", specification.getFullIdentifier());
            }
        });
        object = IsisContext.getPersistenceSession().recreateAdapter(SerialOid.createPersistent(3), specification);
        assertEquals(ResolveState.GHOST, object.getResolveState());
        store.resolveImmediately(object);
        assertEquals(ResolveState.RESOLVED, object.getResolveState());
    }
}
