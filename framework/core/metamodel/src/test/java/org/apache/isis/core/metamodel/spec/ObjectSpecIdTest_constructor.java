package org.apache.isis.core.metamodel.spec;

import org.junit.Test;

public class ObjectSpecIdTest_constructor {

    @Test
    public void happyCase() throws Exception {
        @SuppressWarnings("unused")
        final ObjectSpecId objectSpecId = new ObjectSpecId("CUS");
    }

    @Test(expected=IllegalArgumentException.class)
    public void cannotBeEmpty() throws Exception {
        new ObjectSpecId("");
    }


    @Test(expected=IllegalArgumentException.class)
    public void cannotBeNull() throws Exception {
        new ObjectSpecId(null);
    }


}
