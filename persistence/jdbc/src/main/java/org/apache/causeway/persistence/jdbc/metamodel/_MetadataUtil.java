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
package org.apache.causeway.persistence.jdbc.metamodel;

import java.util.Optional;

import org.springframework.data.relational.core.mapping.RelationalMappingContext;
import org.springframework.data.relational.core.mapping.RelationalPersistentEntity;
import org.springframework.data.relational.core.mapping.RelationalPersistentProperty;

import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.internal.exceptions._Exceptions;
import org.apache.causeway.core.config.beans.CausewayBeanMetaData.PersistenceStack;
import org.apache.causeway.core.metamodel.facets.object.entity.EntityOrmMetadata;
import org.apache.causeway.core.metamodel.facets.object.entity.EntityOrmMetadata.ColumnOrmMetadata;

import lombok.NonNull;
import lombok.experimental.UtilityClass;

@UtilityClass
class _MetadataUtil {

    EntityOrmMetadata ormMetadataFor(
            final @NonNull RelationalMappingContext mappingContext,
            final @NonNull Class<?> entityClass) {

        final RelationalPersistentEntity<?> typeMetadata = lookupJdbcMetamodel(mappingContext, entityClass)
                .orElseThrow(()->
                    _Exceptions.noSuchElement("cannot find JDBC mapping metadata for entity %s", entityClass));
        
        final @NonNull Optional<String> table = Optional.of(typeMetadata.getTableName().getReference());
        final @NonNull Optional<String> schema = Optional.empty();
        
        return new EntityOrmMetadata(
                PersistenceStack.JDBC,
                table, 
                schema, 
                typeMetadata.getRequiredIdProperty().getActualType(),
                columns(typeMetadata),
                typeMetadata);
    }

    // -- HELPER

    private Can<ColumnOrmMetadata> columns(final RelationalPersistentEntity<?> typeMetadata) {
        return Can.ofIterable(typeMetadata) 
                .map(_MetadataUtil::column);
    }

    private ColumnOrmMetadata column(final RelationalPersistentProperty property) {
        final @NonNull String name = property.getColumnName().getReference();
        final @NonNull String memberId = property.getName();
        final @NonNull String javaType = property.getActualType().getName();
        return new ColumnOrmMetadata(name, memberId, javaType, property);
    }

    /**
     * find the JPA meta-model associated with this (corresponding) entity
     */
    private Optional<RelationalPersistentEntity<?>> lookupJdbcMetamodel(
            final RelationalMappingContext mappingContext,
            final Class<?> entityClass) {
        return Optional.ofNullable(mappingContext.getPersistentEntity(entityClass)); 
    }

}
