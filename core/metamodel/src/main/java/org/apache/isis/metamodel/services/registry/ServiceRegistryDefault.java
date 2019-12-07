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
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.isis.applib.annotation.OrderPrecedence;
import org.apache.isis.applib.services.registry.ServiceRegistry;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.internal.base._Lazy;
import org.apache.isis.commons.internal.base._NullSafe;
import org.apache.isis.commons.internal.collections._Maps;
import org.apache.isis.commons.internal.environment.IsisSystemEnvironment;
import org.apache.isis.commons.internal.ioc.ManagedBeanAdapter;
import org.apache.isis.commons.internal.ioc.spring._Spring;
import org.apache.isis.config.beans.IsisBeanTypeRegistryHolder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import lombok.extern.log4j.Log4j2;
import lombok.val;

@Service
@Named("isisMetaModel.ServiceRegistryDefault")
@Order(OrderPrecedence.MIDPOINT)
@Primary
@Qualifier("Default")
@Log4j2
public final class ServiceRegistryDefault implements ServiceRegistry {
    
    // enforces provisioning order (this is a depends-on relationship) 
    @Inject private IsisSystemEnvironment isisSystemEnvironment; 
    @Inject private IsisBeanTypeRegistryHolder isisBeanTypeRegistryHolder;
    
    @Override
    public Optional<ManagedBeanAdapter> lookupRegisteredBeanById(String id) {
        return Optional.ofNullable(managedBeansById.get().get(id));
    }

    @Override
    public Optional<?> lookupBeanById(final String id) {
        return isisSystemEnvironment.getIocContainer().lookupById(id);
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
        
        val beanTypeRegistry = isisBeanTypeRegistryHolder.getIsisBeanTypeRegistry();
        val managedBeanAdapterByName = _Maps.<String, ManagedBeanAdapter>newHashMap();

        isisSystemEnvironment.getIocContainer().streamAllBeans()
        .filter(_NullSafe::isPresent)
        .filter(bean->beanTypeRegistry.isManagedBean(bean.getBeanClass())) // do not register unknown sort
        .forEach(bean->{
            val id = bean.getId();
            managedBeanAdapterByName.put(id, bean);
        });

        return managedBeanAdapterByName;
    }



}
