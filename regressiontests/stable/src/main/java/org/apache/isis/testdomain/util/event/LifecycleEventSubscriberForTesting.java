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
package org.apache.isis.testdomain.util.event;

import javax.inject.Inject;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import org.apache.isis.applib.events.lifecycle.AbstractLifecycleEvent;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.testdomain.jdo.entities.JdoBook;
import org.apache.isis.testdomain.jpa.entities.JpaBook;
import org.apache.isis.testdomain.util.dto.BookDto;
import org.apache.isis.testdomain.util.kv.KVStoreForTesting;

import lombok.RequiredArgsConstructor;
import lombok.val;
import lombok.extern.log4j.Log4j2;

@Service
@RequiredArgsConstructor(onConstructor_ = {@Inject})
@Log4j2
public class LifecycleEventSubscriberForTesting {

    private final KVStoreForTesting kvStore;

    // -- JDO LIFECYCLE EVENTS

    @EventListener(JdoBook.CreatedLifecycleEvent.class)
    public void on(final JdoBook.CreatedLifecycleEvent ev) {
        storeJdoEvent(ev);
    }

    @EventListener(JdoBook.PersistingLifecycleEvent.class)
    public void on(final JdoBook.PersistingLifecycleEvent ev) {
        storeJdoEvent(ev);
    }

    @EventListener(JdoBook.PersistedLifecycleEvent.class)
    public void on(final JdoBook.PersistedLifecycleEvent ev) {
        storeJdoEvent(ev);
    }

    @EventListener(JdoBook.LoadedLifecycleEvent.class)
    public void on(final JdoBook.LoadedLifecycleEvent ev) {
        storeJdoEvent(ev);
    }

    @EventListener(JdoBook.UpdatingLifecycleEvent.class)
    public void on(final JdoBook.UpdatingLifecycleEvent ev) {
        storeJdoEvent(ev);
    }

    @EventListener(JdoBook.UpdatedLifecycleEvent.class)
    public void on(final JdoBook.UpdatedLifecycleEvent ev) {
        storeJdoEvent(ev);
    }

    @EventListener(JdoBook.RemovingLifecycleEvent.class)
    public void on(final JdoBook.RemovingLifecycleEvent ev) {
        storeJdoEvent(ev);
    }

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
        kvStore.clear(LifecycleEventSubscriberForTesting.class);
    }

    public static Can<BookDto> getPublishedEventsJdo(
            final KVStoreForTesting kvStore,
            final Class<? extends AbstractLifecycleEvent<JdoBook>> eventClass) {
        return kvStore.getAll(LifecycleEventSubscriberForTesting.class, eventClass.getName())
                .map(BookDto.class::cast);
    }

    public static Can<BookDto> getPublishedEventsJpa(
            final KVStoreForTesting kvStore,
            final Class<? extends AbstractLifecycleEvent<JpaBook>> eventClass) {
        return kvStore.getAll(LifecycleEventSubscriberForTesting.class, eventClass.getName())
                .map(BookDto.class::cast);
    }

    // -- HELPER

    private void storeJdoEvent(final AbstractLifecycleEvent<JdoBook> ev) {
        val eventType = ev.getClass().getName();

        log.info("on {}", eventType);

        val bookDto = BookDto.from(ev.getSource());
        kvStore.append(LifecycleEventSubscriberForTesting.class, eventType, bookDto);
        kvStore.incrementCounter(LifecycleEventSubscriberForTesting.class, eventType);
    }

    private void storeJpaEvent(final AbstractLifecycleEvent<JpaBook> ev) {
        val eventType = ev.getClass().getName();

        log.info("on {}", eventType);

        val bookDto = BookDto.from(ev.getSource());
        kvStore.append(LifecycleEventSubscriberForTesting.class, eventType, bookDto);
        kvStore.incrementCounter(LifecycleEventSubscriberForTesting.class, eventType);
    }


}
