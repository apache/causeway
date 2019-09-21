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
package org.apache.isis.commons.internal.reflection;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.isis.commons.internal.context._Context;

import static org.apache.isis.commons.internal.collections._Arrays.emptyToNull;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.val;

/**
 * <h1>- internal use only -</h1>
 * <p>
 * <b>WARNING</b>: Do <b>NOT</b> use any of the classes provided by this package! <br/>
 * These may be changed or removed without notice!
 * </p>
 * <p>
 * Motivation: JDK reflection API has no Class.getMethod(name, ...) variant that does not produce an expensive 
 * stack-trace, when no such method exists. 
 * </p> 
 * @apiNote 
 * thread-save, implements AutoCloseable so we can put it on the _Context, which then automatically 
 * takes care of the lifecycle
 * @since 2.0
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class _MethodCache implements AutoCloseable {
    
    public static _MethodCache getInstance() {
        return _Context.computeIfAbsent(_MethodCache.class, _MethodCache::new);
    }

    /**
     * A drop-in replacement for {@link Class#getMethod(String, Class...)} that only looks up 
     * public methods and does not throw {@link NoSuchMethodException}s.    
     */
    public Method lookupMethod(Class<?> type, String name, Class<?>[] paramTypes) {
        
        synchronized(inspectedTypes) {
            if(!inspectedTypes.contains(type)) {
                for(val method : type.getMethods()) {
                    methodsByKey.put(Key.of(type, method), method);
                }
                inspectedTypes.add(type);
            }
        }
        
        return methodsByKey.get(Key.of(type, name, emptyToNull(paramTypes)));
    }

    public int size() {
        return methodsByKey.size();
    }

    // -- IMPLEMENATION DETAILS
    
    private Map<Key, Method> methodsByKey = new HashMap<>();
    private Set<Class<?>> inspectedTypes = new HashSet<>();
    
    @AllArgsConstructor(staticName = "of") @EqualsAndHashCode
    private final static class Key {
        private final Class<?> type;
        private final String name;
        private final Class<?>[] paramTypes;
        
        public static Key of(Class<?> type, Method method) {
            return Key.of(type, method.getName(), emptyToNull(method.getParameterTypes()));
        }
        
    }

    @Override
    public void close() throws Exception {
        synchronized(inspectedTypes) {
            inspectedTypes.clear();
            methodsByKey.clear();
        }
    }
    
    
}
