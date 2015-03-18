/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.isis.applib.services.queryresultscache;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.Callable;
import javax.enterprise.context.RequestScoped;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.annotation.Programmatic;

/**
 * This service (API and implementation) provides a mechanism by which idempotent query results can be cached for the duration of an interaction.
 * Most commonly this allows otherwise &quot;naive&quot; - eg that makes a repository call many times within a loop - to
 * be performance tuned.  The benefit is that the algorithm of the business logic can remain easy to understand.
 *
 * <p>
 * This implementation has no UI and there is only one implementation (this class) in applib, it is annotated with
 * {@link org.apache.isis.applib.annotation.DomainService}.  This means that it is automatically registered and
 * available for use; no further configuration is required.
 */
@DomainService(nature = NatureOfService.DOMAIN)
@RequestScoped
public class QueryResultsCache {

    private static final Logger LOG = LoggerFactory.getLogger(QueryResultsCache.class);

    public static class Key {
        private final Class<?> callingClass;
        private final String methodName;
        private final Object[] keys;
        
        public Key(Class<?> callingClass, String methodName, Object... keys) {
            this.callingClass = callingClass;
            this.methodName = methodName;
            this.keys = keys;
        }
        
        public Class<?> getCallingClass() {
            return callingClass;
        }
        public String getMethodName() {
            return methodName;
        }
        public Object[] getKeys() {
            return keys;
        }
        
        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            Key other = (Key) obj;
            if (callingClass == null) {
                if (other.callingClass != null)
                    return false;
            } else if (!callingClass.equals(other.callingClass))
                return false;
            if (!Arrays.equals(keys, other.keys))
                return false;
            if (methodName == null) {
                if (other.methodName != null)
                    return false;
            } else if (!methodName.equals(other.methodName))
                return false;
            return true;
        }
        
        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((callingClass == null) ? 0 : callingClass.hashCode());
            result = prime * result + Arrays.hashCode(keys);
            result = prime * result + ((methodName == null) ? 0 : methodName.hashCode());
            return result;
        }

        @Override
        public String toString() {
            return callingClass.getName() + "#" + methodName  + Arrays.toString(keys);
        }
    }
    
    public static class Value<T> {
        private T result;
        public Value(T result) {
            this.result = result;
        }
        public T getResult() {
            return result;
        }
    }
    
    // //////////////////////////////////////

    
    private final Map<Key, Value<?>> cache = Maps.newHashMap();

    @Programmatic
    public <T> T execute(final Callable<T> callable, final Class<?> callingClass, final String methodName, final Object... keys) {
        final Key cacheKey = new Key(callingClass, methodName, keys);
        return execute(callable, cacheKey);
    }

    @Programmatic
    @SuppressWarnings("unchecked")
    public <T> T execute(final Callable<T> callable, final Key cacheKey) {
        try {
            final Value<?> cacheValue = cache.get(cacheKey);
            logHitOrMiss(cacheKey, cacheValue);
            if(cacheValue != null) { 
                return (T) cacheValue.getResult();
            }
            // cache miss, so get the result, and cache
            T result = callable.call();
            put(cacheKey, result);
            return result;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Programmatic
    public <T> Value<T> get(final Class<?> callingClass, final String methodName, final Object... keys) {
        return get(new Key(callingClass, methodName, keys));
    }
    
    @Programmatic
    @SuppressWarnings("unchecked")
    public <T> Value<T> get(final Key cacheKey) {
        Value<T> value = (Value<T>) cache.get(cacheKey);
        logHitOrMiss(cacheKey, value);
        return value;
    }

    @Programmatic
    public <T> void put(final Key cacheKey, final T result) {
        if(LOG.isDebugEnabled()) {
            LOG.debug("PUT: " + cacheKey);
        }
        cache.put(cacheKey, new Value<T>(result));
    }

    private static void logHitOrMiss(final Key cacheKey, final Value<?> cacheValue) {
        if(!LOG.isDebugEnabled()) { 
            return; 
        } 
        String hitOrMiss = cacheValue != null ? "HIT" : "MISS";
        LOG.debug( hitOrMiss + ": " + cacheKey.toString());
    }
}
