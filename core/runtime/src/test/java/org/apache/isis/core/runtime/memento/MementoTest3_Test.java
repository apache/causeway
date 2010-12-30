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


package org.apache.isis.core.runtime.memento;

import static org.junit.Assert.assertEquals;

import java.util.Iterator;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.ResolveState;
import org.apache.isis.core.metamodel.adapter.oid.Oid;
import org.apache.isis.core.metamodel.authentication.AuthenticationSession;
import org.apache.isis.core.metamodel.config.internal.PropertiesConfiguration;
import org.apache.isis.core.metamodel.encoding.DataOutputStreamExtended;
import org.apache.isis.core.metamodel.facets.actcoll.typeof.TypeOfFacet;
import org.apache.isis.core.metamodel.facets.collections.modify.CollectionFacet;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.isis.core.metamodel.specloader.ObjectReflector;
import org.apache.isis.core.runtime.context.IsisContextStatic;
import org.apache.isis.core.runtime.memento.CollectionData;
import org.apache.isis.core.runtime.memento.Data;
import org.apache.isis.core.runtime.memento.Memento;
import org.apache.isis.core.runtime.persistence.PersistenceSession;
import org.apache.isis.core.runtime.session.IsisSession;
import org.apache.isis.core.runtime.session.IsisSessionFactory;


public class MementoTest3_Test {
    private Mockery mockery = new JUnit4Mockery(){{
		setImposteriser(ClassImposteriser.INSTANCE);
	}};

    private ObjectAdapter rootAdapter;
//    private ObjectAdapter returnedAdapter;
//
//    private TestObject rootObject;
//
//    private TestObject recreatedObject;

    private Oid oid;

    private Data data;

    private Memento memento;

    private ObjectSpecification emptySpecification;


    private ObjectAdapter element1;





    @Before
    public void setUp() throws Exception {
        Logger.getRootLogger().setLevel(Level.OFF);


        // Root object specification
         emptySpecification = mockery.mock(ObjectSpecification.class, "empty specification");
        final ObjectSpecification rootSpecification = mockery.mock(ObjectSpecification.class);
        final CollectionFacet collectionFacet = mockery.mock(CollectionFacet.class);

        mockery.checking(new Expectations() {
            {
                atLeast(1).of(rootSpecification).isCollection();
                will(returnValue(true));

                atLeast(1).of(rootSpecification).getFullIdentifier();
                will(returnValue(TestObject[].class.getName()));

                atLeast(1).of(rootSpecification).getFacet(CollectionFacet.class);
                will(returnValue(collectionFacet));



                atLeast(0).of(emptySpecification).getFullIdentifier();
                will(returnValue(TestObject.class.getName()));


                /*

                atLeast(1).of(nameField).isPersisted();
                will(returnValue(true));

                atLeast(1).of(nameField).isOneToManyAssociation();
                will(returnValue(false));

                atLeast(1).of(nameField).getSpecification();
                will(returnValue(elementSpecification));

                atLeast(1).of(nameField).getId();
                will(returnValue("name-field"));

                atLeast(1).of(elementSpecification).isEncodeable();
                will(returnValue(true));

                atLeast(1).of(elementSpecification).getFacet(EncodeableFacet.class);
                will(returnValue(encodeableFacet));
                */
            }
        });


        // Root object
        rootAdapter = mockery.mock(ObjectAdapter.class);
//        rootObject = new TestObject("Harry");
//        final ObjectAdapter nameAdapter = mockery.mock(ObjectAdapter.class, "name");
        oid = mockery.mock(Oid.class);


        final TypeOfFacet typeOfFacet = mockery.mock(TypeOfFacet.class, "element 1");


        final Iterator<?> mockIterator = mockery.mock(Iterator.class);

        // object encoding
        mockery.checking(new Expectations() {


        {

               atLeast(1).of(collectionFacet).size(rootAdapter);
               will(returnValue(2));

               atLeast(1).of(collectionFacet).getTypeOfFacet();
               will(returnValue(typeOfFacet));

               atLeast(1).of(typeOfFacet).valueSpec();
               will(returnValue(rootSpecification));

               atLeast(1).of(collectionFacet).iterator(rootAdapter);
               will(returnValue(mockIterator));

               one(mockIterator).hasNext();
               will(returnValue(true));

               one(mockIterator).next();
               element1 = adapter("element", 1, false);
            will(returnValue(element1));

               one(mockIterator).hasNext();
               will(returnValue(true));

               one(mockIterator).next();
               will(returnValue(adapter("element", 2, false)));

               one(mockIterator).hasNext();
               will(returnValue(false));




               atLeast(1).of(rootAdapter).getSpecification();
               will(returnValue(rootSpecification));

               atLeast(1).of(rootAdapter).getOid();
               will(returnValue(oid));

               atLeast(1).of(rootAdapter).getResolveState();
               will(returnValue(ResolveState.RESOLVED));
/*
               atLeast(1).of(nameField).get(rootAdapter);
               will(returnValue(nameAdapter));

               */
          //     one(encodeableFacet).toEncodedString(nameAdapter);
          //     will(returnValue("_HARRY_"));


              /*
               atLeast(1).of(oid).isTransient();
               will(returnValue(false));

               atLeast(1).of(rootAdapter).getObject();
               will(returnValue(rootObject));

               one(mockPersistenceSession).recreateAdapter(oid, rootSpecification);
               will(returnValue(recreatedAdapter));

               atLeast(1).of(recreatedAdapter).getOid();
               will(returnValue(oid));

               one(recreatedAdapter).getResolveState();
               will(returnValue(ResolveState.GHOST));

               one(recreatedAdapter).changeState(ResolveState.UPDATING);

               atLeast(1).of(recreatedAdapter).getSpecification();
               will(returnValue(rootSpecification));

               atLeast(1).of(recreatedAdapter).getObject();
               will(returnValue(recreatedObject));

   /*
               one(mockAdapterManager).adapterFor("Harry", originalAdapter, specification.getAssociation("name"));
               will(returnValue(nameAdapter));

               atLeast(1).of(nameAdapter).getObject();
               will(returnValue("Harry"));
               */
           }
       });


        // object decoding
        mockery.checking(new Expectations() {
           {
               /*
               atLeast(1).of(oid).isTransient();
               will(returnValue(false));

               atLeast(1).of(rootAdapter).getObject();
               will(returnValue(rootObject));
/*
               one(mockPersistenceSession).recreateAdapter(oid, rootSpecification);
               will(returnValue(recreatedAdapter));

               atLeast(1).of(recreatedAdapter).getOid();
               will(returnValue(oid));

               one(recreatedAdapter).getResolveState();
               will(returnValue(ResolveState.GHOST));

               one(recreatedAdapter).changeState(ResolveState.UPDATING);

               atLeast(1).of(recreatedAdapter).getSpecification();
               will(returnValue(rootSpecification));

               atLeast(1).of(recreatedAdapter).getObject();
               will(returnValue(recreatedObject));

   /*
               one(mockAdapterManager).adapterFor("Harry", originalAdapter, specification.getAssociation("name"));
               will(returnValue(nameAdapter));

               atLeast(1).of(nameAdapter).getObject();
               will(returnValue("Harry"));
               */
           }
       });




        // Persistence Session
//        final ObjectReflector reflector = mockery.mock(ObjectReflector.class);
//        final PersistenceSession mockPersistenceSession = mockery.mock(PersistenceSession.class);
        final IsisSessionFactory sessionFactory = mockery.mock(IsisSessionFactory.class);
//        final AuthenticationSession mockSession = mockery.mock(AuthenticationSession.class);
//        final IsisSession session = mockery.mock(IsisSession.class);
    //   final AdapterManager mockAdapterManager = mockery.mock(AdapterManager.class);
/**
        mockery.checking(new Expectations() {
            {
                atLeast(1).of(sessionFactory).getSpecificationLoader();
                will(returnValue(reflector));

                atLeast(1).of(sessionFactory).openSession(mockSession);
                will(returnValue(session));

                atLeast(1).of(reflector).loadSpecification(TestObject.class.getName());
                will(returnValue(rootSpecification));

                atLeast(1).of(session).open();

                atLeast(1).of(session).getPersistenceSession();
                will(returnValue(mockPersistenceSession));

            }});
*/



        IsisContextStatic.createRelaxedInstance(sessionFactory);

 //      IsisContextStatic.getInstance().openSessionInstance(mockSession);

 //       final ObjectAdapter recreatedAdapter = mockery.mock(ObjectAdapter.class, "recreated");

 //       recreatedObject = new TestObject();



       /*
       returnedAdapter = mockery.mock(ObjectAdapter.class, "recreated adapter");
       final Oid returnedOid = mockery.mock(Oid.class, "recreated oid");

       mockery.checking(new Expectations() {
           {


               atLeast(1).of(mockPersistenceSession).recreateAdapter(oid, rootSpecification);
               will(returnValue(returnedAdapter));

               atLeast(1).of(returnedAdapter).getOid();
               will(returnValue(returnedOid));

           }});
*/




        memento = new Memento(rootAdapter);
        data = memento.getData();
     }





    public ObjectAdapter adapter(final String name, final int id, final boolean isTransient) {
        final ObjectAdapter object = mockery.mock(ObjectAdapter.class, name + id);
        final Oid oid = mockery.mock(Oid.class, name + "#" + id);
        mockery.checking(new Expectations() {
            {
                atLeast(0).of(object).getOid();
                will(returnValue(oid));

                atLeast(0).of(object).getResolveState();
                will(returnValue(ResolveState.TRANSIENT));

                atLeast(0).of(object).getSpecification();
                will(returnValue(emptySpecification));

                atLeast(0).of(oid).isTransient();
                will(returnValue(isTransient));
            }});
        return object;
    }







    @Ignore("currently failing - is no longer calling  isTransient on element #1 and element #2")
    @Test
    public void testOid() throws Exception {
        assertEquals(oid, data.getOid());
        mockery.assertIsSatisfied();
    }

    @Ignore("currently failing - is no longer calling  isTransient on element #1 and element #2")
    @Test
    public void testResolved() throws Exception {
        assertEquals(ResolveState.RESOLVED.name(), data.getResolveState());
        mockery.assertIsSatisfied();
    }

    @Ignore("currently failing - is no longer calling  isTransient on element #1 and element #2")
    @Test
    public void testClassName() throws Exception {
        assertEquals(TestObject[].class.getName(), data.getClassName());
        mockery.assertIsSatisfied();
    }

    @Ignore("currently failing - is no longer calling  isTransient on element #1 and element #2")
    @Test
    public void testDataType() throws Exception {
        assertEquals(CollectionData.class, data.getClass());
        mockery.assertIsSatisfied();
    }


    @Ignore("currently failing - is no longer calling  isTransient on element #1 and element #2")
    @Test
    public void testDataLength() throws Exception {
        assertEquals(2, ((CollectionData) data).elements.length);
        mockery.assertIsSatisfied();
    }


    @Ignore("currently failing - is no longer calling  isTransient on element #1 and element #2")
    @Test
    public void testData() throws Exception {
        final DataOutputStreamExtended mockOutputImpl = mockery.mock(DataOutputStreamExtended.class);

        final Oid oid1 = element1.getOid();
        final Oid oid2 = element1.getOid();

        mockery.checking(new Expectations() {
            {

                one(mockOutputImpl).writeUTF(TestObject.class.getName());
                one(mockOutputImpl).writeUTF(ResolveState.RESOLVED.name());
                one(mockOutputImpl).writeEncodable(oid);
                one(mockOutputImpl).writeUTF(TestObject[].class.getName());
                one(mockOutputImpl).writeInt(2);

                one(mockOutputImpl).writeUTF(TestObject.class.getName());
                one(mockOutputImpl).writeUTF(ResolveState.TRANSIENT.name());
                one(mockOutputImpl).writeEncodable(oid1);

                one(mockOutputImpl).writeUTF(TestObject.class.getName());
                one(mockOutputImpl).writeUTF(ResolveState.TRANSIENT.name());
                one(mockOutputImpl).writeEncodable(oid2);

                /*
                one(encoder).add("S");
                one(encoder).add("name-field");
                one(encoder).add("_HARRY_");
                */
            }});
        memento.encodedData(mockOutputImpl );
        mockery.assertIsSatisfied();
    }

}

