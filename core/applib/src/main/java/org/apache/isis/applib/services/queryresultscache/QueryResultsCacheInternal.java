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
package org.apache.isis.applib.services.queryresultscache;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import javax.enterprise.context.RequestScoped;
import javax.inject.Named;

import org.apache.isis.applib.annotation.OrderPrecedence;
import org.springframework.beans.factory.annotation.Autowired;

import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.services.WithTransactionScope;
import org.apache.isis.commons.internal.base._Casts;
import org.apache.isis.commons.internal.base._NullSafe;
import org.apache.isis.commons.internal.collections._Maps;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import lombok.extern.log4j.Log4j2;

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
@Service
@Named("isisApplib.QueryResultsCacheInternal")
@Order(OrderPrecedence.HIGH)
@Primary
@RequestScoped
@Log4j2
public class QueryResultsCacheInternal implements QueryResultsCache, WithTransactionScope {

    private final Map<Key, Value<?>> cache = _Maps.newHashMap();

    @Override
    public <T> T execute(
            final Callable<T> callable,
            final Class<?> callingClass,
            final String methodName,
            final Object... keys) {
        if(isIgnoreCache()) {
            try {
                return callable.call();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        final Key cacheKey = new Key(callingClass, methodName, keys);
        return executeWithCaching(callable, cacheKey);
    }

//XXX not used    
//    private <T> T execute(final Callable<T> callable, final Key cacheKey) {
//        if(isIgnoreCache()) {
//            try {
//                return callable.call();
//            } catch (Exception e) {
//                throw new RuntimeException(e);
//            }
//        }
//        return executeWithCaching(callable, cacheKey);
//    }

    private <T> T executeWithCaching(final Callable<T> callable, final Key cacheKey) {
        try {
            final Value<?> cacheValue = cache.get(cacheKey);
            logHitOrMiss(cacheKey, cacheValue);
            if(cacheValue != null) {
                return _Casts.uncheckedCast(cacheValue.getResult());
            }

            // cache miss, so get the result...
            T result = callable.call();

            // ... and cache
            //
            // (it is possible that the callable just invoked might also have updated the cache, eg if there was
            // some sort of recursion.  However, Map#put(...) is idempotent, so valid to call more than once.
            //
            // note: there's no need for thread-safety synchronization... remember that QueryResultsCache is @RequestScoped
            put(cacheKey, result);

            return result;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

//XXX not used    
//    private <T> Value<T> get(final Class<?> callingClass, final String methodName, final Object... keys) {
//        return get(new Key(callingClass, methodName, keys));
//    }
//
//    @SuppressWarnings("unchecked")
//    private <T> Value<T> get(final Key cacheKey) {
//        Value<T> value = (Value<T>) cache.get(cacheKey);
//        logHitOrMiss(cacheKey, value);
//        return value;
//    }

    private <T> void put(final Key cacheKey, final T result) {
        log.debug("PUT: {}", cacheKey);
        cache.put(cacheKey, new Value<T>(result));
    }

    private static void logHitOrMiss(final Key cacheKey, final Value<?> cacheValue) {
        if(!log.isDebugEnabled()) {
            return;
        }
        log.debug("{}: {}", (cacheValue != null ? "HIT" : "MISS"), cacheKey.toString());
    }

    /**
     * Not API: for framework to call at end of transaction, to clear out the cache.
     *
     * <p>
     * (This service really ought to be considered
     * a transaction-scoped service; since that isn't yet supported by the framework, we have to manually reset).
     * </p>
     */
    @Override
    public void resetForNextTransaction() {
        cache.clear();
    }

    // -- HELPER

    @Autowired(required = false)
    protected List<QueryResultCacheControl> cacheControl;

    private boolean isIgnoreCache() {
        return _NullSafe.stream(cacheControl)
                .anyMatch(c->c.isIgnoreCache());
    }

}