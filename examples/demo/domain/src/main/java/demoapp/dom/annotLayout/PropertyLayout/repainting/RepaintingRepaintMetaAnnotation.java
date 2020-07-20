package demoapp.dom.annotLayout.PropertyLayout.repainting;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.apache.isis.applib.annotation.PropertyLayout;
import org.apache.isis.applib.annotation.Repainting;

//tag::class[]
@PropertyLayout(repainting = Repainting.REPAINT)    // <.>
@Inherited
@Target({
        ElementType.METHOD, ElementType.FIELD,      // <.>
        ElementType.TYPE                            // <.>
})
@Retention(RetentionPolicy.RUNTIME)
public @interface RepaintingRepaintMetaAnnotation {

}
//end::class[]
