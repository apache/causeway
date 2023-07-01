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
package org.apache.causeway.core.metamodel.facets.object.entity;

import java.util.Optional;

import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.core.config.beans.PersistenceStack;

import lombok.NonNull;
import lombok.experimental.Accessors;

// record candidate
@lombok.Value @Accessors(fluent=true)
public class EntityOrmMetadata {

    @lombok.Value @Accessors(fluent=true)
    public static class ColumnOrmMetadata {

        final @NonNull String name;
        final @NonNull String memberId;
        final @NonNull String javaType;
        //final @NonNull String sqlType;

        final @NonNull Object vendorColumnMetadata;

    }

    final @NonNull PersistenceStack persistenceStack;
    final @NonNull Optional<String> table;
    final @NonNull Optional<String> schema;
    final @NonNull Class<?> primaryKeyClass;

    final @NonNull Can<ColumnOrmMetadata> columns;

    final @NonNull Object vendorEntityMetadata;

}
