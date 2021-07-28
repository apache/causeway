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
import org.junit.jupiter.api.TestFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import org.apache.isis.core.config.presets.IsisPresets;
import org.apache.isis.testdomain.conf.Configuration_usingJpa;
import org.apache.isis.testdomain.publishing.CommandPublishingTestAbstract;
import org.apache.isis.testdomain.publishing.PublishingTestFactoryJpa;
import org.apache.isis.testdomain.publishing.PublishingTestFactoryAbstract.ChangeScenario;
import org.apache.isis.testdomain.publishing.conf.Configuration_usingCommandPublishing;

@SpringBootTest(
        classes = {
                Configuration_usingJpa.class,
                Configuration_usingCommandPublishing.class,
                PublishingTestFactoryJpa.class,
                //XrayEnable.class
        },
        properties = {
                "logging.level.org.apache.isis.applib.services.publishing.log.CommandLogger=DEBUG",
                "logging.level.org.springframework.orm.jpa.*=DEBUG",
                "logging.level.org.apache.isis.testdomain.util.rest.KVStoreForTesting=DEBUG",
                "logging.level.org.apache.isis.core.runtimeservices.session.IsisInteractionFactoryDefault=DEBUG"
        })
@TestPropertySource({
    IsisPresets.UseLog4j2Test
})
class JpaCommandPublishingTest
extends CommandPublishingTestAbstract
implements HasPersistenceStandardJpa {

    @Inject private PublishingTestFactoryJpa testFactory;

    @TestFactory @DisplayName("Entity Creation")
    List<DynamicTest> generateTestsForCreation() {
        return testFactory.generateTests(
                ChangeScenario.ENTITY_CREATION, this::given, this::verify);
    }

    @TestFactory @DisplayName("Entity Removal")
    List<DynamicTest> generateTestsForRemoval() {
        return testFactory.generateTests(
                ChangeScenario.ENTITY_REMOVAL, this::given, this::verify);
    }

    @TestFactory @DisplayName("Entity Update")
    List<DynamicTest> generateTestsForUpdate() {
        return testFactory.generateTests(
                ChangeScenario.ENTITY_UPDATE, this::given, this::verify);
    }


}
