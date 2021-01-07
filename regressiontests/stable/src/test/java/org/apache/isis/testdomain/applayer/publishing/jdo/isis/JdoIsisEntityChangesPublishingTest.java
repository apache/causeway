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

import static org.apache.isis.testdomain.applayer.publishing.EntityChangesSubscriberForTesting.clearPublishedEntries;
import static org.apache.isis.testdomain.applayer.publishing.EntityChangesSubscriberForTesting.getCreated;
import static org.apache.isis.testdomain.applayer.publishing.EntityChangesSubscriberForTesting.getDeleted;
import static org.apache.isis.testdomain.applayer.publishing.EntityChangesSubscriberForTesting.getLoaded;
import static org.apache.isis.testdomain.applayer.publishing.EntityChangesSubscriberForTesting.getModified;
import static org.apache.isis.testdomain.applayer.publishing.EntityChangesSubscriberForTesting.getUpdated;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.Timeout;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import org.apache.isis.core.config.presets.IsisPresets;
import org.apache.isis.testdomain.applayer.ApplicationLayerTestFactory;
import org.apache.isis.testdomain.applayer.ApplicationLayerTestFactory.VerificationStage;
import org.apache.isis.testdomain.applayer.publishing.conf.Configuration_usingEntityChangesPublishing;
import org.apache.isis.testdomain.conf.Configuration_usingJdoIsis;
import org.apache.isis.testdomain.util.kv.KVStoreForTesting;
import org.apache.isis.testing.integtestsupport.applib.IsisIntegrationTestAbstract;

@SpringBootTest(
        classes = {
                Configuration_usingJdoIsis.class,
                Configuration_usingEntityChangesPublishing.class,
                ApplicationLayerTestFactory.class
        }, 
        properties = {
                "logging.level.org.apache.isis.persistence.jdo.datanucleus5.persistence.IsisTransactionJdo=DEBUG",
                "logging.level.org.apache.isis.core.runtimeservices.session.IsisInteractionFactoryDefault=DEBUG",
                "logging.level.org.apache.isis.persistence.jdo.integration.changetracking.JdoLifecycleListener=DEBUG",
        })
@TestPropertySource({
    IsisPresets.UseLog4j2Test
})
class JdoIsisEntityChangesPublishingTest extends IsisIntegrationTestAbstract {

    @Inject private ApplicationLayerTestFactory testFactory;
    @Inject private KVStoreForTesting kvStore;

    @DisplayName("Application Layer")
    @TestFactory
    @Timeout(value = 1, unit = TimeUnit.DAYS)
    List<DynamicTest> generateTests() {
        return testFactory.generateTests(this::given, this::verify);
    }

    private void given() {
        clearPublishedEntries(kvStore);
    }
    
    private void verify(VerificationStage verificationStage) {
        switch(verificationStage) {
        case PRE_COMMIT:
        case FAILURE_CASE:
            assertEquals(0, getCreated(kvStore));
            assertEquals(0, getDeleted(kvStore));
            assertEquals(0, getLoaded(kvStore));
            assertEquals(0, getUpdated(kvStore));
            assertEquals(0, getModified(kvStore));
            break;
        case POST_COMMIT_WHEN_PROGRAMMATIC:
        case POST_COMMIT:
            assertEquals(0, getCreated(kvStore));
            assertEquals(0, getDeleted(kvStore));
            //assertEquals(1, getLoaded()); // not reproducible
            assertEquals(1, getUpdated(kvStore));
            assertEquals(1, getModified(kvStore));
            break;
        default:
            // ignore ... no checks
        }
    }

}
