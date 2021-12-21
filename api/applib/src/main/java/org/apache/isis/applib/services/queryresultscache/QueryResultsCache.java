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

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import javax.annotation.Priority;
import javax.inject.Named;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import org.apache.isis.applib.annotation.InteractionScope;
import org.apache.isis.applib.annotation.PriorityPrecedence;
import org.apache.isis.commons.internal.base._Casts;
import org.apache.isis.commons.internal.base._NullSafe;
import org.apache.isis.commons.internal.collections._Maps;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;


/**
 * This service (API and implementation) provides a mechanism by which
 * idempotent query results can be cached for the duration of an {@link org.apache.isis.applib.services.iactn.Interaction}.
 *
 * <p>
 * Caching such values is useful to improve the response time (for the end user)
 * of code that loops &quot;naively&quot; through a set of items, performing
 * an expensive operation each time.  If the data is such that the same
 * expensive operation is made many times, then the query cache is a perfect fit.
 * </p>
 *
 * <p>
 * For example, code that makes a repository call many times within a loop
 * can be performance tuned using this service.  The benefit is that the
 * algorithm of the business logic can remain easy to understand.
 * </p>
 *
 * @since 1.x {@index}
 */
@Component
@Named("isis.applib.QueryResultsCache")
@Priority(PriorityPrecedence.EARLY)
@InteractionScope
@Qualifier("Default")
@Log4j2
public class QueryResultsCache implements DisposableBean {

    private final Map<Key, Value<?>> cache = _Maps.newHashMap();

    /**
     * Executes the callable if not already cached for the supplied calling
     * class, method and keys.
     *
     * @param callable
     * @param callingClass
     * @param methodName
     * @param keys
     * @param <T>
     */
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

    public <R> R execute(final MethodReferences.Call0<? extends R> action, final Class<?> callingClass, final String methodName) {
        if(isIgnoreCache()) {
            return action.call();
        }
        final Key cacheKey = new Key(callingClass, methodName);
        return executeWithCaching(action::call, cacheKey);
    }


    public <R, A0> R execute(final MethodReferences.Call1<? extends R, A0> action, final Class<?> callingClass, final String methodName, final A0 arg0) {
        if(isIgnoreCache()) {
            return action.call(arg0);
        }
        final Key cacheKey = new Key(callingClass, methodName, arg0);
        return executeWithCaching(()->action.call(arg0), cacheKey);
    }

    public <R, A0, A1> R execute(final MethodReferences.Call2<? extends R, A0, A1> action, final Class<?> callingClass, final String methodName, final A0 arg0,
                                 final A1 arg1) {
        if(isIgnoreCache()) {
            return action.call(arg0, arg1);
        }
        final Key cacheKey = new Key(callingClass, methodName, arg0, arg1);
        return executeWithCaching(()->action.call(arg0, arg1), cacheKey);
    }

    public <R, A0, A1, A2> R execute(final MethodReferences.Call3<? extends R, A0, A1, A2> action, final Class<?> callingClass, final String methodName,
                                     final A0 arg0, final A1 arg1, final A2 arg2) {
        if(isIgnoreCache()) {
            return action.call(arg0, arg1, arg2);
        }
        final Key cacheKey = new Key(callingClass, methodName, arg0, arg1, arg2);
        return executeWithCaching(()->action.call(arg0, arg1, arg2), cacheKey);
    }

    public <R, A0, A1, A2, A3> R execute(final MethodReferences.Call4<? extends R, A0, A1, A2, A3> action, final Class<?> callingClass,
                                         final String methodName, final A0 arg0, final A1 arg1, final A2 arg2, final A3 arg3) {
        if(isIgnoreCache()) {
            return action.call(arg0, arg1, arg2, arg3);
        }
        final Key cacheKey = new Key(callingClass, methodName, arg0, arg1, arg2, arg3);
        return executeWithCaching(()->action.call(arg0, arg1, arg2, arg3), cacheKey);
    }

    public <R, A0, A1, A2, A3, A4> R execute(final MethodReferences.Call5<? extends R, A0, A1, A2, A3, A4> action, final Class<?> callingClass,
                                             final String methodName, final A0 arg0, final A1 arg1, final A2 arg2, final A3 arg3, final A4 arg4) {
        if(isIgnoreCache()) {
            return action.call(arg0, arg1, arg2, arg3, arg4);
        }
        final Key cacheKey = new Key(callingClass, methodName, arg0, arg1, arg2, arg3, arg4);
        return executeWithCaching(()->action.call(arg0, arg1, arg2, arg3, arg4), cacheKey);
    }

    @Getter @EqualsAndHashCode
    public static class Key {

        @Getter private final Class<?> callingClass;
        @Getter private final String methodName;
        @Getter private final Object[] keys;

        // not using @RequiredArgsConstructor as we have used varargs here
        public Key(final Class<?> callingClass, final String methodName, final Object... keys) {
            this.callingClass = callingClass;
            this.methodName = methodName;
            this.keys = keys;
        }

        @Override
        public String toString() {
            return callingClass.getName() + "#" + methodName  + Arrays.toString(keys);
        }
    }

    @Data
    class Value<T> {
        private final T result;
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
    public void onTransactionEnded() {
        cache.clear();
    }


    @Override
    public void destroy() throws Exception {
        cache.clear();
    }

    // -- HELPER

    @Autowired(required = false)
    protected List<QueryResultsCacheControl> cacheControl;

    private boolean isIgnoreCache() {
        return _NullSafe.stream(cacheControl)
                .anyMatch(c->c.isIgnoreCache());
    }

}
