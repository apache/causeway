package demoapp.dom.annotDomain.Action.publishing;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.apache.isis.applib.annotation.Property;
import org.apache.isis.applib.annotation.Publishing;

//tag::class[]
@Property(publishing = Publishing.DISABLED)         // <.>
@Inherited
@Target({
        ElementType.METHOD, ElementType.FIELD       // <.>
})
@Retention(RetentionPolicy.RUNTIME)
public @interface ActionDisabledMetaAnnotation {

}
//end::class[]
