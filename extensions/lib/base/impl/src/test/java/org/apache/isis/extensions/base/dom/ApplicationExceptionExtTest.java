package org.apache.isis.extensions.base.dom;

import org.junit.Test;

public class ApplicationExceptionExtTest {

    private Class<? extends Exception> cls;


    @Test
    public void testName() throws Exception {
        final ApplicationExceptionExt ex = new ApplicationExceptionExt("dummy");

    }
}
