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

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.version.SerialNumberVersion;
import org.apache.isis.core.metamodel.adapter.version.Version;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.runtimes.dflt.objectstores.dflt.testsystem.TestProxySystemII;
import org.apache.isis.runtimes.dflt.runtime.persistence.oidgenerator.simple.SerialOid;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.junit.Before;
import org.junit.Test;

public class WriteObjectCommandTest {
    private TrialObjects testObjects;
    private ObjectAdapter object1;
    private ObjectSpecification specification;
    private ObjectAdapter object2;
    private ObjectAdapter object3;
    private ObjectAdapter object4;
    private StateWriter writer;
    private VersionCreator versionCreator;
    private KeyCreator keyCreator;
    private NoSqlCommandContext commandContext;
    private Mockery context;

    @Before
    public void setup() {
        Logger.getRootLogger().setLevel(Level.OFF);
        final TestProxySystemII system = new TestProxySystemII();
        system.init();

        testObjects = new TrialObjects();

        final ExampleValuePojo pojo1 = new ExampleValuePojo();
        pojo1.setName("Fred Smith");
        pojo1.setSize(108);
        SerialOid oid = SerialOid.createPersistent(3);
        object1 = testObjects.createAdapter(pojo1, oid);
        specification = object1.getSpecification();

        final ExampleValuePojo pojo2 = new ExampleValuePojo();
        pojo2.setName("John Brown");
        oid = SerialOid.createPersistent(4);
        object2 = testObjects.createAdapter(pojo2, oid);

        final ExampleReferencePojo pojo3 = new ExampleReferencePojo();
        pojo3.setReference1(pojo1);
        oid = SerialOid.createPersistent(5);
        object3 = testObjects.createAdapter(pojo3, oid);

        final ExampleCollectionPojo pojo4 = new ExampleCollectionPojo();
        pojo4.getHomogenousCollection().add(pojo1);
        pojo4.getHomogenousCollection().add(pojo2);
        oid = SerialOid.createPersistent(6);
        object4 = testObjects.createAdapter(pojo4, oid);

        context = new Mockery();
        writer = context.mock(StateWriter.class);
        commandContext = context.mock(NoSqlCommandContext.class);
        keyCreator = context.mock(KeyCreator.class);
        versionCreator = context.mock(VersionCreator.class);
        ;

        final Version version = new SerialNumberVersion(2, "username", null);

        context.checking(new Expectations() {
            {
                one(versionCreator).newVersion("tester");
                will(returnValue(version));
                one(versionCreator).versionString(version);
                will(returnValue("2"));
                one(versionCreator).timeString(version);
                will(returnValue("1057"));
            }
        });

    }

    @Test
    public void objectWithValues() throws Exception {

        context.checking(new Expectations() {
            {
                one(commandContext).createStateWriter(specification.getFullIdentifier());
                will(returnValue(writer));

                one(keyCreator).key(SerialOid.createPersistent(3));
                will(returnValue("3"));

                one(writer).writeId("3");
                one(writer).writeType(specification.getFullIdentifier());
                one(writer).writeField("name", "Fred Smith");
                one(writer).writeField("size", "108");
                one(writer).writeField("nullable", null);
                one(writer).writeVersion(null, "2");
                one(writer).writeUser("username");
                one(writer).writeTime("1057");

                one(commandContext).insert(writer);

            }
        });

        new WriteObjectCommand(false, keyCreator, versionCreator, object1).execute(commandContext);

        context.assertIsSatisfied();
    }

    @Test
    public void objectWithReferences() throws Exception {

        context.checking(new Expectations() {
            {
                one(commandContext).createStateWriter(object3.getSpecification().getFullIdentifier());
                will(returnValue(writer));

                one(keyCreator).key(SerialOid.createPersistent(5));
                will(returnValue("5"));
                one(keyCreator).reference(object1);
                will(returnValue("ref@3"));

                one(writer).writeId("5");
                one(writer).writeType(object3.getSpecification().getFullIdentifier());
                one(writer).writeField("reference1", "ref@3");
                one(writer).writeField("reference2", null);
                one(writer).writeVersion(null, "2");
                one(writer).writeUser("username");
                one(writer).writeTime("1057");

                one(commandContext).insert(writer);
            }
        });

        new WriteObjectCommand(false, keyCreator, versionCreator, object3).execute(commandContext);

        context.assertIsSatisfied();
    }

    @Test
    public void objectWithCollections() throws Exception {

        context.checking(new Expectations() {
            {
                one(commandContext).createStateWriter(object4.getSpecification().getFullIdentifier());
                will(returnValue(writer));

                one(keyCreator).key(SerialOid.createPersistent(6));
                will(returnValue("6"));
                one(writer).writeId("6");
                one(writer).writeType(object4.getSpecification().getFullIdentifier());

                one(keyCreator).reference(object1);
                will(returnValue("ref@3"));
                one(keyCreator).reference(object2);
                will(returnValue("ref@4"));

                one(writer).writeField("homogenousCollection", "ref@3|ref@4|");

                one(writer).writeVersion(null, "2");
                one(writer).writeUser("username");
                one(writer).writeTime("1057");

                one(commandContext).insert(writer);
            }
        });

        new WriteObjectCommand(false, keyCreator, versionCreator, object4).execute(commandContext);

        context.assertIsSatisfied();
    }

}
