/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.apache.causeway.testing.archtestsupport.applib.classrules;

import java.util.Objects;

import javax.inject.Inject;
import javax.jdo.annotations.Discriminator;
import javax.jdo.annotations.NotPersistent;
import javax.jdo.annotations.PersistenceCapable;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaAnnotation;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaEnumConstant;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;

import static com.tngtech.archunit.base.DescribedPredicate.not;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.fields;

import org.apache.causeway.applib.annotation.DomainObject;

import lombok.val;
import lombok.experimental.UtilityClass;

/**
 * A library of architecture tests to ensure coding conventions are followed for classes annotated with
 * the JDO {@link javax.jdo.annotations.PersistenceCapable} annotation.
 *
 * @since 2.0 {@index}
 */
@UtilityClass
public class ArchitectureJdoRules {

    public static ArchRule every_logicalTypeName_and_jdo_discriminator_must_be_same() {
        return classes()
                .that().areAnnotatedWith(DomainObject.class)
                .and(new DescribedPredicate<>("have a logicalTypeName") {
                    @Override
                    public boolean test(final JavaClass javaClass) {
                        return _LogicalNaming.hasExplicitLogicalName(javaClass);
                    }
                })
                .and(new DescribedPredicate<>("have a @Discriminator") {
                    @Override
                    public boolean test(final JavaClass javaClass) {
                        val discriminatorIfAny = javaClass.tryGetAnnotationOfType(Discriminator.class);
                        return discriminatorIfAny.isPresent();
                    }
                })
                .should(new ArchCondition<>("be the same") {
                    @Override
                    public void check(final JavaClass javaClass, final ConditionEvents conditionEvents) {
                        val logicalTypeName = _LogicalNaming.logicalNameFor(javaClass);
                        val discriminatorValue = javaClass.getAnnotationOfType(Discriminator.class).value();
                        if (!Objects.equals(logicalTypeName, discriminatorValue)) {
                            conditionEvents.add(
                                    new SimpleConditionEvent(javaClass, false,
                                    String.format("@DomainObject(logicalTypeName = '%s') vs @Discriminator('%s')",
                                            logicalTypeName, discriminatorValue)));
                        }
                    }
                });
    }

    /**
     * This rule requires that classes annotated with the JDO {@link javax.jdo.annotations.PersistenceCapable} annotation
     * must also be annotated with the Apache Causeway {@link DomainObject} annotation specifying that its
     * {@link DomainObject#nature() nature} is an {@link org.apache.causeway.applib.annotation.Nature#ENTITY entity}.
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
     *
     * <p>
     *     The rule does <i>not</i> apply to any entities that are subtype entities where there
     *     is a supertype entity.
     * </p>
     */
    public static ArchRule every_jdo_PersistenceCapable_must_be_annotated_with_Version() {
        return classes()
                .that().areAnnotatedWith(javax.jdo.annotations.PersistenceCapable.class)
                .and(not(areSubtypeEntities()))
                .should().beAnnotatedWith(javax.jdo.annotations.Version.class);
    }

    /**
     * This rule requires that classes annotated with the JDO {@link javax.jdo.annotations.PersistenceCapable} annotation
     * must also be annotated with the Apache Causeway {@link javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter} annotation
     * with a value of {@link org.apache.causeway.applib.jaxb.PersistentEntityAdapter}<code>.class</code>.
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
     * and is not a subtype entity, must also be annotated with the JDO {@link javax.jdo.annotations.Uniques} or
     * {@link javax.jdo.annotations.Unique} constraints.
     *
     * <p>
     * This is so that entities will have an alternative business key in addition to the system-defined surrogate
     * key.
     * </p>
     */
    public static ArchRule every_jdo_PersistenceCapable_must_be_annotated_as_Uniques_or_Unique() {
        return classes()
                .that().areAnnotatedWith(javax.jdo.annotations.PersistenceCapable.class)
                .and(not(areSubtypeEntities()))
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
     *
     * <p>
     *     The rule does <i>not</i> apply to any entities that are subtype entities where there
     *     is a supertype entity.
     * </p>
     */
    public static ArchRule every_jdo_PersistenceCapable_with_DATASTORE_identityType_must_be_annotated_as_DataStoreIdentity() {
        return classes()
                .that().areAnnotatedWith(PersistenceCapable_with_DATASTORE_identityType())
                .and(not(areSubtypeEntities()))
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
        return new DescribedPredicate<>("@PersistenceCapable(schema=...)") {
            @Override
            public boolean test(final JavaAnnotation<?> javaAnnotation) {
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
        return new DescribedPredicate<>("@PersistenceCapable(identityType=DATASTORE)") {
            @Override
            public boolean test(final JavaAnnotation<?> javaAnnotation) {
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
        return new DescribedPredicate<>("are entities") {
            @Override
            public boolean test(final JavaClass input) {
                return input.isAnnotatedWith(PersistenceCapable.class);
            }
        };
    }

    static DescribedPredicate<? super JavaClass> areSubtypeEntities() {
        return new DescribedPredicate<>("are subtype entities ") {
            @Override
            public boolean test(final JavaClass input) {
                val superclassIfAny = input.getSuperclass();
                if (!superclassIfAny.isPresent()) {
                    return false;
                }
                val superType = superclassIfAny.get();
                val superClass = superType.toErasure();
                val persistenceCapableIfAny = superClass
                        .tryGetAnnotationOfType(PersistenceCapable.class);
                return persistenceCapableIfAny.isPresent();
            }
        };
    }


}
