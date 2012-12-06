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

package org.apache.isis.core.metamodel.services.container;

import java.util.ArrayList;
import java.util.List;

import org.apache.isis.applib.DomainObjectContainer;
import org.apache.isis.applib.PersistFailedException;
import org.apache.isis.applib.RepositoryException;
import org.apache.isis.applib.filter.Filter;
import org.apache.isis.applib.query.Query;
import org.apache.isis.applib.query.QueryFindAllInstances;
import org.apache.isis.applib.security.RoleMemento;
import org.apache.isis.applib.security.UserMemento;
import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.commons.authentication.AuthenticationSessionProvider;
import org.apache.isis.core.commons.authentication.AuthenticationSessionProviderAware;
import org.apache.isis.core.commons.ensure.Assert;
import org.apache.isis.core.commons.exceptions.IsisException;
import org.apache.isis.core.metamodel.adapter.DomainObjectServices;
import org.apache.isis.core.metamodel.adapter.DomainObjectServicesAware;
import org.apache.isis.core.metamodel.adapter.LocalizationProvider;
import org.apache.isis.core.metamodel.adapter.LocalizationProviderAware;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.ObjectDirtier;
import org.apache.isis.core.metamodel.adapter.ObjectDirtierAware;
import org.apache.isis.core.metamodel.adapter.ObjectPersistor;
import org.apache.isis.core.metamodel.adapter.ObjectPersistorAware;
import org.apache.isis.core.metamodel.adapter.QuerySubmitter;
import org.apache.isis.core.metamodel.adapter.QuerySubmitterAware;
import org.apache.isis.core.metamodel.adapter.mgr.AdapterManager;
import org.apache.isis.core.metamodel.adapter.mgr.AdapterManagerAware;
import org.apache.isis.core.metamodel.adapter.oid.AggregatedOid;
import org.apache.isis.core.metamodel.adapter.util.AdapterUtils;
import org.apache.isis.core.metamodel.consent.InteractionResult;
import org.apache.isis.core.metamodel.services.container.query.QueryFindByPattern;
import org.apache.isis.core.metamodel.services.container.query.QueryFindByTitle;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.SpecificationLoader;
import org.apache.isis.core.metamodel.spec.SpecificationLoaderAware;

public class DomainObjectContainerDefault implements DomainObjectContainer, QuerySubmitterAware, ObjectDirtierAware, DomainObjectServicesAware, ObjectPersistorAware, SpecificationLoaderAware, AuthenticationSessionProviderAware, AdapterManagerAware, LocalizationProviderAware {

    private ObjectDirtier objectDirtier;
    private ObjectPersistor objectPersistor;
    private QuerySubmitter querySubmitter;
    private SpecificationLoader specificationLookup;
    private DomainObjectServices domainObjectServices;
    private AuthenticationSessionProvider authenticationSessionProvider;
    private AdapterManager adapterManager;
    private LocalizationProvider localizationProvider;

    public DomainObjectContainerDefault() {

    }

    // //////////////////////////////////////////////////////////////////
    // titleOf
    // //////////////////////////////////////////////////////////////////

    @Override
    public String titleOf(final Object domainObject) {
        final ObjectAdapter objectAdapter = adapterManager.adapterFor(domainObject);
        return objectAdapter.getSpecification().getTitle(objectAdapter, localizationProvider.getLocalization());
    }

    // //////////////////////////////////////////////////////////////////
    // newInstance, disposeInstance
    // //////////////////////////////////////////////////////////////////

    @Override
    @SuppressWarnings("unchecked")
    public <T> T newTransientInstance(final Class<T> ofClass) {
        final ObjectSpecification spec = getSpecificationLookup().loadSpecification(ofClass);
        if (spec.isParented()) {
            return newAggregatedInstance(this, ofClass);
        } else {
            final ObjectAdapter adapter = doCreateTransientInstance(spec);
            return (T) adapter.getObject();
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T newAggregatedInstance(final Object parent, final Class<T> ofClass) {
        final ObjectSpecification spec = getSpecificationLookup().loadSpecification(ofClass);
        if (!spec.isParented()) {
            throw new IsisException("Type must be annotated as @Aggregated: " + ofClass);
        }
        final ObjectAdapter adapter = doCreateAggregatedInstance(spec, parent);
        if (adapter.getOid() instanceof AggregatedOid) {
            return (T) adapter.getObject();
        } else {
            throw new IsisException("Object instantiated but was not given a AggregatedOid (does the configured object store support aggregates?): " + ofClass);
        }
    }

    /**
     * Returns a new instance of the specified class that will have been
     * persisted.
     */
    @Override
    public <T> T newPersistentInstance(final Class<T> ofClass) {
        final T newInstance = newTransientInstance(ofClass);
        persist(newInstance);
        return newInstance;
    }

    /**
     * Returns a new instance of the specified class that has the sane persisted
     * state as the specified object.
     */
    @Override
    public <T> T newInstance(final Class<T> ofClass, final Object object) {
        if (isPersistent(object)) {
            return newPersistentInstance(ofClass);
        } else {
            return newTransientInstance(ofClass);
        }
    }

    /**
     * Factored out as a potential hook method for subclasses.
     */
    protected ObjectAdapter doCreateTransientInstance(final ObjectSpecification spec) {
        return getDomainObjectServices().createTransientInstance(spec);
    }

    private ObjectAdapter doCreateAggregatedInstance(final ObjectSpecification spec, final Object parent) {
        final ObjectAdapter parentAdapter = getAdapterManager().getAdapterFor(parent);
        return getDomainObjectServices().createAggregatedInstance(spec, parentAdapter);
    }

    @Override
    public void remove(final Object persistentObject) {
        if (persistentObject == null) {
            throw new IllegalArgumentException("Must specify a reference for disposing an object");
        }
        final ObjectAdapter adapter = getAdapterManager().getAdapterFor(persistentObject);
        if (!isPersistent(persistentObject)) {
            throw new RepositoryException("Object not persistent: " + adapter);
        }

        getObjectPersistor().remove(adapter);
    }

    @Override
    public void removeIfNotAlready(final Object object) {
        if (!isPersistent(object)) {
            return;
        }
        remove(object);
    }

    // //////////////////////////////////////////////////////////////////
    // resolve, objectChanged
    // //////////////////////////////////////////////////////////////////

    @Override
    public void resolve(final Object parent) {
        getDomainObjectServices().resolve(parent);
    }

    @Override
    public void resolve(final Object parent, final Object field) {
        getDomainObjectServices().resolve(parent, field);
    }

    @Override
    public void objectChanged(final Object object) {
        getObjectDirtier().objectChanged(object);
    }

    // //////////////////////////////////////////////////////////////////
    // flush, commit
    // //////////////////////////////////////////////////////////////////

    @Override
    public boolean flush() {
        return getDomainObjectServices().flush();
    }

    @Override
    public void commit() {
        getDomainObjectServices().commit();
    }

    // //////////////////////////////////////////////////////////////////
    // isValid, validate
    // //////////////////////////////////////////////////////////////////

    @Override
    public boolean isValid(final Object domainObject) {
        return validate(domainObject) == null;
    }

    @Override
    public String validate(final Object domainObject) {
        final ObjectAdapter adapter = getAdapterManager().adapterFor(domainObject);
        final InteractionResult validityResult = adapter.getSpecification().isValidResult(adapter);
        return validityResult.getReason();
    }

    // //////////////////////////////////////////////////////////////////
    // persistence
    // //////////////////////////////////////////////////////////////////

    @Override
    public boolean isPersistent(final Object domainObject) {
        final ObjectAdapter adapter = getAdapterManager().adapterFor(domainObject);
        return adapter.representsPersistent();
    }

    @Override
    public void persist(final Object transientObject) {
        final ObjectAdapter adapter = getAdapterManager().getAdapterFor(transientObject);
        // TODO check aggregation is supported
        if (adapter.isParented()) {
            return;
        }
        if (isPersistent(transientObject)) {
            throw new PersistFailedException("Object already persistent: " + adapter);
        }
        getObjectPersistor().makePersistent(adapter);
    }

    @Override
    public void persistIfNotAlready(final Object object) {
        if (isPersistent(object)) {
            return;
        }
        persist(object);
    }

    // //////////////////////////////////////////////////////////////////
    // security
    // //////////////////////////////////////////////////////////////////

    @Override
    public UserMemento getUser() {
        final AuthenticationSession session = getAuthenticationSessionProvider().getAuthenticationSession();

        final String name = session.getUserName();
        final List<RoleMemento> roleMementos = asRoleMementos(session.getRoles());

        final UserMemento user = new UserMemento(name, roleMementos);
        return user;
    }

    private List<RoleMemento> asRoleMementos(final List<String> roles) {
        final List<RoleMemento> mementos = new ArrayList<RoleMemento>();
        if (roles != null) {
            for (final String role : roles) {
                mementos.add(new RoleMemento(role));
            }
        }
        return mementos;
    }

    // //////////////////////////////////////////////////////////////////
    // properties
    // //////////////////////////////////////////////////////////////////

    @Override
    public String getProperty(final String name) {
        return getDomainObjectServices().getProperty(name);
    }

    @Override
    public String getProperty(final String name, final String defaultValue) {
        final String value = getProperty(name);
        return value == null ? defaultValue : value;
    }

    @Override
    public List<String> getPropertyNames() {
        return getDomainObjectServices().getPropertyNames();
    }

    // //////////////////////////////////////////////////////////////////
    // info, warn, error messages
    // //////////////////////////////////////////////////////////////////

    @Override
    public void informUser(final String message) {
        getDomainObjectServices().informUser(message);
    }

    @Override
    public void raiseError(final String message) {
        getDomainObjectServices().raiseError(message);
    }

    @Override
    public void warnUser(final String message) {
        getDomainObjectServices().warnUser(message);
    }

    // //////////////////////////////////////////////////////////////////
    // allInstances, allMatches
    // //////////////////////////////////////////////////////////////////

    @Override
    public <T> List<T> allInstances(final Class<T> type) {
        return allMatches(new QueryFindAllInstances<T>(type));
    }

    @Override
    public <T> List<T> allMatches(final Class<T> cls, final Filter<? super T> filter) {
        final List<T> allInstances = allInstances(cls);
        final List<T> filtered = new ArrayList<T>();
        for (final T instance : allInstances) {
            if (filter.accept(instance)) {
                filtered.add(instance);
            }
        }
        return filtered;
    }

    @Override
    public <T> List<T> allMatches(final Class<T> type, final T pattern) {
        Assert.assertTrue("pattern not compatible with type", type.isAssignableFrom(pattern.getClass()));
        return allMatches(new QueryFindByPattern<T>(type, pattern));
    }

    @Override
    public <T> List<T> allMatches(final Class<T> type, final String title) {
        return allMatches(new QueryFindByTitle<T>(type, title));
    }

    @Override
    public <T> List<T> allMatches(final Query<T> query) {
        final List<ObjectAdapter> allMatching = getQuerySubmitter().allMatchingQuery(query);
        return AdapterUtils.unwrap(allMatching);
    }

    // //////////////////////////////////////////////////////////////////
    // firstMatch
    // //////////////////////////////////////////////////////////////////

    @Override
    public <T> T firstMatch(final Class<T> cls, final Filter<T> filter) {
        final List<T> allInstances = allInstances(cls);
        for (final T instance : allInstances) {
            if (filter.accept(instance)) {
                return instance;
            }
        }
        return null;
    }

    @Override
    public <T> T firstMatch(final Class<T> type, final T pattern) {
        final List<T> instances = allMatches(type, pattern);
        return firstInstanceElseNull(instances);
    }

    @Override
    public <T> T firstMatch(final Class<T> type, final String title) {
        final List<T> instances = allMatches(type, title);
        return firstInstanceElseNull(instances);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T firstMatch(final Query<T> query) {
        final ObjectAdapter firstMatching = getQuerySubmitter().firstMatchingQuery(query);
        return (T) AdapterUtils.unwrap(firstMatching);
    }

    // //////////////////////////////////////////////////////////////////
    // uniqueMatch
    // //////////////////////////////////////////////////////////////////

    @Override
    public <T> T uniqueMatch(final Class<T> type, final Filter<T> filter) {
        final List<T> instances = allMatches(type, filter);
        if (instances.size() > 1) {
            throw new RepositoryException("Found more than one instance of " + type + " matching filter " + filter);
        }
        return firstInstanceElseNull(instances);
    }

    @Override
    public <T> T uniqueMatch(final Class<T> type, final T pattern) {
        final List<T> instances = allMatches(type, pattern);
        if (instances.size() > 1) {
            throw new RepositoryException("Found more that one instance of " + type + " matching pattern " + pattern);
        }
        return firstInstanceElseNull(instances);
    }

    @Override
    public <T> T uniqueMatch(final Class<T> type, final String title) {
        final List<T> instances = allMatches(type, title);
        if (instances.size() > 1) {
            throw new RepositoryException("Found more that one instance of " + type + " with title " + title);
        }
        return firstInstanceElseNull(instances);
    }

    @Override
    public <T> T uniqueMatch(final Query<T> query) {
        final List<T> instances = allMatches(query);
        if (instances.size() > 1) {
            throw new RepositoryException("Found more that one instance for query:" + query.getDescription());
        }
        return firstInstanceElseNull(instances);
    }

    private <T> T firstInstanceElseNull(final List<T> instances) {
        return instances.size() == 0 ? null : instances.get(0);
    }

    // //////////////////////////////////////////////////////////////////
    // Dependencies
    // //////////////////////////////////////////////////////////////////

    protected QuerySubmitter getQuerySubmitter() {
        return querySubmitter;
    }

    @Override
    public void setQuerySubmitter(final QuerySubmitter querySubmitter) {
        this.querySubmitter = querySubmitter;
    }

    protected DomainObjectServices getDomainObjectServices() {
        return domainObjectServices;
    }

    @Override
    public void setDomainObjectServices(final DomainObjectServices domainObjectServices) {
        this.domainObjectServices = domainObjectServices;
    }

    protected SpecificationLoader getSpecificationLookup() {
        return specificationLookup;
    }

    @Override
    public void setSpecificationLookup(final SpecificationLoader specificationLookup) {
        this.specificationLookup = specificationLookup;
    }

    protected AuthenticationSessionProvider getAuthenticationSessionProvider() {
        return authenticationSessionProvider;
    }

    @Override
    public void setAuthenticationSessionProvider(final AuthenticationSessionProvider authenticationSessionProvider) {
        this.authenticationSessionProvider = authenticationSessionProvider;
    }

    protected AdapterManager getAdapterManager() {
        return adapterManager;
    }

    @Override
    public void setAdapterManager(final AdapterManager adapterManager) {
        this.adapterManager = adapterManager;
    }

    protected ObjectDirtier getObjectDirtier() {
        return objectDirtier;
    }

    @Override
    public void setObjectDirtier(final ObjectDirtier objectDirtier) {
        this.objectDirtier = objectDirtier;
    }

    protected ObjectPersistor getObjectPersistor() {
        return objectPersistor;
    }

    @Override
    public void setObjectPersistor(final ObjectPersistor objectPersistor) {
        this.objectPersistor = objectPersistor;
    }

    @Override
    public void setLocalizationProvider(final LocalizationProvider localizationProvider) {
        this.localizationProvider = localizationProvider;
    }

}
