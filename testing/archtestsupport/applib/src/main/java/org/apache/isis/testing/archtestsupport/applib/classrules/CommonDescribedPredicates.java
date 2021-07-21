package org.apache.isis.testing.archtestsupport.applib.classrules;

import java.util.Objects;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaAnnotation;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaEnumConstant;

import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.Nature;
import org.apache.isis.applib.jaxb.PersistentEntityAdapter;

import lombok.experimental.UtilityClass;
import lombok.val;

@UtilityClass
class CommonDescribedPredicates {

    static DescribedPredicate<JavaAnnotation<?>> DomainObject_nature_ENTITY() {
        return new DescribedPredicate<JavaAnnotation<?>>("@DomainObject(nature=ENTITY)") {
            @Override public boolean apply(final JavaAnnotation<?> javaAnnotation) {
                if (!javaAnnotation.getRawType().isAssignableTo(DomainObject.class)) {
                    return false;
                }
                val properties = javaAnnotation.getProperties();
                val nature = properties.get("nature");
                return nature instanceof JavaEnumConstant &&
                        Objects.equals(((JavaEnumConstant) nature).name(), Nature.ENTITY.name());
            }
        };
    }

    static DescribedPredicate<JavaAnnotation<?>> XmlJavaTypeAdapter_value_PersistentEntityAdapter() {
        return new DescribedPredicate<JavaAnnotation<?>>("@XmlJavaTypeAdapter(PersistentEntityAdapter.class)") {
            @Override public boolean apply(final JavaAnnotation<?> javaAnnotation) {
                if (!javaAnnotation.getRawType().isAssignableTo(XmlJavaTypeAdapter.class)) {
                    return false;
                }
                val properties = javaAnnotation.getProperties();
                val value = properties.get("value");
                return value instanceof JavaClass &&
                      ((JavaClass)value).isAssignableFrom(PersistentEntityAdapter.class);
            }
        };
    }

}
