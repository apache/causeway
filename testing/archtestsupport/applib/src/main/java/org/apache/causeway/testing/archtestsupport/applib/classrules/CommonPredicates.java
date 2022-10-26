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
import java.util.Optional;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaAnnotation;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaEnumConstant;
import com.tngtech.archunit.core.domain.JavaModifier;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;

import org.apache.causeway.applib.annotation.DomainObject;
import org.apache.causeway.applib.annotation.Nature;
import org.apache.causeway.applib.jaxb.PersistentEntityAdapter;

import lombok.val;
import lombok.experimental.UtilityClass;

@UtilityClass
class CommonPredicates {

    static DescribedPredicate<JavaClass> notAbstract() {
        return new DescribedPredicate<>("not abstract") {
            @Override
            public boolean test(final JavaClass javaClass) {
                val isAbstract = javaClass.getModifiers().stream().anyMatch((final JavaModifier x) -> x == JavaModifier.ABSTRACT);
                return !isAbstract;
            }
        };
    }

    static DescribedPredicate<JavaAnnotation<?>> DomainObject_nature_ENTITY() {
        return DomainObject_nature(Nature.ENTITY);
    }

    static DescribedPredicate<JavaAnnotation<?>> DomainObject_nature_MIXIN() {
        return DomainObject_nature(Nature.MIXIN);
    }

    static DescribedPredicate<JavaAnnotation<?>> DomainObject_nature_VIEW_MODEL() {
        return DomainObject_nature(Nature.VIEW_MODEL);
    }

    static DescribedPredicate<JavaAnnotation<?>> DomainObject_nature(final Nature expectedNature) {
        return new DescribedPredicate<>(String.format("@DomainObject(nature=%s)", expectedNature.name())) {
            @Override
            public boolean test(final JavaAnnotation<?> javaAnnotation) {
                if (!javaAnnotation.getRawType().isAssignableTo(DomainObject.class)) {
                    return false;
                }
                val properties = javaAnnotation.getProperties();
                val nature = properties.get("nature");
                return nature instanceof JavaEnumConstant &&
                        Objects.equals(((JavaEnumConstant) nature).name(), expectedNature.name());
            }
        };
    }

    static DescribedPredicate<JavaAnnotation<?>> XmlJavaTypeAdapter_value_PersistentEntityAdapter() {
        return new DescribedPredicate<JavaAnnotation<?>>("@XmlJavaTypeAdapter(PersistentEntityAdapter.class)") {
            @Override public boolean test(final JavaAnnotation<?> javaAnnotation) {
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

    static DescribedPredicate<JavaClass> ofAnEnum() {
        return new DescribedPredicate<JavaClass>("that is an enum") {
            @Override
            public boolean test(final JavaClass input) {
                return input.isEnum();
            }
        };
    }

    static DescribedPredicate<JavaAnnotation<?>> annotationOf(
            final Class<?> annotClass,
            final String attribute,
            final String attributeValue) {

        val description = String
                .format("@%s(%s = %s)", annotClass.getSimpleName(), attribute, attributeValue);
        return new DescribedPredicate<JavaAnnotation<?>>(description) {
            @Override
            public boolean test(final JavaAnnotation<?> input) {
                if (!input.getRawType().getFullName().equals(annotClass.getName())) {
                    return false;
                }
                final Optional<Object> enumeratedValue = input.get(attribute);
                return enumeratedValue.isPresent() && enumeratedValue.get().toString()
                        .equals(attributeValue);
            }
        };
    }

    static ArchCondition<JavaClass> haveNoArgProtectedConstructor() {
        return new ArchCondition<JavaClass>("have protected no-arg constructor") {
            @Override public void check(final JavaClass javaClass, final ConditionEvents conditionEvents) {
                val noArgConstructorIfAny = javaClass.tryGetConstructor();
                if (!noArgConstructorIfAny.isPresent()) {
                    conditionEvents.add(new SimpleConditionEvent(javaClass, false,
                            String.format("%s does not have a no-arg constructor", javaClass.getSimpleName())));
                    return;
                }
                val noArgConstructor = noArgConstructorIfAny.get();
                val protectedModifierIfAny = noArgConstructor.getModifiers().stream()
                        .filter(x -> x == JavaModifier.PROTECTED).findAny();
                if (!protectedModifierIfAny.isPresent()) {

                    conditionEvents.add(new SimpleConditionEvent(javaClass, false, String.format(
                            "%s has a no-arg constructor but it does not have protected visibility",
                            javaClass.getSimpleName())));
                    return;
                }
            }
        };
    }

    static DescribedPredicate<JavaClass> areNotAbstract() {
        return new DescribedPredicate<JavaClass>("are not abstract") {
            @Override
            public boolean test(final JavaClass javaClass) {
                val isAbstract = javaClass.getModifiers().stream().anyMatch((final JavaModifier x) -> x == JavaModifier.ABSTRACT);
                return !isAbstract;
            }
        };
    }

}
