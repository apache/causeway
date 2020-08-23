package demoapp.dom.annotDomain.Action.command;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.Publishing;

//tag::class[]
@Action(publishing = Publishing.ENABLED)        // <.>
@Inherited
@Target({
        ElementType.TYPE, ElementType.METHOD    // <.>
})
@Retention(RetentionPolicy.RUNTIME)
public @interface ActionCommandEnabledMetaAnnotation {

}
//end::class[]
