package domainapp.dom.types;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.jdo.annotations.Column;

import org.apache.isis.applib.annotation.Editing;
import org.apache.isis.applib.annotation.Parameter;
import org.apache.isis.applib.annotation.ParameterLayout;
import org.apache.isis.applib.annotation.Property;
import org.apache.isis.applib.annotation.PropertyLayout;

@Column(length = Notes.MAX_LEN, allowsNull = "true")
@Property(editing = Editing.ENABLED, maxLength = Notes.MAX_LEN)
@PropertyLayout(multiLine = 5)
@Parameter(maxLength = Notes.MAX_LEN)
@ParameterLayout(multiLine = 5)
@Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface Notes {

    int MAX_LEN = 4000;

}
