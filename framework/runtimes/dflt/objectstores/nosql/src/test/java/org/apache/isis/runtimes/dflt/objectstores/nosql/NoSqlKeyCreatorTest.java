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

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.runtimes.dflt.objectstores.dflt.testsystem.TestProxySystemII;
import org.apache.isis.runtimes.dflt.runtime.persistence.oidgenerator.simple.SerialOid;
import org.apache.isis.runtimes.dflt.runtime.system.context.IsisContext;

public class NoSqlKeyCreatorTest {

    private final int id = 3;
    private final String reference = ExampleReferencePojo.class.getName() + "@" + id;
    private final NoSqlOid oid3 = new NoSqlOid(ExampleReferencePojo.class.getName(), SerialOid.createPersistent(id));
    
    private ObjectSpecification specification;
    
    private NoSqlKeyCreator noSqlKeyCreator;

    @Before
    public void setup() {
        Logger.getRootLogger().setLevel(Level.OFF);
        final TestProxySystemII system = new TestProxySystemII();
        system.init();
        specification = IsisContext.getSpecificationLoader().loadSpecification(ExampleReferencePojo.class);

        noSqlKeyCreator = new NoSqlKeyCreator();
    }

    @Test
    public void oid() throws Exception {
        final NoSqlOid oid = (NoSqlOid) noSqlKeyCreator.oidFromReference(reference);
        assertEquals(oid3.getSerialNo(), oid.getSerialNo());
        assertEquals(oid3.getClassName(), oid.getClassName());
    }

    @Test
    public void specification() throws Exception {
        final ObjectSpecification spec = noSqlKeyCreator.specificationFromReference(reference);
        assertEquals(specification, spec);
    }
}
