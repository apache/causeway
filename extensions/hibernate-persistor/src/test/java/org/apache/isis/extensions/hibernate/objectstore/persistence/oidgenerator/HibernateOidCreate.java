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


package org.apache.isis.extensions.hibernate.objectstore.persistence.oidgenerator;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.Serializable;

import org.junit.Test;

public class HibernateOidCreate {

    private final Serializable primaryKey = "one";
    private final long id = 2L;
    
    @Test
    public void createPersistentUsingSerializablePrimaryKeyIsSaved() {
        final HibernateOid oid = HibernateOid.createPersistent(HibernateOidCreate.class, primaryKey);
        
        assertEquals(primaryKey, oid.getPrimaryKey());
    }

    @Test
    public void createTransientUsingSerializablePrimaryKeyIsSaved() {
        final HibernateOid oid = HibernateOid.createTransient(HibernateOidCreate.class, primaryKey);
        
        assertEquals(primaryKey, oid.getPrimaryKey());
    }

    @Test
    public void createPersistentStoresClassName() {
        final HibernateOid oid = HibernateOid.createPersistent(HibernateOidCreate.class, primaryKey);
        
        assertEquals(HibernateOidCreate.class.getName(), oid.getClassName());
    }

    @Test
    public void createTransientStoresClassName() {
        final HibernateOid oid = HibernateOid.createTransient(HibernateOidCreate.class, primaryKey);
        
        assertEquals(HibernateOidCreate.class.getName(), oid.getClassName());
    }

    @Test
    public void createPersistentUsesSerializablePrimaryKeyAsTheHibernateId() {
        final HibernateOid oid = HibernateOid.createPersistent(HibernateOidCreate.class, primaryKey);
        
        assertEquals(primaryKey, oid.getHibernateId());
    }

    @Test
    public void createTransientHasANullHibernateId() {
        final HibernateOid oid = HibernateOid.createTransient(HibernateOidCreate.class, primaryKey);
        
        assertNull(oid.getHibernateId());
    }

    @Test
    public void createPersistentHasNoPrevious() {
        final HibernateOid oid = HibernateOid.createPersistent(HibernateOidCreate.class, primaryKey);
        
        assertFalse(oid.hasPrevious());
    }

    @Test
    public void createTransientHasNoPrevious() {
        final HibernateOid oid = HibernateOid.createTransient(HibernateOidCreate.class, primaryKey);

        assertFalse(oid.hasPrevious());
    }

    
    @Test
    public void createPersistentIsNotTransient() {
        final HibernateOid oid = HibernateOid.createPersistent(HibernateOidCreate.class, primaryKey);
        
        assertFalse(oid.isTransient());
    }

    @Test
    public void createTransientIsTransient() {
        final HibernateOid oid = HibernateOid.createTransient(HibernateOidCreate.class, primaryKey);
        
        assertTrue(oid.isTransient());
    }


    @Test
    public void createPersistentUsingLongIdIsConvertedImplicitlyAndUsedDirectlyAsSerializablePrimaryKey() {
        final HibernateOid oid = HibernateOid.createPersistent(HibernateOidCreate.class, id);
        
        Serializable primaryKey = oid.getPrimaryKey();
        assertThat(primaryKey, is(Long.class));
        Long primaryKeyAsLong = (Long) primaryKey;
        assertThat(primaryKeyAsLong.longValue(), is(id));
    }

    @Test
    public void createTransientUsingLongIdIsConvertedWithOffsetToLongAndUsedAsSerializablePrimaryKey() {
        final HibernateOid oid = HibernateOid.createTransient(HibernateOidCreate.class, id);
        
        Serializable primaryKey = oid.getPrimaryKey();
        assertThat(primaryKey, is(Long.class));
        Long primaryKeyAsLong = (Long) primaryKey;
        assertThat(primaryKeyAsLong.longValue(), is(id + HibernateOid.STANDARD_OFFSET));
    }


    
}
