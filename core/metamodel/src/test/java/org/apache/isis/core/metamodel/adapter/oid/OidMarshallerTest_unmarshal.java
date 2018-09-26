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

import org.junit.Before;
import org.junit.Test;

import org.apache.isis.core.metamodel.spec.ObjectSpecId;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
* <dt>CUS:123</dt>
* <dd>persistent root</dd>
* <dt>!CUS:123</dt>
* <dd>transient root</dd>
* <dt>CUS:123$items</dt>
* <dd>collection of persistent root</dd>
* <dt>!CUS:123$items</dt>
* <dd>collection of transient root</dd>
* <dt>CUS:123~NME:2</dt>
* <dd>aggregated object within persistent root</dd>
* <dt>!CUS:123~NME:2</dt>
* <dd>aggregated object within transient root</dd>
* <dt>CUS:123~NME:2~CTY:LON</dt>
* <dd>aggregated object within aggregated object within root</dd>
* <dt>CUS:123~NME:2$items</dt>
* <dd>collection of an aggregated object within root</dd>
* <dt>CUS:123~NME:2~CTY:LON$streets</dt>
* <dd>collection of an aggregated object within aggregated object within root</dd>
* <dt>CUS:123^90809::</dt>
* <dd>persistent root with version info (sequence, no user or utc)</dd>
* <dt>CUS:123^90809:joebloggs:</dt>
* <dd>persistent root with version info (sequence, user but no utc)</dd>
* <dt>CUS:123^90809:joebloggs:1231334545123</dt>
* <dd>persistent root with version info (sequence, user, utc)</dd>
* <dt>CUS:123^90809::1231334545123</dt>
* <dd>persistent root with version info (sequence, utc but no user)</dd>
*/
public class OidMarshallerTest_unmarshal {

   private Oid_Marshaller oidMarshaller;
    
    @Before
    public void setUp() throws Exception {
        oidMarshaller = Oid_Marshaller.INSTANCE;
    }
    
    @Test
    public void persistentRoot() {
        final String oidStr = "CUS:123";
        
        final RootOid rootOid = oidMarshaller.unmarshal(oidStr, RootOid.class);
        assertThat(rootOid.isTransient(), is(false));
        assertThat(rootOid.getObjectSpecId(), is(ObjectSpecId.of("CUS")));
        assertThat(rootOid.getIdentifier(), is("123"));
        
        final Oid oid = oidMarshaller.unmarshal(oidStr, Oid.class);
        assertThat(oid, equalTo((Oid)rootOid));
    }

    @Test
    public void persistentRootWithFullyQualifiedSpecId() {
        final String oidStr = "com.planchase.ClassName:8";
        
        final RootOid rootOid = oidMarshaller.unmarshal(oidStr, RootOid.class);
        assertThat(rootOid.isTransient(), is(false));
        assertThat(rootOid.getObjectSpecId(), is(ObjectSpecId.of("com.planchase.ClassName")));
        assertThat(rootOid.getIdentifier(), is("8"));
        
        final Oid oid = oidMarshaller.unmarshal(oidStr, Oid.class);
        assertThat(oid, equalTo((Oid)rootOid));
    }

    @Test
    public void persistentRootWithVersion() {
        final String oidStr = "CUS:123^90809::";
        
        final RootOid rootOid = oidMarshaller.unmarshal(oidStr, RootOid.class);
        assertThat(rootOid.isTransient(), is(false));
        assertThat(rootOid.getObjectSpecId(), is(ObjectSpecId.of("CUS")));
        assertThat(rootOid.getIdentifier(), is("123"));
        assertThat(rootOid.getVersion().getSequence(), is(90809L));
        
        final Oid oid = oidMarshaller.unmarshal(oidStr, Oid.class);
        assertThat(oid, equalTo((Oid)rootOid));
    }

    @Test
    public void persistentRootWithVersionAndUserAndTimestamp() {
        final String oidStr = "CUS:123^90809:fredbloggs:1231231232";
        
        final RootOid rootOid = oidMarshaller.unmarshal(oidStr, RootOid.class);
        assertThat(rootOid.isTransient(), is(false));
        assertThat(rootOid.getObjectSpecId(), is(ObjectSpecId.of("CUS")));
        assertThat(rootOid.getIdentifier(), is("123"));
        assertThat(rootOid.getVersion().getSequence(), is(90809L));
        assertThat(rootOid.getVersion().getUser(), is("fredbloggs"));
        assertThat(rootOid.getVersion().getUtcTimestamp(), is(1231231232L));
        
        final Oid oid = oidMarshaller.unmarshal(oidStr, Oid.class);
        assertThat(oid, equalTo((Oid)rootOid));
    }

    @Test
    public void persistentRootWithVersionAndUserWithAtSymbol() {
        final String oidStr = "CUS:123^90809:fredbloggs@foo.bar:";
        
        final RootOid rootOid = oidMarshaller.unmarshal(oidStr, RootOid.class);
        assertThat(rootOid.getVersion().getUser(), is("fredbloggs@foo.bar"));
        
        final Oid oid = oidMarshaller.unmarshal(oidStr, Oid.class);
        assertThat(oid, equalTo((Oid)rootOid));
    }


    @Test(expected=IllegalArgumentException.class)
    public void persistentRootWithNonNumericVersion() {
        final String oidStr = "CUS:123^d0809";
        
        oidMarshaller.unmarshal(oidStr, RootOid.class);
    }


    @Test
    public void transientRoot() {
        final String oidStr = "!CUS:123";
        
        final RootOid rootOid = oidMarshaller.unmarshal(oidStr, RootOid.class);
        assertThat(rootOid.isTransient(), is(true));
        assertThat(rootOid.getObjectSpecId(), is(ObjectSpecId.of("CUS")));
        assertThat(rootOid.getIdentifier(), is("123"));
        
        final Oid oid = oidMarshaller.unmarshal(oidStr, Oid.class);
        assertThat(oid, equalTo((Oid)rootOid));
    }
    
    @Test
    public void collectionOfPersistentRoot() {
        final String oidStr = "CUS:123$items";
        
        final ParentedOid collectionOid = oidMarshaller.unmarshal(oidStr, ParentedOid.class);
        assertThat(collectionOid.isTransient(), is(false));
        assertThat(collectionOid.getParentOid(), is(oidMarshaller.unmarshal("CUS:123", RootOid.class)));
        assertThat(collectionOid.getName(), is("items"));
        
        final Oid oid = oidMarshaller.unmarshal(oidStr, Oid.class);
        assertThat(oid, equalTo((Oid)collectionOid));
    }

    @Test
    public void collectionOfTransientRoot() {
        final String oidStr = "!CUS:123$items";
        
        final ParentedOid collectionOid = oidMarshaller.unmarshal(oidStr, ParentedOid.class);
        assertThat(collectionOid.isTransient(), is(true));
        assertThat(collectionOid.getParentOid(), is(oidMarshaller.unmarshal("!CUS:123", RootOid.class)));
        assertThat(collectionOid.getName(), is("items"));
        
        final Oid oid = oidMarshaller.unmarshal(oidStr, Oid.class);
        assertThat(oid, equalTo((Oid)collectionOid));
    }



    @Test(expected=IllegalArgumentException.class)
    public void collection_forRoot_oidStr() {
        oidMarshaller.unmarshal("CUS:123", ParentedOid.class);
    }

    @Test(expected=IllegalArgumentException.class)
    public void badPattern() {
        oidMarshaller.unmarshal("xxx", RootOid.class);
    }

    

}
