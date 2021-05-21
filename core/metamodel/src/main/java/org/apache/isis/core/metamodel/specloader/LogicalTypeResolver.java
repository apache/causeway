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

import java.util.Optional;

import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.id.LogicalType;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;

import lombok.NonNull;

/**
 * Provides a lookup table for the purpose of recreating domain objects from bookmarks,
 * in support of logical type names.
 *
 * @apiNote only bookmark-able types will be ever registered
 * @see DomainObject#objectType()
 *
 * @since 2.0
 */
interface LogicalTypeResolver {

    /**
     * Optionally returns the bookmark-able concrete type as registered by given {@code logicalTypeName},
     * based on whether there had been registered any.
     * @param logicalTypeName
     */
    Optional<LogicalType> lookup(@NonNull String logicalTypeName);

    /**
     * Collects concrete types, ignores abstract types and interfaces.
     * Allows types to override their concrete super types.
     * @param spec - type's ObjectSpecification
     */
    void register(@NonNull ObjectSpecification spec);

    /**
     * Removes all entries from the lookup table.
     */
    void clear();

}