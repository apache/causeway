package org.incode.module.base.dom;

import org.junit.Test;

import org.incode.module.base.dom.IncodeApplicationException;

public class IncodeApplicationExceptionTest {

    private Class<? extends Exception> cls;


    @Test
    public void testName() throws Exception {
        final IncodeApplicationException ex = new IncodeApplicationException("dummy");

    }
}
