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
package org.apache.causeway.testdomain.publishing.jpa;

import jakarta.inject.Inject;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.apache.causeway.testdomain.jpa.HasPersistenceStandardJpa;
import org.apache.causeway.testdomain.jpa.conf.Configuration_usingJpa;
import org.apache.causeway.testdomain.jpa.publishing.PublishingTestFactoryJpa;
import org.apache.causeway.testdomain.publishing.PublishingTestFactoryAbstract;
import org.apache.causeway.testdomain.publishing.conf.Configuration_usingEntityPropertyChangePublishing;
import org.apache.causeway.testdomain.publishing.stubs.PropertyPublishingTestAbstract;

@SpringBootTest(
        classes = {
                Configuration_usingJpa.class,
                Configuration_usingEntityPropertyChangePublishing.class,
                PublishingTestFactoryJpa.class,
                //XrayEnable.class
        },
        properties = {
                "logging.level.org.apache.causeway.applib.services.publishing.log.EntityPropertyChangeLogger=DEBUG",
                "logging.level.org.springframework.orm.jpa.*=DEBUG",
                "logging.level.org.apache.causeway.testdomain.util.rest.KVStoreForTesting=DEBUG",
                "causeway.core.runtime-services.entity-property-change-publisher.bulk.threshold=1000",
        })
@DirtiesContext
class JpaPropertySinglePublishingTest
extends PropertyPublishingTestAbstract
implements HasPersistenceStandardJpa {

    @Inject private PublishingTestFactoryJpa testFactory;

    @Override
    protected PublishingTestFactoryAbstract getTestFactory() {
        return testFactory;
    }

}
