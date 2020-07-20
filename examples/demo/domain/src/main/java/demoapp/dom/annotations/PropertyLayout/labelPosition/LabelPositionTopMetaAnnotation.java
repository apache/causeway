package demoapp.dom.annotations.PropertyLayout.labelPosition;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.apache.isis.applib.annotation.LabelPosition;
import org.apache.isis.applib.annotation.ParameterLayout;
import org.apache.isis.applib.annotation.PropertyLayout;

//tag::class[]
@PropertyLayout(labelPosition = LabelPosition.TOP)      // <.>
@ParameterLayout(labelPosition = LabelPosition.TOP)     // <.>
@Inherited
@Target({
        ElementType.METHOD, ElementType.FIELD,          // <.>
        ElementType.PARAMETER,                          // <.>
        ElementType.TYPE                                // <.>
})
@Retention(RetentionPolicy.RUNTIME)
public @interface LabelPositionTopMetaAnnotation {

}
//end::class[]
