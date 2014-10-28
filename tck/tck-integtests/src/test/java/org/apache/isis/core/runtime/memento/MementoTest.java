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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import org.apache.isis.core.commons.encoding.DataInputStreamExtended;
import org.apache.isis.core.commons.encoding.DataOutputStreamExtended;
import org.apache.isis.core.integtestsupport.IsisSystemWithFixtures;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.core.tck.dom.refs.BaseEntity;
import org.apache.isis.core.tck.dom.refs.ParentEntity;
import org.apache.isis.core.tck.dom.refs.ReferencingEntity;
import org.apache.isis.core.tck.dom.refs.SimpleEntity;

public class MementoTest {

    @Rule
    public IsisSystemWithFixtures iswf = IsisSystemWithFixtures.builder().build();
    
    private ObjectAdapter originalAdapterForEpv1;
    private ObjectAdapter originalAdapterForEpr1;
    private ObjectAdapter originalAdapterForEpc1;
    
    private ObjectAdapter recreatedAdapter;

    private Memento mementoForEpv1;
    private Memento mementoForEpr1;
    private Memento mementoForEpc1;
    
    private byte[] bytesForEpv1;
    private byte[] bytesForEpr1;
    private byte[] bytesForEpc1;

    private ByteArrayInputStream bais;
    

    @Before
    public void setUpSystem() throws Exception {
        
//        final Logger logger = LoggerFactory.getLogger(FieldType.class);
//        logger.setLevel(Level.DEBUG);
//        logger.addAppender(new ConsoleAppender());
//        BasicConfigurator.configure();

        iswf.fixtures.smpl1.setName("Fred");
        iswf.fixtures.smpl2.setName("Harry");
        
        iswf.fixtures.rfcg1_a1.setName("Tom");
        
        iswf.fixtures.rfcg1.setReference(iswf.fixtures.smpl1);
        iswf.fixtures.rfcg1.setAggregatedReference(iswf.fixtures.rfcg1_a1);
        
        iswf.fixtures.prnt1.getHomogeneousCollection().add(iswf.fixtures.smpl1);
        iswf.fixtures.prnt1.getHomogeneousCollection().add(iswf.fixtures.smpl2);
        
        iswf.fixtures.prnt1.getHeterogeneousCollection().add(iswf.fixtures.smpl1);
        iswf.fixtures.prnt1.getHeterogeneousCollection().add(iswf.fixtures.rfcg1);
        
        originalAdapterForEpv1 = iswf.adapterFor(iswf.fixtures.smpl1);
        originalAdapterForEpr1 = iswf.adapterFor(iswf.fixtures.rfcg1);
        originalAdapterForEpc1 = iswf.adapterFor(iswf.fixtures.prnt1);

        mementoForEpv1 = new Memento(originalAdapterForEpv1);
        mementoForEpr1 = new Memento(originalAdapterForEpr1);
        mementoForEpc1 = new Memento(originalAdapterForEpc1);
        
        bytesForEpv1 = toBytes(mementoForEpv1);
        bytesForEpr1 = toBytes(mementoForEpr1);
        bytesForEpc1 = toBytes(mementoForEpc1);
    
        iswf.tearDownSystem();
        
//        logger.debug("*************************************");
        
        iswf.setUpSystem();
        
        mementoForEpv1 = fromBytes(bytesForEpv1);
        mementoForEpr1 = fromBytes(bytesForEpr1);
        mementoForEpc1 = fromBytes(bytesForEpc1);

        IsisContext.getTransactionManager().startTransaction();
    }


    @After
    public void tearDown() throws Exception {
        IsisContext.getTransactionManager().endTransaction();
    }
    
    private Memento fromBytes(final byte[] bytes) throws IOException {
        bais = new ByteArrayInputStream(bytes);
        DataInputStreamExtended input = new DataInputStreamExtended(bais);
        final Memento recreate = Memento.recreateFrom(input);
        return recreate;
    }


    private static byte[] toBytes(final Memento memento) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStreamExtended output = new DataOutputStreamExtended(baos);
        memento.encodedData(output);
        return baos.toByteArray();
    }


    @Test
    public void recreateObject_adaptersAreNotSame() throws Exception {

        recreatedAdapter = mementoForEpv1.recreateObject();
        
        assertNotSame(originalAdapterForEpv1, recreatedAdapter);
    }

    @Test
    public void recreateObject_getOid_areEquals() throws Exception {
        recreatedAdapter = mementoForEpv1.recreateObject();

        assertEquals(originalAdapterForEpv1.getOid(), recreatedAdapter.getOid());
    }

    @Test
    public void recreateObject_getSpecification_isSame() throws Exception {
        recreatedAdapter = mementoForEpv1.recreateObject();
        
        final ObjectSpecification specification = originalAdapterForEpv1.getSpecification();
        final ObjectSpecification recreatedSpecification = recreatedAdapter.getSpecification();
        assertSame(specification, recreatedSpecification);
    }

    @Test
    public void recreateObject_valuePreserved() throws Exception {
        recreatedAdapter = mementoForEpv1.recreateObject();
        final SimpleEntity recreatedObject = (SimpleEntity)recreatedAdapter.getObject();
        assertEquals("Fred", recreatedObject.getName());
    }

    @Test
    public void recreateObject_referencePreserved() throws Exception {
        recreatedAdapter = mementoForEpr1.recreateObject();
        final ReferencingEntity recreatedObject = (ReferencingEntity)recreatedAdapter.getObject();
        final SimpleEntity reference1 = recreatedObject.getReference();
        assertNotNull(reference1);
        
        assertThat("Fred", equalTo(reference1.getName()));
    }

    @Test
    public void recreateObject_homogeneousCollectionPreserved() throws Exception {
        recreatedAdapter = mementoForEpc1.recreateObject();
        final ParentEntity recreatedObject = (ParentEntity)recreatedAdapter.getObject();
        final List<SimpleEntity> homogenousCollection = recreatedObject.getHomogeneousCollection();
        assertNotNull(homogenousCollection);
        
        assertThat(homogenousCollection.size(), is(2));
        assertThat(homogenousCollection.get(0).getName(), is("Fred"));
        assertThat(homogenousCollection.get(1).getName(), is("Harry"));
    }

    @Test
    public void recreateObject_heterogeneousCollectionPreserved() throws Exception {
        recreatedAdapter = mementoForEpc1.recreateObject();
        final ParentEntity recreatedObject = (ParentEntity)recreatedAdapter.getObject();
        final List<BaseEntity> hetrogenousCollection = recreatedObject.getHeterogeneousCollection();
        assertNotNull(hetrogenousCollection);
        
        assertThat(hetrogenousCollection.size(), is(2));
        final SimpleEntity firstObj = (SimpleEntity)hetrogenousCollection.get(0);
        assertThat(firstObj.getName(), is("Fred"));
        
        final ReferencingEntity secondObj = (ReferencingEntity)hetrogenousCollection.get(1);
        final SimpleEntity reference1 = secondObj.getReference();
        assertThat(reference1.getName(), is("Fred"));
        
        assertSame(firstObj, reference1);
    }

    @Test
    public void recreateObject_whenNull() throws Exception {
        final Memento memento = new Memento(null);
        ObjectAdapter returnedAdapter = memento.recreateObject();
        assertNull(returnedAdapter);
    }

    
}
