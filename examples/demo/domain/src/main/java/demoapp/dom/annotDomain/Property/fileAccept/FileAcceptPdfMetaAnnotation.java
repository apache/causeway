package demoapp.dom.annotDomain.Property.fileAccept;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.apache.isis.applib.annotation.Parameter;
import org.apache.isis.applib.annotation.Property;

//tag::class[]
@Property(fileAccept = "pdf")                       // <.>
@Parameter(fileAccept = "pdf")                      // <.>
@Inherited
@Target({
        ElementType.METHOD, ElementType.FIELD,      // <.>
        ElementType.PARAMETER,                      // <.>
        ElementType.TYPE                            // <.>
})
@Retention(RetentionPolicy.RUNTIME)
public @interface FileAcceptPdfMetaAnnotation {

}
//end::class[]
