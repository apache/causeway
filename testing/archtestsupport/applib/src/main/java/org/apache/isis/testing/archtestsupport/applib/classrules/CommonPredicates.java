package org.apache.isis.testing.archtestsupport.applib.classrules;

import java.util.Objects;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.base.Optional;
import com.tngtech.archunit.core.domain.JavaAnnotation;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaEnumConstant;
import com.tngtech.archunit.core.domain.JavaModifier;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;

import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.Nature;
import org.apache.isis.applib.jaxb.PersistentEntityAdapter;

import lombok.experimental.UtilityClass;
import lombok.val;

@UtilityClass
class CommonPredicates {

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

    static DescribedPredicate<JavaClass> ofAnEnum() {
        return new DescribedPredicate<JavaClass>("that is an enum") {
            @Override
            public boolean apply(JavaClass input) {
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
            public boolean apply(JavaAnnotation<?> input) {
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

}
