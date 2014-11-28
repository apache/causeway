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

package org.apache.isis.core.metamodel.runtimecontext;

import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.commons.authentication.AuthenticationSessionProvider;
import org.apache.isis.core.commons.components.ApplicationScopedComponent;
import org.apache.isis.core.commons.components.Injectable;
import org.apache.isis.core.metamodel.adapter.DomainObjectServices;
import org.apache.isis.core.metamodel.adapter.LocalizationProvider;
import org.apache.isis.core.metamodel.adapter.ObjectDirtier;
import org.apache.isis.core.metamodel.adapter.ObjectPersistor;
import org.apache.isis.core.metamodel.adapter.QuerySubmitter;
import org.apache.isis.core.metamodel.adapter.ServicesProvider;
import org.apache.isis.core.metamodel.adapter.mgr.AdapterManager;
import org.apache.isis.core.metamodel.deployment.DeploymentCategory;
import org.apache.isis.core.metamodel.spec.ObjectInstantiator;
import org.apache.isis.core.metamodel.spec.SpecificationLoader;

/**
 * Decouples the metamodel from a runtime.
 * 
 */
public interface RuntimeContext extends Injectable, ApplicationScopedComponent {

    public DeploymentCategory getDeploymentCategory();

    /**
     * A mechanism for returning the <tt>current</tt>
     * {@link AuthenticationSession}.
     * 
     * <p>
     * Note that the scope of {@link RuntimeContext} is global, whereas
     * {@link AuthenticationSession} may change over time.
     */
    public AuthenticationSessionProvider getAuthenticationSessionProvider();

    public QuerySubmitter getQuerySubmitter();

    public AdapterManager getAdapterManager();

    public ObjectInstantiator getObjectInstantiator();

    public SpecificationLoader getSpecificationLoader();

    public ServicesProvider getServicesProvider();

    /**
     * aka the ServicesInjector...
     */
    public ServicesInjector getServicesInjector();

    public ObjectDirtier getObjectDirtier();

    public ObjectPersistor getObjectPersistor();

    public DomainObjectServices getDomainObjectServices();

    public LocalizationProvider getLocalizationProvider();


    // ///////////////////////////////////////////
    // transaction state
    // ///////////////////////////////////////////

    public TransactionState getTransactionState();

    public static enum TransactionState {
        
        /**
         * No transaction exists.
         */
        NONE,
        /**
         * Started, still in progress.
         * 
         * <p>
         * May flush, commit or abort.
         */
        IN_PROGRESS,
        /**
         * Started, but has hit an exception.
         * 
         * <p>
         * May not flush or commit (will throw an {@link IllegalStateException}),
         * can only abort.
         * 
         * <p>
         * Similar to <tt>setRollbackOnly</tt> in EJBs.
         */
        MUST_ABORT,
        /**
         * Completed, having successfully committed.
         * 
         * <p>
         * May not flush or abort or commit (will throw {@link IllegalStateException}).
         */
        COMMITTED,
        /**
         * Completed, having aborted.
         * 
         * <p>
         * May not flush, commit or abort (will throw {@link IllegalStateException}).
         */
        ABORTED;

        private TransactionState(){}

        /**
         * Whether it is valid to flush the transaction.
         */
        public boolean canFlush() {
            return this == IN_PROGRESS;
        }

        /**
         * Whether it is valid to commit the transaction.
         */
        public boolean canCommit() {
            return this == IN_PROGRESS;
        }

        /**
         * Whether it is valid to mark as aborted this transaction}.
         */
        public boolean canAbort() {
            return this == IN_PROGRESS || this == MUST_ABORT;
        }

        /**
         * Whether the transaction is complete (and so a new one can be started).
         */
        public boolean isComplete() {
            return this == COMMITTED || this == ABORTED;
        }

        public boolean mustAbort() {
            return this == MUST_ABORT;
        }
    }

}
