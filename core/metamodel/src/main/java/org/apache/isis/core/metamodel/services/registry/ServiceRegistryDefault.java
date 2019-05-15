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

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Singleton;

import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.services.registry.ServiceRegistry;
import org.apache.isis.commons.internal.base._Lazy;
import org.apache.isis.commons.internal.base._NullSafe;
import org.apache.isis.commons.internal.spring._Spring;
import org.apache.isis.commons.ioc.BeanAdapter;

/**
 * @since 2.0.0
 */
@Singleton
public final class ServiceRegistryDefault implements ServiceRegistry {

    private final _Lazy<Set<BeanAdapter>> registeredBeans = _Lazy.threadSafe(this::enumerateBeans);

    @Override
    public boolean isDomainServiceType(Class<?> cls) {
        if(cls.isAnnotationPresent(DomainService.class)) {
            return true;
        }
        return false;
    }

    @Override
    public Stream<BeanAdapter> streamRegisteredBeans() {
        return registeredBeans.get().stream();
    }

    
    // -- HELPER
    
    Set<BeanAdapter> enumerateBeans() {
        return _Spring.streamAllBeans(this::isDomainServiceType)
        .filter(_NullSafe::isPresent)
        .collect(Collectors.toCollection(HashSet::new));
    }

}
