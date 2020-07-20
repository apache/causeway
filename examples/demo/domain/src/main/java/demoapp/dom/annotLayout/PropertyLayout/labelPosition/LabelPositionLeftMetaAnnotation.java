package demoapp.dom.annotLayout.PropertyLayout.labelPosition;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.apache.isis.applib.annotation.LabelPosition;
import org.apache.isis.applib.annotation.ParameterLayout;
import org.apache.isis.applib.annotation.PropertyLayout;

//tag::class[]
@PropertyLayout(labelPosition = LabelPosition.LEFT)     // <.>
@ParameterLayout(labelPosition = LabelPosition.LEFT)    // <.>
@Inherited
@Target({
        ElementType.METHOD, ElementType.FIELD,          // <.>
        ElementType.PARAMETER,                          // <.>
        ElementType.TYPE                                // <.>
})
@Retention(RetentionPolicy.RUNTIME)
public @interface LabelPositionLeftMetaAnnotation {

}
//end::class[]
