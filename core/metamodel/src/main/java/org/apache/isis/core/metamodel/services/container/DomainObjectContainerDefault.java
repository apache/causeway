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
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import com.google.common.base.Predicate;
import org.apache.isis.applib.*;
import org.apache.isis.applib.filter.Filter;
import org.apache.isis.applib.filter.Filters;
import org.apache.isis.applib.query.Query;
import org.apache.isis.applib.query.QueryFindAllInstances;
import org.apache.isis.applib.security.RoleMemento;
import org.apache.isis.applib.security.UserMemento;
import org.apache.isis.applib.services.exceprecog.ExceptionRecognizer;
import org.apache.isis.applib.services.exceprecog.ExceptionRecognizerComposite;
import org.apache.isis.applib.services.exceprecog.ExceptionRecognizerForType;
import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.commons.authentication.AuthenticationSessionProvider;
import org.apache.isis.core.commons.authentication.AuthenticationSessionProviderAware;
import org.apache.isis.core.commons.ensure.Assert;
import org.apache.isis.core.commons.exceptions.IsisException;
import org.apache.isis.core.metamodel.adapter.*;
import org.apache.isis.core.metamodel.adapter.mgr.AdapterManager;
import org.apache.isis.core.metamodel.adapter.mgr.AdapterManagerAware;
import org.apache.isis.core.metamodel.adapter.oid.AggregatedOid;
import org.apache.isis.core.metamodel.adapter.version.ConcurrencyException;
import org.apache.isis.core.metamodel.consent.InteractionResult;
import org.apache.isis.core.metamodel.facets.object.viewmodel.ViewModelFacet;
import org.apache.isis.core.metamodel.services.container.query.QueryFindByPattern;
import org.apache.isis.core.metamodel.services.container.query.QueryFindByTitle;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.SpecificationLoader;
import org.apache.isis.core.metamodel.spec.SpecificationLoaderAware;

public class  DomainObjectContainerDefault implements DomainObjectContainer, QuerySubmitterAware, ObjectDirtierAware, DomainObjectServicesAware, ObjectPersistorAware, SpecificationLoaderAware, AuthenticationSessionProviderAware, AdapterManagerAware, LocalizationProviderAware, ExceptionRecognizer {

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
        final boolean destroyed = objectAdapter.isDestroyed();
        if(!destroyed) {
            return objectAdapter.getSpecification().getTitle(objectAdapter, localizationProvider.getLocalization());
        } else {
            return "[DELETED]";
        }
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

    @SuppressWarnings("unchecked")
    @Override
    public <T extends ViewModel> T newViewModelInstance(Class<T> ofClass, String memento) {
        final ObjectSpecification spec = getSpecificationLookup().loadSpecification(ofClass);
        if (!spec.containsFacet(ViewModelFacet.class)) {
            throw new IsisException("Type must be a ViewModel: " + ofClass);
        }
        final ObjectAdapter adapter = doCreateViewModelInstance(spec, memento);
        if(adapter.getOid().isViewModel()) {
            return (T)adapter.getObject();
        } else {
            throw new IsisException("Object instantiated but was not given a ViewModel Oid; please report as a possible defect in Isis: " + ofClass);
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
     * Returns a new instance of the specified class that has the same persisted
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

    protected ObjectAdapter doCreateViewModelInstance(final ObjectSpecification spec, final String memento) {
        return getDomainObjectServices().createViewModelInstance(spec, memento);
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
        final ObjectAdapter adapter = getAdapterManager().adapterFor(persistentObject);
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
    // injectServicesInto
    // //////////////////////////////////////////////////////////////////

    @Override
    public <T> T injectServicesInto(T domainObject) {
        getDomainObjectServices().injectServicesInto(domainObject);
        return domainObject;
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

    /**
     * {@inheritDoc}
     */
    @Override
    public void persist(final Object domainObject) {
        final ObjectAdapter adapter = getAdapterManager().adapterFor(domainObject);

        if(adapter == null) {
            throw new PersistFailedException("Object not known to framework; instantiate using newTransientInstance(...) rather than simply new'ing up.");
        }
        if (adapter.isParented()) {
            // TODO check aggregation is supported
            return;
        }
        if (isPersistent(domainObject)) {
            throw new PersistFailedException("Object already persistent; OID=" + adapter.getOid());
        }
        getObjectPersistor().makePersistent(adapter);
    }

    /**
     * {@inheritDoc}
     */
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
    // allInstances
    // //////////////////////////////////////////////////////////////////

    @Override
    public <T> List<T> allInstances(final Class<T> type, long... range) {
        return allMatches(new QueryFindAllInstances<T>(type, range));
    }

    // //////////////////////////////////////////////////////////////////
    // allMatches
    // //////////////////////////////////////////////////////////////////
    
    @Override
    public <T> List<T> allMatches(final Class<T> cls, final Predicate<? super T> predicate, long... range) {
        final List<T> allInstances = allInstances(cls, range);
        final List<T> filtered = new ArrayList<T>();
        for (final T instance : allInstances) {
            if (predicate.apply(instance)) {
                filtered.add(instance);
            }
        }
        return filtered;
    }

    @Deprecated
    @Override
    public <T> List<T> allMatches(final Class<T> cls, final Filter<? super T> filter, long... range) {
        return allMatches(cls, Filters.asPredicate(filter), range);
    }
    
    @Override
    public <T> List<T> allMatches(final Class<T> type, final T pattern, long... range) {
        Assert.assertTrue("pattern not compatible with type", type.isAssignableFrom(pattern.getClass()));
        return allMatches(new QueryFindByPattern<T>(type, pattern, range));
    }

    @Override
    public <T> List<T> allMatches(final Class<T> type, final String title, long... range) {
        return allMatches(new QueryFindByTitle<T>(type, title, range));
    }

    @Override
    public <T> List<T> allMatches(final Query<T> query) {
        flush(); // auto-flush any pending changes
        final List<ObjectAdapter> allMatching = getQuerySubmitter().allMatchingQuery(query);
        return ObjectAdapter.Util.unwrapT(allMatching);
    }

    // //////////////////////////////////////////////////////////////////
    // firstMatch
    // //////////////////////////////////////////////////////////////////

    @Override
    public <T> T firstMatch(final Class<T> cls, final Predicate<T> predicate) {
        final List<T> allInstances = allInstances(cls); // Have to fetch all, as matching is done in next loop
        for (final T instance : allInstances) {
            if (predicate.apply(instance)) {
                return instance;
            }
        }
        return null;
    }

    @Deprecated
    @Override
    public <T> T firstMatch(final Class<T> cls, final Filter<T> filter) {
        return firstMatch(cls, Filters.asPredicate(filter));
    }
    
    @Override
    public <T> T firstMatch(final Class<T> type, final T pattern) {
        final List<T> instances = allMatches(type, pattern, 0, 1); // No need to fetch more than 1
        return firstInstanceElseNull(instances);
    }

    @Override
    public <T> T firstMatch(final Class<T> type, final String title) {
        final List<T> instances = allMatches(type, title, 0, 1); // No need to fetch more than 1
        return firstInstanceElseNull(instances);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T firstMatch(final Query<T> query) {
        flush(); // auto-flush any pending changes
        final ObjectAdapter firstMatching = getQuerySubmitter().firstMatchingQuery(query);
        return (T) ObjectAdapter.Util.unwrap(firstMatching);
    }

    // //////////////////////////////////////////////////////////////////
    // uniqueMatch
    // //////////////////////////////////////////////////////////////////

    @Override
    public <T> T uniqueMatch(final Class<T> type, final Predicate<T> predicate) {
        final List<T> instances = allMatches(type, predicate, 0, 2); // No need to fetch more than 2.
        if (instances.size() > 1) {
            throw new RepositoryException("Found more than one instance of " + type + " matching filter " + predicate);
        }
        return firstInstanceElseNull(instances);
    }

    @Deprecated
    @Override
    public <T> T uniqueMatch(final Class<T> type, final Filter<T> filter) {
        final List<T> instances = allMatches(type, filter, 0, 2); // No need to fetch more than 2.
        if (instances.size() > 1) {
            throw new RepositoryException("Found more than one instance of " + type + " matching filter " + filter);
        }
        return firstInstanceElseNull(instances);
    }
    
    @Override
    public <T> T uniqueMatch(final Class<T> type, final T pattern) {
        final List<T> instances = allMatches(type, pattern, 0, 2); // No need to fetch more than 2.
        if (instances.size() > 1) {
            throw new RepositoryException("Found more that one instance of " + type + " matching pattern " + pattern);
        }
        return firstInstanceElseNull(instances);
    }

    @Override
    public <T> T uniqueMatch(final Class<T> type, final String title) {
        final List<T> instances = allMatches(type, title, 0, 2); // No need to fetch more than 2.
        if (instances.size() > 1) {
            throw new RepositoryException("Found more that one instance of " + type + " with title " + title);
        }
        return firstInstanceElseNull(instances);
    }

    @Override
    public <T> T uniqueMatch(final Query<T> query) {
        final List<T> instances = allMatches(query); // No need to fetch more than 2. 
        if (instances.size() > 1) {
            throw new RepositoryException("Found more that one instance for query:" + query.getDescription());
        }
        return firstInstanceElseNull(instances);
    }

    private <T> T firstInstanceElseNull(final List<T> instances) {
        return instances.size() == 0 ? null : instances.get(0);
    }

    
    ///////////////////////////////////////////////////////////////
    // ExceptionRecognizer
    ///////////////////////////////////////////////////////////////

    static class ExceptionRecognizerForConcurrencyException extends ExceptionRecognizerForType {
        public ExceptionRecognizerForConcurrencyException() {
            super(ConcurrencyException.class, prefix("Another user has just changed this data"));
        }
    }
    static class ExceptionRecognizerForRecoverableException extends ExceptionRecognizerForType {
        public ExceptionRecognizerForRecoverableException() {
            super(RecoverableException.class);
        }
    }

    private final ExceptionRecognizer recognizer = 
            new ExceptionRecognizerComposite(
                    new ExceptionRecognizerForConcurrencyException(),
                    new ExceptionRecognizerForRecoverableException()
                );
    
    /**
     * Framework-provided implementation of {@link ExceptionRecognizer},
     * which will automatically recognize any {@link org.apache.isis.applib.RecoverableException}s or
     * any {@link ConcurrencyException}s.
     */
    @Override
    public String recognize(Throwable ex) {
        return recognizer.recognize(ex);
    }
    
    ExceptionRecognizer getRecognizer() {
        return recognizer;
    }

    @PostConstruct
    @Override
    public void init(Map<String, String> properties) {
        recognizer.init(properties);
    }

    @PreDestroy
    @Override
    public void shutdown() {
        recognizer.shutdown();
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
