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
import static org.junit.Assert.assertThat;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hamcrest.CoreMatchers;
import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.commons.exceptions.UnexpectedCallException;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.ResolveState;
import org.apache.isis.core.metamodel.adapter.oid.RootOidDefault;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.testsupport.jmock.JUnitRuleMockery2;
import org.apache.isis.core.testsupport.jmock.JUnitRuleMockery2.Mode;
import org.apache.isis.runtimes.dflt.objectstores.nosql.db.StateReader;
import org.apache.isis.runtimes.dflt.objectstores.nosql.db.mongo.MongoPersistorMechanismInstaller;
import org.apache.isis.runtimes.dflt.objectstores.nosql.encryption.DataEncryption;
import org.apache.isis.runtimes.dflt.objectstores.nosql.keys.KeyCreator;
import org.apache.isis.runtimes.dflt.objectstores.nosql.versions.VersionCreator;
import org.apache.isis.runtimes.dflt.runtime.system.context.IsisContext;
import org.apache.isis.runtimes.dflt.testsupport.IsisSystemWithFixtures;
import org.apache.isis.tck.dom.eg.ExamplePojoWithCollections;
import org.apache.isis.tck.dom.eg.ExamplePojoWithReferences;
import org.apache.isis.tck.dom.eg.ExamplePojoWithValues;

public class ObjectReaderTest {
    
    @Rule
    public IsisSystemWithFixtures iswf = IsisSystemWithFixtures.builder().with(new MongoPersistorMechanismInstaller()).build();

    @Rule
    public JUnitRuleMockery2 context = JUnitRuleMockery2.createFor(Mode.INTERFACES_ONLY);

    @Mock
    private KeyCreator keyCreator;
    @Mock
    private VersionCreator versionCreator;
    
    private ObjectSpecification exampleValuePojoSpec;
    private ObjectSpecification exampleReferencePojoSpec;
    private ObjectSpecification exampleCollectionPojoSpec;
    
    private ObjectReader objectReader;
    
    private StateReader reader1;
    private StateReader reader2;
    
    private Map<String, DataEncryption> dataEncrypter;

    private final RootOidDefault oid3 = RootOidDefault.create("EPV|3"); // ExampleValuePojo
    private final RootOidDefault oid4 = RootOidDefault.create("EPR|4"); // ExampleReferencePojo
    private final RootOidDefault oid5 = RootOidDefault.create("EPC|5"); // ExampleCollectionPojo


    @Before
    public void setup() {
        exampleValuePojoSpec = iswf.loadSpecification(ExamplePojoWithValues.class);
        exampleReferencePojoSpec = iswf.loadSpecification(ExamplePojoWithReferences.class);
        exampleCollectionPojoSpec = iswf.loadSpecification(ExamplePojoWithCollections.class);
                
        objectReader = new ObjectReader();

        dataEncrypter = new HashMap<String, DataEncryption>();
        final DataEncryption dataEncrypter1 = new DataEncryption() {
            @Override
            public String getType() {
                return "etc1";
            }

            @Override
            public void init(final IsisConfiguration configuration) {
            }

            @Override
            public String encrypt(final String plainText) {
                throw new UnexpectedCallException();
            }

            @Override
            public String decrypt(final String encryptedText) {
                return encryptedText.substring(3);
            }
        };
        dataEncrypter.put(dataEncrypter1.getType(), dataEncrypter1);

    }

    @Test
    public void testReadingValues() throws Exception {
        setupObject1();

        context.checking(new Expectations() {
            {
                one(reader1).readObjectType();
                will(returnValue(ExamplePojoWithValues.class.getName()));

                one(reader1).readId();
                will(returnValue("3"));

                one(reader1).readEncrytionType();
                will(returnValue("etc1"));
                one(reader1).readVersion();
                will(returnValue("3"));
                one(reader1).readUser();
                will(returnValue("username"));
                one(reader1).readTime();
                will(returnValue("1020"));
                one(versionCreator).version("3", "username", "1020");
                
                one(keyCreator).oid(exampleValuePojoSpec, "3");
                will(returnValue(oid3));
                ;
            }
        });

        final ObjectAdapter readObject = objectReader.load(reader1, keyCreator, versionCreator, dataEncrypter);
        assertEquals(oid3, readObject.getOid());
        assertEquals(ResolveState.RESOLVED, readObject.getResolveState());

        final ExamplePojoWithValues pojo = (ExamplePojoWithValues) readObject.getObject();
        assertEquals("Fred Smith", pojo.getName());
        assertEquals(34, pojo.getSize());

        context.assertIsSatisfied();
    }

    @Test
    public void testReadingReference() throws Exception {
        reader2 = context.mock(StateReader.class, "reader 2");
        context.checking(new Expectations() {
            {
                one(reader2).readObjectType();
                will(returnValue(ExamplePojoWithReferences.class.getName()));

                one(reader2).readId();
                will(returnValue("4"));

                one(reader2).readEncrytionType();
                will(returnValue("etc1"));
                one(reader2).readVersion();
                will(returnValue("3"));
                one(reader2).readUser();
                will(returnValue("username"));
                one(reader2).readTime();
                will(returnValue("1020"));
                one(versionCreator).version("3", "username", "1020");

                one(keyCreator).oid(exampleReferencePojoSpec, "4");
                will(returnValue(oid4));
                ;

                one(reader2).readField("reference1");
                will(returnValue("ref@3"));

                one(reader2).readField("reference2");
                will(returnValue("null"));

                one(keyCreator).oidFromReference("ref@3");
                will(returnValue(oid3));
                ;
                one(keyCreator).specificationFromReference("ref@3");
                will(returnValue(exampleValuePojoSpec));
            }
        });

        final ObjectAdapter readObject = objectReader.load(reader2, keyCreator, versionCreator, dataEncrypter);
        assertEquals(oid4, readObject.getOid());
        assertEquals(ResolveState.RESOLVED, readObject.getResolveState());

        final ExamplePojoWithReferences pojo = (ExamplePojoWithReferences) readObject.getObject();
        assertEquals(null, pojo.getAggregatedReference());
        assertThat(pojo.getReference(), CoreMatchers.instanceOf(ExamplePojoWithValues.class));

        context.assertIsSatisfied();
    }

    @Test
    public void testReadingCollection() throws Exception {
        final ObjectSpecification specification = IsisContext.getSpecificationLoader().loadSpecification(ExamplePojoWithValues.class);
        reader2 = context.mock(StateReader.class, "reader 2");
        context.checking(new Expectations() {
            {
                one(reader2).readObjectType();
                will(returnValue(ExamplePojoWithCollections.class.getName()));

                one(reader2).readId();
                will(returnValue("5"));

                one(reader2).readEncrytionType();
                will(returnValue("etc1"));
                one(reader2).readVersion();
                will(returnValue("3"));
                one(reader2).readUser();
                will(returnValue("username"));
                one(reader2).readTime();
                will(returnValue("1020"));
                one(versionCreator).version("3", "username", "1020");

                one(keyCreator).oid(exampleCollectionPojoSpec, "5");
                will(returnValue(oid5));
                ;

                one(reader2).readField("hetrogenousCollection");
                will(returnValue(null));
                one(reader2).readField("homogenousCollection");
                will(returnValue("ref@3|ref@4|"));

                one(keyCreator).specificationFromReference("ref@3");
                will(returnValue(specification));
                one(keyCreator).oidFromReference("ref@3");
                will(returnValue(oid3));
                one(keyCreator).specificationFromReference("ref@4");
                will(returnValue(specification));
                one(keyCreator).oidFromReference("ref@4");
                will(returnValue(oid4));
            }
        });

        final ObjectAdapter readObject = objectReader.load(reader2, keyCreator, versionCreator, dataEncrypter);
        assertEquals(oid5, readObject.getOid());
        assertEquals(ResolveState.RESOLVED, readObject.getResolveState());

        final ExamplePojoWithCollections pojo = (ExamplePojoWithCollections) readObject.getObject();
        final List<ExamplePojoWithValues> collection2 = pojo.getHomogeneousCollection();
        assertEquals(2, collection2.size());

        assertThat(collection2.get(0), CoreMatchers.instanceOf(ExamplePojoWithValues.class));
        assertThat(collection2.get(1), CoreMatchers.instanceOf(ExamplePojoWithValues.class));

        context.assertIsSatisfied();
    }

    @Test
    public void updateObjectsState() throws Exception {
        setupObject1();
        context.checking(new Expectations() {
            {

                one(reader1).readEncrytionType();
                will(returnValue("etc1"));
                one(reader1).readVersion();
                will(returnValue("3"));
                one(reader1).readUser();
                will(returnValue("username"));
                one(reader1).readTime();
                will(returnValue("1020"));
                one(versionCreator).version("3", "username", "1020");
            }
        });

        final ObjectSpecification specification = IsisContext.getSpecificationLoader().loadSpecification(ExamplePojoWithValues.class);
        final ObjectAdapter readObject = IsisContext.getPersistenceSession().recreateAdapter(RootOidDefault.create("EVP", ""+4), specification);

        objectReader.update(reader1, keyCreator, versionCreator, dataEncrypter, readObject);

        final ExamplePojoWithValues pojo = (ExamplePojoWithValues) readObject.getObject();
        assertEquals("Fred Smith", pojo.getName());
        assertEquals(34, pojo.getSize());

        context.assertIsSatisfied();
    }

    private void setupObject1() {
        reader1 = context.mock(StateReader.class, "reader 1");
        context.checking(new Expectations() {
            {
                one(reader1).readField("name");
                will(returnValue("ENCFred Smith"));

                one(reader1).readField("size");
                will(returnValue("ENC34"));

                one(reader1).readField("nullable");
                will(returnValue("null"));
            }
        });
    }

}
