package demoapp.dom.annotDomain.Property.optionality;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.apache.isis.applib.annotation.Editing;
import org.apache.isis.applib.annotation.Optionality;
import org.apache.isis.applib.annotation.Parameter;
import org.apache.isis.applib.annotation.Property;

//tag::class[]
@Property(optionality = Optionality.OPTIONAL)       // <.>
@Parameter(optionality = Optionality.OPTIONAL)      // <.>
@Inherited
@Target({
        ElementType.METHOD, ElementType.FIELD,      // <.>
        ElementType.PARAMETER,                      // <.>
})
@Retention(RetentionPolicy.RUNTIME)
public @interface OptionalityOptionalMetaAnnotation {

}
//end::class[]
