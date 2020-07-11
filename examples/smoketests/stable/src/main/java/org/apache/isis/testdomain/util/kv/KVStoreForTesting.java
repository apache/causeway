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
package org.apache.isis.testdomain.util.kv;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Singleton;

import org.springframework.stereotype.Service;

import org.apache.isis.core.commons.concurrent.AwaitableLatch;
import org.apache.isis.core.commons.internal.collections._Maps;

import lombok.NonNull;
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
    
    public void put(Object caller, String keyStr, Object value) {
        val key = Key.of(caller.getClass(), keyStr);
        log.debug("writing {} -> {}", key, value);
        keyValueMap.put(key, value);
        val latch = latchMap.remove(caller.getClass());
        if(latch!=null) {
            latch.countDown();
        }
    }
    
    public Optional<Object> get(Class<?> callerType, String keyStr) {
        return Optional.ofNullable(keyValueMap.get(Key.of(callerType, keyStr)));
    }
    
    public Optional<Object> get(Object caller, String keyStr) {
        return get(caller.getClass(), keyStr);
    }

    // -- COUNTING
    
    public long incrementCounter(Class<?> callerType, String keyStr) {
        val key = Key.of(callerType, keyStr);
        return (long) keyValueMap.compute(key, (k, v) -> (v == null) ? 1L : 1L + (long)v);
    }
    
    public long getCounter(Class<?> callerType, String keyStr) {
        val key = Key.of(callerType, keyStr);
        return (long) keyValueMap.getOrDefault(key, 0L);
    }
    
    // --
    
    
    public void clear(Class<?> callerType) {
        log.debug("clearing {}", callerType);
        keyValueMap.entrySet()
        .removeIf(entry->entry.getKey().getCaller().equals(callerType));
    }

    public void clear(Object caller) {
        clear(caller.getClass());
    }
    
    public long countEntries(Class<?> callerType) {
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

    public AwaitableLatch latch(Class<?> callerType) {
        val latch = new CountDownLatch(1);
        latchMap.put(callerType, latch);
        return AwaitableLatch.of(latch);
    }

}
