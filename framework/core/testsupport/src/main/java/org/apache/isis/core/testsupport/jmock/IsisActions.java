package org.apache.isis.core.testsupport.jmock;

import org.jmock.api.Action;

public final class IsisActions {
    
    private IsisActions() {
    }
    
    public static Action injectInto() {
        return InjectIntoJMockAction.injectInto();
    }

    public static <T> Action returnEach(final T... values) {
        return ReturnEachAction.returnEach(values);
    }

    public static Action returnArgument(final int i) {
        return ReturnArgumentJMockAction.returnArgument(i);
    }


}
