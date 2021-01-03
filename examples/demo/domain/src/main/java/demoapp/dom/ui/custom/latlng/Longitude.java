package demoapp.dom.ui.custom.latlng;


import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.apache.isis.applib.annotation.Parameter;
import org.apache.isis.applib.annotation.Property;

@Property(
        regexPattern = Longitude.PATTERN
        , regexPatternReplacement = "Does not match format of longitude"
)
@Parameter(
        regexPattern = Longitude.PATTERN
        , regexPatternReplacement = "Does not match format of longitude"
)
@Inherited
@Target({
        ElementType.FIELD,
        ElementType.METHOD,
        ElementType.PARAMETER,
        ElementType.ANNOTATION_TYPE
})
@Retention(RetentionPolicy.RUNTIME)
public @interface Longitude {

    String PATTERN = "^[-+]?(180(\\.0+)?|((1[0-7]\\d)|([1-9]?\\d))(\\.\\d+)?)$";

}
