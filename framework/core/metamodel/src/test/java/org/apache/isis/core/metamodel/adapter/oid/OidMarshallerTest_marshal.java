package org.apache.isis.core.metamodel.adapter.oid;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;

import org.apache.isis.core.metamodel.spec.ObjectSpecId;

public class OidMarshallerTest_marshal {

    private OidMarshaller oidMarshaller;
    
    @Before
    public void setUp() throws Exception {
        oidMarshaller = new OidMarshaller();
    }
    
    @Test
    public void rootOid() {
        final String marshal = oidMarshaller.marshal(RootOidDefault.create(ObjectSpecId.of("CUS"),  "123"));
        assertThat(marshal, equalTo("CUS:123"));
    }

    @Test
    public void rootOid_transient() {
        final String marshal = oidMarshaller.marshal(RootOidDefault.createTransient(ObjectSpecId.of("CUS"),  "123"));
        assertThat(marshal, equalTo("!CUS:123"));
    }
    
    @Test
    public void rootOid_versionSequence() {
        final String marshal = oidMarshaller.marshal(RootOidDefault.create(ObjectSpecId.of("CUS"),  "123", 90807L));
        assertThat(marshal, equalTo("CUS:123^90807::"));
    }

    @Test
    public void rootOid_versionSequenceAndUser() {
        final String marshal = oidMarshaller.marshal(RootOidDefault.create(ObjectSpecId.of("CUS"),  "123", 90807L, "joebloggs"));
        assertThat(marshal, equalTo("CUS:123^90807:joebloggs:"));
    }

    @Test
    public void rootOid_versionSequenceAndUtc() {
        final String marshal = oidMarshaller.marshal(RootOidDefault.create(ObjectSpecId.of("CUS"),  "123", 90807L, 3453452141L));
        assertThat(marshal, equalTo("CUS:123^90807::3453452141"));
    }

    @Test
    public void rootOid_versionSequenceAndUserAndUtc() {
        final String marshal = oidMarshaller.marshal(RootOidDefault.create(ObjectSpecId.of("CUS"),  "123", 90807L, "joebloggs", 3453452141L));
        assertThat(marshal, equalTo("CUS:123^90807:joebloggs:3453452141"));
    }


}
