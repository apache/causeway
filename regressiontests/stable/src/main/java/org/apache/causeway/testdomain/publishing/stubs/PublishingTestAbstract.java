/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package org.apache.causeway.testdomain.publishing.stubs;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import org.apache.causeway.testdomain.HasPersistenceStandard;
import org.apache.causeway.testdomain.publishing.PublishingTestFactoryAbstract;
import org.apache.causeway.testdomain.publishing.PublishingTestFactoryAbstract.ChangeScenario;
import org.apache.causeway.testdomain.publishing.PublishingTestFactoryAbstract.VerificationStage;

import lombok.val;

public abstract class PublishingTestAbstract
implements HasPersistenceStandard {

    protected abstract PublishingTestFactoryAbstract getTestFactory();

    protected abstract boolean supportsProgrammaticTesting(ChangeScenario changeScenario);

    @TestFactory @DisplayName("Entity Creation (FactoryService)")
    final List<DynamicTest> generateTestsForCreation() {
        return generateTests(ChangeScenario.ENTITY_CREATION);
    }

    @TestFactory @DisplayName("Entity Persisting")
    final List<DynamicTest> generateTestsForPersisting() {
        return generateTests(ChangeScenario.ENTITY_PERSISTING);
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
