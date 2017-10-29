package org.apache.isis.viewer.restfulobjects.applib;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class Rel_getName_Test {

    @Test
    public void iana_namespace() throws Exception {
        String name = Rel.SELF.getName();
        assertThat(name, is(equalTo("self")));
    }

    @Test
    public void ro_namespace() throws Exception {
        String name = Rel.DOMAIN_TYPE.getName();
        assertThat(name, is(equalTo("urn:org.restfulobjects:rels/domain-type")));
    }

    @Test
    public void impl_namespace() throws Exception {
        String name = Rel.LAYOUT.getName();
        assertThat(name, is(equalTo("urn:org.apache.isis.restfulobjects:rels/layout")));
    }
}