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

package org.apache.isis.core.runtime.persistence.internal;

import java.util.List;
import java.util.Properties;

import org.apache.isis.applib.RecoverableException;
import org.apache.isis.applib.profiles.Localization;
import org.apache.isis.applib.query.Query;
import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.commons.authentication.AuthenticationSessionProvider;
import org.apache.isis.core.commons.authentication.AuthenticationSessionProviderAbstract;
import org.apache.isis.core.commons.authentication.MessageBroker;
import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.metamodel.runtimecontext.DomainObjectServices;
import org.apache.isis.core.metamodel.runtimecontext.DomainObjectServicesAbstract;
import org.apache.isis.core.metamodel.runtimecontext.LocalizationProviderAbstract;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.runtimecontext.ObjectPersistor;
import org.apache.isis.core.metamodel.runtimecontext.ObjectPersistorAbstract;
import org.apache.isis.core.metamodel.runtimecontext.MessageBrokerService;
import org.apache.isis.core.metamodel.runtimecontext.MessageBrokerServiceAbstract;
import org.apache.isis.core.metamodel.adapter.mgr.AdapterManager;
import org.apache.isis.core.metamodel.adapter.mgr.AdapterManagerAware;
import org.apache.isis.core.metamodel.adapter.oid.Oid;
import org.apache.isis.core.metamodel.adapter.oid.RootOid;
import org.apache.isis.core.metamodel.deployment.DeploymentCategory;
import org.apache.isis.core.metamodel.deployment.DeploymentCategoryProvider;
import org.apache.isis.core.metamodel.deployment.DeploymentCategoryProviderAbstract;
import org.apache.isis.core.metamodel.facets.collections.modify.CollectionFacetUtils;
import org.apache.isis.core.metamodel.runtimecontext.RuntimeContextAbstract;
import org.apache.isis.core.metamodel.runtimecontext.ServicesInjector;
import org.apache.isis.core.metamodel.services.container.query.QueryCardinality;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.isis.core.metamodel.transactions.TransactionState;
import org.apache.isis.core.metamodel.transactions.TransactionStateProvider;
import org.apache.isis.core.metamodel.transactions.TransactionStateProviderAbstract;
import org.apache.isis.core.runtime.persistence.container.DomainObjectContainerResolve;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.core.runtime.system.persistence.PersistenceSession;
import org.apache.isis.core.runtime.system.session.IsisSession;
import org.apache.isis.core.runtime.system.transaction.IsisTransaction;
import org.apache.isis.core.runtime.system.transaction.IsisTransactionManager;

/**
 * Provides services to the metamodel based on the currently running
 * {@link IsisSession session} (primarily the {@link PersistenceSession}).
 */
public class RuntimeContextFromSession extends RuntimeContextAbstract {

    private final AuthenticationSessionProvider authenticationSessionProvider;
    private final AdapterManager adapterManager;
    private final ObjectPersistor objectPersistor;
    private final MessageBrokerService messageBrokerService;
    private final DomainObjectServices domainObjectServices;
    private final LocalizationProviderAbstract localizationProvider;

    // //////////////////////////////////////////////////////////////////
    // Constructor
    // //////////////////////////////////////////////////////////////////


    public RuntimeContextFromSession(final IsisConfiguration configuration) {

        final Properties properties = applicationPropertiesFrom(configuration);
        setProperties(properties);

        this.authenticationSessionProvider = new AuthenticationSessionProviderAbstract() {

            @Override
            public AuthenticationSession getAuthenticationSession() {
                return IsisContext.getAuthenticationSession();
            }
        };
        this.adapterManager = new AdapterManager() {

            @Override
            public ObjectAdapter getAdapterFor(Oid oid) {
                return null;
            }

            @Override
            public ObjectAdapter getAdapterFor(final Object pojo) {
                return getRuntimeAdapterManager().getAdapterFor(pojo);
            }

            @Override
            public ObjectAdapter adapterFor(final Object pojo) {
                return getRuntimeAdapterManager().adapterFor(pojo);
            }

            @Override
            public ObjectAdapter adapterFor(final Object pojo, final ObjectAdapter ownerAdapter, final OneToManyAssociation collection) {
                return getRuntimeAdapterManager().adapterFor(pojo, ownerAdapter, collection);
            }

            @Override
            public ObjectAdapter mapRecreatedPojo(Oid oid, Object recreatedPojo) {
                return getRuntimeAdapterManager().mapRecreatedPojo(oid, recreatedPojo);
            }

            @Override
            public void removeAdapter(ObjectAdapter adapter) {
                getRuntimeAdapterManager().removeAdapter(adapter);
            }

            @Override
            public ObjectAdapter adapterFor(RootOid oid) {
            	return getRuntimeAdapterManager().adapterFor(oid);
            }

            @Override
            public ObjectAdapter adapterFor(RootOid oid, ConcurrencyChecking concurrencyChecking) {
                return getRuntimeAdapterManager().adapterFor(oid, concurrencyChecking);
            }

            @Override
            public void injectInto(Object candidate) {
                if (AdapterManagerAware.class.isAssignableFrom(candidate.getClass())) {
                    final AdapterManagerAware cast = AdapterManagerAware.class.cast(candidate);
                    cast.setAdapterManager(this);
                }
            }


        };

        this.objectPersistor = new ObjectPersistorAbstract() {
            @Override
            public void makePersistent(final ObjectAdapter adapter) {
                getPersistenceSession().makePersistentInTransaction(adapter);
            }

            @Override
            public void remove(final ObjectAdapter adapter) {
                getPersistenceSession().destroyObjectInTransaction(adapter);
            }

            @Override
            public ObjectAdapter createTransientInstance(final ObjectSpecification spec) {
                return getPersistenceSession().createTransientInstance(spec);
            }

            @Override
            public ObjectAdapter createViewModelInstance(ObjectSpecification spec, String memento) {
                return getPersistenceSession().createViewModelInstance(spec, memento);
            }

            @Override
            public Object lookup(Bookmark bookmark) {
                return new DomainObjectContainerResolve().lookup(bookmark);
            }


            @Override
            public Bookmark bookmarkFor(Object domainObject) {
                return new DomainObjectContainerResolve().bookmarkFor(domainObject);
            }

            @Override
            public Bookmark bookmarkFor(Class<?> cls, String identifier) {
                return new DomainObjectContainerResolve().bookmarkFor(cls, identifier);
            }


            @Override
            public void resolve(final Object parent) {
                new DomainObjectContainerResolve().resolve(parent);
            }

            @Override
            public void resolve(final Object parent, final Object field) {
                new DomainObjectContainerResolve().resolve(parent, field);
            }

            @Override
            public boolean flush() {
                return getTransactionManager().flushTransaction();
            }

            @Override
            public void commit() {
                getTransactionManager().endTransaction();
            }

            @Override
            public <T> List<ObjectAdapter> allMatchingQuery(final Query<T> query) {
                final ObjectAdapter instances = getPersistenceSession().findInstancesInTransaction(query,
                        QueryCardinality.MULTIPLE);
                return CollectionFacetUtils.convertToAdapterList(instances);
            }

            @Override
            public <T> ObjectAdapter firstMatchingQuery(final Query<T> query) {
                final ObjectAdapter instances = getPersistenceSession().findInstancesInTransaction(query,
                        QueryCardinality.SINGLE);
                final List<ObjectAdapter> list = CollectionFacetUtils.convertToAdapterList(instances);
                return list.size() > 0 ? list.get(0) : null;
            }

        };
        this.domainObjectServices = new DomainObjectServicesAbstract() {

            @Override
            public void informUser(final String message) {
                getMessageBroker().addMessage(message);
            }

            @Override
            public void warnUser(final String message) {
                getMessageBroker().addWarning(message);
            }

            @Override
            public void raiseError(final String message) {
                throw new RecoverableException(message);
            }

            @Override
            public String getProperty(final String name) {
                return RuntimeContextFromSession.this.getProperty(name);
            }

            @Override
            public List<String> getPropertyNames() {
                return RuntimeContextFromSession.this.getPropertyNames();
            }

        };
        this.messageBrokerService = new MessageBrokerServiceAbstract() {

        };
        this.localizationProvider = new LocalizationProviderAbstract() {

            @Override
            public Localization getLocalization() {
                return IsisContext.getLocalization();
            }
        };
    }

    public static Properties applicationPropertiesFrom(final IsisConfiguration configuration) {
        final Properties properties = new Properties();
        final IsisConfiguration applicationConfiguration = configuration.getProperties("application");
        for (final String key : applicationConfiguration) {
            final String value = applicationConfiguration.getString(key);
            final String newKey = key.substring("application.".length());
            properties.setProperty(newKey, value);
        }
        return properties;
    }


    // //////////////////////////////////////////////////////////////////
    // Components
    // //////////////////////////////////////////////////////////////////

    @Override
    public AuthenticationSessionProvider getAuthenticationSessionProvider() {
        return authenticationSessionProvider;
    }

    @Override
    public AdapterManager getAdapterManager() {
        return adapterManager;
    }

    @Override
    public DomainObjectServices getDomainObjectServices() {
        return domainObjectServices;
    }

    @Override
    public LocalizationProviderAbstract getLocalizationProvider() {
        return localizationProvider;
    }

    @Override
    public ObjectPersistor getObjectPersistor() {
        return objectPersistor;
    }

    @Override
    public ServicesInjector getServicesInjector() {
        return servicesInjectorDelegator;
    }

    @Override
    public MessageBrokerService getMessageBrokerService() {
        return messageBrokerService;
    }

    // ///////////////////////////////////////////
    // Dependencies (from context)
    // ///////////////////////////////////////////

    @Override
    public DeploymentCategoryProvider getDeploymentCategoryProvider() {
        return new DeploymentCategoryProviderAbstract() {
            @Override
            public DeploymentCategory getDeploymentCategory() {
                return IsisContext.getDeploymentType().getDeploymentCategory();
            }
        };
    }

    @Override
    public TransactionStateProvider getTransactionStateProvider() {
        return new TransactionStateProviderAbstract() {
            @Override
            public TransactionState getTransactionState() {
                final IsisTransaction transaction = getTransactionManager().getTransaction();
                if(transaction == null) {
                    return TransactionState.NONE;
                }
                IsisTransaction.State state = transaction.getState();
                return state.getRuntimeContextState();
            }
        };
    }


    private static PersistenceSession getPersistenceSession() {
        return IsisContext.getPersistenceSession();
    }

    private static AdapterManager getRuntimeAdapterManager() {
        return getPersistenceSession().getAdapterManager();
    }

    private static IsisTransactionManager getTransactionManager() {
        return getPersistenceSession().getTransactionManager();
    }

    private static MessageBroker getMessageBroker() {
        return IsisContext.getMessageBroker();
    }


}
