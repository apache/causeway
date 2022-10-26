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
package org.apache.causeway.core.config.beans;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import org.apache.causeway.applib.value.semantics.ValueSemanticsProvider;

/**
 * Holds the set of domain services, persistent entities and fixture scripts etc., but not values.
 * @since 2.0
 */
public interface CausewayBeanTypeRegistry {

    Optional<CausewayBeanMetaData> lookupIntrospectableType(Class<?> type);
    Stream<CausewayBeanMetaData> streamIntrospectableTypes();

    Map<Class<?>, CausewayBeanMetaData> getManagedBeansContributing();
    Map<Class<?>, CausewayBeanMetaData> getEntityTypes();
    Map<Class<?>, CausewayBeanMetaData> getMixinTypes();
    Map<Class<?>, CausewayBeanMetaData> getViewModelTypes();
    /** discovered per {@code @Value} annotation (vs. registered using a {@link ValueSemanticsProvider})*/
    Map<Class<?>, CausewayBeanMetaData> getDiscoveredValueTypes();

    // -- LOOKUPS

    /**
     * If given type is part of the meta-model and is available for injection,
     * returns the <em>Managed Bean's</em> name (id) as
     * recognized by the IoC container.
     *
     * @param type
     */
    default Optional<String> lookupManagedBeanNameForType(final Class<?> type) {
        return Optional.ofNullable(getManagedBeansContributing().get(type))
                .map(CausewayBeanMetaData::getBeanName);
    }

    /**
     * Returns either 'JDO' or 'JPA' based on what {@link CausewayBeanTypeClassifier} we find
     * registered with <i>Spring</i>.
     * Alternative implementations could be considered, however this works for now.
     */
    default PersistenceStack determineCurrentPersistenceStack() {
        return CausewayBeanTypeClassifier.get().stream()
                .map(CausewayBeanTypeClassifier::getClass)
                .map(Class::getSimpleName)
                .anyMatch(classifierName->classifierName.startsWith("Jdo"))
                ? PersistenceStack.JDO
                : PersistenceStack.JPA;
    }

}