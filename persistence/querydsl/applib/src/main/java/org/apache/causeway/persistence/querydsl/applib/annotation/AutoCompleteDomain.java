package org.apache.causeway.persistence.querydsl.applib.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface AutoCompleteDomain {
    Class<?> repository() default Object.class;

    String predicateMethod() default "autoCompletePredicate";

    int minLength() default 0;

    int limitResults() default Constants.LIMIT_RESULTS;

    static class Constants {

        public final static int LIMIT_RESULTS = 50;
        public final static int MIN_LENGTH = 1;
    }
}
