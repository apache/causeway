package org.apache.isis.testing.archtestsupport.applib.classrules;

import java.util.Arrays;
import java.util.Objects;

import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.Table;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaAnnotation;
import com.tngtech.archunit.lang.ArchRule;

import org.apache.isis.applib.annotation.DomainObject;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import lombok.experimental.UtilityClass;
import lombok.val;

/**
 * A library of architecture tests to ensure coding conventions are followed for classes annotated with
 * the JPA {@link Entity} annotation.
 *
 * @since 2.0 {@index}
 */
@UtilityClass
public class ArchitectureJpaRules {

  /**
   * This rule requires that classes annotated with the JPA {@link Entity} annotation must also be
   * annotated with the Apache Isis {@link DomainObject} annotation specifying that its
   * {@link DomainObject#nature() nature} is an {@link org.apache.isis.applib.annotation.Nature#ENTITY entity}.
   */
  public static ArchRule classes_annotated_with_Entity_must_also_be_annotated_with_DomainObject_nature_of_ENTITY() {
    return classes()
            .that().areAnnotatedWith(Entity.class)
            .should().beAnnotatedWith(CommonDescribedPredicates.DomainObject_nature_ENTITY());
  }

  /**
   * This rule requires that classes annotated with the JPA {@link Entity} annotation must also be
   * annotated with the Apache Isis {@link javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter} annotation
   * with a value of {@link org.apache.isis.applib.jaxb.PersistentEntityAdapter}<code>.class</code>.
   *
   * <p>
   *     Tnis is so that entities can be transparently referenced from XML-style view models.
   * </p>
   */
  public static ArchRule classes_annotated_with_Entity_must_also_be_annotated_with_XmlJavaAdapter_PersistentEntityAdapter() {
    return classes()
            .that().areAnnotatedWith(Entity.class)
            .should().beAnnotatedWith(CommonDescribedPredicates.XmlJavaTypeAdapter_value_PersistentEntityAdapter());
  }

  /**
   * This rule requires that classes annotated with the JPA {@link Entity} annotation must also be
   * annotated with the {@link javax.persistence.EntityListeners} annotation that includes
   * a value of <code>org.apache.isis.persistence.jpa.applib.integration.IsisEntityListener.class</code>.
   *
   * <p>
   *     Tnis is so that entities can be transparently referenced from XML-style view models.
   * </p>
   */
  public static ArchRule classes_annotated_with_Entity_must_also_be_an_IsisEntityListener() {
    return classes()
            .that().areAnnotatedWith(Entity.class)
            .should().beAnnotatedWith(EntityListeners_with_IsisEntityListener());
  }

  private static DescribedPredicate<JavaAnnotation<?>> EntityListeners_with_IsisEntityListener() {
    return new DescribedPredicate<JavaAnnotation<?>>("@EntityListener({IsisEntityListener.class})") {
      @Override public boolean apply(final JavaAnnotation<?> javaAnnotation) {
        if (javaAnnotation.getRawType().isEquivalentTo(EntityListeners.class)) {
          return false;
        }
        val properties = javaAnnotation.getProperties();
        val listeners = properties.get("value");
        return listeners instanceof Class[] && containsIsisEntityListener((Class<?>[]) listeners);
      }

      private boolean containsIsisEntityListener(final Class<?>[] classes) {
        return Arrays.stream(classes)
                .anyMatch(x -> Objects.equals(x.getCanonicalName(),
                        "org.apache.isis.persistence.jpa.applib.integration.IsisEntityListener"));
      }
    };
  }

  /**
   * This rule requires that classes annotated with the JPA {@link Entity} annotation must also be
   * implement {@link Comparable}.
   *
   * <p>
   *     This is so that entities have a natural ordering and can safely be added to parented collections of type
   *     {@link java.util.SortedSet}.
   * </p>
   */
  public static ArchRule classes_annotated_with_Entity_must_also_implement_Comparable() {
    return classes()
            .that().areAnnotatedWith(Entity.class)
            .should().implement(Comparable.class);
  }

  /**
   * This rule requires that classes annotated with the JPA {@link Entity} annotation must also be annotated with the
   * JPA {@link Table} annotation which includes {@link Table#uniqueConstraints() uniqueConstraints}.
   *
   * <p>
   *     This is so that entities will have an alternative business key in addition to the system-defined surrogate
   *     key.
   * </p>
   */
  public static ArchRule classes_annotated_with_Entity_must_also_be_annotated_with_Table_with_uniqueConstraints() {
    return classes()
            .that().areAnnotatedWith(Entity.class)
            .should().beAnnotatedWith(Table_uniqueConstraints());
  }

  private static DescribedPredicate<JavaAnnotation<?>> Table_uniqueConstraints() {
    return new DescribedPredicate<JavaAnnotation<?>>("@Table(uniqueConstraints=...)") {
      @Override public boolean apply(final JavaAnnotation<?> javaAnnotation) {
        if (javaAnnotation.getRawType().isEquivalentTo(Table.class)) {
          return false;
        }
        val properties = javaAnnotation.getProperties();
        val uniqueConstraints = properties.get("uniqueConstraints");
        return uniqueConstraints != null;
      }
    };
  }


}
