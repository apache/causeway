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

import org.apache.isis.applib.annotation.ActionSemantics;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.oid.Oid;
import org.apache.isis.core.metamodel.spec.feature.OneToManyAssociation;

public interface AdapterManager extends AdapterManagerBase {

    /**
     * Gets the {@link ObjectAdapter adapter} for the {@link Oid} if it exists
     * in the identity map.
     *
     * @param oid
     *            - must not be <tt>null</tt>
     * @return adapter, or <tt>null</tt> if doesn't exist.
     */
    @Programmatic
    ObjectAdapter getAdapterFor(Oid oid);



    enum ConcurrencyChecking {
        NO_CHECK,
        CHECK;

        @Programmatic
        public boolean isChecking() {
            return this == CHECK;
        }

        public static ConcurrencyChecking concurrencyCheckingFor(ActionSemantics.Of actionSemantics) {
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
            protected ConcurrencyChecking initialValue() {
                return CHECK;
            };
        };

        /**
         * Whether concurrency checking is currently disabled.
         */
        public static boolean isCurrentlyEnabled() {
            return concurrencyChecking.get().isChecking();
        }

        /**
         * Allows a caller to temporarily disable concurrency checking for the current thread.
         */
        public static <T> T executeWithConcurrencyCheckingDisabled(final Callable<T> callable) {
            final ConcurrencyChecking prior = ConcurrencyChecking.concurrencyChecking.get();
            try {
                ConcurrencyChecking.concurrencyChecking.set(ConcurrencyChecking.NO_CHECK);
                return callable.call();
            } catch(Exception ex) {
                throw new RuntimeException(ex);
            } finally {
                ConcurrencyChecking.concurrencyChecking.set(prior);
            }
        }

        /**
         * Allows a caller to temporarily disable concurrency checking for the current thread.
         */
        public static void executeWithConcurrencyCheckingDisabled(final Runnable runnable) {
            final ConcurrencyChecking prior = ConcurrencyChecking.concurrencyChecking.get();
            try {
                ConcurrencyChecking.concurrencyChecking.set(ConcurrencyChecking.NO_CHECK);
                runnable.run();
            } finally {
                ConcurrencyChecking.concurrencyChecking.set(prior);
            }
        }


    }




    /**
     * Looks up or creates a collection adapter.
     */
    @Programmatic
    ObjectAdapter adapterFor(
            final Object pojo,
            final ObjectAdapter parentAdapter,
            OneToManyAssociation collection);


    /**
     * Enable RecreatableObjectFacet to 'temporarily' map an existing pojo to an oid.
     */
    @Programmatic
    ObjectAdapter mapRecreatedPojo(Oid oid, Object recreatedPojo);

    /**
     * Enable RecreatableObjectFacet to remove a 'temporarily' mapped an adapter for a pojo.
     */
    @Programmatic
    void removeAdapter(ObjectAdapter adapter);



}
