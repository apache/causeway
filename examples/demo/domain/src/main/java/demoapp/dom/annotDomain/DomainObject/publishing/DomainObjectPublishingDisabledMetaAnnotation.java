package demoapp.dom.annotDomain.DomainObject.publishing;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.Property;
import org.apache.isis.applib.annotation.Publishing;

//tag::class[]
@DomainObject(publishing = Publishing.DISABLED)     // <.>
@Inherited
@Target({
        ElementType.TYPE                            // <.>
})
@Retention(RetentionPolicy.RUNTIME)
public @interface DomainObjectPublishingDisabledMetaAnnotation {

}
//end::class[]
