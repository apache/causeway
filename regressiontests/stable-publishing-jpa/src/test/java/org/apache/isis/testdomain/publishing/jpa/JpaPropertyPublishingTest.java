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

import javax.inject.Inject;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;

import org.apache.isis.core.config.presets.IsisPresets;
import org.apache.isis.testdomain.conf.Configuration_usingJpa;
import org.apache.isis.testdomain.jpa.HasPersistenceStandardJpa;
import org.apache.isis.testdomain.publishing.PublishingTestFactoryAbstract;
import org.apache.isis.testdomain.publishing.PublishingTestFactoryJpa;
import org.apache.isis.testdomain.publishing.conf.Configuration_usingEntityPropertyChangePublishing;
import org.apache.isis.testdomain.publishing.stubs.PropertyPublishingTestAbstract;

@SpringBootTest(
        classes = {
                Configuration_usingJpa.class,
                Configuration_usingEntityPropertyChangePublishing.class,
                PublishingTestFactoryJpa.class,
                //XrayEnable.class
        },
        properties = {
                "logging.level.org.apache.isis.applib.services.publishing.log.EntityPropertyChangeLogger=DEBUG",
                "logging.level.org.springframework.orm.jpa.*=DEBUG",
                "logging.level.org.apache.isis.testdomain.util.rest.KVStoreForTesting=DEBUG",
        })
@TestPropertySource({
    IsisPresets.UseLog4j2Test
})
@DirtiesContext
class JpaPropertyPublishingTest
extends PropertyPublishingTestAbstract
implements HasPersistenceStandardJpa {

    @Inject private PublishingTestFactoryJpa testFactory;

    @Override
    protected PublishingTestFactoryAbstract getTestFactory() {
        return testFactory;
    }

}
