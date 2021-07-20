package org.apache.isis.testing.archtestsupport.applib.classrules;

import com.tngtech.archunit.lang.ArchRule;

import org.apache.isis.applib.annotation.DomainObject;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import lombok.experimental.UtilityClass;

/**
 * A library of architecture tests to ensure coding conventions are followed for classes annotated with
 * the JDO {@link javax.jdo.annotations.PersistenceCapable} annotation.
 *
 * @since 2.0 {@index}
 */
@UtilityClass
public class ArchitectureJdoRules {

  /**
   * This rule requires that classes annotated with the JDO {@link javax.jdo.annotations.PersistenceCapable} annotation
   * must also be annotated with the Apache Isis {@link DomainObject} annotation specifying that its
   * {@link DomainObject#nature() nature} is an {@link org.apache.isis.applib.annotation.Nature#ENTITY entity}.
   */
  public static ArchRule classes_annotated_with_PersistenceCapable_must_also_be_annotated_with_DomainObject_nature_of_ENTITY() {
    return classes()
          .that().areAnnotatedWith(javax.jdo.annotations.PersistenceCapable.class)
          .should().beAnnotatedWith(CommonDescribedPredicates.DomainObject_nature_ENTITY());
  }

  /**
   * This rule requires that classes annotated with the JDO {@link javax.jdo.annotations.PersistenceCapable} annotation
   * must also be annotated with the Apache Isis {@link javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter} annotation
   * with a value of {@link org.apache.isis.applib.jaxb.PersistentEntityAdapter}<code>.class</code>.
   *
   * <p>
   *     Tnis is so that entities can be transparently referenced from XML-style view models.
   * </p>
   */
  public static ArchRule classes_annotated_with_PersistenceCapable_must_also_be_annotated_with_XmlJavaAdapter() {
    return classes()
      .that().areAnnotatedWith(javax.jdo.annotations.PersistenceCapable.class)
            .should().beAnnotatedWith(CommonDescribedPredicates.XmlJavaTypeAdapter_value_PersistentEntityAdapter());
  }

  /**
   * This rule requires that classes annotated with the JDO {@link javax.jdo.annotations.PersistenceCapable} annotation
   * must also implement {@link Comparable}.
   *
   * <p>
   *     This is so that entities have a natural ordering and can safely be added to parented collections of type
   *     {@link java.util.SortedSet}.
   * </p>
   */
  public static ArchRule classes_annotated_with_PersistenceCapable_must_also_implement_Comparable() {
    return classes()
      .that().areAnnotatedWith(javax.jdo.annotations.PersistenceCapable.class)
      .should().implement(Comparable.class);
  }

  /**
   * This rule requires that classes annotated with the JDO {@link javax.jdo.annotations.PersistenceCapable} annotation
   * must also be annotated with the JDO {@link javax.jdo.annotations.Uniques} or {@link javax.jdo.annotations.Unique}
   * constraints.
   *
   * <p>
   *     This is so that entities will have an alternative business key in addition to the system-defined surrogate
   *     key.
   * </p>
   */
  public static ArchRule classes_annotated_with_PersistenceCapable_must_also_be_annotated_with_Uniques_or_Unique() {
    return classes()
      .that().areAnnotatedWith(javax.jdo.annotations.PersistenceCapable.class)
      .should().beAnnotatedWith(javax.jdo.annotations.Uniques.class)
      .orShould().beAnnotatedWith(javax.jdo.annotations.Unique.class);
  }

}
