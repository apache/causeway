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
package org.apache.causeway.core.metamodel.spec.impl;

import java.util.Map;
import java.util.Optional;

import org.apache.causeway.applib.annotation.DomainObject;
import org.apache.causeway.applib.id.LogicalType;
import org.apache.causeway.commons.internal.collections._Maps;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;

import org.jspecify.annotations.NonNull;
import lombok.extern.slf4j.Slf4j;

/**
 * Provides a lookup table for the purpose of recreating domain objects from bookmarks,
 * in support of logical type names.
 *
 * @apiNote only bookmark-able types will be ever registered
 * @see DomainObject#logicalTypeName()
 */
@Slf4j
record LogicalTypeResolver(
        Map<String, LogicalType> logicalTypeByName) {

    LogicalTypeResolver() {
        this(_Maps.newConcurrentHashMap());
    }

    /**
     * Removes all entries from the lookup table.
     */
    void clear() {
        logicalTypeByName.clear();
    }

    /**
     * Optionally returns the bookmark-able concrete type as registered by given {@code logicalTypeName},
     * based on whether there had been registered any.
     * @param logicalTypeName
     */
    Optional<LogicalType> lookup(final @NonNull String logicalTypeName) {
        return Optional.ofNullable(logicalTypeByName.get(logicalTypeName));
    }

    /**
     * Collects concrete types, ignores abstract types and interfaces.
     * Allows types to override their concrete super types.
     * <p>
     * Acts as an identity operator with side-effects.
     * @param spec - type's ObjectSpecification
     */
    <T extends ObjectSpecification> T register(final @NonNull T spec) {

        var logicalTypeName = spec.logicalTypeName();

        if(logicalTypeByName.containsKey(logicalTypeName)) {
            return spec;
        }

        // collect concrete classes (do not collect abstract or anonymous types or interfaces)
        if(!spec.isAbstract()
                && hasTypeIdentity(spec)) {

            putWithWarnOnOverride(logicalTypeName, spec);
        }
        return spec;
    }

    /**
     * Collects aliases for concrete types, ignores abstract types and interfaces.
     * <p>
     * Acts as an identity operator with side-effects.
     * @param spec - type's ObjectSpecification
     */
    ObjectSpecification registerAliases(final @NonNull ObjectSpecification spec) {

        // adding aliases to the lookup map
        spec.getAliases()
        .forEach(alias->{
                putWithWarnOnOverride(alias.logicalName(), spec);
        });

        return spec;
    }

    // -- HELPER

    private boolean hasTypeIdentity(final ObjectSpecification spec) {
        // anonymous inner classes (eg org.estatio.dom.WithTitleGetter$ToString$1)
        // don't have type identity; hence the guard.
        return spec.getCorrespondingClass().getCanonicalName()!=null;
    }

    private void putWithWarnOnOverride(
            final String logicalTypeName,
            final ObjectSpecification spec) {

        final LogicalType previousMapping =
                logicalTypeByName.put(logicalTypeName, spec.logicalType());

        if(previousMapping!=null
                && !spec.logicalType().equals(previousMapping)) {
            var msg = String.format("Overriding existing mapping\n"
                    + "%s -> %s,\n"
                    + "with\n "
                    + "%s -> %s\n "
                    + "This will result in the meta-model validation to fail.",
                    logicalTypeName, previousMapping.correspondingClass(),
                    logicalTypeName, spec.getCorrespondingClass());
            log.warn(msg);
        }

    }

}