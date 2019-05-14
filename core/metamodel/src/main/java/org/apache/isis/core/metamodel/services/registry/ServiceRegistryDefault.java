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

package org.apache.isis.core.metamodel.services.registry;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import javax.inject.Singleton;

import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.services.registry.ServiceRegistry;
import org.apache.isis.commons.internal.base._NullSafe;
import org.apache.isis.commons.internal.collections._Sets;
import org.apache.isis.commons.internal.spring._Spring;
import org.apache.isis.commons.ioc.BeanAdapter;
import org.apache.isis.core.commons.collections.Bin;

/**
 * @since 2.0.0
 */
@Singleton
public final class ServiceRegistryDefault implements ServiceRegistry {

    private final Set<BeanAdapter> registeredBeans = _Sets.newHashSet();
    private final Set<Object> serviceCache = _Sets.newHashSet();

    @Override
    public boolean isDomainServiceType(Class<?> cls) {
        if(cls.isAnnotationPresent(DomainService.class)) {
            return true;
        }
        return false;
    }

    @Override
    public Stream<BeanAdapter> streamRegisteredBeans() {
        if(registeredBeans.isEmpty()) {
            _Spring.streamAllBeans(this::isDomainServiceType)
            .filter(_NullSafe::isPresent)
            .forEach(registeredBeans::add);
        }
        return registeredBeans.stream();
    }

    @Override
    @Deprecated //FIXME [2033] this is bad, we should not even need to do this; root problem are ObjectAdapters requiring pojos
    public Stream<Object> streamServices() {
        if(serviceCache.isEmpty()) {
            streamRegisteredBeans()
            .filter(BeanAdapter::isDomainService)
            .map(BeanAdapter::getInstance)
            .filter(Bin::isCardinalityOne)
            .map(Bin::getSingleton)
            .map(Optional::get)
            .forEach(serviceCache::add);
        }
        return serviceCache.stream();
    }

    @Override
    public boolean isRegisteredBean(Class<?> cls) {
        //FIXME [2033] this is poorly implemented, should not require service objects.
        return streamServices()
        .anyMatch(obj->obj.getClass().equals(cls));
    }

}
