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

import java.util.Map;
import java.util.Optional;

import org.apache.causeway.applib.id.LogicalType;
import org.apache.causeway.commons.internal.collections._Maps;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;

import lombok.NonNull;
import lombok.val;
import lombok.extern.log4j.Log4j2;

@Log4j2
class LogicalTypeResolverDefault implements LogicalTypeResolver {

    private final Map<String, LogicalType> logicalTypeByName = _Maps.newConcurrentHashMap();

    @Override
    public void clear() {
        logicalTypeByName.clear();
    }

    @Override
    public Optional<LogicalType> lookup(final @NonNull String logicalTypeName) {
        return Optional.ofNullable(logicalTypeByName.get(logicalTypeName));
    }

    @Override
    public ObjectSpecification register(final @NonNull ObjectSpecification spec) {

        val logicalTypeName = spec.getLogicalTypeName();

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

    @Override
    public ObjectSpecification registerAliases(final @NonNull ObjectSpecification spec) {

        // adding aliases to the lookup map
        spec.getAliases()
        .forEach(alias->{
                putWithWarnOnOverride(alias.getLogicalTypeName(), spec);
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
                logicalTypeByName.put(logicalTypeName, spec.getLogicalType());

        if(previousMapping!=null
                && !spec.getLogicalType().equals(previousMapping)) {
            val msg = String.format("Overriding existing mapping\n"
                    + "%s -> %s,\n"
                    + "with\n "
                    + "%s -> %s\n "
                    + "This will result in the meta-model validation to fail.",
                    logicalTypeName, previousMapping.getCorrespondingClass(),
                    logicalTypeName, spec.getCorrespondingClass());
            log.warn(msg);
        }

    }

}
