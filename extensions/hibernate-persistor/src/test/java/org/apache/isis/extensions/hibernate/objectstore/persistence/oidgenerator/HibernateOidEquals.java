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

import org.junit.Test;

public class HibernateOidEquals {

    @Test
    public void testEqualsObject() {
        final HibernateOid one = HibernateOid.createPersistent(HibernateOidEquals.class, "one");
        final HibernateOid two = HibernateOid.createPersistent(HibernateOidEquals.class, "two");
        final HibernateOid oneAgain = HibernateOid.createPersistent(HibernateOidEquals.class, "one");
        final HibernateOid oneDifferentClass = HibernateOid.createPersistent(Object.class, "one");

        assertEquals(one, oneAgain);
        assertFalse(one.equals(two));
        assertFalse(one.equals(oneDifferentClass));
    }

}
