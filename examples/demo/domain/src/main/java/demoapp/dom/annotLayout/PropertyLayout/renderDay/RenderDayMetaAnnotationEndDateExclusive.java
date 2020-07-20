package demoapp.dom.annotLayout.PropertyLayout.renderDay;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.apache.isis.applib.annotation.ParameterLayout;
import org.apache.isis.applib.annotation.PropertyLayout;
import org.apache.isis.applib.annotation.RenderDay;

//tag::class[]
@PropertyLayout(renderDay = RenderDay.AS_DAY_BEFORE)    // <.>
@ParameterLayout(renderDay = RenderDay.AS_DAY_BEFORE)   // <.>
@Inherited
@Target({
        ElementType.METHOD, ElementType.FIELD,          // <.>
        ElementType.PARAMETER,                          // <.>
        ElementType.TYPE                                // <.>
})
@Retention(RetentionPolicy.RUNTIME)
public @interface RenderDayMetaAnnotationEndDateExclusive {

}
//end::class[]
