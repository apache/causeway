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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.isis.core.commons.exceptions.UnexpectedCallException;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.ResolveState;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.runtimes.dflt.objectstores.dflt.testsystem.TestProxySystemII;
import org.apache.isis.runtimes.dflt.runtime.persistence.oidgenerator.simple.SerialOid;
import org.apache.isis.runtimes.dflt.runtime.system.context.IsisContext;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.hamcrest.CoreMatchers;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class ObjectReaderTest {

    private ObjectReader objectReader;
    private StateReader reader1;
    private StateReader reader2;
    private Mockery context;
    private KeyCreator keyCreator;
    private VersionCreator versionCreator;
    private Map<String, DataEncrypter> dataEncrypter;

    @Before
    public void setup() {
        Logger.getRootLogger().setLevel(Level.OFF);
        final TestProxySystemII system = new TestProxySystemII();
        system.init();

        context = new Mockery();

        objectReader = new ObjectReader();
        keyCreator = context.mock(KeyCreator.class);
        versionCreator = context.mock(VersionCreator.class);
        
        
        dataEncrypter = new HashMap<String, DataEncrypter>();
        DataEncrypter dataEncrypter1 = new DataEncrypter() {
            public String getType() {
                return "etc1";
            }

            public String encrypt(String plainText) {
                throw new UnexpectedCallException();
            }

            public String decrypt(String encryptedText) {
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
                will(returnValue(ExampleValuePojo.class.getName()));

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
                one(keyCreator).oid("3");

                will(returnValue(SerialOid.createPersistent(3)));
                ;
            }
        });

        final ObjectAdapter readObject = objectReader.load(reader1, keyCreator, versionCreator, dataEncrypter);
        assertEquals(SerialOid.createPersistent(3), readObject.getOid());
        assertEquals(ResolveState.RESOLVED, readObject.getResolveState());

        final ExampleValuePojo pojo = (ExampleValuePojo) readObject.getObject();
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
                will(returnValue(ExampleReferencePojo.class.getName()));

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

                one(keyCreator).oid("4");
                will(returnValue(SerialOid.createPersistent(4)));
                ;

                one(reader2).readField("reference1");
                will(returnValue("ref@3"));

                one(reader2).readField("reference2");
                will(returnValue("null"));

                one(keyCreator).oidFromReference("ref@3");
                will(returnValue(SerialOid.createPersistent(3)));
                ;
                one(keyCreator).specificationFromReference("ref@3");
                will(returnValue(IsisContext.getSpecificationLoader().loadSpecification(ExampleValuePojo.class)));
            }
        });

        final ObjectAdapter readObject = objectReader.load(reader2, keyCreator, versionCreator, dataEncrypter);
        assertEquals(SerialOid.createPersistent(4), readObject.getOid());
        assertEquals(ResolveState.RESOLVED, readObject.getResolveState());

        final ExampleReferencePojo pojo = (ExampleReferencePojo) readObject.getObject();
        assertEquals(null, pojo.getReference2());
        assertThat(pojo.getReference1(), CoreMatchers.instanceOf(ExampleValuePojo.class));

        context.assertIsSatisfied();
    }

    @Test
    public void testReadingCollection() throws Exception {
        final ObjectSpecification specification =
            IsisContext.getSpecificationLoader().loadSpecification(ExampleValuePojo.class);
        reader2 = context.mock(StateReader.class, "reader 2");
        context.checking(new Expectations() {
            {
                one(reader2).readObjectType();
                will(returnValue(ExampleCollectionPojo.class.getName()));

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

                one(keyCreator).oid("5");
                will(returnValue(SerialOid.createPersistent(5)));
                ;

                one(reader2).readField("hetrogenousCollection");
                will(returnValue(null));
                one(reader2).readField("homogenousCollection");
                will(returnValue("ref@3|ref@4|"));

                one(keyCreator).specificationFromReference("ref@3");
                will(returnValue(specification));
                one(keyCreator).oidFromReference("ref@3");
                will(returnValue(SerialOid.createPersistent(3)));
                one(keyCreator).specificationFromReference("ref@4");
                will(returnValue(specification));
                one(keyCreator).oidFromReference("ref@4");
                will(returnValue(SerialOid.createPersistent(4)));
            }
        });

        final ObjectAdapter readObject = objectReader.load(reader2, keyCreator, versionCreator, dataEncrypter);
        assertEquals(SerialOid.createPersistent(5), readObject.getOid());
        assertEquals(ResolveState.RESOLVED, readObject.getResolveState());

        final ExampleCollectionPojo pojo = (ExampleCollectionPojo) readObject.getObject();
        final List<ExampleValuePojo> collection2 = pojo.getHomogenousCollection();
        assertEquals(2, collection2.size());

        assertThat(collection2.get(0), CoreMatchers.instanceOf(ExampleValuePojo.class));
        assertThat(collection2.get(1), CoreMatchers.instanceOf(ExampleValuePojo.class));

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

        final ObjectSpecification specification = IsisContext.getSpecificationLoader().loadSpecification(ExampleValuePojo.class);
        final ObjectAdapter readObject = IsisContext.getPersistenceSession().recreateAdapter(SerialOid.createPersistent(4),
                specification);

        objectReader.update(reader1, keyCreator, versionCreator, dataEncrypter, readObject);

        final ExampleValuePojo pojo = (ExampleValuePojo) readObject.getObject();
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
