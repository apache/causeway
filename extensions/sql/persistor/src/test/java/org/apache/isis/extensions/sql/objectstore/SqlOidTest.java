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


package org.apache.isis.extensions.sql.objectstore;

import org.apache.isis.extensions.sql.objectstore.IntegerPrimaryKey;
import org.apache.isis.extensions.sql.objectstore.SqlOid;
import org.apache.isis.extensions.sql.objectstore.SqlOid.State;

import junit.framework.TestCase;


public class SqlOidTest extends TestCase {

    /*
     * Test method for 'org.apache.isis.persistence.sql.SqlOid.hashCode()'
     */
    public void testHashCode() {
        SqlOid oid1 = new SqlOid("className", new IntegerPrimaryKey(13), State.TRANSIENT);
        SqlOid oid2 = new SqlOid("className", new IntegerPrimaryKey(13), State.TRANSIENT);

        assertEquals(oid1.hashCode(), oid2.hashCode());
    }

    /*
     * Test method for 'org.apache.isis.persistence.sql.SqlOid.copyFrom(Oid)'
     */
    public void testCopyFrom() {

    }

}
