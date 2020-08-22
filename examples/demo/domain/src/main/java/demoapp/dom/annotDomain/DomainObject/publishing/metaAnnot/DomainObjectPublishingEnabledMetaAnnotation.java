package demoapp.dom.annotDomain.DomainObject.publishing.metaAnnot;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.Property;
import org.apache.isis.applib.annotation.Publishing;

//tag::class[]
@DomainObject(publishing = Publishing.ENABLED)          // <.>
@Inherited
@Target({
        ElementType.TYPE, ElementType.ANNOTATION_TYPE   // <.>
})
@Retention(RetentionPolicy.RUNTIME)
public @interface DomainObjectPublishingEnabledMetaAnnotation {

}
//end::class[]
