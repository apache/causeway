package org.apache.isis.testing.archtestsupport.applib.classrules;

import javax.inject.Inject;
import javax.jdo.annotations.NotPersistent;
import javax.jdo.annotations.PersistenceCapable;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaAnnotation;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaEnumConstant;
import com.tngtech.archunit.core.domain.JavaModifier;
import com.tngtech.archunit.lang.ArchRule;

import org.apache.isis.applib.annotation.DomainObject;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.fields;
import lombok.experimental.UtilityClass;
import lombok.val;
import static org.apache.isis.testing.archtestsupport.applib.classrules.CommonPredicates.haveNoArgProtectedConstructor;

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
    public static ArchRule every_jdo_PersistenceCapable_must_be_annotated_with_DomainObject_nature_of_ENTITY() {
        return classes()
                .that().areAnnotatedWith(javax.jdo.annotations.PersistenceCapable.class)
                .should().beAnnotatedWith(CommonPredicates.DomainObject_nature_ENTITY());
    }

    /**
     * This rule requires that classes annotated with the JDO {@link javax.jdo.annotations.PersistenceCapable} annotation
     * must also be annotated with the JDO {@link javax.jdo.annotations.Version} annotation (in support of optimistic
     * locking checks).
     */
    public static ArchRule every_jdo_PersistenceCapable_must_be_annotated_with_Version() {
        return classes()
                .that().areAnnotatedWith(javax.jdo.annotations.PersistenceCapable.class)
                .should().beAnnotatedWith(javax.jdo.annotations.Version.class);
    }

    /**
     * This rule requires that classes annotated with the JDO {@link javax.jdo.annotations.PersistenceCapable} annotation
     * must also be annotated with the Apache Isis {@link javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter} annotation
     * with a value of {@link org.apache.isis.applib.jaxb.PersistentEntityAdapter}<code>.class</code>.
     *
     * <p>
     * Tnis is so that entities can be transparently referenced from XML-style view models.
     * </p>
     */
    public static ArchRule every_jdo_PersistenceCapable_must_be_annotated_as_XmlJavaAdapter_PersistentEntityAdapter() {
        return classes()
                .that().areAnnotatedWith(javax.jdo.annotations.PersistenceCapable.class)
                .should().beAnnotatedWith(CommonPredicates.XmlJavaTypeAdapter_value_PersistentEntityAdapter());
    }

    /**
     * This rule requires that classes annotated with the JDO {@link javax.jdo.annotations.PersistenceCapable} annotation
     * must also implement {@link Comparable}.
     *
     * <p>
     * This is so that entities have a natural ordering and can safely be added to parented collections of type
     * {@link java.util.SortedSet}.
     * </p>
     */
    public static ArchRule every_jdo_PersistenceCapable_must_implement_Comparable() {
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
     * This is so that entities will have an alternative business key in addition to the system-defined surrogate
     * key.
     * </p>
     */
    public static ArchRule every_jdo_PersistenceCapable_must_be_annotated_as_Uniques_or_Unique() {
        return classes()
                .that().areAnnotatedWith(javax.jdo.annotations.PersistenceCapable.class)
                .should().beAnnotatedWith(javax.jdo.annotations.Uniques.class)
                .orShould().beAnnotatedWith(javax.jdo.annotations.Unique.class);
    }

    /**
     * This rule requires that classes annotated with the JDO {@link javax.jdo.annotations.PersistenceCapable} annotation
     * must also be annotated with the JDO {@link javax.jdo.annotations.Uniques} or {@link javax.jdo.annotations.Unique}
     * constraints.
     *
     * <p>
     * This is so that entities will have an alternative business key in addition to the system-defined surrogate
     * key.
     * </p>
     */
    public static ArchRule every_jdo_PersistenceCapable_with_DATASTORE_identityType_must_be_annotated_as_DataStoreIdentity() {
        return classes()
                .that().areAnnotatedWith(PersistenceCapable_with_DATASTORE_identityType())
                .should().beAnnotatedWith(javax.jdo.annotations.DatastoreIdentity.class);
    }

    /**
     * This rule requires that classes annotated with the JDO {@link javax.jdo.annotations.PersistenceCapable}
     * annotation must have the {@link javax.jdo.annotations.PersistenceCapable#schema() schema}  attribute set.
     *
     * <p>
     * This is so that entity tables are organised into an appropriate structure (ideally mirroring that of the
     * entities).
     * </p>
     */
    public static ArchRule every_jdo_PersistenceCapable_must_have_schema() {
        return classes()
                .that().areAnnotatedWith(PersistenceCapable.class)
                .should().beAnnotatedWith(PersistenceCapable_schema());
    }


    static DescribedPredicate<JavaAnnotation<?>> PersistenceCapable_schema() {
        return new DescribedPredicate<JavaAnnotation<?>>("@PersistenceCapable(schema=...)") {
            @Override public boolean apply(final JavaAnnotation<?> javaAnnotation) {
                if (!javaAnnotation.getRawType().isAssignableTo(PersistenceCapable.class)) {
                    return false;
                }
                val properties = javaAnnotation.getProperties();
                val schema = properties.get("schema");
                return schema instanceof String &&
                        ((String) schema).length() > 0;
            }
        };
    }

    static DescribedPredicate<JavaAnnotation<?>> PersistenceCapable_with_DATASTORE_identityType() {
        return new DescribedPredicate<JavaAnnotation<?>>("@PersistenceCapable(identityType=DATASTORE)") {
            @Override public boolean apply(final JavaAnnotation<?> javaAnnotation) {
                if (!javaAnnotation.getRawType().isAssignableTo(PersistenceCapable.class)) {
                    return false;
                }
                val properties = javaAnnotation.getProperties();
                val identityType = properties.get("identityType");
                return identityType instanceof JavaEnumConstant &&
                        identityType.toString().equals("IdentityType.DATASTORE");
            }
        };
    }

    /**
     * This rule requires that injected fields in classes annotated with the JDO {@link PersistenceCapable} annotation
     * must also be annotated with JDO {@link javax.jdo.annotations.NotPersistent} annotation.
     *
     * <p>
     * The rationale here is that injected services are managed by the runtime and are not/cannot be persisted.
     * </p>
     *
     * <p>
     * In fact, JDO is tolerant to such fields and will not trip up. So another more pragmatic reason is that, if
     * using JDO type-safe queries, then the injected fields will not appear incorrectly in the generated Q classes.
     * </p>
     */
    public static ArchRule every_injected_field_of_jdo_PersistenceCapable_must_be_annotated_with_NotPersistent() {
        return fields().that()
                .areDeclaredInClassesThat(areEntities())
                .and().areAnnotatedWith(Inject.class)
                .should().beAnnotatedWith(NotPersistent.class)
                ;
    }

    static DescribedPredicate<JavaClass> areEntities() {
        return new DescribedPredicate<JavaClass>("are entities") {
            @Override
            public boolean apply(JavaClass input) {
                return input.isAnnotatedWith(PersistenceCapable.class);
            }
        };
    }



}
