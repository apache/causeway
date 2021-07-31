package org.apache.isis.testdomain.publishing;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import org.apache.isis.testdomain.publishing.PublishingTestFactoryAbstract.ChangeScenario;
import org.apache.isis.testdomain.publishing.PublishingTestFactoryAbstract.VerificationStage;

import lombok.val;

public abstract class PublishingTestAbstract
implements HasPersistenceStandard {

    protected abstract PublishingTestFactoryAbstract getTestFactory();

    protected abstract boolean supportsProgrammaticTesting(ChangeScenario changeScenario);

    @TestFactory @DisplayName("Entity Creation (FactoryService)")
    final List<DynamicTest> generateTestsForCreation() {
        return generateTests(ChangeScenario.ENTITY_CREATION);
    }

    @TestFactory @DisplayName("Entity Loading")
    final List<DynamicTest> generateTestsForLoading() {
        return generateTests(ChangeScenario.ENTITY_LOADING);
    }

    @TestFactory @DisplayName("Entity Removal")
    final List<DynamicTest> generateTestsForRemoval() {
        return generateTests(ChangeScenario.ENTITY_REMOVAL);
    }

    @TestFactory @DisplayName("Property Update")
    final List<DynamicTest> generateTestsForUpdate() {
        return generateTests(ChangeScenario.PROPERTY_UPDATE);
    }

    @TestFactory @DisplayName("Action Execution")
    final List<DynamicTest> generateTestsForAction() {
        return generateTests(ChangeScenario.ACTION_INVOCATION);
    }

    protected abstract void given();

    protected abstract void verify(
            ChangeScenario changeScenario,
            VerificationStage verificationStage);

    // -- HELPER

    private final List<DynamicTest> generateTests(final ChangeScenario scenario) {
        val testFactory = getTestFactory();
        return testFactory!=null
                ? testFactory.generateTests(
                        scenario, supportsProgrammaticTesting(scenario), this::given, this::verify)
                : Collections.emptyList();
    }

}
