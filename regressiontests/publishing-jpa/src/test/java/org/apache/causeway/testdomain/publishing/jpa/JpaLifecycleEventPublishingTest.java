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
import org.springframework.test.context.TestPropertySource;

import org.apache.causeway.applib.events.lifecycle.AbstractLifecycleEvent;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.core.config.presets.CausewayPresets;
import org.apache.causeway.testdomain.jpa.HasPersistenceStandardJpa;
import org.apache.causeway.testdomain.jpa.conf.Configuration_usingJpa;
import org.apache.causeway.testdomain.jpa.event.LifecycleEventSubscriberJpaForTesting;
import org.apache.causeway.testdomain.jpa.publishing.PublishingTestFactoryJpa;
import org.apache.causeway.testdomain.publishing.PublishingTestFactoryAbstract;
import org.apache.causeway.testdomain.publishing.stubs.LifecycleEventPublishingTestAbstract;
import org.apache.causeway.testdomain.util.dto.BookDto;
import org.apache.causeway.testdomain.util.dto.IBook;
import org.apache.causeway.testdomain.util.kv.KVStoreForTesting;

@SpringBootTest(
        classes = {
                Configuration_usingJpa.class,
                LifecycleEventSubscriberJpaForTesting.class,
                PublishingTestFactoryJpa.class,
                //XrayEnable.class
        },
        properties = {
                "logging.level.org.apache.causeway.testdomain.util.event.LifecycleEventSubscriberForTesting=DEBUG",
                //"logging.level.org.springframework.orm.jpa.*=DEBUG",
                "logging.level.org.apache.causeway.testdomain.util.kv.KVStoreForTesting=DEBUG",
        })
@TestPropertySource({
    CausewayPresets.UseLog4j2Test
})
@DirtiesContext
class JpaLifecycleEventPublishingTest
extends LifecycleEventPublishingTestAbstract
implements HasPersistenceStandardJpa {

    @Inject private PublishingTestFactoryJpa testFactory;

    @Override
    protected PublishingTestFactoryAbstract getTestFactory() {
        return testFactory;
    }

    @Override
    protected final void clearPublishedEvents(KVStoreForTesting kvStore) {
        LifecycleEventSubscriberJpaForTesting.clearPublishedEvents(kvStore);
    }

    @Override
    protected Can<BookDto> getPublishedEvents(KVStoreForTesting kvStore,
            Class<? extends AbstractLifecycleEvent<? extends IBook>> eventClass) {
        return LifecycleEventSubscriberJpaForTesting.getPublishedEventsJpa(kvStore, eventClass);
    }

}
