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

import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.isis.applib.DomainObjectContainer;
import org.apache.isis.applib.PersistFailedException;
import org.apache.isis.applib.RecoverableException;
import org.apache.isis.applib.RepositoryException;
import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.services.exceprecog.ExceptionRecognizer;
import org.apache.isis.applib.services.exceprecog.ExceptionRecognizerComposite;
import org.apache.isis.applib.services.exceprecog.ExceptionRecognizerForType;
import org.apache.isis.applib.services.factory.FactoryService;
import org.apache.isis.applib.services.registry.ServiceRegistry;
import org.apache.isis.applib.services.repository.RepositoryService;
import org.apache.isis.applib.services.wrapper.WrapperFactory;
import org.apache.isis.core.commons.exceptions.IsisException;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.version.ConcurrencyException;
import org.apache.isis.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.core.metamodel.consent.InteractionResult;
import org.apache.isis.core.metamodel.facets.object.viewmodel.ViewModelFacet;
import org.apache.isis.core.metamodel.services.persistsession.PersistenceSessionServiceInternal;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;

@DomainService(
        nature = NatureOfService.DOMAIN,
        menuOrder = "" + Integer.MAX_VALUE
)
public class DomainObjectContainerDefault
        implements DomainObjectContainer, ExceptionRecognizer {



    //region > newXxxInstance, remove

    @Deprecated
    @Programmatic
    @Override
    @SuppressWarnings("unchecked")
    public <T> T newTransientInstance(final Class<T> ofClass) {
        return factoryService.instantiate(ofClass);
    }

    @Programmatic
    @SuppressWarnings("unchecked")
    @Override
    public <T> T newViewModelInstance(Class<T> ofClass, String memento) {
        final ObjectSpecification spec = specificationLoader.loadSpecification(ofClass);
        if (!spec.containsFacet(ViewModelFacet.class)) {
            throw new IsisException("Type must be a ViewModel: " + ofClass);
        }
        final ObjectAdapter adapter = persistenceSessionServiceInternal.createViewModelInstance(spec, memento);
        if(adapter.getOid().isViewModel()) {
            return (T)adapter.getObject();
        } else {
            throw new IsisException("Object instantiated but was not given a ViewModel Oid; please report as a possible defect in Isis: " + ofClass);
        }
    }

    /**
     * @deprecated - Aggregated objects are no longer supported
     */
    @Deprecated
    @Programmatic
    @Override
    @SuppressWarnings("unchecked")
    public <T> T newAggregatedInstance(final Object parent, final Class<T> ofClass) {
        throw new RuntimeException("Aggregated objects are no longer supported");
    }

    /**
     * Returns a new instance of the specified class that will have been
     * persisted.
     */
    @Deprecated
    @Programmatic
    @Override
    public <T> T newPersistentInstance(final Class<T> ofClass) {
        final T newInstance = newTransientInstance(ofClass);
        persist(newInstance);
        return newInstance;
    }

    /**
     * Returns a new instance of the specified class that has the same persisted
     * state as the specified object.
     *
     * @deprecated - use {@link FactoryService#instantiate(Class)}.
     */
    @Programmatic
    @Override
    @Deprecated
    public <T> T newInstance(final Class<T> ofClass, final Object object) {
        if (isPersistent(object)) {
            return newPersistentInstance(ofClass);
        } else {
            return newTransientInstance(ofClass);
        }
    }


    @Deprecated
    @Programmatic
    @Override
    public <T> T mixin(final Class<T> mixinClass, final Object mixedIn) {
        return factoryService.mixin(mixinClass, mixedIn);
    }

    @Programmatic
    @Override
    public void remove(final Object persistentObject) {
        if (persistentObject == null) {
            throw new IllegalArgumentException("Must specify a reference for disposing an object");
        }
        final ObjectAdapter adapter = persistenceSessionServiceInternal.adapterFor(unwrapped(persistentObject));
        if (!isPersistent(persistentObject)) {
            throw new RepositoryException("Object not persistent: " + adapter);
        }

        persistenceSessionServiceInternal.remove(adapter);
    }

    @Programmatic
    @Override
    public void removeIfNotAlready(final Object object) {
        repositoryService.remove(object);
    }

    //endregion


    //region > resolve, objectChanged (DEPRECATED)

    /**
     * Re-initialises the fields of an object, using the
     * JDO {@link javax.jdo.PersistenceManager#refresh(Object) refresh} API.
     *
     * <p>
     *     Previously this method was provided for manual control of lazy loading; with the JDO/DataNucleus objectstore
     *     that original functionality is performed automatically by the framework.
     * </p>
     *
     * @deprecated - equivalent to {@link org.apache.isis.applib.services.jdosupport.IsisJdoSupport#refresh(Object)}.
     */
    @Programmatic
    @Deprecated
    @Override
    public void resolve(final Object parent) {
        persistenceSessionServiceInternal.resolve(unwrapped(parent));
    }

    /**
     * Provided that the <tt>field</tt> parameter is <tt>null</tt>, re-initialises the fields of an object, using the
     * JDO {@link javax.jdo.PersistenceManager#refresh(Object) refresh} API.
     *
     * <p>
     *     Previously this method was provided for manual control of lazy loading; with the JDO/DataNucleus objectstore
     *     that original functionality is performed automatically by the framework.
     * </p>
     *
     * @deprecated - equivalent to {@link org.apache.isis.applib.services.jdosupport.IsisJdoSupport#refresh(Object)}.
     */
    @Programmatic
    @Deprecated
    @Override
    public void resolve(final Object parent, final Object field) {
        persistenceSessionServiceInternal.resolve(unwrapped(parent), field);
    }

    /**
     * @deprecated - no-op.
     */
    @Deprecated
    @Programmatic
    @Override
    public void objectChanged(final Object object) {
    }

    //endregion

    //region > commit (deprecated)

    /**
     * @deprecated
     */
    @Deprecated
    @Programmatic
    @Override
    public void commit() {
        persistenceSessionServiceInternal.commit();
    }

    //endregion

    //region > isValid, validate

    @Programmatic
    @Override
    public boolean isValid(final Object domainObject) {
        return validate(domainObject) == null;
    }

    @Programmatic
    @Override
    public String validate(final Object domainObject) {
        final ObjectAdapter adapter = persistenceSessionServiceInternal.adapterFor(unwrapped(domainObject));
        final InteractionResult validityResult =
                adapter.getSpecification().isValidResult(adapter, InteractionInitiatedBy.FRAMEWORK);
        return validityResult.getReason();
    }

    //endregion


    //region > isViewModel

    @Programmatic
    @Override
    public boolean isViewModel(final Object domainObject) {
        final ObjectAdapter adapter = persistenceSessionServiceInternal.adapterFor(unwrapped(domainObject));
        return adapter.getSpecification().isViewModel();
    }
    //endregion

    //region > persistence


    @Programmatic
    @Override
    public boolean isPersistent(final Object domainObject) {
        final ObjectAdapter adapter = persistenceSessionServiceInternal.adapterFor(unwrapped(domainObject));
        return adapter.representsPersistent();
    }

    /**
     * {@inheritDoc}
     */
    @Programmatic
    @Override
    public void persist(final Object domainObject) {
        final ObjectAdapter adapter = persistenceSessionServiceInternal.adapterFor(unwrapped(domainObject));

        if(adapter == null) {
            throw new PersistFailedException("Object not known to framework; instantiate using newTransientInstance(...) rather than simply new'ing up.");
        }
        if (adapter.isParentedCollection()) {
            // TODO check aggregation is supported
            return;
        }
        if (isPersistent(domainObject)) {
            throw new PersistFailedException("Object already persistent; OID=" + adapter.getOid());
        }
        persistenceSessionServiceInternal.makePersistent(adapter);
    }

    /**
     * {@inheritDoc}
     */
    @Programmatic
    @Override
    public void persistIfNotAlready(final Object object) {
        repositoryService.persist(object);
    }


    //endregion




    // //////////////////////////////////////////////////////////////////



    //region > ExceptionRecognizer

    static class ExceptionRecognizerForConcurrencyException
            extends ExceptionRecognizerForType {
        public ExceptionRecognizerForConcurrencyException() {
            super(Category.CONCURRENCY, ConcurrencyException.class, prefix("Another user has just changed this data"));
        }
    }
    static class ExceptionRecognizerForRecoverableException extends ExceptionRecognizerForType {
        public ExceptionRecognizerForRecoverableException() {
            super(Category.CLIENT_ERROR, RecoverableException.class);
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
    @Programmatic
    @Override
    public String recognize(Throwable ex) {
        return recognizer.recognize(ex);
    }

    //endregion

    //region > init, shutdown

    @Programmatic
    @PostConstruct
    @Override
    public void init(Map<String, String> properties) {
        serviceRegistry.injectServicesInto(recognizer);
        recognizer.init(properties);
    }

    @Programmatic
    @PreDestroy
    @Override
    public void shutdown() {
        recognizer.shutdown();
    }

    //endregion

    //region > helpers

    private Object unwrapped(Object domainObject) {
        return wrapperFactory != null ? wrapperFactory.unwrap(domainObject) : domainObject;
    }
    //endregion


    //region > service dependencies

    @javax.inject.Inject
    SpecificationLoader specificationLoader;

    @javax.inject.Inject
    FactoryService factoryService;

    @javax.inject.Inject
    RepositoryService repositoryService;

    @javax.inject.Inject
    ServiceRegistry serviceRegistry;

    @javax.inject.Inject
    WrapperFactory wrapperFactory;

    @javax.inject.Inject
    PersistenceSessionServiceInternal persistenceSessionServiceInternal;

    //endregion

}
