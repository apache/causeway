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

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import org.apache.isis.core.metamodel.spec.ObjectSpecId;

public class OidMarshallerTest_roundtripping {

    private OidMarshaller oidMarshaller = new OidMarshaller();
    
    @Test
    public void rootOid_withNoVersion() {
        RootOid oid = RootOidDefault.create(ObjectSpecId.of("CUS"), "123");
        
        final String enString = oid.enString(oidMarshaller);
        final RootOid deString = RootOidDefault.deString(enString, oidMarshaller);
        assertThat(deString, is(oid));
    }

    @Test
    public void rootOid_withVersion() {
        RootOid oid = RootOidDefault.create(ObjectSpecId.of("CUS"), "123", 90807L);
        
        final String enString = oid.enString(oidMarshaller);
        final RootOid deString = RootOidDefault.deString(enString, oidMarshaller);
        assertThat(deString, is(oid));
        assertThat(deString.getVersion(), is(oid.getVersion())); // assert separately because not part of equality check
    }

    
    @Test
    public void aggregatedOid_withNoVersion() {
        RootOid parentOid = RootOidDefault.create(ObjectSpecId.of("CUS"), "123");
        AggregatedOid oid = new AggregatedOid(ObjectSpecId.of("CUS"), parentOid, "456");
        
        final String enString = oid.enString(oidMarshaller);
        final AggregatedOid deString = AggregatedOid.deString(enString, oidMarshaller);
        assertThat(deString, is(oid));
    }

    @Test
    public void aggregatedOid_withVersion() {
        RootOid parentOid = RootOidDefault.create(ObjectSpecId.of("CUS"), "123", 90807L, "fred@foo.bar", 123123123L);
        AggregatedOid oid = new AggregatedOid(ObjectSpecId.of("CUS"), parentOid, "456");
        
        final String enString = oid.enString(oidMarshaller);
        final AggregatedOid deString = AggregatedOid.deString(enString, oidMarshaller);
        assertThat(deString, is(oid));
        
        // assert each of remaining separately because not part of respective equality checks
        assertThat(deString.getVersion(), is(oid.getVersion())); 
        assertThat(deString.getVersion().getUser(), is(oid.getVersion().getUser()));
        assertThat(deString.getVersion().getUtcTimestamp(), is(oid.getVersion().getUtcTimestamp()));
    }

    
    @Test
    public void collectionOid_withNoVersion() {
        RootOid parentOid = RootOidDefault.create(ObjectSpecId.of("CUS"), "123");
        CollectionOid oid = new CollectionOid(parentOid, "items");
        
        final String enString = oid.enString(oidMarshaller);
        final CollectionOid deString = CollectionOid.deString(enString, oidMarshaller);
        assertThat(deString, is(oid));
    }

    @Test
    public void collectionOid_withVersion() {
        RootOid parentOid = RootOidDefault.create(ObjectSpecId.of("CUS"), "123", 90807L);
        CollectionOid oid = new CollectionOid(parentOid, "items");
        
        final String enString = oid.enString(oidMarshaller);
        final CollectionOid deString = CollectionOid.deString(enString, oidMarshaller);
        assertThat(deString, is(oid));
        assertThat(deString.getParentOid().getVersion(), is(parentOid.getVersion())); // assert separately because not part of equality check
    }

}
