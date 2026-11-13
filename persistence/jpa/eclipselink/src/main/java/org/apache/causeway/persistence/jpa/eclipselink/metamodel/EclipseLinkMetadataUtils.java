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
package org.apache.causeway.persistence.jpa.eclipselink.metamodel;

import java.util.Optional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.metamodel.EntityType;
import jakarta.persistence.metamodel.SingularAttribute;

import org.eclipse.persistence.mappings.foundation.AbstractColumnMapping;
import org.jspecify.annotations.NonNull;

import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.functional.Try;
import org.apache.causeway.commons.internal.base._NullSafe;
import org.apache.causeway.commons.internal.exceptions._Exceptions;
import org.apache.causeway.core.config.beans.CausewayBeanMetaData.PersistenceStack;
import org.apache.causeway.core.metamodel.facets.object.entity.EntityOrmMetadata;
import org.apache.causeway.core.metamodel.facets.object.entity.EntityOrmMetadata.ColumnOrmMetadata;

import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

/**
 * Utility that provides vendor independent ORM metadata {@link EntityOrmMetadata}.
 * @since 3.4.0 
 */
@UtilityClass
public class EclipseLinkMetadataUtils {
    
    public EntityOrmMetadata ormMetadataFor(
            final @NonNull EntityManager entityManager,
            final @NonNull Class<?> entityClass) {

        final EntityType<?> typeMetadata = lookupJpaMetadata(entityManager, entityClass)
                .orElseThrow(()->
                    _Exceptions.noSuchElement("cannot find JPA metadata for entity %s", entityClass));
        var classDescriptor = _EclipseLinkInternals.getClassDescriptor(typeMetadata);

        return new EntityOrmMetadata(
                PersistenceStack.JPA,
                Optional.of(classDescriptor.getTableName()),
                //TODO not sure if this is the correct method to lookup the schema String
                Optional.of(classDescriptor.getDefaultTable().getTableQualifier()), 
                typeMetadata.getIdType().getJavaType(),
                columns(typeMetadata),
                classDescriptor);
    }

    // -- HELPER
    
    private Can<ColumnOrmMetadata> columns(final EntityType<?> typeMetadata) {
        return _NullSafe.stream(typeMetadata.getSingularAttributes())
                .map(EclipseLinkMetadataUtils::column)
                .collect(Can.toCan());
    }
    
    @SneakyThrows
    private ColumnOrmMetadata column(final SingularAttribute<?, ?> sa) {
        var databaseMapping = _EclipseLinkInternals.getDatabaseMapping(sa);
        String colName = (databaseMapping instanceof AbstractColumnMapping abstractColumnMapping)
                ? abstractColumnMapping.getField().getName()
                : "?";
        return new ColumnOrmMetadata(colName, sa.getName(), sa.getBindableJavaType().getName(), databaseMapping);
    }

    /**
     * find the JPA meta-model associated with this (corresponding) entity
     */
    private <T> Optional<EntityType<T>> lookupJpaMetadata(
            final EntityManager entityManager,
            final Class<T> entityClass) {
        
        var entityType = Try.call(()->entityManager.getMetamodel().entity(entityClass))
                .getValue();
        return entityType;
    }
    
}
