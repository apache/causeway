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
package org.apache.isis.testdomain.util.rest;

import java.util.Map;
import java.util.Optional;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.springframework.stereotype.Service;

import org.apache.isis.core.commons.internal.collections._Maps;

import lombok.NonNull;
import lombok.Value;
import lombok.val;
import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
public class KVStoreForTesting {
    
    private Map<Key, Object> keyValueMap;
    
    @PostConstruct
    public void init() {
        log.info("about to initialize");
        keyValueMap = _Maps.newConcurrentHashMap();
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
    }
    
    public Optional<Object> get(Class<?> callerType, String keyStr) {
        return Optional.ofNullable(keyValueMap.get(Key.of(callerType, keyStr)));
    }
    
    public Optional<Object> get(Object caller, String keyStr) {
        return get(caller.getClass(), keyStr);
    }
    
    public void clear(Class<?> callerType) {
        log.debug("clearing {}", callerType);
        keyValueMap.entrySet()
        .removeIf(entry->entry.getKey().getCaller().equals(callerType));
    }

    public void clear(Object caller) {
        clear(caller.getClass());
    }
    
    @Value(staticConstructor = "of")
    private final static class Key {
        @NonNull Class<?> caller;
        @NonNull String keyStr;
    }
    
    
}
