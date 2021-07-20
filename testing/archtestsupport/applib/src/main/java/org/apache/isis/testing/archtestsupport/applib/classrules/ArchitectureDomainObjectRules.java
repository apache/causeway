package org.apache.isis.testing.archtestsupport.applib.classrules;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaAnnotation;
import com.tngtech.archunit.lang.ArchRule;

import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.DomainObjectLayout;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import lombok.experimental.UtilityClass;
import lombok.val;

/**
 * A library of architecture tests to ensure coding conventions are followed for classes annotated with
 * {@link DomainObject}.
 *
 * @since 2.0 {@index}
 */
@UtilityClass
public class ArchitectureDomainObjectRules {

  /**
   * This rule requires that classes annotated with the {@link DomainObject} annotation must specify their
   * {@link DomainObject#logicalTypeName() logicalTypeName}.
   */
  public static ArchRule classes_annotated_with_DomainObject_must_specify_logicalTypeName() {
    return classes()
            .that().areAnnotatedWith(DomainObject.class)
            .should().beAnnotatedWith(DomainObject_logicalTypeName());
  }

  static DescribedPredicate<JavaAnnotation<?>> DomainObject_logicalTypeName() {
    return new DescribedPredicate<JavaAnnotation<?>>("@DomainObject(logicalTypeName=...)") {
      @Override public boolean apply(final JavaAnnotation<?> javaAnnotation) {
        if (javaAnnotation.getRawType().isEquivalentTo(DomainObject.class)) {
          return false;
        }
        val properties = javaAnnotation.getProperties();
        val value = properties.get("logicalTypeName");
        return value instanceof String && ((String) value).length() > 0;
      }
    };
  }

  /**
   * This rule requires that classes annotated with the {@link DomainObject} annotation must also be
   * annotated with the {@link DomainObjectLayout} annotation.
   */
  public static ArchRule classes_annotated_with_DomainObject_must_also_be_annotated_with_DomainObjectLayout() {
    return classes()
            .that().areAnnotatedWith(DomainObject.class)
            .should().beAnnotatedWith(DomainObjectLayout.class);
  }


}
