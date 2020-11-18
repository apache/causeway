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

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.springframework.stereotype.Service;

import org.apache.isis.applib.services.iactn.Interaction.Execution;
import org.apache.isis.applib.services.publish.PublishedObjects;
import org.apache.isis.applib.services.publish.PublisherService;
import org.apache.isis.applib.util.schema.ChangesDtoUtils;
import org.apache.isis.applib.util.schema.MemberExecutionDtoUtils;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.testdomain.util.kv.KVStoreForTesting;

import lombok.val;
import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
public class PublisherServiceForTesting implements PublisherService {

    @Inject private KVStoreForTesting kvStore;

    @PostConstruct
    public void init() {
        log.info("about to initialize");
    }

    @Override
    public void publish(Execution<?, ?> execution) {

        @SuppressWarnings("unchecked")
        val publishedEntries = 
        (List<Execution<?, ?>>) kvStore.get(this, "publishedExecutions").orElseGet(ArrayList::new);

        publishedEntries.add(execution);

        kvStore.put(this, "publishedExecutions", publishedEntries);
        log.debug("publish execution {}", ()->MemberExecutionDtoUtils.toXml(execution.getDto()));
    }

    @Override
    public void publish(PublishedObjects publishedObjects) {

        @SuppressWarnings("unchecked")
        val publishedEntries = 
        (List<PublishedObjects>) kvStore.get(this, "publishedObjects").orElseGet(ArrayList::new);

        publishedEntries.add(publishedObjects);

        kvStore.put(this, "publishedObjects", publishedEntries);
        log.debug("publish objects {}", ()->ChangesDtoUtils.toXml(publishedObjects.getDto()));

    }

    // -- UTILITIES

    @SuppressWarnings("unchecked")
    public static Can<PublishedObjects> getPublishedObjects(KVStoreForTesting kvStore) {
        return Can.ofCollection(
                (List<PublishedObjects>) kvStore.get(PublisherServiceForTesting.class, "publishedObjects")
                .orElse(null));
    }

    @SuppressWarnings("unchecked")
    public static Can<Execution<?, ?>> getPublishedExecutions(KVStoreForTesting kvStore) {
        return Can.ofCollection(
                (List<Execution<?, ?>>) kvStore.get(PublisherServiceForTesting.class, "publishedExecutions")
                .orElse(null));
    }

    public static void clearPublishedEntries(KVStoreForTesting kvStore) {
        kvStore.clear(PublisherServiceForTesting.class);
    }

    public static int getCreated(KVStoreForTesting kvStore) {
        val publishedObjects = getPublishedObjects(kvStore);
        return publishedObjects.stream().mapToInt(PublishedObjects::getNumberCreated).sum();
    }

    public static int getDeleted(KVStoreForTesting kvStore) {
        val publishedObjects = getPublishedObjects(kvStore);
        return publishedObjects.stream().mapToInt(PublishedObjects::getNumberDeleted).sum();
    }

    public static int getLoaded(KVStoreForTesting kvStore) {
        val publishedObjects = getPublishedObjects(kvStore);
        return publishedObjects.stream().mapToInt(PublishedObjects::getNumberLoaded).sum();
    }

    public static int getUpdated(KVStoreForTesting kvStore) {
        val publishedObjects = getPublishedObjects(kvStore);
        return publishedObjects.stream().mapToInt(PublishedObjects::getNumberUpdated).sum();
    }

    public static int getModified(KVStoreForTesting kvStore) {
        val publishedObjects = getPublishedObjects(kvStore);
        return publishedObjects.stream().mapToInt(PublishedObjects::getNumberPropertiesModified).sum();
    }


}