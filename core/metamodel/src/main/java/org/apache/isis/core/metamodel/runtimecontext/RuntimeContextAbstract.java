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

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import com.google.common.collect.Lists;
import org.apache.isis.core.metamodel.services.ServicesInjectorSpiDelegator;
import org.apache.isis.core.metamodel.spec.ObjectSpecId;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.SpecificationLoader;
import org.apache.isis.core.metamodel.spec.SpecificationLoaderDelegator;
import org.apache.isis.core.metamodel.spec.SpecificationLoaderSpi;
import org.apache.isis.core.metamodel.spec.SpecificationLoaderSpiAware;

public abstract class RuntimeContextAbstract implements RuntimeContext, SpecificationLoaderSpiAware, ServicesInjectorAware {

    private final SpecificationLoaderDelegator specificationLookupDelegator = new SpecificationLoaderDelegator();
    protected final ServicesInjectorSpiDelegator servicesInjectorDelegator = new ServicesInjectorSpiDelegator();

    private Properties properties;


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
        getAdapterManager().injectInto(candidate);
        getAuthenticationSessionProvider().injectInto(candidate);
        getServicesInjector().injectInto(candidate);
        getDomainObjectServices().injectInto(candidate);
        getLocalizationProvider().injectInto(candidate);
        getObjectInstantiator().injectInto(candidate);
        getObjectDirtier().injectInto(candidate);
        getObjectPersistor().injectInto(candidate);
        getQuerySubmitter().injectInto(candidate);
        getServicesProvider().injectInto(candidate);
        getSpecificationLoader().injectInto(candidate);
    }

    @Override
    public SpecificationLoader getSpecificationLoader() {
        return specificationLookupDelegator;
    }

    /**
     * Is injected into when the reflector is initialized.
     */
    @Override
    public void setSpecificationLoaderSpi(final SpecificationLoaderSpi specificationLoader) {
        this.specificationLookupDelegator.setDelegate(new SpecificationLoader() {

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

            @Override
            public ObjectSpecification loadSpecification(String fullyQualifiedClassName) {
                return specificationLoader.loadSpecification(fullyQualifiedClassName);
            }

            @Override
            public boolean loadSpecifications(List<Class<?>> typesToLoad) {
                return specificationLoader.loadSpecifications(typesToLoad);
            }

            @Override
            public boolean loadSpecifications(List<Class<?>> typesToLoad, Class<?> typeToIgnore) {
                return specificationLoader.loadSpecifications(typesToLoad, typeToIgnore);
            }

            @Override
            public boolean loaded(Class<?> cls) {
                return specificationLoader.loaded(cls);
            }

            @Override
            public boolean loaded(String fullyQualifiedClassName) {
                return specificationLoader.loaded(fullyQualifiedClassName);
            }

            @Override
            public ObjectSpecification introspectIfRequired(ObjectSpecification spec) {
                return specificationLoader.introspectIfRequired(spec);
            }

            @Override
            public List<Class<?>> getServiceClasses() {
                return specificationLoader.getServiceClasses();
            }

            @Override
            public void invalidateCache(Class<?> domainClass) {
                specificationLoader.invalidateCache(domainClass);
            }

            @Override
            public boolean isInjectorMethodFor(Method method, Class<? extends Object> serviceClass) {
                return specificationLoader.isInjectorMethodFor(method, serviceClass);
            }
        });
    }

    public void setProperties(final Properties properties) {
        this.properties = properties;
    }

    public String getProperty(final String name) {
        return properties.getProperty(name);
    }

    public List<String> getPropertyNames() {
        final List<String> list = Lists.newArrayList();
        for (final Object key : properties.keySet()) {
            list.add((String) key);
        }
        return list;
    }


    //region > injected services
    @Override
    public void setServicesInjector(ServicesInjector servicesInjector) {
        this.servicesInjectorDelegator.setServicesInjectorSpi((org.apache.isis.core.metamodel.services.ServicesInjectorSpi) servicesInjector);
    }
    //endregion

}
