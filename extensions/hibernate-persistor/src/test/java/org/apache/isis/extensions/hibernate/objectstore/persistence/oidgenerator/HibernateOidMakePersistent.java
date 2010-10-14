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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.Serializable;

import org.junit.Test;

public class HibernateOidMakePersistent {

    private final Serializable primaryKey = "one";
    private final long id = 2L;
    

    @Test
    public void noLongerTransientPersistent() {
        final HibernateOid oid = HibernateOid.createTransient(Object.class, 2L);
        
        assertTrue(oid.isTransient());

        oid.setHibernateId("one");
        oid.makePersistent();
        
        assertFalse(oid.isTransient());
    }

    @Test
    public void getPreviousPopulatedAndIsEqualToCopy() {
        final HibernateOid oid = HibernateOid.createTransient(Object.class, 2L);
        final HibernateOid oidCopy = HibernateOid.createPersistent(Object.class, "x");
        oidCopy.copyFrom(oid); // for later
        
        assertFalse(oid.hasPrevious());

        oid.setHibernateId("one");
        oid.makePersistent();
        
        assertTrue(oid.hasPrevious());
        assertEquals(oidCopy, oid.getPrevious());
    }

    @Test
    public void setHibernateIdIsStored() {
        Serializable hibernateId = "one";
        
        final HibernateOid oid = HibernateOid.createTransient(Object.class, 2L);
        
        assertNull(oid.getHibernateId());

        oid.setHibernateId(hibernateId);
        oid.makePersistent();
        
        assertEquals(hibernateId, oid.getHibernateId());
    }


    @Test
    public void equalToExpected() {
        final HibernateOid oid = HibernateOid.createTransient(Object.class, 2L);
        
        oid.setHibernateId("one");
        oid.makePersistent();
        
        final HibernateOid expectedPersistent = HibernateOid.createPersistent(Object.class, "one");
        assertEquals(expectedPersistent, oid);
    }

}
