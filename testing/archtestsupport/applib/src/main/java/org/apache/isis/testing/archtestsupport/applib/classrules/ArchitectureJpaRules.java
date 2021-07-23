package org.apache.isis.testing.archtestsupport.applib.classrules;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Objects;

import javax.inject.Inject;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.Version;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaAnnotation;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import com.tngtech.archunit.lang.syntax.elements.ClassesShouldConjunction;

import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.persistence.jpa.applib.integration.IsisEntityListener;
import org.apache.isis.persistence.jpa.applib.integration.JpaEntityInjectionPointResolver;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.fields;
import lombok.experimental.UtilityClass;
import lombok.val;
import static org.apache.isis.testing.archtestsupport.applib.classrules.CommonPredicates.annotationOf;
import static org.apache.isis.testing.archtestsupport.applib.classrules.CommonPredicates.haveNoArgProtectedConstructor;
import static org.apache.isis.testing.archtestsupport.applib.classrules.CommonPredicates.ofAnEnum;

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
    public static ArchRule every_jpa_Entity_must_be_annotated_with_DomainObject_nature_of_ENTITY() {
        return classes()
                .that().areAnnotatedWith(Entity.class)
                .should().beAnnotatedWith(CommonPredicates.DomainObject_nature_ENTITY());
    }

    /**
     * This rule requires that classes annotated with the JPA {@link Entity} annotation must also be
     * annotated with the Apache Isis {@link javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter} annotation
     * with a value of {@link org.apache.isis.applib.jaxb.PersistentEntityAdapter}<code>.class</code>.
     *
     * <p>
     * Tnis is so that entities can be transparently referenced from XML-style view models.
     * </p>
     */
    public static ArchRule every_jpa_Entity_must_be_annotated_with_XmlJavaAdapter_of_PersistentEntityAdapter() {
        return classes()
                .that().areAnnotatedWith(Entity.class)
                .should().beAnnotatedWith(CommonPredicates.XmlJavaTypeAdapter_value_PersistentEntityAdapter());
    }

    /**
     * This rule requires that classes annotated with the JPA {@link Entity} annotation must also be
     * annotated with the {@link javax.persistence.EntityListeners} annotation that includes
     * a value of <code>org.apache.isis.persistence.jpa.applib.integration.IsisEntityListener.class</code>.
     *
     * <p>
     * Tnis is so that entities can be transparently referenced from XML-style view models.
     * </p>
     */
    public static ArchRule every_jpa_Entity_must_be_annotated_as_an_IsisEntityListener() {
        return classes()
                .that().areAnnotatedWith(Entity.class)
                .should().beAnnotatedWith(EntityListeners_with_IsisEntityListener());
    }

    private static DescribedPredicate<JavaAnnotation<?>> EntityListeners_with_IsisEntityListener() {
        return new DescribedPredicate<JavaAnnotation<?>>("@EntityListener({IsisEntityListener.class})") {
            @Override public boolean apply(final JavaAnnotation<?> javaAnnotation) {
                if (!javaAnnotation.getRawType().isAssignableTo(EntityListeners.class)) {
                    return false;
                }
                val properties = javaAnnotation.getProperties();
                val listeners = properties.get("value");
                return listeners instanceof JavaClass[] && containsIsisEntityListener((JavaClass[]) listeners);
            }

            private boolean containsIsisEntityListener(final JavaClass[] classes) {
                //noinspection deprecation
                return Arrays.stream(classes)
                        .anyMatch(x -> Objects.equals(x.getFullName(), JpaEntityInjectionPointResolver.class.getName())
                                || x.isAssignableTo(IsisEntityListener.class));
            }
        };
    }

    /**
     * This rule requires that classes annotated with the JPA {@link Entity} annotation must also be
     * implement {@link Comparable}.
     *
     * <p>
     * This is so that entities have a natural ordering and can safely be added to parented collections of type
     * {@link java.util.SortedSet}.
     * </p>
     */
    public static ArchRule every_jpa_Entity_must_implement_Comparable() {
        return classes()
                .that().areAnnotatedWith(Entity.class)
                .should().implement(Comparable.class);
    }

    /**
     * This rule requires that classes annotated with the JPA {@link Entity} annotation must also be annotated with the
     * JPA {@link Table} annotation which includes {@link Table#uniqueConstraints() uniqueConstraints}.
     *
     * <p>
     * This is so that entities will have an alternative business key in addition to the system-defined surrogate
     * key.
     * </p>
     */
    public static ArchRule every_jpa_Entity_must_be_annotated_as_Table_with_uniqueConstraints() {
        return classes()
                .that().areAnnotatedWith(Entity.class)
                .should().beAnnotatedWith(Table_uniqueConstraints());
    }

    /**
     * This rule requires that classes annotated with the JPA {@link Entity} annotation must also be annotated with the
     * JPA {@link Table} annotation which includes {@link Table#schema()}  schema}.
     *
     * <p>
     * This is so that entity tables are organised into an appropriate structure (ideally mirroring that of the
     * entities).
     * </p>
     */
    public static ArchRule every_jpa_Entity_must_be_annotated_as_Table_with_schema() {
        return classes()
                .that().areAnnotatedWith(Entity.class)
                .should().beAnnotatedWith(Table_schema());
    }

    /**
     * This rule requires that enum fields in classes annotated with the JPA {@link Entity} annotation must also be
     * annotated with the JPA {@link Enumerated} annotation indicating that they should be persisted as
     * {@link javax.persistence.EnumType#STRING string}s (rather than ordinal numbers).
     *
     * <p>
     * The rationale here is that a string is (arguably) more stable than an ordinal number, and is certainly easier
     * to work with when querying the database.  The downside is slightly more space to persist the data, and slightly
     * less performant (not that it would be noticeable).
     * </p>
     */
    public static ArchRule every_enum_field_of_jpa_Entity_must_be_annotated_with_Enumerable_STRING() {
        return fields().that()
                .areDeclaredInClassesThat(areEntities())
                .and().haveRawType(ofAnEnum())
                .should().beAnnotatedWith(annotationOf(Enumerated.class, "value",
                        "EnumType.STRING"))
                ;
    }

    /**
     * This rule requires that injected fields in classes annotated with the JPA {@link Entity} annotation must also be
     * annotated with JPA {@link Transient} annotation.
     *
     * <p>
     * The rationale here is that injected services are managed by the runtime and are not/cannot be persisted.
     * </p>
     */
    public static ArchRule every_injected_field_of_jpa_Entity_must_be_annotated_with_Transient() {
        return fields().that()
                .areDeclaredInClassesThat(areEntities())
                .and().areAnnotatedWith(Inject.class)
                .should().beAnnotatedWith(Transient.class)
                ;
    }

    /**
     * This rule requires that classes annotated with the JPA {@link Entity} annotation must contain an <code>id</code>
     * field that is itself annotated with {@link Id}.
     *
     * <p>
     * This is part of the standard contract for JPA entities.
     * </p>
     */
    public static ArchRule every_jpa_Entity_must_have_an_id_field() {
        return everyJpa_Entity_must_have_a_field_named_and_annotated("id", Id.class);
    }

    /**
     * This rule requires that classes annotated with the JPA {@link Entity} annotation must contain a
     * <code>version</code> field that is itself annotated with {@link javax.persistence.Version}.
     *
     * <p>
     * This is good practice for JPA entities to implement optimistic locking
     * </p>
     */
    public static ArchRule every_jpa_Entity_must_have_a_version_field() {
        return everyJpa_Entity_must_have_a_field_named_and_annotated("version", Version.class);
    }

    private static ClassesShouldConjunction everyJpa_Entity_must_have_a_field_named_and_annotated(
            final String fieldName,
            final Class<? extends Annotation> annotationClass) {
        final String fieldAnnotation = annotationClass.getSimpleName();
        return classes().that()
                .areAnnotatedWith(Entity.class)
                .should(new ArchCondition<JavaClass>(
                        String.format("have a field named '%s' annotated with '@%s'", fieldName, fieldAnnotation)) {
                    @Override public void check(final JavaClass javaClass, final ConditionEvents conditionEvents) {
                        val javaFieldIfAny = javaClass.getAllFields().stream()
                                .filter(x -> Objects.equals(x.getName(), fieldName)).findAny();
                        if(!javaFieldIfAny.isPresent()) {
                            conditionEvents.add(new SimpleConditionEvent(javaClass, false,
                                    String.format("%s does not have a field named '%s'", javaClass.getSimpleName(), fieldName)));
                            return;
                        }
                        val javaField = javaFieldIfAny.get();
                        if(!javaField.isAnnotatedWith(annotationClass)) {
                            conditionEvents.add(new SimpleConditionEvent(javaClass, false,
                                    String.format("%s has field named '%s' but it is not annotated with '@%s'",
                                            javaClass.getSimpleName(), fieldName, fieldAnnotation)));
                        }
                    }
                })
                ;
    }

    private static DescribedPredicate<JavaAnnotation<?>> Table_schema() {
        return new DescribedPredicate<JavaAnnotation<?>>("@Table(schema=...)") {
            @Override public boolean apply(final JavaAnnotation<?> javaAnnotation) {
                if (!javaAnnotation.getRawType().isAssignableTo(Table.class)) {
                    return false;
                }
                val properties = javaAnnotation.getProperties();
                val schema = properties.get("schema");
                return schema instanceof String &&
                        ((String) schema).length() > 0;
            }
        };
    }

    private static DescribedPredicate<JavaClass> areEntities() {
        return new DescribedPredicate<JavaClass>("are entities") {
            @Override
            public boolean apply(JavaClass input) {
                return input.isAnnotatedWith(Entity.class);
            }
        };
    }

    private static DescribedPredicate<JavaAnnotation<?>> Table_uniqueConstraints() {
        return new DescribedPredicate<JavaAnnotation<?>>("@Table(uniqueConstraints=...)") {
            @Override public boolean apply(final JavaAnnotation<?> javaAnnotation) {
                if (!javaAnnotation.getRawType().isAssignableTo(Table.class)) {
                    return false;
                }
                val properties = javaAnnotation.getProperties();
                val uniqueConstraints = properties.get("uniqueConstraints");
                return uniqueConstraints instanceof JavaAnnotation[] &&
                        ((JavaAnnotation<?>[]) uniqueConstraints).length > 0;
            }
        };
    }

    /**
     * This rule requires that classes annotated with the JPA {@link Entity} annotation have a no-arg constructor
     * with <code>protected</code> visibility.
     *
     * <p>
     * The rationale is to encourage the use of static factory methods.
     * </p>
     */
    public static ArchRule every_jpa_Entity_must_have_protected_no_arg_constructor() {
        return classes().that().areAnnotatedWith(Entity.class)
                .should(haveNoArgProtectedConstructor());
    }


}
