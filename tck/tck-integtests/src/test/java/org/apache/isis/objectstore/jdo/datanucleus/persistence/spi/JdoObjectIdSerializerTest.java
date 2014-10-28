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
package org.apache.isis.objectstore.jdo.datanucleus.persistence.spi;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.math.BigInteger;
import java.util.Date;

import org.datanucleus.identity.OIDImpl;
import org.junit.Rule;
import org.junit.Test;

import org.apache.isis.applib.annotation.ObjectType;
import org.apache.isis.core.commons.matchers.IsisMatchers;
import org.apache.isis.core.integtestsupport.IsisSystemWithFixtures;
import org.apache.isis.core.metamodel.adapter.oid.RootOidDefault;
import org.apache.isis.core.metamodel.spec.ObjectSpecId;

public class JdoObjectIdSerializerTest {

	@ObjectType("CUS")
	public static class Customer {}
	
	public static class CustomerRepository {
		public void foo(Customer x) {}
	}
	
    @Rule
    public IsisSystemWithFixtures iswf = IsisSystemWithFixtures.builder()
        .withServices(new CustomerRepository())
        .build();
    
    
    
    @Test
    public void whenJavaxJdoIntIdentity() throws Exception {
        Object jdoObjectId = new javax.jdo.identity.IntIdentity(Customer.class, 123);
        String id = JdoObjectIdSerializer.toOidIdentifier(jdoObjectId);
        assertThat(id, is("i_123"));
        
        RootOidDefault oid = RootOidDefault.create(ObjectSpecId.of("CUS"), id);
        Object jdoObjectIdRecreated = JdoObjectIdSerializer.toJdoObjectId(oid);
        
        assertThat(jdoObjectIdRecreated, is(jdoObjectId));
    }


    @Test
    public void whenJavaxJdoStringIdentity() throws Exception {
        Object jdoObjectId = new javax.jdo.identity.StringIdentity(Customer.class, "123");
        String id = JdoObjectIdSerializer.toOidIdentifier(jdoObjectId);
        assertThat(id, is("s_123"));
        
        RootOidDefault oid = RootOidDefault.create(ObjectSpecId.of("CUS"), id);
        Object jdoObjectIdRecreated = JdoObjectIdSerializer.toJdoObjectId(oid);
        
        assertThat(jdoObjectIdRecreated, is(jdoObjectId));
    }

    
    @Test
    public void whenJavaxJdoLongIdentity() throws Exception {
        Object jdoObjectId = new javax.jdo.identity.LongIdentity(Customer.class, 123L);
        String id = JdoObjectIdSerializer.toOidIdentifier(jdoObjectId);
        assertThat(id, is("l_123"));
        
        RootOidDefault oid = RootOidDefault.create(ObjectSpecId.of("CUS"), id);
        Object jdoObjectIdRecreated = JdoObjectIdSerializer.toJdoObjectId(oid);
        
        assertThat(jdoObjectIdRecreated, is(jdoObjectId));
    }


    @Test
    public void whenLong() throws Exception {
        Object jdoOid = new OIDImpl(Customer.class.getName(), 123L);
        String id = JdoObjectIdSerializer.toOidIdentifier(jdoOid);
        assertThat(id, is("L_123"));
        
        RootOidDefault oid = RootOidDefault.create(ObjectSpecId.of("CUS"), id);
        Object jdoOidRecreated = JdoObjectIdSerializer.toJdoObjectId(oid);

        assertThat(jdoOidRecreated, is((Object)("123"+ "[OID]" + Customer.class.getName())));
    }

    @Test
    public void whenDataNucleusOidAndLong() throws Exception {
        Object jdoOid = new OIDImpl(Customer.class.getName(), 123L);
        String id = JdoObjectIdSerializer.toOidIdentifier(jdoOid);
        assertThat(id, is("L_123"));
        
        RootOidDefault oid = RootOidDefault.create(ObjectSpecId.of("CUS"), id);
        Object jdoOidRecreated = JdoObjectIdSerializer.toJdoObjectId(oid);

        assertThat(jdoOidRecreated, is((Object)("123"+ "[OID]" + Customer.class.getName())));
    }

    @Test
    public void whenDataNucleusOidAndBigInteger() throws Exception {
        Object jdoOid = new OIDImpl(Customer.class.getName(), new BigInteger("123"));
        String id = JdoObjectIdSerializer.toOidIdentifier(jdoOid);
        assertThat(id, is("B_123"));
        
        RootOidDefault oid = RootOidDefault.create(ObjectSpecId.of("CUS"), id);
        Object jdoOidRecreated = JdoObjectIdSerializer.toJdoObjectId(oid);
        
        assertThat(jdoOidRecreated, is(((Object)("123"+ "[OID]" + Customer.class.getName()))));
    }

    @Test
    public void whenDataNucleusOidAndString() throws Exception {
        Object jdoOid = new OIDImpl(Customer.class.getName(), "456");
        String id = JdoObjectIdSerializer.toOidIdentifier(jdoOid);
        assertThat(id, is("S_456"));
        
        RootOidDefault oid = RootOidDefault.create(ObjectSpecId.of("CUS"), id);
        Object jdoOidRecreated = JdoObjectIdSerializer.toJdoObjectId(oid);
        
        assertThat(jdoOidRecreated, is((Object)("456" + "[OID]" + Customer.class.getName())));
    }

    @Test
    public void whenDataNucleusOidAndOtherKeyValue() throws Exception {
        Date key = new Date();
		Object jdoOid = new OIDImpl(Customer.class.getName(), key);
        String id = JdoObjectIdSerializer.toOidIdentifier(jdoOid);
        assertThat(id, IsisMatchers.startsWith(OIDImpl.class.getName() + "_" + key.toString()));
        
        RootOidDefault oid = RootOidDefault.create(ObjectSpecId.of("CUS"), id);
        Object jdoOidRecreated = JdoObjectIdSerializer.toJdoObjectId(oid);

        assertThat(jdoOidRecreated, is((Object)(key.toString() + "[OID]" + Customer.class.getName())));
    }

}
