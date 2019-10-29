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

import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import javax.inject.Inject;

import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.services.registry.ServiceRegistry;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.internal.base._Lazy;
import org.apache.isis.commons.internal.base._NullSafe;
import org.apache.isis.commons.internal.collections._Maps;
import org.apache.isis.commons.internal.environment.IsisSystemEnvironment;
import org.apache.isis.commons.internal.ioc.ManagedBeanAdapter;
import org.apache.isis.commons.internal.ioc.spring._Spring;
import org.apache.isis.config.registry.IsisBeanTypeRegistry;

import lombok.val;

/**
 * @since 2.0
 */
@DomainService(nature = NatureOfService.DOMAIN)
public final class ServiceRegistryDefault implements ServiceRegistry {
    
    // enforces provisioning order (this is a depends-on relationship) 
    @Inject private IsisSystemEnvironment isisSystemEnvironment; 
    
    @Override
    public Optional<ManagedBeanAdapter> lookupRegisteredBeanById(String id) {
        return Optional.ofNullable(managedBeansById.get().get(id));
    }

    @Override
    public Stream<ManagedBeanAdapter> streamRegisteredBeans() {
        return managedBeansById.get().values().stream();
    }
    
    @Override
    public <T> Can<T> select(Class<T> type, Annotation[] qualifiers) {
        return isisSystemEnvironment.getIocContainer()
                .select(type, _Spring.filterQualifiers(qualifiers));
    }
    
    // -- HELPER

    private final _Lazy<Map<String, ManagedBeanAdapter>> managedBeansById = 
            _Lazy.threadSafe(this::enumerateManagedBeans);

    private Map<String, ManagedBeanAdapter> enumerateManagedBeans() {
        
        val filter = IsisBeanTypeRegistry.current();
        val managedBeanAdapterByName = _Maps.<String, ManagedBeanAdapter>newHashMap();

        isisSystemEnvironment.getIocContainer().streamAllBeans()
        .filter(_NullSafe::isPresent)
        .filter(bean->filter.isManagedBean(bean.getBeanClass())) // do not register unknown sort
        .forEach(bean->{
            val id = bean.getId();
            managedBeanAdapterByName.put(id, bean);
        });

        return managedBeanAdapterByName;
    }



}
