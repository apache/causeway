package org.apache.isis.testing.archtestsupport.applib.classrules;

import java.io.Serializable;
import java.lang.annotation.Annotation;

import javax.inject.Inject;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaAnnotation;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaModifier;
import com.tngtech.archunit.lang.ArchRule;

import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.DomainObjectLayout;
import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.DomainServiceLayout;
import org.apache.isis.applib.annotation.Nature;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Repository;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.fields;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;
import lombok.experimental.UtilityClass;
import lombok.val;

/**
 * A library of architecture tests to ensure coding conventions are followed for domain classes, for example as
 * annotated with {@link DomainObject} or {@link DomainService} but also more generally, for example repositories
 * that are not necessarily part of the framework's metamodel.
 *
 * @since 2.0 {@index}
 */
@UtilityClass
public class ArchitectureDomainRules {

    /**
     * This rule requires that classes annotated with the {@link DomainObject} annotation must specify their
     * {@link DomainObject#logicalTypeName() logicalTypeName}.
     */
    public static ArchRule every_DomainObject_must_specify_logicalTypeName() {
        return classes()
                .that().areAnnotatedWith(DomainObject.class)
                .should().beAnnotatedWith(DomainObject_logicalTypeName());
    }

    /**
     * This rule requires that classes annotated with the {@link DomainObject} annotation must also be
     * annotated with the {@link DomainObjectLayout} annotation.
     */
    public static ArchRule every_DomainObject_must_also_be_annotated_with_DomainObjectLayout() {
        return classes()
                .that().areAnnotatedWith(DomainObject.class)
                .should().beAnnotatedWith(DomainObjectLayout.class);
    }

    /**
     * This rule requires that classes annotated with the {@link DomainService} annotation must specify their
     * {@link DomainService#logicalTypeName() logicalTypeName}.
     */
    public static ArchRule every_DomainService_must_specify_logicalTypeName() {
        return classes()
                .that().areAnnotatedWith(DomainService.class)
                .should().beAnnotatedWith(DomainService_logicalTypeName());
    }

    /**
     * This rule requires that classes annotated with the {@link DomainService} annotation must also be
     * annotated with the {@link DomainServiceLayout} annotation.
     */
    public static ArchRule every_DomainService_must_also_be_annotated_with_DomainServiceLayout() {
        return classes()
                .that().areAnnotatedWith(DomainService.class)
                .should().beAnnotatedWith(DomainServiceLayout.class);
    }


    static DescribedPredicate<JavaAnnotation<?>> DomainService_logicalTypeName() {
        return DomainXxx_logicalTypeName(DomainService.class);
    }

    static DescribedPredicate<JavaAnnotation<?>> DomainObject_logicalTypeName() {
        return DomainXxx_logicalTypeName(DomainObject.class);
    }

    static DescribedPredicate<JavaAnnotation<?>> DomainXxx_logicalTypeName(final Class<? extends Annotation> annotationClass) {
        return new DescribedPredicate<JavaAnnotation<?>>(
                String.format("@%s(logicalTypeName=...)", annotationClass.getSimpleName())) {
            @Override public boolean apply(final JavaAnnotation<?> javaAnnotation) {
                if (!javaAnnotation.getRawType().isAssignableTo(annotationClass)) {
                    return false;
                }
                val properties = javaAnnotation.getProperties();
                val value = properties.get("logicalTypeName");
                return value instanceof String && ((String) value).length() > 0;
            }
        };
    }

    /**
     * This rule requires that classes annotated with the {@link Repository} annotation should follow the naming
     * convention <code>XxxRepository</code>.
     *
     * <p>
     *     The rationale is so that the pattern is easy to spot and to search for,
     * </p>
     *
     * @see #every_class_named_Repository_must_be_annotated_correctly()
     */
    public static ArchRule every_Repository_must_be_follow_naming_conventions() {
        return classes()
                .that().areAnnotatedWith(Repository.class)
                .should().haveNameMatching(".*Repository");
    }

    /**
     * This rule requires that classes annotated with the {@link org.springframework.stereotype.Controller} annotation
     * should follow the naming convention <code>XxxController</code>.
     *
     * <p>
     *     The rationale is so that the pattern is easy to spot and to search for,
     * </p>
     *
     * @see #every_class_named_Controller_must_be_annotated_correctly()
     */
    public static ArchRule every_Controller_must_be_follow_naming_conventions() {
        return classes()
                .that().areAnnotatedWith(Controller.class)
                .should().haveNameMatching(".*Controller");
    }

    /**
     * This rule requires that classes named <code>XxxRepository</code> should also be annotated with the
     * {@link org.springframework.stereotype.Repository} annotation.
     *
     * <p>
     *     The rationale is so that the pattern is easy to spot and to search for,
     * </p>
     *
     * @see #every_Repository_must_be_follow_naming_conventions()
     */
    public static ArchRule every_class_named_Repository_must_be_annotated_correctly() {
        return classes()
                .that().haveNameMatching(".*Repository")
                .should().beAnnotatedWith(Repository.class);
    }

    /**
     * This rule requires that classes named <code>XxxController</code> should also be annotated with the
     * {@link org.springframework.stereotype.Controller} annotation.
     *
     * <p>
     *     The rationale is so that the pattern is easy to spot and to search for,
     * </p>
     *
     * @see #every_Controller_must_be_follow_naming_conventions()
     */
    public static ArchRule every_class_named_Controller_must_be_annotated_correctly() {
        return classes()
                .that().haveNameMatching(".*Controller")
                .should().beAnnotatedWith(Controller.class);
    }


    /**
     * This rule requires that injected fields in jaxb view models (that is, classes annotated with the JAXB
     * {@link javax.xml.bind.annotation.XmlRootElement} annotation) must also be annotated with JAXB
     * {@link javax.xml.bind.annotation.XmlTransient} annotation.
     *
     * <p>
     * The rationale here is that injected services are managed by the runtime and are not/cannot be serialized to
     * XML.
     * </p>
     */
    public static ArchRule every_injected_field_of_jaxb_view_model_must_be_annotated_with_XmlTransient() {
        return fields().that()
                .areDeclaredInClassesThat(areJaxbViewModels())
                .and().areAnnotatedWith(Inject.class)
                .should().beAnnotatedWith(XmlTransient.class)
                ;
    }

    private static DescribedPredicate<JavaClass> areJaxbViewModels() {
        return new DescribedPredicate<JavaClass>("are JAXB view models") {
            @Override
            public boolean apply(JavaClass input) {
                return input.isAnnotatedWith(XmlRootElement.class);
            }
        };
    }

    /**
     * This rule requires that injected fields in jaxb view models (that is, classes annotated with the JAXB
     * {@link javax.xml.bind.annotation.XmlRootElement} annotation) must also be annotated with JAXB
     * {@link javax.xml.bind.annotation.XmlTransient} annotation.
     *
     * <p>
     * The rationale here is that injected services are managed by the runtime and are not/cannot be serialized to
     * XML.
     * </p>
     */
    public static ArchRule every_injected_field_of_serializable_view_model_must_be_transient() {
        return fields().that()
                .areDeclaredInClassesThat(areSerializableViewModels())
                .and().areAnnotatedWith(Inject.class)
                .should().haveModifier(JavaModifier.TRANSIENT)
                ;
    }

    private static DescribedPredicate<JavaClass> areSerializableViewModels() {
        return new DescribedPredicate<JavaClass>("are serializable view models") {
            @Override
            public boolean apply(JavaClass input) {
                val domainObjectIfAny = input.tryGetAnnotationOfType(DomainObject.class);
                if(!domainObjectIfAny.isPresent()) {
                    return false;
                }
                val domainObject = domainObjectIfAny.get();
                final Nature nature = domainObject.nature();
                if(nature != Nature.VIEW_MODEL) {
                    return false;
                }
                return input.isAssignableTo(Serializable.class);
            }
        };
    }

    /**
     * This rule requires that finders of repos reutrn either {@link java.util.Collection}s or
     * {@link java.util.Optional}s.
     *
     * <p>
     *     In particular, this excludes the option of returning a simple scalar, such as <code>Customer</code>;
     *     they must return an <code>Optional&lt;Customer&gt;</code> instead.  This forces the caller to
     *     handle the fact that the result might be empty (ie no result).
     * </p>
     * @return
     */
    public static ArchRule every_finder_method_in_Repository_must_return_either_Collection_or_Optional() {
        return methods()
                .that().haveNameMatching("find.*")
                .and().areDeclaredInClassesThat().areAnnotatedWith(Repository.class)
                .should().haveRawReturnType(eitherOptionalOrCollection());
    }

    private static DescribedPredicate<JavaClass> eitherOptionalOrCollection() {
        return new DescribedPredicate<JavaClass>("either Optional or Collection") {
            @Override
            public boolean apply(JavaClass input) {
                return input.isAssignableTo(java.util.Optional.class)
                        || input.isAssignableTo(java.util.Collection.class);
            }
        };
    }



}
