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

package org.apache.isis.core.metamodel.runtimecontext.noruntime;

import java.util.List;

import org.apache.isis.applib.profiles.Localization;
import org.apache.isis.applib.query.Query;
import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.commons.authentication.AuthenticationSessionProvider;
import org.apache.isis.core.commons.authentication.AuthenticationSessionProviderAbstract;
import org.apache.isis.core.commons.config.IsisConfigurationDefault;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.mgr.AdapterManager;
import org.apache.isis.core.metamodel.adapter.mgr.AdapterManagerAware;
import org.apache.isis.core.metamodel.adapter.oid.Oid;
import org.apache.isis.core.metamodel.deployment.DeploymentCategory;
import org.apache.isis.core.metamodel.runtimecontext.LocalizationDefault;
import org.apache.isis.core.metamodel.runtimecontext.LocalizationProvider;
import org.apache.isis.core.metamodel.runtimecontext.LocalizationProviderAbstract;
import org.apache.isis.core.metamodel.runtimecontext.MessageBrokerService;
import org.apache.isis.core.metamodel.runtimecontext.MessageBrokerServiceAbstract;
import org.apache.isis.core.metamodel.runtimecontext.PersistenceSessionService;
import org.apache.isis.core.metamodel.runtimecontext.PersistenceSessionServiceAbstract;
import org.apache.isis.core.metamodel.runtimecontext.RuntimeContextAbstract;
import org.apache.isis.core.metamodel.runtimecontext.ServicesInjector;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.isis.core.metamodel.transactions.TransactionState;
import org.apache.isis.core.metamodel.transactions.TransactionStateProvider;
import org.apache.isis.core.metamodel.transactions.TransactionStateProviderAbstract;

public class RuntimeContextNoRuntime extends RuntimeContextAbstract {

    private final AuthenticationSessionProviderAbstract authenticationSessionProvider;
    private final PersistenceSessionServiceAbstract persistenceSessionService;
    private final LocalizationProviderAbstract localizationProvider;
    private final MessageBrokerServiceAbstract messageBrokerService;

    public RuntimeContextNoRuntime(
            final ServicesInjector servicesInjector) {
        this(DeploymentCategory.PRODUCTION, new IsisConfigurationDefault(null), servicesInjector);
    }

    public RuntimeContextNoRuntime(
            final DeploymentCategory deploymentCategory,
            final IsisConfigurationDefault isisConfiguration,
            final ServicesInjector servicesInjector) {
        super(deploymentCategory, isisConfiguration, servicesInjector);
        authenticationSessionProvider = new AuthenticationSessionProviderAbstract() {
            @Override
            public AuthenticationSession getAuthenticationSession() {
                throw new UnsupportedOperationException("Not supported by this implementation of RuntimeContext");
            }
        };
        persistenceSessionService = new PersistenceSessionServiceAbstract() {

            @Override
            public void injectInto(final Object candidate) {
                if (AdapterManagerAware.class.isAssignableFrom(candidate.getClass())) {
                    final AdapterManagerAware cast = AdapterManagerAware.class.cast(candidate);
                    cast.setAdapterManager(this);
                }
            }

            @Override
            public ObjectAdapter getAdapterFor(final Object pojo) {
                throw new UnsupportedOperationException("Not supported by this implementation of RuntimeContext");
            }

            @Override
            public ObjectAdapter adapterFor(final Object pojo, final ObjectAdapter ownerAdapter, final OneToManyAssociation collection) {
                throw new UnsupportedOperationException("Not supported by this implementation of RuntimeContext");
            }

            @Override
            public ObjectAdapter mapRecreatedPojo(Oid oid, Object recreatedPojo) {
                throw new UnsupportedOperationException("Not supported by this implementation of RuntimeContext");
            }

            @Override
            public void removeAdapter(ObjectAdapter adapter) {
                throw new UnsupportedOperationException("Not supported by this implementation of RuntimeContext");
            }

            @Override
            public ObjectAdapter adapterFor(final Object domainObject) {
                throw new UnsupportedOperationException("Not supported by this implementation of RuntimeContext");
            }

            @Override
            public ObjectAdapter getAdapterFor(Oid oid) {
                throw new UnsupportedOperationException("Not supported by this implementation of RuntimeContext");
            }

            @Override
            public ObjectAdapter createTransientInstance(final ObjectSpecification spec) {
                throw new UnsupportedOperationException("Not supported by this implementation of RuntimeContext");
            }

            @Override
            public ObjectAdapter createViewModelInstance(ObjectSpecification spec, String memento) {
                throw new UnsupportedOperationException("Not supported by this implementation of RuntimeContext");
            }

            @Override
            public Object lookup(Bookmark bookmark) {
                throw new UnsupportedOperationException("Not supported by this implementation of RuntimeContext");
            }

            @Override
            public Bookmark bookmarkFor(Object domainObject) {
                throw new UnsupportedOperationException("Not supported by this implementation of RuntimeContext");
            }

            @Override
            public Bookmark bookmarkFor(Class<?> cls, String identifier) {
                throw new UnsupportedOperationException("Not supported by this implementation of RuntimeContext");
            }

            @Override
            public void resolve(final Object parent, final Object field) {
                throw new UnsupportedOperationException("Not supported by this implementation of RuntimeContext");
            }

            @Override
            public void resolve(final Object parent) {
                throw new UnsupportedOperationException("Not supported by this implementation of RuntimeContext");
            }

            @Override
            public boolean flush() {
                throw new UnsupportedOperationException("Not supported by this implementation of RuntimeContext");
            }

            @Override
            public void commit() {
                throw new UnsupportedOperationException("Not supported by this implementation of RuntimeContext");
            }

            @Override
            public void remove(final ObjectAdapter adapter) {
                throw new UnsupportedOperationException("Not supported by this implementation of RuntimeContext");
            }

            @Override
            public void makePersistent(final ObjectAdapter adapter) {
                throw new UnsupportedOperationException("Not supported by this implementation of RuntimeContext");
            }

            @Override
            public <T> ObjectAdapter firstMatchingQuery(final Query<T> query) {
                throw new UnsupportedOperationException("Not supported by this implementation of RuntimeContext");
            }

            @Override
            public <T> List<ObjectAdapter> allMatchingQuery(final Query<T> query) {
                throw new UnsupportedOperationException("Not supported by this implementation of RuntimeContext");
            }

        };
        localizationProvider = new LocalizationProviderAbstract() {

            private final Localization defaultLocalization = new LocalizationDefault();

            @Override
            public Localization getLocalization() {
                return defaultLocalization;
            }
        };
        messageBrokerService = new MessageBrokerServiceAbstract() {

            @Override
            public void informUser(final String message) {
                throw new UnsupportedOperationException("Not supported by this implementation of RuntimeContext");
            }

            @Override
            public void warnUser(final String message) {
                throw new UnsupportedOperationException("Not supported by this implementation of RuntimeContext");
            }

            @Override
            public void raiseError(final String message) {
                throw new UnsupportedOperationException("Not supported by this implementation of RuntimeContext");
            }


        };
    }

    // ///////////////////////////////////////////
    // Components
    // ///////////////////////////////////////////


    @Override
    public TransactionStateProvider getTransactionStateProvider() {
        return new TransactionStateProviderAbstract() {
            @Override
            public TransactionState getTransactionState() {
                throw new UnsupportedOperationException("Not supported by this implementation of RuntimeContext");
            }
        };
    }


    @Override
    public AuthenticationSessionProvider getAuthenticationSessionProvider() {
        return authenticationSessionProvider;
    }

    @Override
    public AdapterManager getAdapterManager() {
        return getPersistenceSessionService();
    }

    @Override
    public PersistenceSessionService getPersistenceSessionService() {
        return persistenceSessionService;
    }

    @Override
    public MessageBrokerService getMessageBrokerService() {
        return messageBrokerService;
    }

    @Override
    public LocalizationProvider getLocalizationProvider() {
        return localizationProvider;
    }


}
