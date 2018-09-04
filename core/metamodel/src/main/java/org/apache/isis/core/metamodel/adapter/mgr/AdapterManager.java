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

package org.apache.isis.core.metamodel.adapter.mgr;

import java.util.concurrent.Callable;

import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.annotation.SemanticsOf;

public interface AdapterManager /*extends ObjectAdapterProvider*/ {


    enum ConcurrencyChecking {
        NO_CHECK,
        CHECK;

        @Programmatic
        public boolean isChecking() {
            return this == CHECK;
        }

        public static ConcurrencyChecking concurrencyCheckingFor(SemanticsOf actionSemantics) {
            return actionSemantics.isSafeInNature()
                    ? ConcurrencyChecking.NO_CHECK
                            : ConcurrencyChecking.CHECK;
        }

        /**
         * Provides a mechanism to temporarily disable concurrency checking.
         *
         * <p>
         * A {@link ThreadLocal} is used because typically there is JDO/DataNucleus code between the Isis code
         * that wishes to disable the concurrency checking and the code (an Isis callback) that needs to
         * check if checking has been disabled.
         */
        private static ThreadLocal<ConcurrencyChecking> concurrencyChecking = new ThreadLocal<ConcurrencyChecking>(){
            @Override
            protected ConcurrencyChecking initialValue() {
                return CHECK;
            };
        };

        /**
         * Whether concurrency checking is currently enabled or disabled.
         */
        public static boolean isCurrentlyEnabled() {
            return concurrencyChecking.get().isChecking();
        }

        /**
         * Allows a caller to temporarily disable concurrency checking for the current thread.
         */
        public static <T> T executeWithConcurrencyCheckingDisabled(final Callable<T> callable) {
            ConcurrencyChecking prior = null;
            try {
                prior = disable();
                return callable.call();
            } catch(Exception ex) {
                throw new RuntimeException(ex);
            } finally {
                reset(prior);
            }
        }

        /**
         * Recommended instead to call {@link #executeWithConcurrencyCheckingDisabled(Runnable)} or {@link #executeWithConcurrencyCheckingDisabled(Callable)}.
         *
         * <p>
         *     If this method is used, then make sure to call {@link #reset(ConcurrencyChecking)} afterwards, using the value returned by this method.
         * </p>
         *
         * @return the value of the {@link ConcurrencyChecking} thread-local prior to disabling it (to allow for nested calls).
         */
        public static ConcurrencyChecking disable() {
            final ConcurrencyChecking prior = ConcurrencyChecking.concurrencyChecking.get();
            ConcurrencyChecking.concurrencyChecking.set(ConcurrencyChecking.NO_CHECK);
            return prior;
        }

        public static void reset(ConcurrencyChecking prior) {
            if(prior == null) {
                return;
            }
            ConcurrencyChecking.concurrencyChecking.set(prior);
        }

        /**
         * Allows a caller to temporarily disable concurrency checking for the current thread.
         */
        public static void executeWithConcurrencyCheckingDisabled(final Runnable runnable) {
            final ConcurrencyChecking prior = ConcurrencyChecking.concurrencyChecking.get();
            try {
                disable();
                runnable.run();
            } finally {
                ConcurrencyChecking.concurrencyChecking.set(prior);
            }
        }


    }

    // -- DEPRECATIONS

//    /**
//     * Enable RecreatableObjectFacet to 'temporarily' map an existing pojo to an oid.
//     * @deprecated don't expose caching
//     */
//    @Programmatic @Deprecated
//    ObjectAdapter addRecreatedPojoToCache(Oid oid, Object recreatedPojo);
//
//    /**
//     * Enable RecreatableObjectFacet to remove a 'temporarily' mapped adapter for a pojo.
//     * @deprecated don't expose caching
//     */
//    @Programmatic @Deprecated
//    void removeAdapterFromCache(ObjectAdapter adapter);
//
//    
//    /**
//     * Gets the {@link ObjectAdapter adapter} for the specified domain object if
//     * it exists in the identity map.
//     *
//     * <p>
//     * Provided by the <tt>AdapterManager</tt> when used by framework.
//     *
//     * @param pojo
//     *            - must not be <tt>null</tt>
//     * @return adapter, or <tt>null</tt> if doesn't exist.
//     * @deprecated don't expose caching
//     */
//    @Programmatic @Deprecated
//    ObjectAdapter lookupAdapterFor(Object pojo);
//
//    /**
//     * Gets the {@link ObjectAdapter adapter} for the {@link Oid} if it exists
//     * in the identity map.
//     *
//     * @param oid
//     *            - must not be <tt>null</tt>
//     * @return adapter, or <tt>null</tt> if doesn't exist.
//     * @deprecated don't expose caching
//     */
//    @Programmatic @Deprecated
//    ObjectAdapter lookupAdapterFor(Oid oid);

}
