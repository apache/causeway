package demoapp.dom.annotLayout.PropertyLayout.named;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.apache.isis.applib.annotation.ParameterLayout;
import org.apache.isis.applib.annotation.PropertyLayout;

//tag::class[]
@PropertyLayout(named = "Named from meta-annotation")       // <.>
@ParameterLayout(named = "Named from meta-annotation")      // <.>
@Inherited
@Target({
        ElementType.METHOD, ElementType.FIELD,              // <.>
        ElementType.PARAMETER,                              // <.>
        ElementType.TYPE                                    // <.>
})
@Retention(RetentionPolicy.RUNTIME)
public @interface NamedMetaAnnotation {

}
//end::class[]
