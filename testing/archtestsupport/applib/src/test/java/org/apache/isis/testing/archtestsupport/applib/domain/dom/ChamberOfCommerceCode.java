package org.apache.isis.testing.archtestsupport.applib.domain.dom;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.apache.isis.applib.annotation.Optionality;
import org.apache.isis.applib.annotation.Parameter;
import org.apache.isis.applib.annotation.ParameterLayout;
import org.apache.isis.applib.annotation.Property;
import org.apache.isis.applib.annotation.PropertyLayout;

@javax.jdo.annotations.Column(length = ChamberOfCommerceCode.MAX_LEN, allowsNull = "true")
@Property(maxLength = ChamberOfCommerceCode.MAX_LEN, optionality = Optionality.OPTIONAL)
@PropertyLayout(named = "Chamber of Commerce Code")
@Parameter(maxLength = ChamberOfCommerceCode.MAX_LEN, optionality = Optionality.OPTIONAL)
@ParameterLayout(named = "Chamber of Commerce Code")
@Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface ChamberOfCommerceCode {
    int MAX_LEN = 30;
}
