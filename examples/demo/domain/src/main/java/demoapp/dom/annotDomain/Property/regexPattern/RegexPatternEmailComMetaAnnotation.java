package demoapp.dom.annotDomain.Property.regexPattern;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.regex.Pattern;

import org.apache.isis.applib.annotation.Parameter;
import org.apache.isis.applib.annotation.ParameterLayout;
import org.apache.isis.applib.annotation.Property;
import org.apache.isis.applib.annotation.PropertyLayout;

//tag::class[]
@Property(
    regexPattern = "^\\w+@\\w+[.]com$"                      // <.>
    , regexPatternReplacement = "Must be .com email address"    // <.>
    , regexPatternFlags = Pattern.CASE_INSENSITIVE              // <.>
)
@Parameter(
    regexPattern = "^\\w+@\\w+[.]com$"                      // <.>
    , regexPatternReplacement = "Must be .com email address"    // <.>
    , regexPatternFlags = Pattern.CASE_INSENSITIVE              // <.>
)
@PropertyLayout(
    describedAs =
        "@Parameter(regexPattern = \"^\\w+@\\w+[.]com$\")"
)
@ParameterLayout(
    describedAs =
        "@Parameter(regexPattern = \"^\\w+@\\w+[.]com$\")"
)
@Inherited
@Target({
    ElementType.METHOD, ElementType.FIELD,                      // <.>
    ElementType.PARAMETER,                                      // <.>
})
@Retention(RetentionPolicy.RUNTIME)
public @interface RegexPatternEmailComMetaAnnotation {

}
//end::class[]
