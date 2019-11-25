package org.incode.module.unittestsupport.dom.assertions;

public final class Asserting {

    private Asserting() {
    }


    public static <T> T assertType(Object o, Class<T> type) {
        if(o == null) {
            throw new AssertionError("Object is null");
        }
        if(!type.isAssignableFrom(o.getClass())) {
            throw new AssertionError(
                    String.format("Object %s (%s) is not an instance of %s", o.getClass().getName(), o.toString(), type));
        }
        return type.cast(o);
    }


}
