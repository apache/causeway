package org.apache.isis.core.metamodel.services.container;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import org.apache.isis.core.metamodel.adapter.oid.RootOidDefault;
import org.apache.isis.core.metamodel.adapter.version.ConcurrencyException;
import org.apache.isis.core.metamodel.spec.ObjectSpecId;
import org.apache.isis.core.unittestsupport.jmocking.JUnitRuleMockery2;
import org.apache.isis.core.unittestsupport.jmocking.JUnitRuleMockery2.Mode;

public class DomainObjectContainerDefaultTest_recognizes {

    static class SomeRandomException extends Exception {
        private static final long serialVersionUID = 1L;
    }

    @Rule
    public JUnitRuleMockery2 context = JUnitRuleMockery2.createFor(Mode.INTERFACES_AND_CLASSES);

    private Exception ex;
    
    private DomainObjectContainerDefault container;
    
    @Before
    public void setUp() throws Exception {
        container = new DomainObjectContainerDefault();
    }
    
    @Test
    public void whenConcurrencyException_is_recognized() throws Exception {
        ex = new ConcurrencyException("foo", RootOidDefault.create(ObjectSpecId.of("CUS"), "123"));
        assertThat(container.recognize(ex), is(not(nullValue())));
    }

    @Test
    public void whenSomeRandomException_is_not_recognized() throws Exception {
        ex = new SomeRandomException();
        assertThat(container.recognize(ex), is(nullValue()));
    }
    
}
