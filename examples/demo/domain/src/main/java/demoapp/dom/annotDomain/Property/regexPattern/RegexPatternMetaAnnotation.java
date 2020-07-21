package demoapp.dom.annotDomain.Property.regexPattern;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.regex.Pattern;

import org.apache.isis.applib.annotation.Parameter;
import org.apache.isis.applib.annotation.Property;

//tag::class[]
@Property(
    regexPattern = "^[^@+]@[^\\.+]\\.com$"                      // <.>
    , regexPatternReplacement = "Must be .com email address"    // <.>
    , regexPatternFlags = Pattern.CASE_INSENSITIVE              // <.>
)
@Parameter(
    regexPattern = "^[^@+]@[^\\.+]\\.com$"                      // <.>
    , regexPatternReplacement = "Must be .com email address"    // <.>
    , regexPatternFlags = Pattern.CASE_INSENSITIVE              // <.>
)
@Inherited
@Target({
    ElementType.METHOD, ElementType.FIELD,                      // <.>
    ElementType.PARAMETER,                                      // <.>
    ElementType.TYPE                                            // <.>
})
@Retention(RetentionPolicy.RUNTIME)
public @interface RegexPatternMetaAnnotation {

}
//end::class[]
