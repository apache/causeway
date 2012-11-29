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
package org.apache.isis.core.metamodel.adapter.oid;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import org.apache.isis.core.metamodel.spec.ObjectSpecId;

public class RootOidDefaultTest_compareAgainst  {

    private ObjectSpecId cusObjectSpecId = ObjectSpecId.of("CUS");
    private ObjectSpecId ordObjectSpecId = ObjectSpecId.of("ORD");
    
    private RootOid oid1, oid2;
    
    @Test
    public void whenEquivalentAndSameVersion() throws Exception {
        oid1 = RootOidDefault.create(cusObjectSpecId, "123", 90807L);
        oid2 = RootOidDefault.create(cusObjectSpecId, "123", 90807L);
        
        assertThat(oid1, is(equalTo(oid2)));
        assertThat(oid1.compareAgainst(oid2), is(RootOid.Comparison.EQUIVALENT_AND_UNCHANGED));
    }
    
    @Test
    public void whenEquivalentAndDifferentVersions() throws Exception {
        oid1 = RootOidDefault.create(cusObjectSpecId, "123", 90807L);
        oid2 = RootOidDefault.create(cusObjectSpecId, "123", 90808L);
        
        assertThat(oid1, is(equalTo(oid2)));
        assertThat(oid1.compareAgainst(oid2), is(RootOid.Comparison.EQUIVALENT_BUT_CHANGED));
    }

    @Test
    public void whenEquivalentAndNoVersionInfoForLeftHand() throws Exception {
        oid1 = RootOidDefault.create(cusObjectSpecId, "123");
        oid2 = RootOidDefault.create(cusObjectSpecId, "123", 90808L);
        
        assertThat(oid1, is(equalTo(oid2)));
        assertThat(oid1.compareAgainst(oid2), is(RootOid.Comparison.EQUIVALENT_BUT_NO_VERSION_INFO));
    }

    @Test
    public void whenEquivalentAndNoVersionInfoForRightHand() throws Exception {
        oid1 = RootOidDefault.create(cusObjectSpecId, "123", 90807L);
        oid2 = RootOidDefault.create(cusObjectSpecId, "123");
        
        assertThat(oid1, is(equalTo(oid2)));
        assertThat(oid1.compareAgainst(oid2), is(RootOid.Comparison.EQUIVALENT_BUT_NO_VERSION_INFO));
    }

    @Test
    public void whenEquivalentAndNoVersionInfoForEither() throws Exception {
        oid1 = RootOidDefault.create(cusObjectSpecId, "123");
        oid2 = RootOidDefault.create(cusObjectSpecId, "123");
        
        assertThat(oid1, is(equalTo(oid2)));
        assertThat(oid1.compareAgainst(oid2), is(RootOid.Comparison.EQUIVALENT_BUT_NO_VERSION_INFO));
    }

    @Test
    public void whenNotEquivalentById() throws Exception {
        oid1 = RootOidDefault.create(cusObjectSpecId, "123");
        oid2 = RootOidDefault.create(cusObjectSpecId, "124");
        
        assertThat(oid1, is(not(equalTo(oid2))));
        assertThat(oid1.compareAgainst(oid2), is(RootOid.Comparison.NOT_EQUIVALENT));
    }

    @Test
    public void whenNotEquivalentByObjectSpecId() throws Exception {
        oid1 = RootOidDefault.create(cusObjectSpecId, "123");
        oid2 = RootOidDefault.create(ordObjectSpecId, "123");
        
        assertThat(oid1, is(not(equalTo(oid2))));
        assertThat(oid1.compareAgainst(oid2), is(RootOid.Comparison.NOT_EQUIVALENT));
    }

    @Test
    public void whenNotEquivalentByState() throws Exception {
        oid1 = RootOidDefault.create(cusObjectSpecId, "123");
        oid2 = RootOidDefault.createTransient(cusObjectSpecId, "123");
        
        assertThat(oid1, is(not(equalTo(oid2))));
        assertThat(oid1.compareAgainst(oid2), is(RootOid.Comparison.NOT_EQUIVALENT));
    }


}
