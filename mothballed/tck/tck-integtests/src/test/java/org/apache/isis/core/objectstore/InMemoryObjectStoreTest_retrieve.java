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

package org.apache.isis.core.objectstore;

import java.util.List;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.apache.isis.core.integtestsupport.IsisSystemWithFixtures;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.oid.OidMarshaller;
import org.apache.isis.core.metamodel.adapter.oid.RootOidDefault;
import org.apache.isis.core.metamodel.adapter.oid.TypedOid;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.runtime.persistence.ObjectNotFoundException;
import org.apache.isis.core.runtime.persistence.query.PersistenceQueryFindAllInstances;
import org.apache.isis.core.runtime.persistence.query.PersistenceQueryFindByTitle;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.core.tck.dom.refs.SimpleEntity;

import static org.junit.Assert.*;

public class InMemoryObjectStoreTest_retrieve {

    @Rule
    public IsisSystemWithFixtures iswf = IsisSystemWithFixtures.builder().build();
    
    protected ObjectAdapter epv2Adapter;
    protected ObjectAdapter epv3Adapter;
    protected ObjectAdapter epv4Adapter;
    protected ObjectSpecification epvSpecification;

    protected InMemoryObjectStore getStore() {
        return (InMemoryObjectStore) IsisContext.getPersistenceSession().getObjectStore();
    }

    @Before
    public void setUpFixtures() throws Exception {
        epv2Adapter = iswf.adapterFor(iswf.fixtures.smpl2);
        epv3Adapter = iswf.adapterFor(iswf.fixtures.smpl3);
        epv4Adapter = iswf.adapterFor(iswf.fixtures.smpl4);
        epvSpecification = iswf.loadSpecification(SimpleEntity.class);
    }

    @Test
    public void getObject_whenDoesNotExist() {
        final TypedOid oid = RootOidDefault.deString("SMPL:10", new OidMarshaller());
        try {
            getStore().loadInstanceAndAdapt(oid);
            fail();
        } catch (final ObjectNotFoundException expected) {
        }
    }

    @Test
    public void getObject_whenExists_returnsAdapter() throws Exception {
        
        // given
        iswf.persist(iswf.fixtures.smpl2);
        iswf.bounceSystem();

        final ObjectAdapter retrievedAdapter = getStore().loadInstanceAndAdapt((TypedOid) epv2Adapter.getOid());
        
        assertNotSame(epv2Adapter, retrievedAdapter);
        assertEquals(((SimpleEntity)epv2Adapter.getObject()).getName(), ((SimpleEntity)retrievedAdapter.getObject()).getName());
        assertEquals(epv2Adapter.getOid(), retrievedAdapter.getOid());
    }

    @Test
    public void getInstances_whenDoesNotExist() throws Exception {
        final List<ObjectAdapter> retrievedAdapters = getStore().loadInstancesAndAdapt(new PersistenceQueryFindByTitle(epvSpecification, epv2Adapter.titleString()));
        assertEquals(0, retrievedAdapters.size());
    }

    @Test
    public void getInstances_findByTitle() throws Exception {
        // given
        iswf.persist(iswf.fixtures.smpl2);
        iswf.bounceSystem();

        // when
        final List<ObjectAdapter> retrievedAdapters = getStore().loadInstancesAndAdapt(new PersistenceQueryFindByTitle(epvSpecification, epv2Adapter.titleString()));
        
        // then
        assertEquals(1, retrievedAdapters.size());
        final ObjectAdapter retrievedAdapter = retrievedAdapters.get(0);

        assertNotSame(epv2Adapter, retrievedAdapter);
        assertEquals(((SimpleEntity)epv2Adapter.getObject()).getName(), ((SimpleEntity)retrievedAdapter.getObject()).getName());
        assertEquals(epv2Adapter.getOid(), retrievedAdapter.getOid());
    }

    @Test
    public void getInstances_findAll() throws Exception {
        // given
        iswf.persist(iswf.fixtures.smpl2);
        iswf.bounceSystem();

        // when
        final List<ObjectAdapter> retrievedAdapters = getStore().loadInstancesAndAdapt(new PersistenceQueryFindAllInstances(epvSpecification));
        
        // then
        assertEquals(1, retrievedAdapters.size());
        final ObjectAdapter retrievedAdapter = retrievedAdapters.get(0);

        assertNotSame(epv2Adapter, retrievedAdapter);
        assertEquals(((SimpleEntity)epv2Adapter.getObject()).getName(), ((SimpleEntity)retrievedAdapter.getObject()).getName());
        assertEquals(epv2Adapter.getOid(), retrievedAdapter.getOid());
    }

    @Ignore // gonna retire soon anyway...
    @Test
    public void getInstances_findRange() throws Exception {
        // given
        iswf.persist(iswf.fixtures.smpl1); // 0
        iswf.persist(iswf.fixtures.smpl2); // 1
        iswf.persist(iswf.fixtures.smpl3); // 2 <- this one
        iswf.persist(iswf.fixtures.smpl4); // 3 <- this one
        iswf.bounceSystem();

        // when
        final List<ObjectAdapter> retrievedAdapters = getStore().loadInstancesAndAdapt(new PersistenceQueryFindAllInstances(epvSpecification, 2, 2));
        
        // then
        assertEquals(2, retrievedAdapters.size());
        final ObjectAdapter retrievedAdapter = retrievedAdapters.get(0);

        assertNotSame(epv4Adapter, retrievedAdapter);
        assertEquals(((SimpleEntity)epv4Adapter.getObject()).getName(), ((SimpleEntity)retrievedAdapter.getObject()).getName());
        assertEquals(epv4Adapter.getOid(), retrievedAdapter.getOid());
    }
    
    
    @Test
    public void hasInstances_whenEmpty() throws Exception {
        assertEquals(false, getStore().hasInstances(epvSpecification));
    }

    @Test
    public void hasInstances_whenHasSome() throws Exception {
        iswf.persist(iswf.fixtures.smpl2);
        iswf.bounceSystem();

        assertEquals(true, getStore().hasInstances(epvSpecification));
    }


}
