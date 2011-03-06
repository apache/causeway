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

import java.util.Date;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.junit.Before;
import org.junit.Test;
import org.apache.isis.runtimes.dflt.objectstores.nosql.DestroyObjectCommandImplementation;
import org.apache.isis.runtimes.dflt.objectstores.nosql.NoSqlCommandContext;
import org.apache.isis.runtimes.dflt.objectstores.nosql.SerialKeyCreator;
import org.apache.isis.runtimes.dflt.objectstores.nosql.SerialNumberVersionCreator;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.version.SerialNumberVersion;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.runtimes.dflt.runtime.context.IsisContext;
import org.apache.isis.runtimes.dflt.runtime.persistence.oidgenerator.simple.SerialOid;
import org.apache.isis.runtimes.dflt.objectstores.dflt.testsystem.TestProxySystemII;


public class DestroyObjectCommandImplementationTest {

    @Before
    public void setup() {
        Logger.getRootLogger().setLevel(Level.OFF);
        TestProxySystemII system = new TestProxySystemII();
        system.init();
    }

    @Test
    public void testname() throws Exception {
        final ObjectSpecification specification = IsisContext.getSpecificationLoader().loadSpecification(
                ExampleReferencePojo.class);
        ObjectAdapter object = IsisContext.getPersistenceSession().createInstance(specification);
        object.setOptimisticLock(new SerialNumberVersion(3, "username", new Date(1000)));
        long id = ((SerialOid) object.getOid()).getSerialNo();
        final String key = Long.toHexString(id);

        Mockery context = new Mockery();
        final NoSqlCommandContext commandContext = context.mock(NoSqlCommandContext.class);
        context.checking(new Expectations() {
            {
                one(commandContext).delete(specification.getFullIdentifier(), key, "3");
            }
        });

        DestroyObjectCommandImplementation command = new DestroyObjectCommandImplementation(new SerialKeyCreator(),
                new SerialNumberVersionCreator(), object);
        command.execute(commandContext);
    }
}

