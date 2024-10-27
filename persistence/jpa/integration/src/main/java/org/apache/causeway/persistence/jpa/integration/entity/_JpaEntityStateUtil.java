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

import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;
import javax.persistence.PersistenceUnitUtil;

import org.eclipse.persistence.exceptions.DescriptorException;
import org.eclipse.persistence.sessions.UnitOfWork;

import org.apache.causeway.applib.services.repository.EntityState;
import org.apache.causeway.core.metamodel.facets.object.entity.EntityFacet.PrimaryKeyType;

import lombok.experimental.UtilityClass;

@UtilityClass
class _JpaEntityStateUtil {

    EntityState getEntityState(
            final EntityManager entityManager,
            final PersistenceUnitUtil persistenceUnitUtil,
            final Class<?> entityClass,
            final PrimaryKeyType<?> primaryKeyType,
            final Object pojo) {

        if (entityManager.contains(pojo)) {
            var primaryKey = persistenceUnitUtil.getIdentifier(pojo);
            if (primaryKey == null) {
                return EntityState.ATTACHED_NO_OID;
            }
            return EntityState.ATTACHED;
        }

        /*
         * With JPA there is no obvious way to distinguish between TRANSIENT, REMOVED or DETACHED.
         *
         * Presence of a primary key AND a successful call to UnitOfWork#getReference(...) suggests
         * that the state is DETACHED.
         */

        final Object primaryKey;
        try {
            primaryKey = persistenceUnitUtil.getIdentifier(pojo);
        } catch (PersistenceException ex) {
            /* horrible hack, but encountered NPEs if using a composite key (eg CommandLogEntry)
            (this was without any weaving) */
            final Throwable cause = ex.getCause();
            if (cause instanceof DescriptorException) {
                var descriptorException = (DescriptorException) cause;
                final Throwable internalException = descriptorException.getInternalException();
                if (internalException instanceof NullPointerException) {
                    return EntityState.TRANSIENT_OR_REMOVED;
                }
            }
            if (cause instanceof NullPointerException) {
                // horrible hack, encountered if using composite key (eg ExecutionLogEntry) with dynamic weaving
                return EntityState.TRANSIENT_OR_REMOVED;
            }
            throw ex;
        }

        if(primaryKey == null
                || !primaryKeyType.isValid(primaryKey)) {
            return EntityState.TRANSIENT_OR_REMOVED;
        }

        final UnitOfWork session = entityManager.unwrap(UnitOfWork.class);
        final Object reference = session.getReference(entityClass, primaryKey);
        return reference != null
                ? EntityState.DETACHED
                : EntityState.TRANSIENT_OR_REMOVED;
    }

    @Deprecated
    EntityState getEntityStateLegacy(
            final EntityManager entityManager,
            final PersistenceUnitUtil persistenceUnitUtil,
            final Class<?> entityClass,
            final PrimaryKeyType<?> primaryKeyType,
            final Object pojo) {
        if (entityManager.contains(pojo)) {
            var primaryKey = persistenceUnitUtil.getIdentifier(pojo);
            if (primaryKey == null) {
                return EntityState.ATTACHED_NO_OID;
            }
            return EntityState.ATTACHED;
        }

        try {
            var primaryKey = persistenceUnitUtil.getIdentifier(pojo);
            if (primaryKey == null) {
                return EntityState.TRANSIENT_OR_REMOVED;
            } else {
                // detect shallow primary key
                //TODO this is a hack - see whether we can actually ask the EntityManager to give us an accurate answer
                return primaryKeyType.isValid(primaryKey)
                    ? EntityState.DETACHED
                    : EntityState.TRANSIENT_OR_REMOVED;
            }
        } catch (PersistenceException ex) {
            /* horrible hack, but encountered NPEs if using a composite key (eg CommandLogEntry)
                (this was without any weaving) */
            Throwable cause = ex.getCause();
            if (cause instanceof DescriptorException) {
                DescriptorException descriptorException = (DescriptorException) cause;
                Throwable internalException = descriptorException.getInternalException();
                if (internalException instanceof NullPointerException) {
                    return EntityState.TRANSIENT_OR_REMOVED;
                }
            }
            if (cause instanceof NullPointerException) {
                // horrible hack, encountered if using composite key (eg ExecutionLogEntry) with dynamic weaving
                return EntityState.TRANSIENT_OR_REMOVED;
            }
            throw ex;
        }
    }

}