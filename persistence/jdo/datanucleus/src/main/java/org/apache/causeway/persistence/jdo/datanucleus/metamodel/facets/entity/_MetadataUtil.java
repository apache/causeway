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
package org.apache.causeway.persistence.jdo.datanucleus.metamodel.facets.entity;

import java.util.Optional;

import javax.jdo.PersistenceManager;
import javax.jdo.metadata.MemberMetadata;
import javax.jdo.metadata.TypeMetadata;

import org.datanucleus.api.jdo.JDOPersistenceManagerFactory;
import org.datanucleus.identity.SCOID;

import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.internal.base._NullSafe;
import org.apache.causeway.core.config.beans.PersistenceStack;
import org.apache.causeway.core.metamodel.facets.object.entity.EntityOrmMetadata;
import org.apache.causeway.core.metamodel.facets.object.entity.EntityOrmMetadata.ColumnOrmMetadata;

import lombok.NonNull;

import lombok.experimental.UtilityClass;
import lombok.extern.log4j.Log4j2;

@UtilityClass
@Log4j2
class _MetadataUtil {

    EntityOrmMetadata ormMetadataFor(
            final @NonNull PersistenceManager persistenceManager,
            final @NonNull Class<?> entityClass) {

        var pmf = (JDOPersistenceManagerFactory) persistenceManager.getPersistenceManagerFactory();
        var typeMetadata = pmf.getMetadata(entityClass.getName());

        return new EntityOrmMetadata(
                PersistenceStack.JDO,
                Optional.ofNullable(typeMetadata.getTable()),
                Optional.ofNullable(typeMetadata.getSchema()),
                primaryKeyTypeFor(pmf, typeMetadata, entityClass),
                columns(typeMetadata),
                typeMetadata);
    }

    // -- HELPER

    private Can<ColumnOrmMetadata> columns(final TypeMetadata typeMetadata) {
        return _NullSafe.stream(typeMetadata.getMembers())
                .map(_MetadataUtil::column)
                .collect(Can.toCan());
    }

    private ColumnOrmMetadata column(final MemberMetadata c) {
        return Optional.ofNullable(c.getColumn()) // null if is plural
                .map(colName->new ColumnOrmMetadata(colName, c.getName(), fieldType(c), c))
                .orElse(null);
    }

    private String fieldType(final MemberMetadata c) {
        return Optional.ofNullable(c.getFieldType())
                .orElseGet(()->{
                    log.warn("failed to get java field type from JDO MemberMetadata in {}", c.getParent());
                    return Object.class.getName();
                });
    }

    private Class<?> primaryKeyTypeFor(
            final @NonNull JDOPersistenceManagerFactory pmf,
            final @NonNull TypeMetadata typeMetadata,
            final @NonNull Class<?> entityClass) {

        var identityType = typeMetadata.getIdentityType();
        switch (identityType) {
            case APPLICATION: {
                var contextLoader = Thread.currentThread().getContextClassLoader();
                var nucleusContext = pmf.getNucleusContext();
                var clr = nucleusContext.getClassLoaderResolver(contextLoader);
                final String objectIdClass = typeMetadata.getObjectIdClass();
                return clr.classForName(objectIdClass);
            }
            case DATASTORE: {
                var nucleusContext = pmf.getNucleusContext();
                return nucleusContext.getIdentityManager().getDatastoreIdClass();
            }
            case NONDURABLE:
                return SCOID.class;
            case UNSPECIFIED:
            default:
                throw new IllegalStateException(String.format(
                        "JdoEntityFacet was installed on '%s', "
                        + "yet this entity has IdentityType '%s', "
                        + "which is not supported by the framework's "
                        + "JDO implementation.",
                        entityClass.getName(), identityType));
        }
    }

}
