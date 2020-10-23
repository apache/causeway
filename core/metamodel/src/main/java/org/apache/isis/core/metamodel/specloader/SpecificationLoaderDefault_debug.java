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
package org.apache.isis.core.metamodel.specloader;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.Logger;

import org.apache.isis.core.metamodel.spec.ObjectSpecification;

import lombok.val;

final class SpecificationLoaderDefault_debug {

    static void logBefore(
            Logger log, 
            SpecificationCache<ObjectSpecification> cache, 
            List<? extends ObjectSpecification> scanned) {
        
        if(!log.isDebugEnabled()) {
            return;
        }
        
        val cached = cache.snapshotSpecs();
        
        log.debug(String.format(
                "scanned.size = %d ; cached.size = %d",
                scanned.size(), cached.size()));

        val registryNotCached = scanned.stream()
                .filter(spec -> !cached.contains(spec))
                .collect(Collectors.toList());
        val cachedNotRegistry = cached.stream()
                .filter(spec -> !scanned.contains(spec))
                .collect(Collectors.toList());

        log.debug(String.format(
                "registryNotCached.size = %d ; cachedNotRegistry.size = %d",
                registryNotCached.size(), cachedNotRegistry.size()));
    }

    static void logAfter(
            Logger log, 
            SpecificationCache<ObjectSpecification> cache, 
            Collection<? extends ObjectSpecification> scanned) {
        
        if(!log.isDebugEnabled()) {
            return;
        }

        val cached = cache.snapshotSpecs();
        val cachedAfterNotBefore = cached.stream()
                .filter(spec -> !scanned.contains(spec))
                .collect(Collectors.toList());
        
        log.debug(String.format(
                "cachedSpecificationsAfter.size = %d ; cachedAfterNotBefore.size = %d",
                cached.size(), cachedAfterNotBefore.size()));
    }

}
