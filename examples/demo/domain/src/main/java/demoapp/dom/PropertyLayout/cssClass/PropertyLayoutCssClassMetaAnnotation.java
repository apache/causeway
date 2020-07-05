package demoapp.dom.PropertyLayout.cssClass;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.apache.isis.applib.annotation.PropertyLayout;

//tag::class[]
@PropertyLayout(cssClass = "red")
@Inherited
@Target({ ElementType.METHOD, ElementType.FIELD, ElementType.TYPE, ElementType.ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface PropertyLayoutCssClassMetaAnnotation {

}
//end::class[]
