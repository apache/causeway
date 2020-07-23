package demoapp.dom.annotDomain.Property.hidden;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.apache.isis.applib.annotation.Property;
import org.apache.isis.applib.annotation.Where;

//tag::class[]
@Property(hidden = Where.EVERYWHERE)                // <.>
@Inherited
@Target({
        ElementType.METHOD, ElementType.FIELD,      // <.>
        ElementType.PARAMETER,                      // <.>
        ElementType.TYPE                            // <.>
})
@Retention(RetentionPolicy.RUNTIME)
public @interface HiddenEverywhereMetaAnnotation {

}
//end::class[]
