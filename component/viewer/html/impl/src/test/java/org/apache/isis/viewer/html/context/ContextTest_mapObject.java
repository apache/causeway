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
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import org.apache.isis.core.integtestsupport.IsisSystemWithFixtures;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.ResolveState;
import org.apache.isis.core.metamodel.adapter.mgr.AdapterManager;
import org.apache.isis.core.metamodel.adapter.oid.Oid;
import org.apache.isis.core.metamodel.adapter.version.Version;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.core.runtime.system.persistence.PersistenceSession;
import org.apache.isis.viewer.html.action.ActionException;

public class ContextTest_mapObject {

    @Rule
    public IsisSystemWithFixtures iswf = IsisSystemWithFixtures.builder().build();

    private ObjectAdapter originalAdapter;
    private Oid oid;
    private ObjectAdapter restoredAdapter;

    private Context context;

    @Before
    public void setUp() throws Exception {
        Logger.getRootLogger().setLevel(Level.OFF);
        context = new Context(null);
    }
    
    @Test
    public void mapObject_then_restoreAllObjectsToLoader() throws Exception {

        // given
        originalAdapter = getAdapterManager().adapterFor(iswf.fixtures.smpl1);
        oid = originalAdapter.getOid();
        context.mapObject(originalAdapter);
        
        iswf.bounceSystem();
        
        // when
        context.restoreAllObjectsToLoader();
        restoredAdapter = getAdapterManager().getAdapterFor(oid);

        assertNotNull("loaders is missing the object", getAdapterManager().getAdapterFor(oid));
        assertNotSame("expect the loader to have a new adapter", originalAdapter, restoredAdapter);
        
        // then
        assertEquals(originalAdapter.getOid(), restoredAdapter.getOid());
        assertNotSame(originalAdapter, restoredAdapter);
        assertEquals(originalAdapter.getObject().getClass(), restoredAdapter.getObject().getClass());
        assertEquals(originalAdapter.getVersion(), restoredAdapter.getVersion());
        assertEquals(ResolveState.TRANSIENT, restoredAdapter.getResolveState());
    }

    
    @Test
    public void mapObject_forTransient_then_getMappedObject_byId() {

        // given
        originalAdapter = getAdapterManager().adapterFor(iswf.fixtures.smpl1);
        oid = originalAdapter.getOid();
        final String id = context.mapObject(originalAdapter);
        
        // when
        restoredAdapter = context.getMappedObject(id);
        
        // then
        assertEquals(originalAdapter, restoredAdapter);
    }


    @Test
    public void mapObject_forPersistent_then_getMappedObject_byId() {

        // given
        iswf.persist(iswf.fixtures.smpl1);

        originalAdapter = getAdapterManager().adapterFor(iswf.fixtures.smpl1);
        oid = originalAdapter.getOid();
        final String id = context.mapObject(originalAdapter);
        
        // when
        restoredAdapter = context.getMappedObject(id);
        
        // then
        assertEquals(originalAdapter, restoredAdapter);
    }


    @Test
    public void getMappedObject_forPersistent_whenChanged() throws Exception {

        // given
        iswf.persist(iswf.fixtures.smpl1);
        
        originalAdapter = getAdapterManager().adapterFor(iswf.fixtures.smpl1);
        final Oid oid = originalAdapter.getOid();
        final String id = context.mapObject(originalAdapter);
        
        final Version version = originalAdapter.getVersion();
        
        iswf.fixtures.smpl1.setName("changed date");
        iswf.bounceSystem(); // does a commit, which will bump the version
        
        originalAdapter = getAdapterManager().getAdapterFor(oid);
        
        final Version version2 = originalAdapter.getVersion();
        
        assertThat(version.different(version2), is(true));
        
        
        // when
        restoredAdapter = context.getMappedObject(id);
        final String message = context.getMessage(1);
        
        // then
        assertEquals("Reloaded object " + restoredAdapter.titleString(), message);
    }

    
    @Test
    public void mapObject_forTransient_alwaysReturnsSameId() {

        // given
        originalAdapter = getAdapterManager().adapterFor(iswf.fixtures.smpl1);
        
        // when
        final String id = context.mapObject(originalAdapter);
        final String id2 = context.mapObject(originalAdapter);
        
        // then
        assertEquals(id, id2);
    }

    
    @Test
    public void mapObject_forPersistent_alwaysReturnsSameId() {

        // given
        iswf.persist(iswf.fixtures.smpl1);
        
        originalAdapter = getAdapterManager().adapterFor(iswf.fixtures.smpl1);
        
        // when
        final String id = context.mapObject(originalAdapter);
        final String id2 = context.mapObject(originalAdapter);
        
        // then
        assertEquals(id, id2);
    }

    
    @Test
    public void mapObject_forTransient_returnsDifferentIdsForDifferentObjects() {

        // when
        final String id = context.mapObject(getAdapterManager().adapterFor(iswf.fixtures.smpl1));
        final String id2 = context.mapObject(getAdapterManager().adapterFor(iswf.fixtures.smpl2));
        
        // then
        assertThat(id, not(equalTo(id2)));
    }


    @Test
    public void mapObject_forPersistent_returnsDifferentIdsForDifferentObjects() {

        // given
        iswf.persist(iswf.fixtures.smpl1);
        iswf.persist(iswf.fixtures.smpl2);
        
        // when
        final String id = context.mapObject(getAdapterManager().adapterFor(iswf.fixtures.smpl1));
        final String id2 = context.mapObject(getAdapterManager().adapterFor(iswf.fixtures.smpl2));
        
        // then
        assertThat(id, not(equalTo(id2)));
    }

    
    @Test
    public void restoreAllObjectsToLoader_restoredAsGhosts() throws Exception {

        // given
        iswf.persist(iswf.fixtures.smpl1);
        originalAdapter = getAdapterManager().adapterFor(iswf.fixtures.smpl1);
        oid = originalAdapter.getOid();
        
        iswf.bounceSystem();
        
        // when
        context.restoreAllObjectsToLoader();
        
        // then
        restoredAdapter = getAdapterManager().getAdapterFor(oid);
        
        assertEquals("expect versions to match", originalAdapter.getVersion(), restoredAdapter.getVersion());
        assertEquals(ResolveState.GHOST, restoredAdapter.getResolveState());
    }

    
    
    @Test
    public void testExceptionThrownWhenNoActionForIdentity() {
        
        iswf.persist(iswf.fixtures.smpl1);
        iswf.persist(iswf.fixtures.smpl2);
        
        // when
        context.mapObject(getAdapterManager().adapterFor(iswf.fixtures.smpl1));

        try {
            assertNull(context.getMappedObject("NON-EXISTENT-ID"));
            fail();
        } catch (final ActionException ex) {
            // expected
        }
    }

    

    private PersistenceSession getPersistenceSession() {
        return IsisContext.getPersistenceSession();
    }

    private AdapterManager getAdapterManager() {
        return getPersistenceSession().getAdapterManager();
    }

}

