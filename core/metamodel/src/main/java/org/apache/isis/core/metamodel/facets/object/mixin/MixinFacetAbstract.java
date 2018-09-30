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

package org.apache.isis.core.metamodel.facets.object.mixin;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

import org.apache.isis.applib.services.title.TitleService;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.ObjectAdapterProvider;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.SingleValueFacetAbstract;
import org.apache.isis.core.metamodel.services.ServicesInjector;

public abstract class MixinFacetAbstract extends SingleValueFacetAbstract<String> implements MixinFacet {

    private final Class<?> mixinType;
    private final Class<?> constructorType;
    private final ServicesInjector servicesInjector;

    public static Class<? extends Facet> type() {
        return MixinFacet.class;
    }

    public MixinFacetAbstract(
            final Class<?> mixinType,
            final String value, final Class<?> constructorType,
            final FacetHolder holder,
            final ServicesInjector servicesInjector) {
        super(type(), value, holder);
        this.mixinType = mixinType;
        this.constructorType = constructorType;
        this.servicesInjector = servicesInjector;
    }

    @Override
    public boolean isMixinFor(final Class<?> candidateDomainType) {
        if (candidateDomainType == null) {
            return false;
        }
        return constructorType.isAssignableFrom(candidateDomainType);
    }

    @Override
    public Object instantiate(final Object domainPojo) {
        if(domainPojo == null) {
            return null;
        }
        if(!constructorType.isAssignableFrom(domainPojo.getClass())) {
            // shouldn't happen; ought we to fail-fast instead?
            return null;
        }
        try {
            final Constructor<?> constructor = mixinType.getConstructor(constructorType);
            final Object mixinPojo = constructor.newInstance(domainPojo);
            servicesInjector.injectServicesInto(mixinPojo);
            return mixinPojo;
        } catch (NoSuchMethodException e) {
            // shouldn't happen; ought we to fail-fast instead?
            return null;
        } catch (InvocationTargetException e) {
            // shouldn't happen; ought we to fail-fast instead?
            return null;
        } catch (InstantiationException e) {
            // shouldn't happen; ought we to fail-fast instead?
            return null;
        } catch (IllegalAccessException e) {
            // shouldn't happen; ought we to fail-fast instead?
            return null;
        }
    }

    @Override
    public ObjectAdapter mixedIn(ObjectAdapter mixinAdapter, final Policy policy) {

        final Object mixin = mixinAdapter.getPojo();

        final Field[] declaredFields = mixinType.getDeclaredFields();
        for (final Field declaredField : declaredFields) {
            if(declaredField.getType().isAssignableFrom(constructorType)) {
                declaredField.setAccessible(true);
                try {
                    Object o = declaredField.get(mixin);
                    return getObjectAdapterProvider().adapterFor(o);
                } catch (IllegalAccessException e) {
                    if(policy == Policy.FAIL_FAST) {
                        throw new RuntimeException(
                                "Unable to access " + declaredField + " for " + getTitleService().titleOf(mixin));
                    }
                    // otherwise continue to next possible field.
                }
            }
        }
        if(policy == Policy.FAIL_FAST) {
            throw new RuntimeException(
                    "Could not find the \"mixed-in\" domain object within " + getTitleService().titleOf(mixin)
                    + " (tried to guess by looking at all private fields and matching one against the constructor parameter)");
        }
        // else just...
        return null;
    }

    private ObjectAdapterProvider getObjectAdapterProvider() {
        return servicesInjector.getPersistenceSessionServiceInternal();
    }

    private TitleService getTitleService() {
        return servicesInjector.lookupService(TitleService.class);
    }

}
