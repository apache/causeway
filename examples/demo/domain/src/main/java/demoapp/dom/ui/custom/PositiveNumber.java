package demoapp.dom.ui.custom;


import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.apache.isis.applib.annotation.Parameter;
import org.apache.isis.applib.annotation.Property;
import org.apache.isis.applib.spec.AbstractSpecification;

@Property(mustSatisfy = PositiveNumber.Specification.class)
@Parameter(mustSatisfy = PositiveNumber.Specification.class)
@Inherited
@Target({
        ElementType.FIELD,
        ElementType.METHOD,
        ElementType.PARAMETER,
        ElementType.ANNOTATION_TYPE
})
@Retention(RetentionPolicy.RUNTIME)
public @interface PositiveNumber {

    class Specification extends AbstractSpecification<Integer> {

        @Override
        public String satisfiesSafely(Integer candidate) {
            return candidate <= 0 ? "Must be a positive number" : null;
        }
    }
}
