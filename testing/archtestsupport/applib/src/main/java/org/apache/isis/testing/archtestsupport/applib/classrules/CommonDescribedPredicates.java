package org.apache.isis.testing.archtestsupport.applib.classrules;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaAnnotation;

import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.Nature;
import org.apache.isis.applib.jaxb.PersistentEntityAdapter;

import lombok.experimental.UtilityClass;
import lombok.val;

@UtilityClass
public class CommonDescribedPredicates {

  static DescribedPredicate<JavaAnnotation<?>> DomainObject_nature_ENTITY() {
    return new DescribedPredicate<JavaAnnotation<?>>("@DomainObject(nature=ENTITY)") {
      @Override public boolean apply(final JavaAnnotation<?> javaAnnotation) {
        if (javaAnnotation.getRawType().isEquivalentTo(DomainObject.class)) {
          return false;
        }
        val properties = javaAnnotation.getProperties();
        val nature = properties.get("nature");
        return nature == Nature.ENTITY;
      }
    };
  }

  static DescribedPredicate<JavaAnnotation<?>> XmlJavaTypeAdapter_value_PersistentEntityAdapter() {
    return new DescribedPredicate<JavaAnnotation<?>>("@XmlJavaTypeAdapter(PersistentEntityAdapter.class)") {
      @Override public boolean apply(final JavaAnnotation<?> javaAnnotation) {
        if (javaAnnotation.getRawType().isEquivalentTo(XmlJavaTypeAdapter.class)) {
          return false;
        }
        val properties = javaAnnotation.getProperties();
        val value = properties.get("value");
        return value == PersistentEntityAdapter.class;
      }
    };
  }


}
