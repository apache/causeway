package org.apache.isis.extensions.excel.dom;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface PivotValue {
    int order();
    AggregationType type() default AggregationType.SUM;
}
