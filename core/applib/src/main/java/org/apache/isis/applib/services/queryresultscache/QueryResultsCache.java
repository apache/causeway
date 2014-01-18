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

import org.apache.isis.applib.services.queryresultscache.QueryResultsCache.CacheKey;
import org.apache.isis.applib.services.queryresultscache.QueryResultsCache.CacheValue;

@RequestScoped
public class QueryResultsCache {

    static class CacheKey {
        private final Class<?> callingClass;
        private final String methodName;
        private final Object[] keys;
        
        public CacheKey(Class<?> callingClass, String methodName, Object... keys) {
            this.callingClass = callingClass;
            this.methodName = methodName;
            this.keys = keys;
        }
        
        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            CacheKey other = (CacheKey) obj;
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
    }
    
    public static class CacheValue {
        public CacheValue(Object object) {
            this.object = object;
        }
        Object object;
    }
    
    private final Map<CacheKey, CacheValue> cache = Maps.newHashMap();

    @SuppressWarnings("unchecked")
    public <T> T execute(Callable<T> callable, Class<?> callingClass, String methodName, Object... keys) {
        try {
            final CacheKey ck = new CacheKey(callingClass, methodName, keys);
            final CacheValue cv = cache.get(ck);
            if(cv != null) { 
                return (T) cv.object;
            }
            // cache miss, so get the result, and cache
            T result = callable.call();
            cache.put(ck, new CacheValue(result));
            return result;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
