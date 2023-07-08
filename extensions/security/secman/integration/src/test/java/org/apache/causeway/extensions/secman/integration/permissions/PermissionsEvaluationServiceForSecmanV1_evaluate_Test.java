package org.apache.causeway.extensions.secman.integration.permissions;

import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.specloader.SpecificationLoader;
import org.apache.causeway.extensions.secman.applib.permission.dom.ApplicationPermissionValue;
import org.apache.causeway.extensions.secman.applib.permission.dom.ApplicationPermissionValueSet;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.apache.causeway.applib.services.appfeat.ApplicationFeatureId.*;
import static org.apache.causeway.core.config.CausewayConfiguration.Extensions.Secman.PermissionsEvaluationPolicy.ALLOW_BEATS_VETO;
import static org.apache.causeway.extensions.secman.applib.permission.dom.ApplicationPermissionMode.CHANGING;
import static org.apache.causeway.extensions.secman.applib.permission.dom.ApplicationPermissionMode.VIEWING;
import static org.apache.causeway.extensions.secman.applib.permission.dom.ApplicationPermissionRule.ALLOW;
import static org.apache.causeway.extensions.secman.applib.permission.dom.ApplicationPermissionRule.VETO;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class PermissionsEvaluationServiceForSecmanV1_evaluate_Test {

    static class Customer {}

    @Mock SpecificationLoader mockSpecificationLoader;
    @Mock ObjectSpecification mockSpecificationForCustomerClass;

    PermissionsEvaluationServiceForSecman evaluator;
    ApplicationFeatureIdTransformer applicationFeatureIdTransformer;

    @BeforeEach
    void setup() {
        applicationFeatureIdTransformer = new ApplicationFeatureIdTransformerV1Compatibility(mockSpecificationLoader);

        lenient().when(mockSpecificationLoader.specForLogicalTypeName("customer.Customer")).thenReturn(Optional.of(mockSpecificationForCustomerClass));
        lenient().when(mockSpecificationForCustomerClass.getCorrespondingClass()).then(__ -> ApplicationFeatureIdTransformerV1Compatibility_Test.Customer.class);

        evaluator = PermissionsEvaluationServiceForSecman.builder()
                .applicationFeatureIdTransformer(applicationFeatureIdTransformer)
                .policy(ALLOW_BEATS_VETO)
                .build();
    }

    @Test
    void granted_viewing_via_namespace_viewing() {
        ApplicationPermissionValueSet.Evaluation evaluate = evaluator.evaluate(
                newMember("customer.Customer#lastName"),
                VIEWING,
                List.of(
                    new ApplicationPermissionValue(newNamespace("customer"), ALLOW, VIEWING)
                )
        );
        assertThat(evaluate.isGranted()).isTrue();
    }

    @Test
    void granted_viewing_via_namespace_changing() {
        ApplicationPermissionValueSet.Evaluation evaluate = evaluator.evaluate(
                newMember("customer.Customer#lastName"),
                VIEWING,
                List.of(
                    new ApplicationPermissionValue(newNamespace("customer"), ALLOW, CHANGING)
                )
        );
        assertThat(evaluate.isGranted()).isTrue();
    }

    @Test
    void no_opinion_changing_via_namespace_viewing() {
        ApplicationPermissionValueSet.Evaluation evaluate = evaluator.evaluate(
                newMember("customer.Customer#lastName"),
                CHANGING,
                List.of(
                    new ApplicationPermissionValue(newNamespace("customer"), ALLOW, VIEWING)
                )
        );
        assertThat(evaluate).isNull();
    }

    @Test
    void granted_changing_via_namespace_changing() {
        ApplicationPermissionValueSet.Evaluation evaluate = evaluator.evaluate(
                newMember("customer.Customer#lastName"),
                CHANGING,
                List.of(
                    new ApplicationPermissionValue(newNamespace("customer"), ALLOW, CHANGING)
                )
        );
        assertThat(evaluate.isGranted()).isTrue();
    }

    @Test
    void granted_viewing_via_type() {
        ApplicationPermissionValueSet.Evaluation evaluate = evaluator.evaluate(
                newMember("customer.Customer#lastName"),
                VIEWING,
                List.of(
                    new ApplicationPermissionValue(newType("customer.Customer"), ALLOW, VIEWING)
                )
        );
        assertThat(evaluate.isGranted()).isTrue();
    }

    @Test
    void veto_viewing_via_namespace_viewing() {
        ApplicationPermissionValueSet.Evaluation evaluate = evaluator.evaluate(
                newMember("customer.Customer#lastName"),
                VIEWING,
                List.of(
                        new ApplicationPermissionValue(newNamespace("customer"), VETO, VIEWING)
                )
        );
        assertThat(evaluate.isGranted()).isFalse();
    }

    @Test
    void granted_viewing_when_namespace_veto_viewing_overridden_by_type_allowing() {
        ApplicationPermissionValueSet.Evaluation evaluate = evaluator.evaluate(
                newMember("customer.Customer#lastName"),
                VIEWING,
                List.of(
                        new ApplicationPermissionValue(newNamespace("customer"), VETO, VIEWING),
                        new ApplicationPermissionValue(newNamespace("customer.Customer"), ALLOW, VIEWING)
                )
        );
        assertThat(evaluate.isGranted()).isFalse();
    }

}