package org.apache.isis.testing.archtestsupport.applib.classrules;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaAnnotation;
import com.tngtech.archunit.lang.ArchRule;

import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.DomainServiceLayout;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import lombok.experimental.UtilityClass;
import lombok.val;

/**
 * A library of architecture tests to ensure coding conventions are followed for classes annotated with
 * {@link DomainService}.
 *
 * @since 2.0 {@index}
 */
@UtilityClass
public class ArchitectureDomainServiceRules {

    /**
     * This rule requires that classes annotated with the {@link DomainService} annotation must specify their
     * {@link DomainService#logicalTypeName() logicalTypeName}.
     */
    public static ArchRule classes_annotated_with_DomainService_must_specify_logicalTypeName() {
        return classes()
                .that().areAnnotatedWith(DomainService.class)
                .should().beAnnotatedWith(DomainService_logicalTypeName());
    }

    static DescribedPredicate<JavaAnnotation<?>> DomainService_logicalTypeName() {
        return new DescribedPredicate<JavaAnnotation<?>>("@DomainService(logicalTypeName=...)") {
            @Override public boolean apply(final JavaAnnotation<?> javaAnnotation) {
                if (!javaAnnotation.getRawType().isAssignableTo(DomainService.class)) {
                    return false;
                }
                val properties = javaAnnotation.getProperties();
                val value = properties.get("logicalTypeName");
                return value instanceof String && ((String) value).length() > 0;
            }
        };
    }

    /**
     * This rule requires that classes annotated with the {@link DomainService} annotation must also be
     * annotated with the {@link DomainServiceLayout} annotation.
     */
    public static ArchRule classes_annotated_with_DomainService_must_also_be_annotated_with_DomainServiceLayout() {
        return classes()
                .that().areAnnotatedWith(DomainService.class)
                .should().beAnnotatedWith(DomainServiceLayout.class);
    }

}
