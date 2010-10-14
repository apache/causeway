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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class HibernateOidCopyFrom {
    

    @Test
    public void isEquals() {
        final HibernateOid one = HibernateOid.createPersistent(HibernateOidCopyFrom.class, "one");
        final HibernateOid copy = HibernateOid.createTransient(Object.class, 2L);

        assertFalse(one.equals(copy));
        copy.copyFrom(one);
        assertEquals(one, copy);
    }

    @Test
    public void syncsTheHibernateId() {
        final HibernateOid one = HibernateOid.createPersistent(HibernateOidCopyFrom.class, "one");
        final HibernateOid copy = HibernateOid.createTransient(Object.class, 2L);

        copy.copyFrom(one);
        assertEquals(one.getHibernateId(), copy.getHibernateId());
    }


    @Test
    public void transientStateIsCopiedOver() {
        final HibernateOid oid = HibernateOid.createTransient(Object.class, 2L);
        final HibernateOid oidCopy = HibernateOid.createPersistent(Object.class, "x");

        assertNull(oid.getHibernateId());
        assertTrue(oid.isTransient());

        assertNotNull(oidCopy.getHibernateId());
        assertFalse(oidCopy.isTransient());

        oidCopy.copyFrom(oid);
        
        assertNull(oidCopy.getHibernateId());
        assertTrue(oidCopy.isTransient());
    }

    @Test
    public void persistentStateIsCopiedOver() {
        final HibernateOid oid = HibernateOid.createPersistent(Object.class, "x");
        final HibernateOid oidCopy = HibernateOid.createTransient(Object.class, 2L);

        assertNotNull(oid.getHibernateId());
        assertFalse(oid.isTransient());

        assertNull(oidCopy.getHibernateId());
        assertTrue(oidCopy.isTransient());

        oidCopy.copyFrom(oid);
        
        assertNotNull(oidCopy.getHibernateId());
        assertFalse(oidCopy.isTransient());
    }


    @Test
    public void previousStateIsCopiedOver() {
        final HibernateOid oid = HibernateOid.createTransient(Object.class, 2L);
        final HibernateOid oidCopy = HibernateOid.createPersistent(Object.class, "x");

        oidCopy.copyFrom(oid);
        assertFalse(oid.hasPrevious());
    }

}
