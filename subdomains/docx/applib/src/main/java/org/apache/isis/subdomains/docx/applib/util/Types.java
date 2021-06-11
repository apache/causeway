package org.apache.isis.subdomains.docx.applib.util;

import java.util.function.Predicate;

public final class Types {

    private Types(){}

    public static Predicate<Object> withType(final Class<?> cls) {
        return new Predicate<Object>(){
            public boolean test(Object object) {
                return cls.isAssignableFrom(object.getClass());
            }
        };
    }

}
