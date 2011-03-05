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


package org.apache.isis.alternatives.objectstore.nosql;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.apache.isis.alternatives.objectstore.nosql.SerialKeyCreator;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.oid.Oid;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.runtimes.dflt.runtime.context.IsisContext;
import org.apache.isis.runtimes.dflt.runtime.persistence.oidgenerator.simple.SerialOid;
import org.apache.isis.defaults.objectstore.testsystem.TestProxySystemII;

import static org.junit.Assert.assertEquals;


public class SerialKeyCreatorTest {


    private ObjectSpecification specification;
    private ObjectAdapter object;
    private String reference = ExampleReferencePojo.class.getName() + "@3";
    private SerialKeyCreator serialKeyCreator;


    @Before
    public void setup() {
        Logger.getRootLogger().setLevel(Level.OFF);
        TestProxySystemII system = new TestProxySystemII();
        system.init();


        serialKeyCreator = new SerialKeyCreator();
    }


    @Test
    public void reference() throws Exception {
        specification = IsisContext.getSpecificationLoader().loadSpecification(ExampleReferencePojo.class);
        object = IsisContext.getPersistenceSession().createInstance(specification);
        ((SerialOid) object.getOid()).setId(3);
        object.getOid().makePersistent();

        assertEquals(reference, serialKeyCreator.reference(object));
    }
    
    @Test
    public void oid() throws Exception {
        Oid oid = serialKeyCreator.oidFromReference(reference);
        assertEquals(SerialOid.createPersistent(3), oid);
    }
    
    @Test
    public void specification() throws Exception {
        specification = IsisContext.getSpecificationLoader().loadSpecification(ExampleReferencePojo.class);
        ObjectSpecification spec = serialKeyCreator.specificationFromReference(reference);
        assertEquals(specification, spec);
    }
}


