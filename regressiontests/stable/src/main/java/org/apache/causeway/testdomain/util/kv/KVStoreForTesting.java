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
package org.apache.causeway.testdomain.util.kv;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Singleton;

import org.springframework.stereotype.Service;

import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.internal.collections._Maps;

import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.Value;
import lombok.val;
import lombok.extern.log4j.Log4j2;

@Service @Singleton
@Log4j2
public class KVStoreForTesting {

    private Map<Key, Object> keyValueMap;
    private Map<Class<?>, CountDownLatch> latchMap;

    @PostConstruct
    public void init() {
        log.info("about to initialize");
        keyValueMap = _Maps.newConcurrentHashMap();
        latchMap = _Maps.newConcurrentHashMap();
    }

    @PreDestroy
    public void shutdown() {
        log.info("about to shutdown");
        keyValueMap.clear();
    }

    public void put(final Object caller, final String keyStr, final Object value) {
        val key = Key.of(caller.getClass(), keyStr);
        log.debug("writing {} -> {}", key, value);
        keyValueMap.put(key, value);
        val latch = latchMap.remove(caller.getClass());
        if(latch!=null) {
            latch.countDown();
        }
    }

    public Optional<Object> get(final Class<?> callerType, final String keyStr) {
        return Optional.ofNullable(keyValueMap.get(Key.of(callerType, keyStr)));
    }

    public Optional<Object> get(final Object caller, final String keyStr) {
        return get(caller.getClass(), keyStr);
    }

    // -- NON-SCALAR SUPPORT

    public void append(final Object caller, final String keyStr, final Object value) {
        val canRef = get(caller, keyStr);
        if(! canRef.isPresent()) {
            put(caller, keyStr, Can.ofSingleton(value));
        } else {
            @SuppressWarnings("unchecked")
            val newCan = ((Can<Object>)canRef.get()).add(value);
            put(caller, keyStr, newCan);
        }
    }

    @SuppressWarnings("unchecked")
    public Can<Object> getAll(final Class<?> callerType, final String keyStr) {
        val canRef = get(callerType, keyStr);
        return ! canRef.isPresent()
            ? Can.empty()
            : ((Can<Object>)canRef.get());
    }

    public Can<Object> getAll(final Object caller, final String keyStr) {
        return getAll(caller.getClass(), keyStr);
    }


    // -- COUNTING

    public long incrementCounter(final Class<?> callerType, final String keyStr) {
        val key = Key.of(callerType, keyStr);
        return (long) keyValueMap.compute(key, (k, v) -> (v == null) ? 1L : 1L + (long)v);
    }

    public long getCounter(final Class<?> callerType, final String keyStr) {
        val key = Key.of(callerType, keyStr);
        return (long) keyValueMap.getOrDefault(key, 0L);
    }

    // --


    public void clear(final Class<?> callerType) {
        log.debug("clearing {}", callerType);
        keyValueMap.entrySet()
        .removeIf(entry->entry.getKey().getCaller().equals(callerType));
    }

    public void clear(final Object caller) {
        clear(caller.getClass());
    }

    public long countEntries(final Class<?> callerType) {
        return keyValueMap.entrySet()
        .stream()
        .filter(entry->entry.getKey().getCaller().equals(callerType))
        .count();
    }

    @Value(staticConstructor = "of")
    private final static class Key {
        @NonNull Class<?> caller;
        @NonNull String keyStr;
    }

    /** blocks until a new lock becomes available */
    @SneakyThrows
    public void requestLock(final Class<?> callerType) {
        synchronized(this) {
            val latch = latchMap.get(callerType);
            if(latch!=null) {
                log.warn("entering WAIT state until lock becomes available");
                latch.await();
                log.info("lock has become available");
            }
            val newLatch = new CountDownLatch(1);
            latchMap.put(callerType, newLatch);
        }
    }

    /** unblocks any threads waiting for a new lock */
    public void releaseLock(final Class<?> callerType) {
        synchronized(this) {
            val latch = latchMap.remove(callerType);
            if(latch!=null) {
                latch.countDown();
            }
        }
    }

}
