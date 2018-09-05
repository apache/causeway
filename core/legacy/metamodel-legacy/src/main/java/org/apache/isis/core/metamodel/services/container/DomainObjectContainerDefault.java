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

import org.apache.isis.applib.DomainObjectContainer;
import org.apache.isis.applib.PersistFailedException;
import org.apache.isis.applib.RepositoryException;
import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.services.repository.RepositoryService;
import org.apache.isis.applib.services.wrapper.WrapperFactory;
import org.apache.isis.core.commons.exceptions.IsisException;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.ObjectAdapterProvider;
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
public class DomainObjectContainerDefault implements DomainObjectContainer {


    // -- newViewModelInstance

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

    

    // --  remove

    @Programmatic
    @Override
    public void remove(final Object persistentObject) {
        if (persistentObject == null) {
            throw new IllegalArgumentException("Must specify a reference for disposing an object");
        }
        final ObjectAdapter adapter = getObjectAdapterProvider().adapterFor(unwrapped(persistentObject));
        if (!repositoryService.isPersistent(persistentObject)) {
            throw new RepositoryException("Object not persistent: " + adapter);
        }

        persistenceSessionServiceInternal.remove(adapter);
    }

    


    // -- resolve, objectChanged (DEPRECATED)

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

    

    // -- commit (deprecated)

    /**
     * @deprecated
     */
    @Deprecated
    @Programmatic
    @Override
    public void commit() {
        persistenceSessionServiceInternal.commit();
    }

    

    // -- isValid, validate

    @Programmatic
    @Override
    public boolean isValid(final Object domainObject) {
        return validate(domainObject) == null;
    }

    @Programmatic
    @Override
    public String validate(final Object domainObject) {
        final ObjectAdapter adapter = getObjectAdapterProvider().adapterFor(unwrapped(domainObject));
        final InteractionResult validityResult =
                adapter.getSpecification().isValidResult(adapter, InteractionInitiatedBy.FRAMEWORK);
        return validityResult.getReason();
    }

    


    // -- isViewModel

    @Programmatic
    @Override
    public boolean isViewModel(final Object domainObject) {
        final ObjectAdapter adapter = getObjectAdapterProvider().adapterFor(unwrapped(domainObject));
        return adapter.getSpecification().isViewModel();
    }
    

    // -- persistence


    /**
     * {@inheritDoc}
     */
    @Programmatic
    @Override
    public void persist(final Object domainObject) {
        final ObjectAdapter adapter = getObjectAdapterProvider().adapterFor(unwrapped(domainObject));

        if(adapter == null) {
            throw new PersistFailedException("Object not known to framework; instantiate using newTransientInstance(...) rather than simply new'ing up.");
        }
        if (adapter.isParentedCollection()) {
            // TODO check aggregation is supported
            return;
        }
        if (repositoryService.isPersistent(domainObject)) {
            throw new PersistFailedException("Object already persistent; OID=" + adapter.getOid());
        }
        persistenceSessionServiceInternal.makePersistent(adapter);
    }


    




    // //////////////////////////////////////////////////////////////////




    // -- helpers

    private Object unwrapped(Object domainObject) {
        return wrapperFactory != null ? wrapperFactory.unwrap(domainObject) : domainObject;
    }
    
    private ObjectAdapterProvider getObjectAdapterProvider() {
        return persistenceSessionServiceInternal;
    }

    // -- service dependencies

    @javax.inject.Inject
    SpecificationLoader specificationLoader;

    @javax.inject.Inject
    RepositoryService repositoryService;

    @javax.inject.Inject
    WrapperFactory wrapperFactory;

    @javax.inject.Inject
    PersistenceSessionServiceInternal persistenceSessionServiceInternal;

    

}
