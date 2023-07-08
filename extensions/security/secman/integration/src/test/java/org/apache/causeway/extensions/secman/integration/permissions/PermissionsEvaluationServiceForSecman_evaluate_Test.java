package org.apache.causeway.extensions.secman.integration.permissions;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

import org.apache.causeway.extensions.secman.applib.permission.dom.ApplicationPermissionValue;
import org.apache.causeway.extensions.secman.applib.permission.dom.ApplicationPermissionValueSet;

import static org.apache.causeway.applib.services.appfeat.ApplicationFeatureId.newMember;
import static org.apache.causeway.applib.services.appfeat.ApplicationFeatureId.newNamespace;
import static org.apache.causeway.applib.services.appfeat.ApplicationFeatureId.newType;
import static org.apache.causeway.core.config.CausewayConfiguration.Extensions.Secman.PermissionsEvaluationPolicy.ALLOW_BEATS_VETO;
import static org.apache.causeway.extensions.secman.applib.permission.dom.ApplicationPermissionMode.CHANGING;
import static org.apache.causeway.extensions.secman.applib.permission.dom.ApplicationPermissionMode.VIEWING;
import static org.apache.causeway.extensions.secman.applib.permission.dom.ApplicationPermissionRule.ALLOW;
import static org.apache.causeway.extensions.secman.applib.permission.dom.ApplicationPermissionRule.VETO;

@ExtendWith(MockitoExtension.class)
class PermissionsEvaluationServiceForSecman_evaluate_Test {

    PermissionsEvaluationServiceForSecman evaluator;
    ApplicationFeatureIdTransformer applicationFeatureIdTransformer;

    @BeforeEach
    void setup() {
        applicationFeatureIdTransformer = new ApplicationFeatureIdTransformerIdentity();

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