package org.apache.isis.runtimes.dflt.objectstores.jdo.datanucleus.persistence.spi;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.math.BigInteger;
import java.util.Date;

import org.apache.isis.applib.annotation.ObjectType;
import org.apache.isis.core.commons.matchers.IsisMatchers;
import org.apache.isis.core.metamodel.adapter.oid.RootOidDefault;
import org.apache.isis.core.metamodel.spec.ObjectSpecId;
import org.apache.isis.runtimes.dflt.testsupport.IsisSystemWithFixtures;
import org.datanucleus.identity.OIDImpl;
import org.junit.Rule;
import org.junit.Test;

public class JdoOidSerializerTest {

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
        Object jdoOid = new javax.jdo.identity.IntIdentity(Customer.class, 123);
        String id = JdoOidSerializer.toString(jdoOid);
        assertThat(id, is("i~123"));
        
        RootOidDefault oid = RootOidDefault.create(ObjectSpecId.of("CUS"), id);
        String oidStr = JdoOidSerializer.toOidStr(oid);
        
        // REVIEW: not completely certain if should have the [OID] suffix or not for JDO identity
        assertThat(oidStr, is("123"+ "[OID]" + Customer.class.getName()));
    }


    @Test
    public void whenJavaxJdoStringIdentity() throws Exception {
        Object jdoOid = new javax.jdo.identity.StringIdentity(Customer.class, "123");
        String id = JdoOidSerializer.toString(jdoOid);
        assertThat(id, is("s~123"));
        
        RootOidDefault oid = RootOidDefault.create(ObjectSpecId.of("CUS"), id);
        String oidStr = JdoOidSerializer.toOidStr(oid);
        
        // REVIEW: not completely certain if should have the [OID] suffix or not for JDO identity
        assertThat(oidStr, is("123" + "[OID]" + Customer.class.getName()));
    }

    
    @Test
    public void whenJavaxJdoLongIdentity() throws Exception {
        Object jdoOid = new javax.jdo.identity.LongIdentity(Customer.class, 123L);
        String id = JdoOidSerializer.toString(jdoOid);
        assertThat(id, is("l~123"));
        
        RootOidDefault oid = RootOidDefault.create(ObjectSpecId.of("CUS"), id);
        String oidStr = JdoOidSerializer.toOidStr(oid);
        
        // REVIEW: not completely certain if should have the [OID] suffix or not for JDO identity
        assertThat(oidStr, is("123"+ "[OID]" + Customer.class.getName()));
    }


    @Test
    public void whenLong() throws Exception {
        Object jdoOid = new OIDImpl(Customer.class.getName(), 123L);
        String id = JdoOidSerializer.toString(jdoOid);
        assertThat(id, is("L~123"));
        
        RootOidDefault oid = RootOidDefault.create(ObjectSpecId.of("CUS"), id);
        String oidStr = JdoOidSerializer.toOidStr(oid);

        assertThat(oidStr, is("123"+ "[OID]" + Customer.class.getName()));
    }

    @Test
    public void whenDataNucleusOidAndLong() throws Exception {
        Object jdoOid = new OIDImpl(Customer.class.getName(), 123L);
        String id = JdoOidSerializer.toString(jdoOid);
        assertThat(id, is("L~123"));
        
        RootOidDefault oid = RootOidDefault.create(ObjectSpecId.of("CUS"), id);
        String oidStr = JdoOidSerializer.toOidStr(oid);

        assertThat(oidStr, is("123"+ "[OID]" + Customer.class.getName()));
    }

    @Test
    public void whenDataNucleusOidAndBigInteger() throws Exception {
        Object jdoOid = new OIDImpl(Customer.class.getName(), new BigInteger("123"));
        String id = JdoOidSerializer.toString(jdoOid);
        assertThat(id, is("B~123"));
        
        RootOidDefault oid = RootOidDefault.create(ObjectSpecId.of("CUS"), id);
        String oidStr = JdoOidSerializer.toOidStr(oid);
        
        assertThat(oidStr, is("123"+ "[OID]" + Customer.class.getName()));
    }

    @Test
    public void whenDataNucleusOidAndString() throws Exception {
        Object jdoOid = new OIDImpl(Customer.class.getName(), "456");
        String id = JdoOidSerializer.toString(jdoOid);
        assertThat(id, is("S~456"));
        
        RootOidDefault oid = RootOidDefault.create(ObjectSpecId.of("CUS"), id);
        String oidStr = JdoOidSerializer.toOidStr(oid);
        
        assertThat(oidStr, is("456" + "[OID]" + Customer.class.getName()));
    }

    @Test
    public void whenDataNucleusOidAndOtherKeyValue() throws Exception {
        Date key = new Date();
		Object jdoOid = new OIDImpl(Customer.class.getName(), key);
        String id = JdoOidSerializer.toString(jdoOid);
        assertThat(id, IsisMatchers.startsWith(OIDImpl.class.getName() + "~" + key.toString()));
        
        RootOidDefault oid = RootOidDefault.create(ObjectSpecId.of("CUS"), id);
        String oidStr = JdoOidSerializer.toOidStr(oid);

        assertThat(oidStr, is(key.toString() + "[OID]" + Customer.class.getName()));
    }

}
