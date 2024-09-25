package org.apache.causeway.persistence.querydsl.applib;

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

    int limitResults() default AutoCompleteGeneratedDslQuery.LIMIT_RESULTS;
}
