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
package org.apache.causeway.testdomain.util.event;

import javax.inject.Inject;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import org.apache.causeway.applib.events.lifecycle.AbstractLifecycleEvent;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.testdomain.jpa.entities.JpaBook;
import org.apache.causeway.testdomain.util.dto.BookDto;
import org.apache.causeway.testdomain.util.kv.KVStoreForTesting;

import lombok.RequiredArgsConstructor;
import lombok.val;
import lombok.extern.log4j.Log4j2;

@Service
@RequiredArgsConstructor(onConstructor_ = {@Inject})
@Log4j2
public class LifecycleEventSubscriberJpaForTesting {

    private final KVStoreForTesting kvStore;

    // -- JPA LIFECYCLE EVENTS

    @EventListener(JpaBook.CreatedLifecycleEvent.class)
    public void on(final JpaBook.CreatedLifecycleEvent ev) {
        storeJpaEvent(ev);
    }

    @EventListener(JpaBook.PersistingLifecycleEvent.class)
    public void on(final JpaBook.PersistingLifecycleEvent ev) {
        storeJpaEvent(ev);
    }

    @EventListener(JpaBook.PersistedLifecycleEvent.class)
    public void on(final JpaBook.PersistedLifecycleEvent ev) {
        storeJpaEvent(ev);
    }

    @EventListener(JpaBook.LoadedLifecycleEvent.class)
    public void on(final JpaBook.LoadedLifecycleEvent ev) {
        storeJpaEvent(ev);
    }

    @EventListener(JpaBook.UpdatingLifecycleEvent.class)
    public void on(final JpaBook.UpdatingLifecycleEvent ev) {
        storeJpaEvent(ev);
    }

    @EventListener(JpaBook.UpdatedLifecycleEvent.class)
    public void on(final JpaBook.UpdatedLifecycleEvent ev) {
        storeJpaEvent(ev);
    }

    @EventListener(JpaBook.RemovingLifecycleEvent.class)
    public void on(final JpaBook.RemovingLifecycleEvent ev) {
        storeJpaEvent(ev);
    }

    // -- UTILITY

    public static void clearPublishedEvents(final KVStoreForTesting kvStore) {
        kvStore.clear(LifecycleEventSubscriberJpaForTesting.class);
    }

    public static Can<BookDto> getPublishedEventsJpa(
            final KVStoreForTesting kvStore,
            final Class<? extends AbstractLifecycleEvent<JpaBook>> eventClass) {
        return kvStore.getAll(LifecycleEventSubscriberJpaForTesting.class, eventClass.getName())
                .map(BookDto.class::cast);
    }

    // -- HELPER

    private void storeJpaEvent(final AbstractLifecycleEvent<JpaBook> ev) {
        val eventType = ev.getClass().getName();

        log.debug("on {}", eventType);

        val bookDto = BookDto.from(ev.getSource());
        kvStore.append(this, eventType, bookDto);
    }


}
