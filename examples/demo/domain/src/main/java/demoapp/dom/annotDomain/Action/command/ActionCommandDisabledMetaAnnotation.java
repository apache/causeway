package demoapp.dom.annotDomain.Action.command;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.apache.isis.applib.annotation.Action;

//tag::class[]
@Action()                                       // <.>
@Inherited
@Target({
        ElementType.TYPE, ElementType.METHOD    // <.>
})
@Retention(RetentionPolicy.RUNTIME)
public @interface ActionCommandDisabledMetaAnnotation {

}
//end::class[]
