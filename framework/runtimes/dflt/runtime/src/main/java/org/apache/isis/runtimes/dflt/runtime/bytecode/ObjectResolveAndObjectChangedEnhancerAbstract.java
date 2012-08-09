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

package org.apache.isis.runtimes.dflt.runtime.bytecode;

import static org.apache.isis.core.commons.ensure.Ensure.ensureThatArg;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;

import java.lang.reflect.Method;

import org.apache.isis.core.metamodel.facets.ImperativeFacet;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.SpecificationLoaderSpi;
import org.apache.isis.core.metamodel.specloader.specimpl.dflt.ObjectSpecificationDefault;
import org.apache.isis.runtimes.dflt.runtime.persistence.objectfactory.ObjectChanger;
import org.apache.isis.runtimes.dflt.runtime.persistence.objectfactory.ObjectResolver;

public abstract class ObjectResolveAndObjectChangedEnhancerAbstract {

    protected final ObjectResolver objectResolver;
    protected final ObjectChanger objectChanger;
    protected final SpecificationLoaderSpi specificationLoader;

    public ObjectResolveAndObjectChangedEnhancerAbstract(final ObjectResolver objectResolver, final ObjectChanger objectChanger, final SpecificationLoaderSpi specificationLoader) {
        ensureThatArg(objectResolver, is(notNullValue()));
        ensureThatArg(objectChanger, is(notNullValue()));
        ensureThatArg(specificationLoader, is(notNullValue()));

        this.objectResolver = objectResolver;
        this.objectChanger = objectChanger;
        this.specificationLoader = specificationLoader;
    }

    /**
     * Subclasses should call from their constructor, and setup their
     * implementation-specific callback mechanism.
     */
    protected abstract void createCallback();

    protected ObjectSpecificationDefault getJavaSpecificationOfOwningClass(final Method method) {
        return getJavaSpecification(method.getDeclaringClass());
    }

    protected ObjectSpecificationDefault getJavaSpecification(final Class<?> cls) {
        final ObjectSpecification nos = getSpecification(cls);
        if (!(nos instanceof ObjectSpecificationDefault)) {
            throw new UnsupportedOperationException("Only Java is supported (specification is '" + nos.getClass().getCanonicalName() + "')");
        }
        return (ObjectSpecificationDefault) nos;
    }

    protected boolean impliesResolve(final ImperativeFacet[] imperativeFacets) {
        for (final ImperativeFacet imperativeFacet : imperativeFacets) {
            if (imperativeFacet.impliesResolve()) {
                return true;
            }
        }
        return false;
    }

    protected boolean impliesObjectChanged(final ImperativeFacet[] imperativeFacets) {
        for (final ImperativeFacet imperativeFacet : imperativeFacets) {
            if (imperativeFacet.impliesObjectChanged()) {
                return true;
            }
        }
        return false;
    }

    private ObjectSpecification getSpecification(final Class<?> type) {
        return specificationLoader.loadSpecification(type);
    }

    // /////////////////////////////////////////////////////////////
    // Dependencies (from constructor)
    // /////////////////////////////////////////////////////////////

    public final ObjectResolver getObjectResolver() {
        return objectResolver;
    }

    public final ObjectChanger getObjectChanger() {
        return objectChanger;
    }

    public final SpecificationLoaderSpi getSpecificationLoader() {
        return specificationLoader;
    }

}