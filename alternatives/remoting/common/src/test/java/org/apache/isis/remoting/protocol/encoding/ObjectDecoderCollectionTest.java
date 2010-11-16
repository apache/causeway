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


package org.apache.isis.remoting.protocol.encoding;

import java.util.Collection;
import java.util.Enumeration;
import java.util.Vector;

import org.apache.commons.collections.CollectionUtils;
import org.apache.isis.core.commons.exceptions.NotYetImplementedException;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.facets.collections.modify.CollectionFacet;
import org.apache.isis.core.metamodel.testspec.TestProxySpecification;
import org.apache.isis.core.progmodel.facets.collections.modify.CollectionFacetAbstract;
import org.apache.isis.core.runtime.persistence.adaptermanager.ObjectToAdapterTransformer;
import org.apache.isis.core.runtime.testsystem.ProxyJunit3TestCase;
import org.apache.isis.core.runtime.testsystem.TestPojo;
import org.apache.isis.core.runtime.testsystem.TestProxyOid;
import org.apache.isis.core.runtime.testsystem.TestProxyVersion;
import org.apache.isis.remoting.data.DummyCollectionData;
import org.apache.isis.remoting.data.DummyObjectData;
import org.apache.isis.remoting.data.common.CollectionData;
import org.apache.isis.remoting.data.common.ObjectData;
import org.apache.isis.remoting.data.common.ReferenceData;
import org.apache.isis.remoting.protocol.internal.FieldOrderCache;
import org.apache.isis.remoting.protocol.internal.ObjectDeserializer;


public class ObjectDecoderCollectionTest extends ProxyJunit3TestCase {

    private ObjectDeserializer deserializer;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        FieldOrderCache fieldOrderCache = null;  // TODO: should provide a mock here?
		deserializer = new ObjectDeserializer(fieldOrderCache);

        final TestProxySpecification specification = system.getSpecification(Vector.class);
        specification.addFacet(new CollectionFacetAbstract(specification) {

            public void init(final ObjectAdapter collection, final ObjectAdapter[] initData) {
                for (int i = 0; i < initData.length; i++) {
                    collectionOfUnderlying(collection).add(initData[i].getObject());
                }
            }

            @SuppressWarnings("unchecked")
            public Collection<ObjectAdapter> collection(ObjectAdapter collectionAdapter) {
                Collection<Object> collection = collectionOfUnderlying(collectionAdapter);
                return CollectionUtils.collect(collection, new ObjectToAdapterTransformer());
            }

            public ObjectAdapter firstElement(final ObjectAdapter collection) {
                throw new NotYetImplementedException();
            }

            public int size(final ObjectAdapter collection) {
                return collectionOfUnderlying(collection).size();
            }

            @SuppressWarnings("unchecked")
            private Collection<Object> collectionOfUnderlying(final ObjectAdapter collectionNO) {
                return (Collection<Object>) collectionNO.getObject();
            }

        });
    }

    public void testRecreateEmptyCollection() {
        final TestProxyOid collectionOid = new TestProxyOid(123);
        final ReferenceData[] elementData = null;
        final CollectionData data = new DummyCollectionData(collectionOid, Vector.class.getName(), TestPojo.class.getName(),
                elementData, new TestProxyVersion());

        final ObjectAdapter adapter = deserializer.deserialize(data);

        final Vector restoredCollection = (Vector) adapter.getObject();
        assertEquals(0, restoredCollection.size());

        final CollectionFacet facet = adapter.getSpecification().getFacet(CollectionFacet.class);
        assertEquals(0, facet.size(adapter));
    }

    public void testRecreateCollection() {
        final ReferenceData elements[] = new ObjectData[2];
        final TestProxyOid element0Oid = new TestProxyOid(345, true);
        elements[0] = new DummyObjectData(element0Oid, TestPojo.class.getName(), false, new TestProxyVersion(3));
        final TestProxyOid element1Oid = new TestProxyOid(678, true);
        elements[1] = new DummyObjectData(element1Oid, TestPojo.class.getName(), false, new TestProxyVersion(7));

        final TestProxyOid collectionOid = new TestProxyOid(123);
        final CollectionData data = new DummyCollectionData(collectionOid, Vector.class.getName(), TestPojo.class.getName(),
                elements, new TestProxyVersion());

        final ObjectAdapter adapter = deserializer.deserialize(data);

        final Vector restoredCollection = (Vector) adapter.getObject();
        assertEquals(2, restoredCollection.size());

        final CollectionFacet facet = adapter.getSpecification().getFacet(CollectionFacet.class);
        final Enumeration elements2 = facet.elements(adapter);
        final ObjectAdapter element0 = (ObjectAdapter) elements2.nextElement();
        final ObjectAdapter element2 = (ObjectAdapter) elements2.nextElement();

        assertNotNull(element0.getObject());
        assertNotNull(element2.getObject());

        assertEquals(TestPojo.class, element0.getObject().getClass());
        assertEquals(TestPojo.class, element2.getObject().getClass());

        assertEquals(new TestProxyOid(678, true), element2.getOid());
        assertEquals(new TestProxyOid(345, true), element0.getOid());

        // version not set as there is no field data for elements
        // assertEquals(new DummyVersion(3), adapter.elementAt(0).getVersion());
        // assertEquals(new DummyVersion(7), adapter.elementAt(1).getVersion());
    }
}
