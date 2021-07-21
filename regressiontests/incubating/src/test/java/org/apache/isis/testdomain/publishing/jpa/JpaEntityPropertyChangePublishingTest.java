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
package org.apache.isis.testdomain.publishing.jpa;

import java.util.List;

import javax.inject.Inject;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.internal.debug.xray.XrayEnable;
import org.apache.isis.core.config.presets.IsisPresets;
import org.apache.isis.testdomain.conf.Configuration_usingJpa;
import org.apache.isis.testdomain.publishing.ApplicationLayerTestFactoryAbstract.VerificationStage;
import org.apache.isis.testdomain.publishing.ApplicationLayerTestFactoryJpa;
import org.apache.isis.testdomain.publishing.conf.Configuration_usingEntityPropertyChangePublishing;
import org.apache.isis.testdomain.publishing.subscriber.EntityPropertyChangeSubscriberForTesting;
import org.apache.isis.testdomain.util.CollectionAssertions;
import org.apache.isis.testdomain.util.kv.KVStoreForTesting;

import lombok.val;

@SpringBootTest(
        classes = {
                Configuration_usingJpa.class,
                Configuration_usingEntityPropertyChangePublishing.class,
                ApplicationLayerTestFactoryJpa.class,
                XrayEnable.class
        },
        properties = {
                "logging.level.org.apache.isis.applib.services.publishing.log.*=DEBUG",
                "logging.level.org.apache.isis.testdomain.util.rest.KVStoreForTesting=DEBUG",
                "logging.level.org.apache.isis.core.transaction.changetracking.EntityChangeTrackerDefault=DEBUG",
        })
@TestPropertySource({
    IsisPresets.UseLog4j2Test
})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class JpaEntityPropertyChangePublishingTest {

    @Inject private ApplicationLayerTestFactoryJpa testFactory;
    @Inject private KVStoreForTesting kvStore;

    @DisplayName("Application Layer")
    @TestFactory
    List<DynamicTest> generateTests() {
        return testFactory.generateTests(this::given, this::verify);
    }

    private void given() {
        EntityPropertyChangeSubscriberForTesting.clearPropertyChangeEntries(kvStore);
    }

    private void verify(final VerificationStage verificationStage) {
        switch(verificationStage) {
        case PRE_COMMIT:
        case FAILURE_CASE:
            assertHasPropertyChangeEntries(Can.empty());
            break;
        case POST_COMMIT_WHEN_PROGRAMMATIC:
        case POST_COMMIT:
            assertHasPropertyChangeEntries(Can.of(
                    "Jpa Book/name: 'Sample Book' -> 'Book #2'"));
            break;
        default:
            // ignore ... no checks
        }
    }

    // -- HELPER

    private void assertHasPropertyChangeEntries(final Can<String> expectedAuditEntries) {
        val actualAuditEntries = EntityPropertyChangeSubscriberForTesting.getPropertyChangeEntries(kvStore);
        CollectionAssertions.assertComponentWiseEquals(expectedAuditEntries, actualAuditEntries);
    }


}
