package demoapp.dom.ui.custom.latlng;


import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.apache.isis.applib.annotation.Parameter;
import org.apache.isis.applib.annotation.Property;
import org.apache.isis.applib.spec.AbstractSpecification;

@Property(mustSatisfy = Zoom.Specification.class)
@Parameter(mustSatisfy = Zoom.Specification.class)
@Inherited
@Target({
        ElementType.FIELD,
        ElementType.METHOD,
        ElementType.PARAMETER,
        ElementType.ANNOTATION_TYPE
})
@Retention(RetentionPolicy.RUNTIME)
public @interface Zoom {

    class Specification extends AbstractSpecification<Integer> {

        @Override
        public String satisfiesSafely(Integer candidate) {
            return candidate >= 1 && candidate <=20
                    ? null
                    : "Must be between 1 and 20";
        }
    }
}
