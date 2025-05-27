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
package org.apache.causeway.core.metamodel.specloader;

import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.slf4j.Logger;

import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;

import lombok.experimental.UtilityClass;

@UtilityClass
final class _LogUtil {

    void logBefore(
            final Logger log,
            final Supplier<Can<ObjectSpecification>> snapshot,
            final List<? extends ObjectSpecification> scanned) {

        if(!log.isDebugEnabled()) {
            return;
        }

        var cached = snapshot.get();
        log.debug(String.format(
                "scanned.size = %d ; cached.size = %d",
                scanned.size(), cached.size()));

        var registryNotCached = scanned.stream()
                .filter(spec -> !cached.contains(spec))
                .collect(Collectors.toList());
        var cachedNotRegistry = cached.stream()
                .filter(spec -> !scanned.contains(spec))
                .collect(Collectors.toList());

        log.debug(String.format(
                "registryNotCached.size = %d ; cachedNotRegistry.size = %d",
                registryNotCached.size(), cachedNotRegistry.size()));
    }

    void logAfter(
            final Logger log,
            final Supplier<Can<ObjectSpecification>> snapshot,
            final Collection<? extends ObjectSpecification> scanned) {

        if(!log.isDebugEnabled()) {
            return;
        }

        var cached = snapshot.get();
        var cachedAfterNotBefore = cached.stream()
                .filter(spec -> !scanned.contains(spec))
                .collect(Collectors.toList());

        log.debug(String.format(
                "cachedSpecificationsAfter.size = %d ; cachedAfterNotBefore.size = %d",
                cached.size(), cachedAfterNotBefore.size()));
    }

}
