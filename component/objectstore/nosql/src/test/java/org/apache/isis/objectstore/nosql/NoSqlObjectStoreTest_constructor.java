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

package org.apache.isis.objectstore.nosql;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Map;

import com.google.common.collect.Maps;

import org.jmock.Expectations;
import org.jmock.Sequence;
import org.jmock.auto.Mock;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import org.apache.isis.core.runtime.system.persistence.OidGenerator;
import org.apache.isis.core.unittestsupport.jmocking.JUnitRuleMockery2;
import org.apache.isis.core.unittestsupport.jmocking.JUnitRuleMockery2.Mode;
import org.apache.isis.objectstore.nosql.db.NoSqlDataDatabase;
import org.apache.isis.objectstore.nosql.encryption.DataEncryption;
import org.apache.isis.objectstore.nosql.versions.VersionCreator;

public class NoSqlObjectStoreTest_constructor {

    @Rule
    public JUnitRuleMockery2 context = JUnitRuleMockery2.createFor(Mode.INTERFACES_AND_CLASSES);
    
    @Mock
    private NoSqlDataDatabase db;
    
    @Mock
    private VersionCreator versionCreator;

    private Map<String, DataEncryption> dataEncrypter = Maps.newHashMap();

    private NoSqlObjectStore store;

    @Before
    public void setup() {
        org.apache.log4j.Logger.getRootLogger().setLevel(org.apache.log4j.Level.OFF);
    }

    @Test
    public void withFixturesNotInstalled() throws Exception {
        final Sequence constructor = context.sequence("<init>");
        context.checking(new Expectations() {
            {
                one(db).open();
                inSequence(constructor);
                
                one(db).containsData();
                will(returnValue(false));
                inSequence(constructor);
                
                one(db).close();
                inSequence(constructor);
            }
        });
        store = new NoSqlObjectStore(db, new OidGenerator(new NoSqlIdentifierGenerator(db)), versionCreator, null, dataEncrypter);
        assertFalse(store.isFixturesInstalled());
    }

    @Test
    public void withFixturesInstalled() throws Exception {
        final Sequence constructor = context.sequence("<init>");
        context.checking(new Expectations() {
            {
                one(db).open();
                inSequence(constructor);
                
                one(db).containsData();
                will(returnValue(true));
                inSequence(constructor);
                
                one(db).close();
                inSequence(constructor);
            }
        });
        store = new NoSqlObjectStore(db, new OidGenerator(new NoSqlIdentifierGenerator(db)), versionCreator, null, dataEncrypter);
        assertTrue(store.isFixturesInstalled());
    }

}
