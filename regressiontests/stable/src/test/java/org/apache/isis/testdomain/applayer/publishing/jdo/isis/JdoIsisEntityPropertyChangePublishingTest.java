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
package org.apache.isis.testdomain.applayer.publishing.jdo.isis;

import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.Timeout;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import org.apache.isis.commons.collections.Can;
import org.apache.isis.core.config.presets.IsisPresets;
import org.apache.isis.testdomain.applayer.ApplicationLayerTestFactory;
import org.apache.isis.testdomain.applayer.ApplicationLayerTestFactory.VerificationStage;
import org.apache.isis.testdomain.applayer.publishing.EntityPropertyChangeSubscriberForTesting;
import org.apache.isis.testdomain.applayer.publishing.conf.Configuration_usingEntityPropertyChangePublishing;
import org.apache.isis.testdomain.conf.Configuration_usingJdoIsis;
import org.apache.isis.testdomain.util.CollectionAssertions;
import org.apache.isis.testdomain.util.kv.KVStoreForTesting;
import org.apache.isis.testing.integtestsupport.applib.IsisIntegrationTestAbstract;

import lombok.val;

@SpringBootTest(
        classes = {
                Configuration_usingJdoIsis.class,
                Configuration_usingEntityPropertyChangePublishing.class,
                ApplicationLayerTestFactory.class
        }, 
        properties = {
                "logging.level.org.apache.isis.applib.services.publishing.log.*=DEBUG",
                "logging.level.org.apache.isis.testdomain.util.rest.KVStoreForTesting=DEBUG",
                "logging.level.org.apache.isis.persistence.jdo.integration.changetracking.JdoLifecycleListener=DEBUG",
        })
@TestPropertySource({
    IsisPresets.UseLog4j2Test
})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class JdoIsisEntityPropertyChangePublishingTest extends IsisIntegrationTestAbstract {

    @Inject private ApplicationLayerTestFactory testFactory;
    @Inject private KVStoreForTesting kvStore;

    @DisplayName("Application Layer")
    @TestFactory
    @Timeout(value = 1, unit = TimeUnit.DAYS)
    List<DynamicTest> generateTests() {
        return testFactory.generateTests(this::given, this::verify);
    }

    private void given() {
        EntityPropertyChangeSubscriberForTesting.clearPropertyChangeEntries(kvStore);
    }

    private void verify(VerificationStage verificationStage) {
        switch(verificationStage) {
        case PRE_COMMIT:
        case FAILURE_CASE:
            assertHasPropertyChangeEntries(Can.empty());
            break;
        case POST_COMMIT_WHEN_PROGRAMMATIC:
        case POST_COMMIT:
            assertHasPropertyChangeEntries(Can.of(
                    "Jdo Book/name: 'Sample Book' -> 'Book #2'"));
            break;
        default:
            // ignore ... no checks
        }
    }

    // -- HELPER

    private void assertHasPropertyChangeEntries(Can<String> expectedAuditEntries) {
        val actualAuditEntries = EntityPropertyChangeSubscriberForTesting.getPropertyChangeEntries(kvStore);
        CollectionAssertions.assertComponentWiseEquals(expectedAuditEntries, actualAuditEntries);
    }


}
