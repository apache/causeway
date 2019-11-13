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
package org.apache.isis.metamodel.adapter.oid;

import org.junit.Test;

import org.apache.isis.metamodel.adapter.oid.Oid.Factory;
import org.apache.isis.metamodel.spec.ObjectSpecId;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;

public class OidVersionTest  {

    private ObjectSpecId cusObjectSpecId = ObjectSpecId.of("CUS");
    private ObjectSpecId ordObjectSpecId = ObjectSpecId.of("ORD");

    private RootOid oid1, oid2;

//    @Test
//    public void whenEquivalentAndSameVersion() throws Exception {
//        oid1 = Factory.persistentOf(cusObjectSpecId, "123", 90807L);
//        oid2 = Factory.persistentOf(cusObjectSpecId, "123", 90807L);
//
//        assertThat(oid1, is(equalTo(oid2)));
//    }
//
//    @Test
//    public void whenEquivalentAndDifferentVersions() throws Exception {
//        oid1 = Factory.persistentOf(cusObjectSpecId, "123", 90807L);
//        oid2 = Factory.persistentOf(cusObjectSpecId, "123", 90808L);
//
//        assertThat(oid1, is(equalTo(oid2)));
//    }
//
//    @Test
//    public void whenEquivalentAndNoVersionInfoForLeftHand() throws Exception {
//        oid1 = Factory.persistentOf(cusObjectSpecId, "123");
//        oid2 = Factory.persistentOf(cusObjectSpecId, "123", 90808L);
//
//        assertThat(oid1, is(equalTo(oid2)));
//    }
//
//    @Test
//    public void whenEquivalentAndNoVersionInfoForRightHand() throws Exception {
//        oid1 = Factory.persistentOf(cusObjectSpecId, "123", 90807L);
//        oid2 = Factory.persistentOf(cusObjectSpecId, "123");
//
//        assertThat(oid1, is(equalTo(oid2)));
//    }

    @Test
    public void whenEquivalentAndNoVersionInfoForEither() throws Exception {
        oid1 = Factory.persistentOf(cusObjectSpecId, "123");
        oid2 = Factory.persistentOf(cusObjectSpecId, "123");

        assertThat(oid1, is(equalTo(oid2)));
    }

    @Test
    public void whenNotEquivalentById() throws Exception {
        oid1 = Factory.persistentOf(cusObjectSpecId, "123");
        oid2 = Factory.persistentOf(cusObjectSpecId, "124");

        assertThat(oid1, is(not(equalTo(oid2))));
    }

    @Test
    public void whenNotEquivalentByObjectSpecId() throws Exception {
        oid1 = Factory.persistentOf(cusObjectSpecId, "123");
        oid2 = Factory.persistentOf(ordObjectSpecId, "123");

        assertThat(oid1, is(not(equalTo(oid2))));
    }

    @Test
    public void whenNotEquivalentByState() throws Exception {
        oid1 = Factory.persistentOf(cusObjectSpecId, "123");
        oid2 = Factory.transientOf(cusObjectSpecId, "123");

        assertThat(oid1, is(not(equalTo(oid2))));
    }


}
