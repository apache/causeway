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
package org.apache.causeway.core.metamodel._testing;

import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

import org.apache.causeway.applib.id.LogicalType;
import org.apache.causeway.applib.services.registry.ServiceRegistry;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.internal.assertions._Assert;
import org.apache.causeway.commons.internal.base._Casts;
import org.apache.causeway.commons.internal.base._NullSafe;
import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.commons.internal.collections._Maps;
import org.apache.causeway.commons.internal.collections._Sets;
import org.apache.causeway.commons.internal.context._Context;
import org.apache.causeway.commons.internal.exceptions._Exceptions;
import org.apache.causeway.commons.internal.ioc._IocContainer;
import org.apache.causeway.commons.internal.ioc._SingletonBeanProvider;
import org.apache.causeway.core.metamodel.context.MetaModelContext;

import lombok.Getter;
import org.jspecify.annotations.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@RequiredArgsConstructor
class ServiceRegistry_forTesting implements ServiceRegistry {

    @NonNull private final MetaModelContext metaModelContext;

    @Getter @Setter private _IocContainer iocContainer;
    private final Set<_SingletonBeanProvider> registeredBeans = _Sets.newHashSet();

    @Override
    public <T> Can<T> select(final Class<T> type, final Annotation[] qualifiers) {

        if(iocContainer!=null) {
            return iocContainer.select(type, qualifiers);
        }

// ignore
//        if(qualifiers!=null && qualifiers.length>0) {
//            throw _Exceptions.notImplemented();
//        }

        Optional<T> match = streamBeans()
                .filter(_SingletonBeanProvider.satisfying(type))
                .map(_SingletonBeanProvider::getInstanceElseFail)
                .map(_Casts::<T>uncheckedCast)
                .findFirst();

        if(match.isPresent()) {
            return Can.ofSingleton(match.get());
        }

        // lookup the _Context
        // XXX lombok bug, cannot use var here (https://github.com/rzwitserloot/lombok/issues/1588)
        T singleton = _Context.getIfAny(type);
        if(singleton!=null) {
            return Can.ofSingleton(singleton);
        }

        return Can.empty();
    }

    private final Map<String, _SingletonBeanProvider> registeredBeanById = _Maps.newHashMap();

    @Override
    public Stream<_SingletonBeanProvider> streamRegisteredBeans() {
        return registeredBeans().stream();
    }

    @Override
    public Optional<_SingletonBeanProvider> lookupRegisteredBeanById(final LogicalType id) {
        return Optional.ofNullable(registeredBeanById.get(id.logicalName()));
    }

    @Override
    public Optional<?> lookupBeanById(final String id) {
        throw _Exceptions.notImplemented();
    }

    void invalidateRegisteredBeans() {
        synchronized(registeredBeans) {
            registeredBeans.clear();
        }
        streamRegisteredBeans().count();
    }

    // -- HELPER

    private Set<_SingletonBeanProvider> registeredBeans() {

        AtomicBoolean triggerPostInit = new AtomicBoolean(false);

        synchronized(registeredBeans) {
            if(registeredBeans.isEmpty()) {
                streamBeans()
                .filter(_NullSafe::isPresent)
                .peek(bean->_Assert.assertTrue(_Strings.isNotEmpty(bean.id())))
                .forEach(bean->{
                    registeredBeans.add(bean);
                    registeredBeanById.put(bean.id(), bean);
                });
                triggerPostInit.set(true);
            }
        }

        if(triggerPostInit.getAndSet(false)) {
            postinitWhenTesting();
        }

        return registeredBeans;
    }

    private Stream<_SingletonBeanProvider> streamBeans() {
        // lookup the MetaModelContextBean's list of singletons
        var mmc = metaModelContext;
        if(mmc instanceof MetaModelContext_forTesting) {
            var mmcb = (MetaModelContext_forTesting) mmc;
            return mmcb.streamBeanAdapters();
        }
        return Stream.empty();
    }

    @Override
    public void clearRegisteredBeans() {
        var mmc = metaModelContext;
        if(mmc instanceof MetaModelContext_forTesting) {
            var mmcb = (MetaModelContext_forTesting) mmc;
            mmcb.clearRegisteredBeans();
        }
    }

    private void postinitWhenTesting() {
        var mmc = metaModelContext;
        if(mmc instanceof MetaModelContext_forTesting) {
            var mmcb = (MetaModelContext_forTesting) mmc;
            mmcb.runPostconstruct();
        }
    }

}
