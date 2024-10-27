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
package org.apache.causeway.persistence.jpa.integration.entity;

import java.util.Optional;

import javax.persistence.EntityManager;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.SingularAttribute;

import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.internal.base._NullSafe;
import org.apache.causeway.commons.internal.exceptions._Exceptions;
import org.apache.causeway.core.config.beans.PersistenceStack;
import org.apache.causeway.core.metamodel.facets.object.entity.EntityOrmMetadata;
import org.apache.causeway.core.metamodel.facets.object.entity.EntityOrmMetadata.ColumnOrmMetadata;

import lombok.NonNull;
import lombok.experimental.UtilityClass;

@UtilityClass
class _MetadataUtil {

    EntityOrmMetadata ormMetadataFor(
            final @NonNull EntityManager entityManager,
            final @NonNull Class<?> entityClass) {

        final EntityType<?> typeMetadata = lookupJpaMetamodel(entityManager, entityClass)
                .orElseThrow(()->
                    _Exceptions.noSuchElement("cannot find JPA metadata for entity %s", entityClass));

        return new EntityOrmMetadata(
                PersistenceStack.JPA,
                Optional.empty(), // if somebody knows how to implement this, feel free to inform us
                Optional.empty(), // if somebody knows how to implement this, feel free to inform us
                typeMetadata.getIdType().getJavaType(),
                columns(typeMetadata),
                typeMetadata);
    }

    // -- HELPER

    private Can<ColumnOrmMetadata> columns(final EntityType<?> typeMetadata) {
        return _NullSafe.stream(typeMetadata.getSingularAttributes())
                .map(_MetadataUtil::column)
                .collect(Can.toCan());
    }

    private ColumnOrmMetadata column(final SingularAttribute sa) {
        // if somebody knows how to implement this, feel free to inform us
        return null;
    }

    /**
     * find the JPA meta-model associated with this (corresponding) entity
     */
    private Optional<EntityType<?>> lookupJpaMetamodel(
            final EntityManager entityManager,
            final Class<?> entityClass) {
        return entityManager.getMetamodel().getEntities()
                .stream()
                .filter(type -> type.getJavaType().equals(entityClass))
                .findFirst();
    }

}
