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

import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.commons.exceptions.UnexpectedCallException;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.oid.RootOidDefault;
import org.apache.isis.core.metamodel.adapter.version.SerialNumberVersion;
import org.apache.isis.core.metamodel.adapter.version.Version;
import org.apache.isis.core.testsupport.jmock.JUnitRuleMockery2;
import org.apache.isis.core.testsupport.jmock.JUnitRuleMockery2.Mode;
import org.apache.isis.runtimes.dflt.objectstores.nosql.db.StateWriter;
import org.apache.isis.runtimes.dflt.objectstores.nosql.encryption.DataEncryption;
import org.apache.isis.runtimes.dflt.objectstores.nosql.versions.VersionCreator;
import org.apache.isis.runtimes.dflt.testsupport.IsisSystemWithFixtures;
import org.apache.isis.tck.dom.eg.ExamplePojoWithCollections;
import org.apache.isis.tck.dom.eg.ExamplePojoWithReferences;
import org.apache.isis.tck.dom.eg.ExamplePojoWithValues;

public class WriteObjectCommandTest {
    
    @Rule
    public IsisSystemWithFixtures iswf = IsisSystemWithFixtures.builder().build();

    @Rule
    public JUnitRuleMockery2 context = JUnitRuleMockery2.createFor(Mode.INTERFACES_AND_CLASSES);

    @Mock
    private StateWriter writer;
    @Mock
    private VersionCreator versionCreator;
    
    @Mock
    private NoSqlCommandContext commandContext;

    private DataEncryption dataEncrypter;
    //private KeyCreatorDefault keyCreator;

    private ObjectAdapter epv1Adapter;
    private ObjectAdapter epr1Adapter;
    private ObjectAdapter epc1Adapter;

    private ExamplePojoWithValues epv1;
    private ExamplePojoWithValues epv2;

    private ExamplePojoWithReferences epr1;

    private ExamplePojoWithCollections epc1;

    @Before
    public void setup() {

        //keyCreator = new KeyCreatorDefault();
        
        epv1 = iswf.fixtures.epv1;
        epv1.setName("Fred Smith");
        epv1.setSize(108);
        epv1Adapter = iswf.remapAsPersistent(epv1, RootOidDefault.deString("EPV:1"));

        epv2 = iswf.fixtures.epv2;
        epv2.setName("John Brown");
        iswf.remapAsPersistent(epv2, RootOidDefault.deString("EPV:2"));

        epr1 = iswf.fixtures.epr1;
        epr1.setReference(epv1);
        epr1Adapter = iswf.remapAsPersistent(epr1, RootOidDefault.deString("EPR:1"));

        epc1 = iswf.fixtures.epc1;
        epc1.getHomogeneousCollection().add(epv1);
        epc1.getHomogeneousCollection().add(epv2);
        
        epc1Adapter = iswf.remapAsPersistent(epc1, RootOidDefault.deString("EPC:1"));

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
                one(commandContext).createStateWriter(epv1Adapter.getSpecification().getSpecId());
                will(returnValue(writer));

//                one(writer).writeId("1");
//                one(writer).writeObjectType(specification.getFullIdentifier());

                final RootOidDefault oid = RootOidDefault.create(epv1Adapter.getSpecification().getSpecId(), "1");
                exactly(2).of(writer).writeOid(oid); // once for the id, once for the type

                one(writer).writeField("name", "ENCFred Smith");
                one(writer).writeField("size", "ENC108");
                one(writer).writeField("date", null);
                one(writer).writeField("nullable", null);
                one(writer).writeVersion(null, "2");
                one(writer).writeUser("username");
                one(writer).writeTime("1057");
                one(writer).writeEncryptionType("etc1");

                one(commandContext).insert(writer);

            }
        });

        final WriteObjectCommand command = new WriteObjectCommand(WriteObjectCommand.Mode.NON_UPDATE, versionCreator, dataEncrypter, epv1Adapter);
        command.execute(commandContext);
    }

    @Test
    public void objectWithReferences() throws Exception {

        context.checking(new Expectations() {
            {
                one(commandContext).createStateWriter(epr1Adapter.getSpecification().getSpecId());
                will(returnValue(writer));

//                one(writer).writeId("1");
//                one(writer).writeObjectType(epr1Adapter.getSpecification().getFullIdentifier());
                
                final RootOidDefault oid = RootOidDefault.create(epr1Adapter.getSpecification().getSpecId(), "1");
                exactly(2).of(writer).writeOid(oid); // once for the id, once for the type
                
                one(writer).writeField("reference", "EPV:1");
                one(writer).writeField("aggregatedReference", null);
                
                one(writer).writeVersion(null, "2");
                one(writer).writeUser("username");
                one(writer).writeTime("1057");
                one(writer).writeEncryptionType("etc1");

                one(commandContext).insert(writer);
            }
        });

        final WriteObjectCommand command = new WriteObjectCommand(WriteObjectCommand.Mode.NON_UPDATE, versionCreator, dataEncrypter, epr1Adapter);
        command.execute(commandContext);
    }

    @Test
    public void objectWithCollections() throws Exception {

        context.checking(new Expectations() {
            {
                one(commandContext).createStateWriter(epc1Adapter.getSpecification().getSpecId());
                will(returnValue(writer));

//                one(writer).writeId("1");
//                one(writer).writeObjectType(epc1Adapter.getSpecification().getFullIdentifier());

                final RootOidDefault oid = RootOidDefault.create(epc1Adapter.getSpecification().getSpecId(), "1");
                exactly(2).of(writer).writeOid(oid); // once for the id, once for the type

                one(writer).writeField("homogeneousCollection", "EPV:1|EPV:2|");

                one(writer).writeVersion(null, "2");
                one(writer).writeUser("username");
                one(writer).writeTime("1057");
                one(writer).writeEncryptionType("etc1");

                one(commandContext).insert(writer);
            }
        });

        final WriteObjectCommand command = new WriteObjectCommand(WriteObjectCommand.Mode.NON_UPDATE, versionCreator, dataEncrypter, epc1Adapter);
        command.execute(commandContext);
    }
}
