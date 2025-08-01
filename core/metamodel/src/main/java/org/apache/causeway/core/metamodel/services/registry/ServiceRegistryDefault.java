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

import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.applib.id.LogicalType;
import org.apache.causeway.applib.services.registry.ServiceRegistry;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.internal.base._Lazy;
import org.apache.causeway.commons.internal.collections._Maps;
import org.apache.causeway.commons.internal.ioc.SingletonBeanProvider;
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
    public Optional<SingletonBeanProvider> lookupRegisteredBeanById(final LogicalType id) {
        return Optional.ofNullable(contributingDomainServicesById.get().get(id.logicalName()));
    }

    @Override
    public Optional<?> lookupBeanById(final String id) {
        return causewaySystemEnvironment.springContextHolder().lookupBean(id);
    }

    @Override
    public Stream<SingletonBeanProvider> streamRegisteredBeans() {
        return contributingDomainServicesById.get().values().stream();
    }

    @Override
    public <T> Can<T> select(final Class<T> type, final Annotation[] qualifiers) {
        var iocContainer = causewaySystemEnvironment.springContextHolder();
        return iocContainer
                .select(type, qualifiers);
    }

    @Override
    public void clearRegisteredBeans() {
        contributingDomainServicesById.clear();
    }

    // -- HELPER

    private final _Lazy<Map<String, SingletonBeanProvider>> contributingDomainServicesById =
            _Lazy.threadSafe(this::enumerateContributingDomainServices);

    private Map<String, SingletonBeanProvider> enumerateContributingDomainServices() {
        var managedBeanAdapterByName = _Maps.<String, SingletonBeanProvider>newHashMap();

        causewaySystemEnvironment.springContextHolder()
            .streamAllBeans()
            .filter(contributes())
            .forEach(singletonProvider->
                managedBeanAdapterByName.put(singletonProvider.id(), singletonProvider));

        return managedBeanAdapterByName;
    }

    private Predicate<SingletonBeanProvider> contributes() {
        return singletonProvider->singletonProvider!=null
                ? causewayBeanTypeRegistry.containsManagedBeansContributing(singletonProvider.beanClass())
                // do not register unknown sort
                : false;
    }

}
