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

package org.apache.isis.viewer.html.context;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.util.List;

import com.google.common.collect.Lists;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import org.apache.isis.core.integtestsupport.IsisSystemWithFixtures;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.mgr.AdapterManager;
import org.apache.isis.core.metamodel.adapter.oid.Oid;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.core.runtime.system.persistence.PersistenceSession;
import org.apache.isis.core.tck.dom.refs.SimpleEntity;
import org.apache.isis.viewer.html.action.ActionException;

public class ContextTest_mapCollection {

    @Rule
    public IsisSystemWithFixtures iswf = IsisSystemWithFixtures.builder().build();

    private Context context;

    @Before
    public void setUp() throws Exception {
        Logger.getRootLogger().setLevel(Level.OFF);
        context = new Context(null);
    }

    @Test
    public void getMappedCollection_forNonExistentId() {


        try {
            assertNull(context.getMappedCollection("NON-EXISTENT-ID"));
            fail();
        } catch (final ActionException ex) {
            // expected
        }
    }

    @Test
    public void mapCollection_then_getMappedCollection() throws Exception {

        // given
        iswf.persist(iswf.fixtures.smpl1);
        iswf.persist(iswf.fixtures.smpl2);

        final List<SimpleEntity> collection = Lists.newArrayList();
        collection.add(iswf.fixtures.smpl1);
        collection.add(iswf.fixtures.smpl2);
        
        final Oid oid1 = getAdapterManager().adapterFor(iswf.fixtures.smpl1).getOid();
        final Oid oid2 = getAdapterManager().adapterFor(iswf.fixtures.smpl2).getOid();
        
        final ObjectAdapter collectionAdapter = getAdapterManager().adapterFor(collection);
        final String id = context.mapCollection(collectionAdapter);
        
        iswf.bounceSystem();

        // when
        final ObjectAdapter mappedCollection = context.getMappedCollection(id);
        
        // then
        final List<?> list = (List<?>) mappedCollection.getObject();

        final Oid oid1Remapped = getAdapterManager().adapterFor(list.get(0)).getOid();
        final Oid oid2Remapped = getAdapterManager().adapterFor(list.get(1)).getOid();
        assertEquals(oid1, oid1Remapped);
        assertEquals(oid2, oid2Remapped);
    }

    

    @Test
    public void testRegisteredCollectionReturnSameIdentityForSameCollection() {
        
        // given
        iswf.persist(iswf.fixtures.smpl1);
        iswf.persist(iswf.fixtures.smpl2);

        final List<SimpleEntity> collection = Lists.newArrayList();
        collection.add(iswf.fixtures.smpl1);
        collection.add(iswf.fixtures.smpl2);
        
        final ObjectAdapter collectionAdapter = getAdapterManager().adapterFor(collection);
        
        // when
        final String id = context.mapCollection(collectionAdapter);
        final String id2 = context.mapCollection(collectionAdapter);
        
        // then
        assertEquals(id, id2);
    }

    @Test
    public void testRegisteredCollectionReturnDifferentIdentityForDifferentCollection() {

        // given
        iswf.persist(iswf.fixtures.smpl1);
        iswf.persist(iswf.fixtures.smpl2);

        final List<SimpleEntity> collection1 = Lists.newArrayList();
        collection1.add(iswf.fixtures.smpl1);
        final ObjectAdapter collection1Adapter = getAdapterManager().adapterFor(collection1);

        final List<SimpleEntity> collection2 = Lists.newArrayList();
        collection2.add(iswf.fixtures.smpl2);
        final ObjectAdapter collection2Adapter = getAdapterManager().adapterFor(collection2);

        // when
        final String id = context.mapCollection(collection1Adapter);
        final String id2 = context.mapCollection(collection2Adapter);
        
        // then
        assertThat(id, not(equalTo(id2)));
    }

    
    private PersistenceSession getPersistenceSession() {
        return IsisContext.getPersistenceSession();
    }

    private AdapterManager getAdapterManager() {
        return getPersistenceSession().getAdapterManager();
    }

}
