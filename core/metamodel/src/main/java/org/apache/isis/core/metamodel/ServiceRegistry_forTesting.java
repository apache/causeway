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
package org.apache.isis.core.metamodel;

import java.lang.annotation.Annotation;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import org.apache.isis.applib.services.registry.ServiceRegistry;
import org.apache.isis.commons.internal.base._Casts;
import org.apache.isis.commons.internal.base._NullSafe;
import org.apache.isis.commons.internal.collections._Sets;
import org.apache.isis.commons.internal.context._Context;
import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.commons.internal.spring._Spring;
import org.apache.isis.commons.ioc.BeanAdapter;
import org.apache.isis.core.commons.collections.Bin;
import org.apache.isis.core.metamodel.services.registry.ServiceRegistryDefault;

import lombok.Builder;
import lombok.Value;
import lombok.val;

class ServiceRegistry_forTesting implements ServiceRegistry {
    
    private final Set<BeanAdapter> registeredBeans = _Sets.newHashSet();
    private final ServiceRegistry delegate = new ServiceRegistryDefault(); 
    
    @Override
    public <T> Bin<T> select(Class<T> type, Annotation[] qualifiers) {
        
        if(_Spring.isContextAvailable()) {
            return ServiceRegistry.super.select(type, qualifiers);
        }
        
        if(qualifiers!=null && qualifiers.length>0) {
            throw _Exceptions.notImplemented();
        }

        Optional<T> match = streamSingletons()
        .filter(singleton->type.isAssignableFrom(singleton.getClass()))
        .map(_Casts::<T>uncheckedCast)
        .findFirst();
            
        if(match.isPresent()) {
            return Bin.ofSingleton(match.get());
        }
        
        // lookup the _Context 
        // XXX lombok bug, cannot use val here (https://github.com/rzwitserloot/lombok/issues/1588)
        T singleton = _Context.getIfAny(type);
        if(singleton!=null) {
            return Bin.ofSingleton(singleton);
        }
        
        return Bin.empty();
    }

    @Override
    public boolean isDomainServiceType(Class<?> cls) {
        return delegate.isDomainServiceType(cls);
    }

    @Override
    public Stream<BeanAdapter> streamRegisteredBeans() {
        return registeredBeans().stream();
    }

    @Override
    public Stream<Object> streamServices() {
        throw _Exceptions.notImplemented();
    }

    @Override
    public boolean isRegisteredBean(Class<?> cls) {
        throw _Exceptions.notImplemented();
    }

    // -- HELPER
    
    private Set<BeanAdapter> registeredBeans() {
        if(registeredBeans.isEmpty()) {
            streamSingletons()
            .map(this::toBeanAdapter)
            .filter(_NullSafe::isPresent)
            .forEach(registeredBeans::add);
        }
        return registeredBeans;
    }
    
    private Stream<Object> streamSingletons() {
        // lookup the MetaModelContextBean's list of singletons
        val mmc = MetaModelContext.current();
        if(mmc instanceof MetaModelContext_forTesting) {
            val mmcb = (MetaModelContext_forTesting) mmc;
            return mmcb.streamSingletons();
        }
        return Stream.empty();
    }
    
    @Value @Builder
    private static class PojoBeanAdapter implements BeanAdapter {
        
        String id;
        Bin<?> instance;
        public Class<?> beanClass;
        boolean domainService;
        
        @Override
        public boolean isCandidateFor(Class<?> requiredType) {
            throw _Exceptions.notImplemented();
        }
        
    }
    
    private BeanAdapter toBeanAdapter(Object singleton) {
        
        return PojoBeanAdapter.builder()
                .id(singleton.getClass().getName())
                .instance(Bin.ofSingleton(singleton))
                .beanClass(singleton.getClass())
                .domainService(isDomainServiceType(singleton.getClass()))
                .build();
        
        
    }

}
