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

package org.apache.isis.runtimes.dflt.runtime.memento;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Before;
import org.junit.Test;

import org.apache.isis.core.commons.encoding.DataOutputStreamExtended;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.ResolveState;
import org.apache.isis.core.metamodel.adapter.oid.Oid;
import org.apache.isis.core.metamodel.facets.object.encodeable.EncodableFacet;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.isis.runtimes.dflt.runtime.system.context.IsisContextStatic;
import org.apache.isis.runtimes.dflt.runtime.system.session.IsisSessionFactory;

public class MementoTest2_Test {
    private final Mockery mockery = new JUnit4Mockery() {
        {
            setImposteriser(ClassImposteriser.INSTANCE);
        }
    };

    private ObjectAdapter rootAdapter;
    // private ObjectAdapter returnedAdapter;
    //
    // private TestObject rootObject;
    //
    // private TestObject recreatedObject;

    private Oid oid;

    private Data data;

    private Memento memento;

    @Before
    public void setUp() throws Exception {
        Logger.getRootLogger().setLevel(Level.OFF);

        // Root object specification
        final ObjectSpecification rootSpecification = mockery.mock(ObjectSpecification.class);
        final OneToOneAssociation nameField = mockery.mock(OneToOneAssociation.class);
        final ObjectSpecification nameSpecification = mockery.mock(ObjectSpecification.class, "name specification");
        final EncodableFacet encodeableFacet = mockery.mock(EncodableFacet.class);

        mockery.checking(new Expectations() {
            {
                atLeast(1).of(rootSpecification).isCollection();
                will(returnValue(false));

                atLeast(1).of(rootSpecification).getAssociations();
                will(returnValue(Arrays.asList((ObjectAssociation) nameField)));

                atLeast(1).of(rootSpecification).getFullIdentifier();
                will(returnValue(TestObject.class.getName()));

                atLeast(1).of(nameField).isNotPersisted();
                will(returnValue(false));

                atLeast(1).of(nameField).isOneToManyAssociation();
                will(returnValue(false));

                atLeast(1).of(nameField).getSpecification();
                will(returnValue(nameSpecification));

                atLeast(1).of(nameField).getId();
                will(returnValue("name-field"));

                atLeast(1).of(nameSpecification).isEncodeable();
                will(returnValue(true));

                atLeast(1).of(nameSpecification).getFacet(EncodableFacet.class);
                will(returnValue(encodeableFacet));
            }
        });

        // Root object
        rootAdapter = mockery.mock(ObjectAdapter.class);
        // rootObject = new TestObject("Harry");
        final ObjectAdapter nameAdapter = mockery.mock(ObjectAdapter.class, "name");
        oid = mockery.mock(Oid.class);

        // object encoding
        mockery.checking(new Expectations() {
            {
                atLeast(1).of(rootAdapter).getSpecification();
                will(returnValue(rootSpecification));

                atLeast(1).of(rootAdapter).getOid();
                will(returnValue(oid));

                atLeast(1).of(rootAdapter).getResolveState();
                will(returnValue(ResolveState.RESOLVED));

                atLeast(1).of(nameField).get(rootAdapter);
                will(returnValue(nameAdapter));

                one(encodeableFacet).toEncodedString(nameAdapter);
                will(returnValue("_HARRY_"));

                /*
                 * atLeast(1).of(oid).isTransient(); will(returnValue(false));
                 * 
                 * atLeast(1).of(rootAdapter).getObject();
                 * will(returnValue(rootObject));
                 * 
                 * one(mockPersistenceSession).recreateAdapter(oid,
                 * rootSpecification); will(returnValue(recreatedAdapter));.
                 * 
                 * atLeast(1).of(recreatedAdapter).getOid();
                 * will(returnValue(oid));
                 * 
                 * one(recreatedAdapter).getResolveState();
                 * will(returnValue(ResolveState.GHOST));
                 * 
                 * one(recreatedAdapter).changeState(ResolveState.UPDATING);
                 * 
                 * atLeast(1).of(recreatedAdapter).getSpecification();
                 * will(returnValue(rootSpecification));
                 * 
                 * atLeast(1).of(recreatedAdapter).getObject();
                 * will(returnValue(recreatedObject));
                 * 
                 * /* one(mockAdapterManager).adapterFor("Harry",
                 * originalAdapter, specification.getAssociation("name"));
                 * will(returnValue(nameAdapter));
                 * 
                 * atLeast(1).of(nameAdapter).getObject();
                 * will(returnValue("Harry"));
                 */
            }
        });

        // object decoding
        mockery.checking(new Expectations() {
            {
                /*
                 * atLeast(1).of(oid).isTransient(); will(returnValue(false));
                 * 
                 * atLeast(1).of(rootAdapter).getObject();
                 * will(returnValue(rootObject)); /*
                 * one(mockPersistenceSession).recreateAdapter(oid,
                 * rootSpecification); will(returnValue(recreatedAdapter));
                 * 
                 * atLeast(1).of(recreatedAdapter).getOid();
                 * will(returnValue(oid));
                 * 
                 * one(recreatedAdapter).getResolveState();
                 * will(returnValue(ResolveState.GHOST));
                 * 
                 * one(recreatedAdapter).changeState(ResolveState.UPDATING);
                 * 
                 * atLeast(1).of(recreatedAdapter).getSpecification();
                 * will(returnValue(rootSpecification));
                 * 
                 * atLeast(1).of(recreatedAdapter).getObject();
                 * will(returnValue(recreatedObject));
                 * 
                 * /* one(mockAdapterManager).adapterFor("Harry",
                 * originalAdapter, specification.getAssociation("name"));
                 * will(returnValue(nameAdapter));
                 * 
                 * atLeast(1).of(nameAdapter).getObject();
                 * will(returnValue("Harry"));
                 */
            }
        });

        // Persistence Session
        // final ObjectReflector reflector =
        // mockery.mock(ObjectReflector.class);
        // final PersistenceSession mockPersistenceSession =
        // mockery.mock(PersistenceSession.class);
        final IsisSessionFactory sessionFactory = mockery.mock(IsisSessionFactory.class);
        // final AuthenticationSession mockSession =
        // mockery.mock(AuthenticationSession.class);
        // final IsisSession session = mockery.mock(IsisSession.class);
        // final AdapterManager mockAdapterManager =
        // mockery.mock(AdapterManager.class);
        /**
         * mockery.checking(new Expectations() { {
         * atLeast(1).of(sessionFactory).getSpecificationLoader();
         * will(returnValue(reflector));
         * 
         * atLeast(1).of(sessionFactory).openSession(mockSession);
         * will(returnValue(session));
         * 
         * atLeast(1).of(reflector).loadSpecification(TestObject.class.getName()
         * ); will(returnValue(rootSpecification));
         * 
         * atLeast(1).of(session).open();
         * 
         * atLeast(1).of(session).getPersistenceSession();
         * will(returnValue(mockPersistenceSession));
         * 
         * }});
         */

        IsisContextStatic.createRelaxedInstance(sessionFactory);

        // IsisContextStatic.getInstance().openSessionInstance(mockSession);

        // final ObjectAdapter recreatedAdapter =
        // mockery.mock(ObjectAdapter.class, "recreated");

        // recreatedObject = new TestObject();

        /*
         * returnedAdapter = mockery.mock(ObjectAdapter.class,
         * "recreated adapter"); final Oid returnedOid = mockery.mock(Oid.class,
         * "recreated oid");
         * 
         * mockery.checking(new Expectations() { {
         * 
         * 
         * atLeast(1).of(mockPersistenceSession).recreateAdapter(oid,
         * rootSpecification); will(returnValue(returnedAdapter));
         * 
         * atLeast(1).of(returnedAdapter).getOid();
         * will(returnValue(returnedOid));
         * 
         * }});
         */

        memento = new Memento(rootAdapter);
        data = memento.getData();
    }

    @Test
    public void testOid() throws Exception {
        assertEquals(oid, data.getOid());
        mockery.assertIsSatisfied();
    }

    @Test
    public void testResolved() throws Exception {
        assertEquals("Resolved", data.getResolveState());
        mockery.assertIsSatisfied();
    }

    @Test
    public void testClassName() throws Exception {
        assertEquals(TestObject.class.getName(), data.getClassName());
        mockery.assertIsSatisfied();
    }

    @Test
    public void testStringField() throws Exception {
        assertEquals(ObjectData.class, data.getClass());
        assertEquals("_HARRY_", ((ObjectData) data).getEntry("name-field"));
        mockery.assertIsSatisfied();
    }

    @Test
    public void testEncode() throws Exception {
        final DataOutputStreamExtended mockOutputImpl = mockery.mock(DataOutputStreamExtended.class);

        mockery.checking(new Expectations() {
            {
                one(mockOutputImpl).writeEncodable(with(any(ObjectData.class)));
                // one(mockOutputImpl).writeUTF(TestObject.class.getName());
                // one(mockOutputImpl).writeUTF(ResolveState.RESOLVED.name());
                // one(mockOutputImpl).writeEncodable(oid);
                // one(mockOutputImpl).writeInt(1);
                // one(mockOutputImpl).writeByte((byte)1); // indicates a string
                // one(mockOutputImpl).writeUTF("name-field");
                // one(mockOutputImpl).writeUTF("_HARRY_");
            }
        });
        memento.encodedData(mockOutputImpl);
        mockery.assertIsSatisfied();
    }

    /*
     * @Test public void testDifferentAdaptersReturned() throws Exception {
     * final Memento memento = new Memento(rootAdapter); returnedAdapter =
     * memento.recreateObject();
     * 
     * rootAdapter.getObject(); returnedAdapter.getObject();
     * 
     * assertNotSame(rootAdapter, returnedAdapter); mockery.assertIsSatisfied();
     * }
     * 
     * @Test public void testDifferentObjectsReturned() throws Exception { final
     * Memento memento = new Memento(rootAdapter); returnedAdapter =
     * memento.recreateObject(); assertNotSame(rootAdapter.getObject(),
     * returnedAdapter.getObject()); mockery.assertIsSatisfied(); }
     * 
     * @Test public void testHaveSameOid() throws Exception { final Memento
     * memento = new Memento(rootAdapter); returnedAdapter =
     * memento.recreateObject();
     * 
     * rootAdapter.getObject(); returnedAdapter.getObject();
     * 
     * assertEquals(rootAdapter.getOid(), returnedAdapter.getOid());
     * mockery.assertIsSatisfied(); }
     * 
     * @Test public void testHaveSameSpecification() throws Exception { final
     * Memento memento = new Memento(rootAdapter); returnedAdapter =
     * memento.recreateObject();
     * 
     * rootAdapter.getObject(); returnedAdapter.getObject();
     * 
     * assertEquals(rootAdapter.getSpecificatio assertEquals("", ((ObjectData)
     * data).getEntry("name-field")); n(), returnedAdapter.getSpecification());
     * mockery.assertIsSatisfied(); }
     */

    /*
     * @Test public void testEncoding() throws Exception { final
     * TransferableWriter writer = mockery.mock(TransferableWriter.class); final
     * Transferable object = mockery.mock(Transferable.class);
     * mockery.checking(new Expectations() { { one(writer).writeObject(object );
     * 
     * ignoring(object); } });
     * 
     * final Memento memento = new Memento(rootAdapter);
     * memento.writeData(writer); mockery.assertIsSatisfied();
     * 
     * }
     */
    /*
     * @Test public void testName() throws Exception { final Memento memento =
     * new Memento(rootAdapter); returnedAdapter = memento.recreateObject();
     * assertEquals("Harry", ((TestObject) rootAdapter.getObject()).getName());
     * assertEquals("Harry", ((TestObject)
     * returnedAdapter.getObject()).getName()); }
     */

    /*
     * @Test public void testNull2() throws Exception { final Memento memento =
     * new Memento(null); Data data = memento.getData();
     * 
     * assertEquals(null, data); // mockery.assertIsSatisfied(); }
     * 
     * @Test public void testNull() throws Exception { final Memento memento =
     * new Memento(null); returnedAdapter = memento.recreateObject();
     * Assert.assertNull(returnedAdapter); // mockery.assertIsSatisfied(); }
     */

}
