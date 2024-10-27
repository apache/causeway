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
package org.apache.causeway.core.metamodel.services.registry;

import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.applib.id.LogicalType;
import org.apache.causeway.applib.services.registry.ServiceRegistry;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.internal.base._Lazy;
import org.apache.causeway.commons.internal.collections._Maps;
import org.apache.causeway.commons.internal.ioc._SingletonBeanProvider;
import org.apache.causeway.core.config.beans.CausewayBeanTypeRegistry;
import org.apache.causeway.core.config.environment.CausewaySystemEnvironment;
import org.apache.causeway.core.metamodel.CausewayModuleCoreMetamodel;

/**
 * Default implementation of {@link ServiceRegistry}.
 *
 * @since 1.x revised for 2.0 {@index}
 */
@Service
@Named(CausewayModuleCoreMetamodel.NAMESPACE + ".ServiceRegistryDefault")
@Priority(PriorityPrecedence.MIDPOINT)
@Qualifier("Default")
public final class ServiceRegistryDefault implements ServiceRegistry {

    // enforces provisioning order (this is a depends-on relationship)
    @Inject private CausewaySystemEnvironment causewaySystemEnvironment;
    @Inject private CausewayBeanTypeRegistry causewayBeanTypeRegistry;

    @Override
    public Optional<_SingletonBeanProvider> lookupRegisteredBeanById(final LogicalType id) {
        return Optional.ofNullable(contributingDomainServicesById.get().get(id.getLogicalTypeName()));
    }

    @Override
    public Optional<?> lookupBeanById(final String id) {
        return causewaySystemEnvironment.getIocContainer().lookupBean(id);
    }

    @Override
    public Stream<_SingletonBeanProvider> streamRegisteredBeans() {
        return contributingDomainServicesById.get().values().stream();
    }

    @Override
    public <T> Can<T> select(final Class<T> type, final Annotation[] qualifiers) {
        var iocContainer = causewaySystemEnvironment.getIocContainer();
        return iocContainer
                .select(type, qualifiers);
    }

    @Override
    public void clearRegisteredBeans() {
        contributingDomainServicesById.clear();
    }

    // -- HELPER

    private final _Lazy<Map<String, _SingletonBeanProvider>> contributingDomainServicesById =
            _Lazy.threadSafe(this::enumerateContributingDomainServices);

    private Map<String, _SingletonBeanProvider> enumerateContributingDomainServices() {
        var managedBeanAdapterByName = _Maps.<String, _SingletonBeanProvider>newHashMap();

        causewaySystemEnvironment.getIocContainer()
        .streamAllBeans()
        .filter(contributes())
        .forEach(singletonProvider->
            managedBeanAdapterByName.put(singletonProvider.getId(), singletonProvider));

        return managedBeanAdapterByName;
    }

    private Predicate<_SingletonBeanProvider> contributes() {
        var managedBeansContributing = causewayBeanTypeRegistry.getManagedBeansContributing().keySet();
        return singletonProvider->singletonProvider!=null
                ? managedBeansContributing.contains(singletonProvider.getBeanClass()) // do not register unknown sort
                : false;
    }

}
