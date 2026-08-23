package demoapp.dom.types.javamath.bigdecimals.meta;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.constraints.Digits;

@Digits(integer = 12, fraction = 2)
// @jakarta.persistence.Column(precision = 14, scale = 2) // JPA doesn't support meta-annotations
@Inherited
@Target({
        ElementType.METHOD, ElementType.FIELD,                      // <.>
        ElementType.PARAMETER,                                      // <.>
})
@Retention(RetentionPolicy.RUNTIME)
public @interface Money {
}
