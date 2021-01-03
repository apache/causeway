package demoapp.dom.ui.custom.latlng;


import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.apache.isis.applib.annotation.Parameter;
import org.apache.isis.applib.annotation.Property;

@Property(
        regexPattern = Latitude.PATTERN
        , regexPatternReplacement = "Does not match format of latitude"
)
@Parameter(
        regexPattern = Latitude.PATTERN
        , regexPatternReplacement = "Does not match format of latitude"
)
@Inherited
@Target({
        ElementType.FIELD,
        ElementType.METHOD,
        ElementType.PARAMETER,
        ElementType.ANNOTATION_TYPE
})
@Retention(RetentionPolicy.RUNTIME)
public @interface Latitude {

    String PATTERN = "^[-+]?([1-8]?\\d(\\.\\d+)?|90(\\.0+)?)$";

}
