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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

import org.apache.isis.applib.DomainObjectContainer;
import org.apache.isis.core.metamodel.services.container.DomainObjectContainerAware;
import org.apache.isis.core.metamodel.spec.ObjectSpecId;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.SpecificationLoader;
import org.apache.isis.core.metamodel.spec.SpecificationLoaderAware;
import org.apache.isis.core.metamodel.spec.SpecificationLookup;
import org.apache.isis.core.metamodel.spec.SpecificationLookupDelegator;

public abstract class RuntimeContextAbstract implements RuntimeContext, SpecificationLoaderAware, DomainObjectContainerAware {

    private final SpecificationLookupDelegator specificationLookupDelegator;
    private DomainObjectContainer container;
    private Properties properties;

    public RuntimeContextAbstract() {
        this.specificationLookupDelegator = new SpecificationLookupDelegator();
    }

    @Override
    public void init() {
    }

    @Override
    public void shutdown() {
    }


    @Override
    public void injectInto(final Object candidate) {
        if (RuntimeContextAware.class.isAssignableFrom(candidate.getClass())) {
            final RuntimeContextAware cast = RuntimeContextAware.class.cast(candidate);
            cast.setRuntimeContext(this);
        }
        injectSubcomponentsInto(candidate);
    }

    protected void injectSubcomponentsInto(final Object candidate) {
        getAdapterMap().injectInto(candidate);
        getAuthenticationSessionProvider().injectInto(candidate);
        getDependencyInjector().injectInto(candidate);
        getDomainObjectServices().injectInto(candidate);
        getLocalizationProvider().injectInto(candidate);
        getObjectInstantiator().injectInto(candidate);
        getObjectDirtier().injectInto(candidate);
        getObjectPersistor().injectInto(candidate);
        getQuerySubmitter().injectInto(candidate);
        getServicesProvider().injectInto(candidate);
        getSpecificationLookup().injectInto(candidate);
        
    }

    @Override
    public SpecificationLookup getSpecificationLookup() {
        return specificationLookupDelegator;
    }

    /**
     * Is injected into when the reflector is
     * {@link ObjectReflectorAbstract#init() initialized}.
     */
    @Override
    public void setSpecificationLoader(final SpecificationLoader specificationLoader) {
        this.specificationLookupDelegator.setDelegate(new SpecificationLookup() {

            @Override
            public void injectInto(final Object candidate) {
                specificationLoader.injectInto(candidate);
            }

            @Override
            public ObjectSpecification loadSpecification(final Class<?> cls) {
                return specificationLoader.loadSpecification(cls);
            }

            @Override
            public Collection<ObjectSpecification> allSpecifications() {
                return specificationLoader.allSpecifications();
            }

            @Override
            public ObjectSpecification lookupBySpecId(ObjectSpecId objectSpecId) {
                return specificationLoader.lookupBySpecId(objectSpecId);
            }
        });
    }

    protected DomainObjectContainer getContainer() {
        return container;
    }

    /**
     * So that {@link #injectDependenciesInto(Object)} can also inject the
     * {@link DomainObjectContainer}.
     */
    @Override
    public void setContainer(final DomainObjectContainer container) {
        this.container = container;
    }

    public void setProperties(final Properties properties) {
        this.properties = properties;
    }

    public String getProperty(final String name) {
        return properties.getProperty(name);
    }

    public List<String> getPropertyNames() {
        final List<String> list = new ArrayList<String>();
        for (final Object key : properties.keySet()) {
            list.add((String) key);
        }
        return list;
    }

}
