/*
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
import java.util.Map;
import java.util.concurrent.Callable;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;

import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.services.WithTransactionScope;
import org.apache.isis.commons.internal.base._Casts;
import org.apache.isis.commons.internal.collections._Maps;

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
@DomainService(
        nature = NatureOfService.DOMAIN,
        menuOrder = "" + Integer.MAX_VALUE
        )
@RequestScoped @Log4j2
public class QueryResultsCacheInternal implements QueryResultsCache, WithTransactionScope {

    private final Map<Key, Value<?>> cache = _Maps.newHashMap();

    @Programmatic
    @Override
    public <T> T execute(
            final Callable<T> callable,
            final Class<?> callingClass,
            final String methodName,
            final Object... keys) {
        if(control.isFixturesInstalling()) {
            try {
                return callable.call();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        final Key cacheKey = new Key(callingClass, methodName, keys);
        return executeWithCaching(callable, cacheKey);
    }

    @Programmatic
    private <T> T execute(final Callable<T> callable, final Key cacheKey) {
        if(control.isFixturesInstalling()) {
            try {
                return callable.call();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return executeWithCaching(callable, cacheKey);
    }

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

    @Programmatic
    private <T> Value<T> get(final Class<?> callingClass, final String methodName, final Object... keys) {
        return get(new Key(callingClass, methodName, keys));
    }

    @Programmatic
    @SuppressWarnings("unchecked")
    private <T> Value<T> get(final Key cacheKey) {
        Value<T> value = (Value<T>) cache.get(cacheKey);
        logHitOrMiss(cacheKey, value);
        return value;
    }

    @Programmatic
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
    @Programmatic
    @Override
    public void resetForNextTransaction() {
        cache.clear();
    }


    @Inject
    protected QueryResultCacheControl control;

}