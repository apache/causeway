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

package org.apache.isis.metamodel.services.registry;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import javax.inject.Inject;

import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.services.registry.ServiceRegistry;
import org.apache.isis.commons.internal.base._Lazy;
import org.apache.isis.commons.internal.base._NullSafe;
import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.commons.internal.collections._Maps;
import org.apache.isis.commons.internal.ioc.ManagedBeanAdapter;
import org.apache.isis.commons.internal.ioc.spring._Spring;
import org.apache.isis.commons.internal.reflection._Reflect;
import org.apache.isis.config.IsisConfigurationLegacy;
import org.apache.isis.config.registry.IsisBeanTypeRegistry;

import lombok.val;

/**
 * @since 2.0
 */
@DomainService(nature = NatureOfService.DOMAIN)
public final class ServiceRegistryDefault implements ServiceRegistry {
    
    //XXX to enforce provisioning order, a depends-on relationship; 
    // keep as long as IsisConfigurationLegacy is used 
    @Inject private IsisConfigurationLegacy isisConfigurationLegacy; 

    @Override
    public Optional<ManagedBeanAdapter> lookupRegisteredBeanById(String id) {
        return Optional.ofNullable(registeredBeansById.get().get(id));
    }

    @Override
    public Stream<ManagedBeanAdapter> streamRegisteredBeans() {
        return registeredBeansById.get().values().stream();
    }

    
    // -- HELPER

    private final _Lazy<Map<String, ManagedBeanAdapter>> registeredBeansById = 
            _Lazy.threadSafe(this::enumerateBeans);

    private Map<String, ManagedBeanAdapter> enumerateBeans() {

        val beanSortClassifier = IsisBeanTypeRegistry.current();
        val map = _Maps.<String, ManagedBeanAdapter>newHashMap();

        _Spring.streamAllBeans(beanSortClassifier)
        .filter(_NullSafe::isPresent)
        .filter(x->!x.getManagedObjectSort().isUnknown()) // do not register unknown sort
        .forEach(bean->{
            val id = extractObjectType(bean.getBeanClass()).orElse(bean.getId());
            map.put(id, bean);
        });

        return map;
    }

    //TODO[2112] this would be the responsibility of the specloader, but 
    // for now we use as very simple approach
    private Optional<String> extractObjectType(Class<?> type) {

        val aDomainService = _Reflect.getAnnotation(type, DomainService.class);
        if(aDomainService!=null) {
            val objectType = aDomainService.objectType();
            if(_Strings.isNotEmpty(objectType)) {
                return Optional.of(objectType); 
            }
            return Optional.empty(); // stop processing
        }

        val aDomainObject = _Reflect.getAnnotation(type, DomainObject.class);
        if(aDomainObject!=null) {
            val objectType = aDomainObject.objectType();
            if(_Strings.isNotEmpty(objectType)) {
                return Optional.of(objectType); 
            }
            return Optional.empty(); // stop processing
        }

        return Optional.empty();

    }


}
