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

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.junit.Before;
import org.junit.Test;
import org.apache.isis.runtimes.dflt.objectstores.nosql.NoSqlDataDatabase;
import org.apache.isis.runtimes.dflt.objectstores.nosql.NoSqlOidGenerator;
import org.apache.isis.runtimes.dflt.runtime.persistence.oidgenerator.simple.SerialOid;

import static org.junit.Assert.*;


public class NoSqlOidGeneratorTest {

    private Mockery context;
    private NoSqlDataDatabase db;
    private NoSqlOidGenerator oidGenerator;
    private SerialOid oid;

    @Before
    public void setup() {
        Logger.getRootLogger().setLevel(Level.OFF);

        context = new Mockery();
        db = context.mock(NoSqlDataDatabase.class);
        context.checking(new Expectations() {{
            one(db).nextSerialNumberBatch(4);
           will(returnValue(1L));
        }});
        oidGenerator = new NoSqlOidGenerator(db, -999, 4);
        oid = oidGenerator.createTransientOid(null);
    }

    @Test
    public void transientOid() throws Exception {
        assertEquals(-999, oid.getSerialNo());
        assertTrue(oid.isTransient());
        oid = oidGenerator.createTransientOid(null);
        assertEquals(-998, oid.getSerialNo());
    }
    
    @Test
    public void batchCreated() throws Exception {
        oidGenerator.convertTransientToPersistentOid(oid);
        assertFalse(oid.isTransient());
        assertEquals(1, oid.getSerialNo());
        context.assertIsSatisfied();
    }
    
    @Test
    public void batchReused() throws Exception {
        oidGenerator.convertTransientToPersistentOid(oid);
        oid = oidGenerator.createTransientOid(null);
        oidGenerator.convertTransientToPersistentOid(oid);
        assertFalse(oid.isTransient());
        assertEquals(2, oid.getSerialNo());
        context.assertIsSatisfied();
    }
    
    @Test
    public void secondbatchCreated() throws Exception {
        oidGenerator.convertTransientToPersistentOid(oid);
        oidGenerator.convertTransientToPersistentOid(oid = oidGenerator.createTransientOid(null));
        oidGenerator.convertTransientToPersistentOid(oid = oidGenerator.createTransientOid(null));
        assertEquals(3, oid.getSerialNo());
        oidGenerator.convertTransientToPersistentOid(oid = oidGenerator.createTransientOid(null));
        assertEquals(4, oid.getSerialNo());
        
        context.checking(new Expectations() {{
            one(db).nextSerialNumberBatch(4);
            will(returnValue(5L));        
        }});
        oidGenerator.convertTransientToPersistentOid(oid = oidGenerator.createTransientOid(null));
        assertFalse(oid.isTransient());
        assertEquals(5, oid.getSerialNo());
        context.assertIsSatisfied();
    }

}


