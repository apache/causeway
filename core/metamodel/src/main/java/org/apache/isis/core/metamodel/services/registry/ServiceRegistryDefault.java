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

import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import org.apache.isis.applib.annotation.PriorityPrecedence;
import org.apache.isis.applib.services.registry.ServiceRegistry;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.internal.base._Lazy;
import org.apache.isis.commons.internal.base._NullSafe;
import org.apache.isis.commons.internal.collections._Maps;
import org.apache.isis.commons.internal.ioc._ManagedBeanAdapter;
import org.apache.isis.core.config.beans.IsisBeanTypeRegistry;
import org.apache.isis.core.config.environment.IsisSystemEnvironment;

import lombok.val;

@Service
@Named("isis.metamodel.ServiceRegistryDefault")
@Priority(PriorityPrecedence.MIDPOINT)
@Qualifier("Default")
public final class ServiceRegistryDefault implements ServiceRegistry {

    // enforces provisioning order (this is a depends-on relationship)
    @Inject private IsisSystemEnvironment isisSystemEnvironment;
    @Inject private IsisBeanTypeRegistry isisBeanTypeRegistry;

    @Override
    public Optional<_ManagedBeanAdapter> lookupRegisteredBeanById(String id) {
        return Optional.ofNullable(managedBeansById.get().get(id));
    }

    @Override
    public Optional<?> lookupBeanById(final String id) {
        return isisSystemEnvironment.getIocContainer().lookupById(id);
    }

    @Override
    public Stream<_ManagedBeanAdapter> streamRegisteredBeans() {
        return managedBeansById.get().values().stream();
    }

    @Override
    public <T> Can<T> select(Class<T> type, Annotation[] qualifiers) {
        return isisSystemEnvironment.getIocContainer()
                .select(type, qualifiers);
    }

    // -- HELPER

    private final _Lazy<Map<String, _ManagedBeanAdapter>> managedBeansById =
            _Lazy.threadSafe(this::enumerateManagedBeans);

    private Map<String, _ManagedBeanAdapter> enumerateManagedBeans() {

        val managedBeanAdapterByName = _Maps.<String, _ManagedBeanAdapter>newHashMap();
        val managedBeansContributing = isisBeanTypeRegistry.getManagedBeansContributing().keySet();

        isisSystemEnvironment.getIocContainer().streamAllBeans()
        .filter(_NullSafe::isPresent)
        .filter(bean->managedBeansContributing.contains(bean.getBeanClass())) // do not register unknown sort
        .forEach(bean->{
            val id = bean.getId();
            managedBeanAdapterByName.put(id, bean);
        });

        return managedBeanAdapterByName;
    }

    @Override
    public void clearRegisteredBeans() {
        managedBeansById.clear();
    }



}
