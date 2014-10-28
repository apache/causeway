/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.isis.core.metamodel.spec;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;

/**
 * Allows a {@link SpecificationLoader} to be provided even if the concrete
 * implementation is only available later.
 */
public class SpecificationLoaderDelegator extends SpecificationLoaderAbstract {

    private SpecificationLoader specificationLoaderDelegate;

    public void setDelegate(final SpecificationLoader specificationLoaderDelegate) {
        this.specificationLoaderDelegate = specificationLoaderDelegate;
    }

    @Override
    public ObjectSpecification loadSpecification(final Class<?> cls) {
        if (specificationLoaderDelegate == null) {
            throw new IllegalStateException("No SpecificationLookup provided");
        }
        return specificationLoaderDelegate.loadSpecification(cls);
    }

    @Override
    public Collection<ObjectSpecification> allSpecifications() {
        return specificationLoaderDelegate.allSpecifications();
    }

    @Override
    public ObjectSpecification lookupBySpecId(ObjectSpecId objectSpecId) {
        return specificationLoaderDelegate.lookupBySpecId(objectSpecId);
    }

    @Override
    public boolean loadSpecifications(List<Class<?>> typesToLoad, Class<?> typeToIgnore) {
        return specificationLoaderDelegate.loadSpecifications(typesToLoad, typeToIgnore);
    }

    @Override
    public ObjectSpecification loadSpecification(String fullyQualifiedClassName) {
        return specificationLoaderDelegate.loadSpecification(fullyQualifiedClassName);
    }

    @Override
    public boolean loaded(Class<?> cls) {
        return specificationLoaderDelegate.loaded(cls);
    }

    @Override
    public boolean loaded(String fullyQualifiedClassName) {
        return specificationLoaderDelegate.loaded(fullyQualifiedClassName);
    }

    @Override
    public boolean loadSpecifications(List<Class<?>> typesToLoad) {
        return specificationLoaderDelegate.loadSpecifications(typesToLoad);
    }

    @Override
    public ObjectSpecification introspectIfRequired(ObjectSpecification spec) {
        return specificationLoaderDelegate.introspectIfRequired(spec);
    }

    @Override
    public List<Class<?>> getServiceClasses() {
        return specificationLoaderDelegate.getServiceClasses();
    }

    @Override
    public void invalidateCache(Class<?> domainClass) {
        specificationLoaderDelegate.invalidateCache(domainClass);
    }

    @Override
    public boolean isInjectorMethodFor(Method method, Class<? extends Object> serviceClass) {
        return specificationLoaderDelegate.isInjectorMethodFor(method, serviceClass);
    }

    @Override
    public void injectInto(Object candidate) {
        super.injectInto(candidate);
        if(specificationLoaderDelegate != null) {
            specificationLoaderDelegate.injectInto(candidate);
        }
    }



}
