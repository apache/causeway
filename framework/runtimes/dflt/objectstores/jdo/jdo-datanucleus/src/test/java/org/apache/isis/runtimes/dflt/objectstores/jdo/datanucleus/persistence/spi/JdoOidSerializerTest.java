package org.apache.isis.runtimes.dflt.objectstores.jdo.datanucleus.persistence.spi;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

import org.datanucleus.identity.OIDImpl;
import org.junit.Test;

import org.apache.isis.core.commons.matchers.IsisMatchers;

public class JdoOidSerializerTest {


    public static class Customer {}
    
    @Test
    public void whenInt() throws Exception {
        Object jdoOid = new OIDImpl(Customer.class.getName(), 123);
        String id = JdoOidSerializer.toString(jdoOid);
        assertThat(id, is("I~123"));
        
        String oidStr = JdoOidSerializer.toOidStr(id);
        assertThat(oidStr, is("123"));
    }

    @Test
    public void whenString() throws Exception {
        Object jdoOid = new OIDImpl(Customer.class.getName(), "456");
        String id = JdoOidSerializer.toString(jdoOid);
        assertThat(id, is("S~456"));
        
        String oidStr = JdoOidSerializer.toOidStr(id);
        assertThat(oidStr, is("456"));
    }

    @Test
    public void whenOtherKeyValue() throws Exception {
        Object jdoOid = new OIDImpl(Customer.class.getName(), 789L);
        String id = JdoOidSerializer.toString(jdoOid);
        assertThat(id, IsisMatchers.startsWith(OIDImpl.class.getName() + "~789"));
        
        String oidStr = JdoOidSerializer.toOidStr(id);
        assertThat(oidStr, IsisMatchers.startsWith("789"));
    }

}
