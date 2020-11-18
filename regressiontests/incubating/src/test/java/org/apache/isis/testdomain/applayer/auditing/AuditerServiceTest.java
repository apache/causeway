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
package org.apache.isis.testdomain.applayer.auditing;

import java.util.List;

import javax.inject.Inject;

import org.apache.isis.commons.collections.Can;
import org.apache.isis.core.config.presets.IsisPresets;
import org.apache.isis.testdomain.applayer.ApplicationLayerTestFactory;
import org.apache.isis.testdomain.conf.Configuration_usingJdo;
import org.apache.isis.testdomain.util.CollectionAssertions;
import org.apache.isis.testdomain.util.kv.KVStoreForTesting;
import org.apache.isis.testing.integtestsupport.applib.IsisIntegrationTestAbstract;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import lombok.val;

@SpringBootTest(
        classes = {
                Configuration_usingJdo.class,
                Configuration_usingAuditing.class,
                ApplicationLayerTestFactory.class
        }, 
        properties = {
                "logging.level.org.apache.isis.testdomain.util.rest.KVStoreForTesting=DEBUG"
        })
@TestPropertySource({
    IsisPresets.UseLog4j2Test
})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AuditerServiceTest extends IsisIntegrationTestAbstract {

    @Inject private ApplicationLayerTestFactory testFactory;
    @Inject private KVStoreForTesting kvStore;
    
    @TestFactory @DisplayName("Application Layer")
    List<DynamicTest> generateTests() {
    	return testFactory.generateTests(this::given, this::thenHappyCase, this::thenFailureCase);
    }

    private void given() {
    	AuditerServiceForTesting.clearAuditEntries(kvStore);
    }
    
    private void thenHappyCase() {
    	assertHasAuditEntries(Can.of(
              "Jdo Book/name: 'Sample Book' -> 'Book #2'"));
    }

    private void thenFailureCase() {
    	assertHasAuditEntries(Can.empty());
    }

    // -- HELPER

    private void assertHasAuditEntries(Can<String> expectedAuditEntries) {
        val actualAuditEntries = AuditerServiceForTesting.getAuditEntries(kvStore);
        CollectionAssertions.assertComponentWiseEquals(expectedAuditEntries, actualAuditEntries);
    }


}
