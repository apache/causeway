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
package org.apache.causeway.core.metamodel.services.grid;

import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import org.apache.causeway.applib.layout.grid.bootstrap.BSGrid;
import org.apache.causeway.applib.services.grid.GridService.LayoutKey;
import org.apache.causeway.commons.functional.Try;

import lombok.extern.slf4j.Slf4j;

/**
 * Cache for {@link BSGrid} instances,.
 *
 * @since 4.0
 */
@Slf4j
record GridCache(
    ConcurrentMapWrapper<LayoutKey, Try<BSGrid>> gridsByKey) {

    public GridCache(final GridLoadingContext gridLoadingContext) {
        this(new ConcurrentMapWrapper<>(new ConcurrentHashMap<>()));
    }

    /**
     * To support metamodel invalidation/rebuilding of spec.
     */
    public void remove(final Class<?> domainClass) {
        gridsByKey.map().entrySet().removeIf(entry->entry.getKey().domainClass().equals(domainClass));
    }

    public Try<BSGrid> computeIfAbsent(final LayoutKey layoutKey, final Function<LayoutKey, Try<BSGrid>> factory) {
        return gridsByKey.computeIfAbsent(layoutKey, factory);
    }

    /* when using ConcurrentHashMap directly we may see
    java.lang.IllegalStateException: Recursive update
    at java.base/java.util.concurrent.ConcurrentHashMap.computeIfAbsent(ConcurrentHashMap.java:1779)
    at org.apache.causeway.core.metamodel.services.grid.GridCache.computeIfAbsent(GridCache.java:52)
    at org.apache.causeway.core.metamodel.services.grid.GridServiceDefault.load(GridServiceDefault.java:95)
    at org.apache.causeway.core.metamodel.facets.object.grid.BSGridFacet.load(BSGridFacet.java:117)
    at org.apache.causeway.core.metamodel.facets.object.grid.BSGridFacet.lambda$normalized$0(BSGridFacet.java:86)
    at java.base/java.util.concurrent.ConcurrentHashMap.compute(ConcurrentHashMap.java:1932)
    at org.apache.causeway.core.metamodel.facets.object.grid.BSGridFacet.normalized(BSGridFacet.java:82)
    at org.apache.causeway.core.metamodel.facets.object.grid.BSGridFacet.getGrid(BSGridFacet.java:68)
    at org.apache.causeway.core.metamodel.util.Facets.lambda$gridPreload$0(Facets.java:204)
    at java.base/java.util.Optional.ifPresent(Optional.java:178)
    at org.apache.causeway.core.metamodel.util.Facets.gridPreload(Facets.java:200)
    at org.apache.causeway.core.metamodel.spec.impl.ObjectSpecificationDefault.introspectFully(ObjectSpecificationDefault.java:640)
    at org.apache.causeway.core.metamodel.spec.impl.ObjectSpecificationDefault.introspectUpTo(ObjectSpecificationDefault.java:615)
    at org.apache.causeway.core.metamodel.spec.impl.ObjectSpecificationDefault.streamDeclaredAssociations(ObjectSpecificationDefault.java:1000)
    at org.apache.causeway.core.metamodel.spec.impl.ObjectMemberContainer.streamAssociations(ObjectMemberContainer.java:127)
    at org.apache.causeway.core.metamodel.spec.impl.ObjectMemberContainer.streamAssociations(ObjectMemberContainer.java:128)
    at org.apache.causeway.core.metamodel.spec.feature.ObjectAssociationContainer.streamProperties(ObjectAssociationContainer.java:118)
    at org.apache.causeway.core.metamodel.services.grid.ObjectMemberResolverForGrid.validateAndNormalize(ObjectMemberResolverForGrid.java:148)
    at org.apache.causeway.core.metamodel.services.grid.ObjectMemberResolverForGrid.resolve(ObjectMemberResolverForGrid.java:115)
    at org.apache.causeway.core.metamodel.services.grid.GridServiceDefault.lambda$tryLoadNoCache$7(GridServiceDefault.java:110)
    at java.base/java.util.Optional.orElseGet(Optional.java:364)
    at org.apache.causeway.core.metamodel.services.grid.GridServiceDefault.lambda$tryLoadNoCache$6(GridServiceDefault.java:110)
    at org.springframework.util.function.ThrowingFunction.apply(ThrowingFunction.java:63)
    at org.springframework.util.function.ThrowingFunction.apply(ThrowingFunction.java:51)
    ..
    at org.apache.causeway.core.metamodel.services.grid.GridServiceDefault.tryLoadNoCache(GridServiceDefault.java:110)
    at java.base/java.util.concurrent.ConcurrentHashMap.computeIfAbsent(ConcurrentHashMap.java:1724)
    at org.apache.causeway.core.metamodel.services.grid.GridCache.computeIfAbsent(GridCache.java:52)
    at org.apache.causeway.core.metamodel.services.grid.GridServiceDefault.load(GridServiceDefault.java:95)
    at org.apache.causeway.core.metamodel.facets.object.grid.BSGridFacet.load(BSGridFacet.java:117)
    at org.apache.causeway.core.metamodel.facets.object.grid.BSGridFacet.lambda$normalized$0(BSGridFacet.java:86)
    at java.base/java.util.concurrent.ConcurrentHashMap.compute(ConcurrentHashMap.java:1932)
    at org.apache.causeway.core.metamodel.facets.object.grid.BSGridFacet.normalized(BSGridFacet.java:82)
    at org.apache.causeway.core.metamodel.facets.object.grid.BSGridFacet.getGrid(BSGridFacet.java:68)
    ..
    at org.apache.causeway.core.metamodel.util.Facets.gridPreload(Facets.java:200)*/
    record ConcurrentMapWrapper<K, V>(ConcurrentHashMap<K, V> map) {

        public V computeIfAbsent(final K key, final java.util.function.Function<? super K, ? extends V> mappingFunction) {
            // Use an atomic flag to track whether a recursive call is currently happening.
            // It ensures that multiple threads can manage their recursion state independently.
            final ThreadLocal<Boolean> isRecursing = ThreadLocal.withInitial(() -> false);

            // Define a recursive compute function
            return map.compute(key, (k, v) -> {
                if (isRecursing.get()) return v; // return current value if already in recursion

                isRecursing.set(true); // mark as in recursion
                try {
                    return v == null ? mappingFunction.apply(k) : v;
                } finally {
                    isRecursing.remove(); // clean up the flag variable
                }
            });
        }
    }

}
