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

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import org.apache.isis.core.metamodel.adapter.oid.RootOidDefault;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.SpecificationLoader;
import org.apache.isis.core.testsupport.jmock.JUnitRuleMockery2;
import org.apache.isis.core.testsupport.jmock.JUnitRuleMockery2.Mode;
import org.apache.isis.runtimes.dflt.objectstores.nosql.db.NoSqlDataDatabase;

public class NoSqlOidGeneratorTest {

    public static class ExamplePojo {}
    
    @Rule
    public JUnitRuleMockery2 context = JUnitRuleMockery2.createFor(Mode.INTERFACES_ONLY);

    @Mock
    private NoSqlDataDatabase db;
    @Mock
    private SpecificationLoader mockSpecificationLoader;
    @Mock
    private ObjectSpecification mockSpecification;
    
    private RootOidDefault oid;
    private NoSqlOidGenerator oidGenerator;


    
    @Before
    public void setup() {
        Logger.getRootLogger().setLevel(Level.OFF);

        context.checking(new Expectations() {{
           allowing(mockSpecificationLoader).loadSpecification(with(ExamplePojo.class));
           will(returnValue(mockSpecification));

           allowing(mockSpecification).getCorrespondingClass();
           will(returnValue(ExamplePojo.class));
        }});
        
        oidGenerator = new NoSqlOidGenerator(db, -999, 4) {
            @Override
            protected SpecificationLoader getSpecificationLoader() {
                return mockSpecificationLoader;
            }
        };
        oid = (RootOidDefault) oidGenerator.createTransientOid(new ExamplePojo());
    }

    @Test
    public void transientOid() throws Exception {
        
        // TODO: REVIEW: how did this ever call db.nextSerialNumberBatch?
        
        assertEquals(-999, oid.getIdentifier());
        assertTrue(oid.isTransient());
        oid = (RootOidDefault) oidGenerator.createTransientOid(new ExamplePojo());
        assertEquals(-998, oid.getIdentifier());
    }

    @Test
    public void batchCreated() throws Exception {
          context.checking(new Expectations() {
              {
                  one(db).nextSerialNumberBatch("_id", 4);
                  will(returnValue(1L));
              }
          });

        oidGenerator.asPersistent(oid);
        assertFalse(oid.isTransient());
        assertEquals(1, oid.getIdentifier());
        context.assertIsSatisfied();
    }

    @Test
    public void batchReused() throws Exception {
        context.checking(new Expectations() {
            {
                one(db).nextSerialNumberBatch("_id", 4);
                will(returnValue(1L));
            }
        });

        oidGenerator.asPersistent(oid);
        oid = (RootOidDefault) oidGenerator.createTransientOid(new ExamplePojo());
        oidGenerator.asPersistent(oid);
        assertFalse(oid.isTransient());
        assertEquals(2, oid.getIdentifier());
        context.assertIsSatisfied();
    }

    @Test
    public void secondbatchCreated() throws Exception {
        context.checking(new Expectations() {
            {
                one(db).nextSerialNumberBatch("_id", 4);
                will(returnValue(1L));
            }
        });

        oidGenerator.asPersistent(oid);
        oidGenerator.asPersistent(oid = (RootOidDefault) oidGenerator.createTransientOid(new ExamplePojo()));
        oidGenerator.asPersistent(oid = (RootOidDefault) oidGenerator.createTransientOid(new ExamplePojo()));
        assertEquals(3, oid.getIdentifier());
        oidGenerator.asPersistent(oid = (RootOidDefault) oidGenerator.createTransientOid(new ExamplePojo()));
        assertEquals(4, oid.getIdentifier());

        context.checking(new Expectations() {
            {
                one(db).nextSerialNumberBatch("_id", 4);
                will(returnValue(5L));
            }
        });
        oidGenerator.asPersistent(oid = (RootOidDefault) oidGenerator.createTransientOid(new ExamplePojo()));
        assertFalse(oid.isTransient());
        assertEquals(5, oid.getIdentifier());
        context.assertIsSatisfied();
    }

}
