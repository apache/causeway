package org.apache.isis.core.metamodel.adapter.oid;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import org.apache.isis.core.metamodel.spec.ObjectSpecId;
import org.junit.Test;

public class RootOidDefaultTest_create  {


    @Test
    public void create() throws Exception {
        ObjectSpecId objectSpecId = ObjectSpecId.of("CUS");
        RootOidDefault oid = RootOidDefault.create(objectSpecId, "123");
        assertThat(oid.getObjectSpecId(), is(objectSpecId));
        assertThat(oid.getIdentifier(), is("123"));
        assertThat(oid.getVersion(), is(nullValue()));
        
        assertThat(oid.isTransient(), is(false));
    }
    
    @Test
    public void createTransient() throws Exception {
        ObjectSpecId objectSpecId = ObjectSpecId.of("CUS");
        RootOidDefault oid = RootOidDefault.createTransient(objectSpecId, "123");
        assertThat(oid.getObjectSpecId(), is(objectSpecId));
        assertThat(oid.getIdentifier(), is("123"));
        assertThat(oid.getVersion(), is(nullValue()));
        
        assertThat(oid.isTransient(), is(true));
    }

    
    @Test
    public void createWithVersion() throws Exception {
        ObjectSpecId objectSpecId = ObjectSpecId.of("CUS");
        RootOidDefault oid = RootOidDefault.create(objectSpecId, "123", 456L);
        assertThat(oid.getObjectSpecId(), is(objectSpecId));
        assertThat(oid.getIdentifier(), is("123"));
        assertThat(oid.getVersion().getSequence(), is(456L));
        
        assertThat(oid.isTransient(), is(false));
    }
    
    @Test
    public void createTransientNoVersion() throws Exception {
        ObjectSpecId objectSpecId = ObjectSpecId.of("CUS");
        RootOidDefault oid = RootOidDefault.createTransient(objectSpecId, "123");
        assertThat(oid.getObjectSpecId(), is(objectSpecId));
        assertThat(oid.getIdentifier(), is("123"));
        assertThat(oid.getVersion(), is(nullValue()));
        
        assertThat(oid.isTransient(), is(true));
    }

}
