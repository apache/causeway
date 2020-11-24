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
package org.apache.isis.testdomain.applayer.publishing;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import javax.inject.Inject;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import org.apache.isis.applib.services.command.Command;
import org.apache.isis.applib.services.iactn.Interaction;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.core.config.presets.IsisPresets;
import org.apache.isis.testdomain.applayer.ApplicationLayerTestFactory;
import org.apache.isis.testdomain.applayer.ApplicationLayerTestFactory.VerificationStage;
import org.apache.isis.testdomain.applayer.publishing.conf.Configuration_usingCommandPublishing;
import org.apache.isis.testdomain.conf.Configuration_usingJdo;
import org.apache.isis.testdomain.util.CollectionAssertions;
import org.apache.isis.testdomain.util.kv.KVStoreForTesting;
import org.apache.isis.testing.integtestsupport.applib.IsisIntegrationTestAbstract;

import lombok.val;

@SpringBootTest(
        classes = {
                Configuration_usingJdo.class,
                Configuration_usingCommandPublishing.class,
                ApplicationLayerTestFactory.class
        }, 
        properties = {
                "logging.level.org.apache.isis.persistence.jdo.datanucleus5.persistence.IsisTransactionJdo=DEBUG",
                "logging.level.org.apache.isis.core.runtimeservices.session.IsisInteractionFactoryDefault=DEBUG",
                "logging.level.org.apache.isis.persistence.jdo.datanucleus5.datanucleus.service.JdoPersistenceLifecycleService=DEBUG"
        })
@TestPropertySource({
    IsisPresets.UseLog4j2Test
})
class CommandPublishingTest extends IsisIntegrationTestAbstract {

    @Inject private ApplicationLayerTestFactory testFactory;
    @Inject private KVStoreForTesting kvStore;

    @TestFactory @DisplayName("Application Layer")
    List<DynamicTest> generateTests() {
        return testFactory.generateTests(this::given, this::verify);
    }

    private void given() {
        CommandSubscriberForTesting.clearPublishedEntries(kvStore);
    }
    
    private void verify(VerificationStage verificationStage) {
        switch(verificationStage) {
        
        case FAILURE_CASE:
            assertHasCommandEntries(Can.empty());
            break;
        case POST_COMMIT:
        
            
            Interaction interaction = null;
            String propertyId = "org.apache.isis.testdomain.jdo.entities.JdoBook#name";
            Object target = null;
            Object argValue = "Book #2";
            String targetMemberName = "name???";
            String targetClass = "org.apache.isis.testdomain.jdo.entities.JdoBook";
            assertHasCommandEntries(Can.of(
                    new Command(UUID.randomUUID())
                    ));
            break;
        default:
            // ignore ... no checks
        }
    }
    
    // -- HELPER

    private void assertHasCommandEntries(Can<Command> expectedCommands) {
        val actualCommands = CommandSubscriberForTesting.getPublishedCommands(kvStore);
        CollectionAssertions.assertComponentWiseEquals(
                expectedCommands, actualCommands, this::commandDifference);
    }
    
    private String commandDifference(Command a, Command b) {
        if(!Objects.equals(a.getLogicalMemberIdentifier(), b.getLogicalMemberIdentifier())) {
            return String.format("differing member identifier %s != %s", 
                    a.getLogicalMemberIdentifier(), b.getLogicalMemberIdentifier());
        }
        if(!Objects.equals(a.getResult(), b.getResult())) {
            return String.format("differing results %s != %s", 
                    a.getResult(), b.getResult());
        }
        return null; // no difference
    }
    


}
