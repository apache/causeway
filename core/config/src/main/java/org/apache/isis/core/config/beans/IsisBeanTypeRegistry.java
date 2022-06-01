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
package org.apache.isis.core.config.beans;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import org.apache.isis.applib.value.semantics.ValueSemanticsProvider;

/**
 * Holds the set of domain services, persistent entities and fixture scripts etc., but not values.
 * @since 2.0
 */
public interface IsisBeanTypeRegistry {

    Optional<IsisBeanMetaData> lookupIntrospectableType(Class<?> type);
    Stream<IsisBeanMetaData> streamIntrospectableTypes();

    Map<Class<?>, IsisBeanMetaData> getManagedBeansContributing();
    Map<Class<?>, IsisBeanMetaData> getEntityTypes();
    Map<Class<?>, IsisBeanMetaData> getMixinTypes();
    Map<Class<?>, IsisBeanMetaData> getViewModelTypes();
    /** discovered per {@code @Value} annotation (vs. registered using a {@link ValueSemanticsProvider})*/
    Map<Class<?>, IsisBeanMetaData> getDiscoveredValueTypes();

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
                .map(IsisBeanMetaData::getBeanName);
    }

    /**
     * Returns either 'JDO' or 'JPA' based on what {@link IsisBeanTypeClassifier} we find
     * registered with <i>Spring</i>.
     * Alternative implementations could be considered, however this works for now.
     */
    default PersistenceStack determineCurrentPersistenceStack() {
        return IsisBeanTypeClassifier.get().stream()
                .map(IsisBeanTypeClassifier::getClass)
                .map(Class::getSimpleName)
                .anyMatch(classifierName->classifierName.startsWith("Jdo"))
                ? PersistenceStack.JDO
                : PersistenceStack.JPA;
    }

}