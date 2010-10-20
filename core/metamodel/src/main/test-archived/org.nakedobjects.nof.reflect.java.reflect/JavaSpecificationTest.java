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


package org.apache.isis.progmodel.java5.reflect;

import org.apache.isis.noa.adapter.Persistable;
import org.apache.isis.noa.spec.ObjectSpecification;
import org.apache.isis.nof.reflect.spec.AbstractSpecification;
import org.apache.isis.nof.reflect.spec.ReflectionPeerBuilder;
import org.apache.isis.nof.testsystem.ProxyTestCase;


public class JavaSpecificationTest extends ProxyTestCase {

    public static void main(final String[] args) {
        junit.textui.TestRunner.run(JavaSpecificationTest.class);
    }

    private ReflectionPeerBuilder builder = new DummyBuilder();

    protected void setUp() throws Exception {
        super.setUp();
    }

    protected void tearDown() throws Exception {
        system.shutdown();
    }

    public void testNoFlag() throws Exception {
        AbstractSpecification spec = new JavaSpecification(TestObject.class, new DummyBuilder(), new JavaReflector());
        spec.introspect(builder);
        assertEquals(0, spec.getFeatures());
    }

    public void testNotPersistable() throws Exception {
        AbstractSpecification spec = new JavaSpecification(JavaObjectMarkedAsTransient.class, new DummyBuilder(),
                new JavaReflector());
        spec.introspect(builder);
        assertEquals(Persistable.TRANSIENT, spec.persistable());

    }

    public void testPersistable() throws Exception {
        AbstractSpecification spec = new JavaSpecification(JavaObjectWithBasicProgramConventions.class, new DummyBuilder(), new JavaReflector());
        spec.introspect(builder);
        assertEquals(Persistable.USER_PERSISTABLE, spec.persistable());
    }

    public void testService() throws Exception {
        AbstractSpecification spec = new JavaSpecification(TestObjectAsService.class, new DummyBuilder(), new JavaReflector());
        spec.introspect(builder);
        spec.markAsService();
        assertEquals(ObjectSpecification.SERVICE, spec.getFeatures());
    }
}
