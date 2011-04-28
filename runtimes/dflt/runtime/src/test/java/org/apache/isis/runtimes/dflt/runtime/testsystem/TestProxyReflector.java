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

package org.apache.isis.runtimes.dflt.runtime.testsystem;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.google.inject.internal.Maps;

import org.apache.isis.core.commons.debug.DebugBuilder;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.runtimecontext.RuntimeContext;
import org.apache.isis.core.metamodel.services.ServicesInjector;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.SpecificationLoaderAware;
import org.apache.isis.core.metamodel.specloader.ObjectReflector;
import org.apache.isis.core.metamodel.specloader.classsubstitutor.ClassSubstitutor;
import org.apache.isis.core.metamodel.specloader.speccache.SpecificationCache;
import org.apache.isis.core.metamodel.testspec.TestProxySpecification;
import org.apache.isis.runtimes.dflt.runtime.system.persistence.ObjectFactory;
import org.apache.isis.runtimes.dflt.runtime.system.persistence.PersistenceSession;

public class TestProxyReflector implements ObjectReflector {

    private final Map<String, ObjectSpecification> specificationByFullName =
        Maps.newHashMap();

    private final ObjectFactory objectFactory = new TestObjectFactory();
    private final ClassSubstitutor classSubstitutor = new TestClassSubstitutor();

    public TestProxyReflector() {

    }

    @Override
    public void init() {
    }

    @Override
    public void shutdown() {
    }

    @Override
    public Collection<ObjectSpecification> allSpecifications() {
        return Collections.unmodifiableCollection(specificationByFullName.values());
    }

    @Override
    public void debugData(final DebugBuilder debug) {
        final Collection<ObjectSpecification> list = allSpecifications();
        for (ObjectSpecification objectSpecification : list) {
            debug.appendln(objectSpecification.getFullIdentifier());
        }
    }

    @Override
    public String debugTitle() {
        return null;
    }

    public void installServiceSpecification(final Class<?> class1) {
    }

    @Override
    public ObjectSpecification loadSpecification(final Class<?> type) {
        return loadSpecification(type.getName());
    }

    @Override
    public ObjectSpecification loadSpecification(final String name) {
        if (specificationByFullName.containsKey(name)) {
            return specificationByFullName.get(name);
        } else {
            final TestProxySpecification specification = new TestProxySpecification(name);
            specificationByFullName.put(specification.getFullIdentifier(), specification);
            return specification;

            // throw new ObjectAdapterRuntimeException("no specification for " + name);
        }
    }

    public ObjectAdapter createCollectionAdapter(final Object collection, final ObjectSpecification elementSpecification) {
        return null;
    }

    public ServicesInjector createServicesInjector() {
        return new ServicesInjectorNoop();
    }

    public void addSpecification(final ObjectSpecification specification) {
        specificationByFullName.put(specification.getFullIdentifier(), specification);
    }

    public ObjectFactory getObjectFactory() {
        return objectFactory;
    }

    public void setCache(SpecificationCache cache) {
        // ignored.
    }

    public void setObjectPersistor(PersistenceSession objectPersistor) {
        // ignored.
    }

    @Override
    public boolean loaded(Class<?> cls) {
        return false;
    }

    @Override
    public boolean loaded(String fullyQualifiedClassName) {
        return false;
    }

    @Override
    public void injectInto(Object candidate) {
        if (SpecificationLoaderAware.class.isAssignableFrom(candidate.getClass())) {
            SpecificationLoaderAware cast = SpecificationLoaderAware.class.cast(candidate);
            cast.setSpecificationLoader(this);
        }
    }

    protected ClassSubstitutor getClassSubstitutor() {
        return classSubstitutor;
    }

    @Override
    public void setRuntimeContext(RuntimeContext runtimeContext) {
        // ignored
    }

    @Override
    public void setServiceClasses(List<Class<?>> serviceClasses) {
        // ignored.
    }

    @Override
    public boolean loadSpecifications(List<Class<?>> typesToLoad, Class<?> typeToIgnore) {
        return false;
    }

    @Override
    public boolean loadSpecifications(List<Class<?>> typesToLoad) {
        return false;
    }

}
