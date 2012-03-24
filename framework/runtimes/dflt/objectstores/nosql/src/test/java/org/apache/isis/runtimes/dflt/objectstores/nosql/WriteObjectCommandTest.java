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
import org.jmock.auto.Mock;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.commons.exceptions.UnexpectedCallException;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.version.SerialNumberVersion;
import org.apache.isis.core.metamodel.adapter.version.Version;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.testsupport.jmock.JUnitRuleMockery2;
import org.apache.isis.core.testsupport.jmock.JUnitRuleMockery2.Mode;
import org.apache.isis.runtimes.dflt.objectstores.dflt.InMemoryPersistenceMechanismInstaller;
import org.apache.isis.runtimes.dflt.objectstores.nosql.db.StateWriter;
import org.apache.isis.runtimes.dflt.objectstores.nosql.db.mongo.MongoPersistorMechanismInstaller;
import org.apache.isis.runtimes.dflt.objectstores.nosql.encryption.DataEncryption;
import org.apache.isis.runtimes.dflt.objectstores.nosql.keys.KeyCreator;
import org.apache.isis.runtimes.dflt.objectstores.nosql.versions.VersionCreator;
import org.apache.isis.runtimes.dflt.runtime.installerregistry.installerapi.PersistenceMechanismInstaller;
import org.apache.isis.runtimes.dflt.runtime.persistence.oidgenerator.serial.RootOidDefault;
import org.apache.isis.runtimes.dflt.runtime.system.context.IsisContext;
import org.apache.isis.runtimes.dflt.testsupport.TestSystem;
import org.apache.isis.runtimes.dflt.testsupport.IsisSystemWithFixtures;
import org.apache.isis.runtimes.dflt.testsupport.TestSystemWithObjectStoreTestAbstract;
import org.apache.isis.runtimes.dflt.testsupport.domain.ExamplePojoWithCollections;
import org.apache.isis.runtimes.dflt.testsupport.domain.ExamplePojoWithReferences;
import org.apache.isis.runtimes.dflt.testsupport.domain.ExamplePojoWithValues;

public class WriteObjectCommandTest extends TestSystemWithObjectStoreTestAbstract {
    
    @Override
    protected PersistenceMechanismInstaller createPersistenceMechanismInstaller() {
        return new MongoPersistorMechanismInstaller();
    }

    @Rule
    public JUnitRuleMockery2 context = JUnitRuleMockery2.createFor(Mode.INTERFACES_AND_CLASSES);

    @Mock
    private StateWriter writer;
    @Mock
    private VersionCreator versionCreator;
    @Mock
    private KeyCreator keyCreator;
    @Mock
    private NoSqlCommandContext commandContext;

    private DataEncryption dataEncrypter;

    private ObjectAdapter adapter1;
    private ObjectSpecification specification;
    private ObjectAdapter adapter2;
    private ObjectAdapter adapter3;
    private ObjectAdapter object4;
    

    private final String objectType = "FOO";

    private TestSystem system;

    @Before
    public void setup() {

        final ExamplePojoWithValues pojo1 = new ExamplePojoWithValues();
        final String objectType = "EVP";
        
        pojo1.setName("Fred Smith");
        pojo1.setSize(108);
        adapter1 = system.recreateAdapter(pojo1, RootOidDefault.create(objectType, "3"));

        final ExamplePojoWithValues pojo2 = new ExamplePojoWithValues();
        pojo2.setName("John Brown");
        adapter2 = system.recreateAdapter(pojo2, RootOidDefault.create(objectType, "4"));

        final ExamplePojoWithReferences pojo3 = new ExamplePojoWithReferences();
        pojo3.setReference1(pojo1);
        adapter3 = system.recreateAdapter(pojo3, RootOidDefault.create(objectType, "5"));

        final ExamplePojoWithCollections adapter4 = new ExamplePojoWithCollections();
        adapter4.getHomogenousCollection().add(pojo1);
        adapter4.getHomogenousCollection().add(pojo2);
        
        object4 = system.recreateAdapter(adapter4, RootOidDefault.create(objectType, "6"));

        specification = adapter1.getSpecification();

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

        dataEncrypter = new DataEncryption() {
            @Override
            public String getType() {
                return "etc1";
            }

            @Override
            public void init(final IsisConfiguration configuration) {
            }

            @Override
            public String encrypt(final String plainText) {
                return "ENC" + plainText;
            }

            @Override
            public String decrypt(final String encryptedText) {
                throw new UnexpectedCallException();
            }
        };
    }


    @Test
    public void objectWithValues() throws Exception {

        context.checking(new Expectations() {

            {
                one(commandContext).createStateWriter(specification.getFullIdentifier());
                will(returnValue(writer));

                one(keyCreator).key(RootOidDefault.create(objectType, ""+3));
                will(returnValue("3"));

                one(writer).writeId("3");
                one(writer).writeType(specification.getFullIdentifier());
                one(writer).writeField("name", "ENCFred Smith");
                one(writer).writeField("size", "ENC108");
                one(writer).writeField("nullable", null);
                one(writer).writeVersion(null, "2");
                one(writer).writeUser("username");
                one(writer).writeTime("1057");
                one(writer).writeEncryptionType("etc1");

                one(commandContext).insert(writer);

            }
        });

        new WriteObjectCommand(false, keyCreator, versionCreator, dataEncrypter, adapter1).execute(commandContext);

        context.assertIsSatisfied();
    }

    @Test
    public void objectWithReferences() throws Exception {

        context.checking(new Expectations() {
            {
                one(commandContext).createStateWriter(adapter3.getSpecification().getFullIdentifier());
                will(returnValue(writer));

                one(keyCreator).key(RootOidDefault.create(objectType, ""+5));
                will(returnValue("5"));
                one(keyCreator).reference(adapter1);
                will(returnValue("ref@3"));

                one(writer).writeId("5");
                one(writer).writeType(adapter3.getSpecification().getFullIdentifier());
                one(writer).writeField("reference1", "ref@3");
                one(writer).writeField("reference2", null);
                one(writer).writeVersion(null, "2");
                one(writer).writeUser("username");
                one(writer).writeTime("1057");
                one(writer).writeEncryptionType("etc1");

                one(commandContext).insert(writer);
            }
        });

        new WriteObjectCommand(false, keyCreator, versionCreator, dataEncrypter, adapter3).execute(commandContext);

        context.assertIsSatisfied();
    }

    @Test
    public void objectWithCollections() throws Exception {

        context.checking(new Expectations() {
            {
                one(commandContext).createStateWriter(object4.getSpecification().getFullIdentifier());
                will(returnValue(writer));

                one(keyCreator).key(RootOidDefault.create(objectType, ""+6));
                will(returnValue("6"));
                one(writer).writeId("6");
                one(writer).writeType(object4.getSpecification().getFullIdentifier());

                one(keyCreator).reference(adapter1);
                will(returnValue("ref@3"));
                one(keyCreator).reference(adapter2);
                will(returnValue("ref@4"));

                one(writer).writeField("homogenousCollection", "ref@3|ref@4|");

                one(writer).writeVersion(null, "2");
                one(writer).writeUser("username");
                one(writer).writeTime("1057");
                one(writer).writeEncryptionType("etc1");

                one(commandContext).insert(writer);
            }
        });

        new WriteObjectCommand(false, keyCreator, versionCreator, dataEncrypter, object4).execute(commandContext);

        context.assertIsSatisfied();
    }

}
