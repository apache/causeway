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
package org.apache.isis.core.metamodel.services.objectlifecycle;

import java.sql.Timestamp;

import org.apache.isis.applib.services.factory.FactoryService;
import org.apache.isis.applib.services.publishing.spi.EntityPropertyChange;
import org.apache.isis.applib.services.xactn.TransactionId;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.core.metamodel.facets.properties.property.entitychangepublishing.EntityPropertyChangePublishingPolicyFacet;
import org.apache.isis.core.metamodel.objectmanager.ObjectManager;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ManagedObjects;
import org.apache.isis.core.metamodel.spec.feature.MixedIn;

import lombok.NonNull;

/**
 * Responsible for collecting, then immediately publishing changes to domain objects,
 * that is,
 * notify publishing subscribers and call the various persistence call-back facets.
 *
 * @since 2.0 {index}
 */
public interface ObjectLifecyclePublisher {

    /**
     * Independent of the persistence stack, only triggered by {@link FactoryService}
     * and internal {@link ObjectManager}.
     * @param domainObject - an entity or view-model
     */
    void onPostCreate(ManagedObject domainObject);

    void onPostLoad(ManagedObject entity);

    void onPrePersist(ManagedObject entity);

    void onPostPersist(ManagedObject entity);

    void onPreUpdate(ManagedObject entity, Can<PropertyChangeRecord> changeRecords);

    void onPostUpdate(ManagedObject entity);

    void onPreRemove(ManagedObject entity);

    //void onPostRemove(ManagedObject entity);

    // -- PUBLISHING PAYLOAD FACTORIES

    static HasEnlistedEntityPropertyChanges publishingPayloadForCreation(
            final @NonNull ManagedObject entity) {

        return new HasEnlistedEntityPropertyChanges() {

            @Override
            public Can<EntityPropertyChange> getPropertyChanges(
                    final Timestamp timestamp,
                    final String user,
                    final TransactionId txId) {

                return entity
                .getSpecification()
                .streamProperties(MixedIn.EXCLUDED)
                .filter(property->!EntityPropertyChangePublishingPolicyFacet.isExcludedFromPublishing(property))
                .map(property->
                        PropertyChangeRecord
                        .of(
                                entity,
                                property,
                                PreAndPostValue
                                    .pre(PropertyValuePlaceholder.NEW)
                                    .withPost(ManagedObjects.UnwrapUtil.single(property.get(entity))))
                        .toEntityPropertyChange(
                                timestamp,
                                user,
                                txId)
                )
                .collect(Can.toCan());

            }

        };

    }

    static HasEnlistedEntityPropertyChanges publishingPayloadForDeletion(
            final @NonNull ManagedObject entity) {

        return new HasEnlistedEntityPropertyChanges() {

            @Override
            public Can<EntityPropertyChange> getPropertyChanges(
                    final Timestamp timestamp,
                    final String user,
                    final TransactionId txId) {

                return entity
                .getSpecification()
                .streamProperties(MixedIn.EXCLUDED)
                .filter(property->!EntityPropertyChangePublishingPolicyFacet.isExcludedFromPublishing(property))
                .map(property->
                        PropertyChangeRecord
                        .of(
                                entity,
                                property,
                                PreAndPostValue
                                    .pre(ManagedObjects.UnwrapUtil.single(property.get(entity)))
                                    .withPost(PropertyValuePlaceholder.DELETED))
                        .toEntityPropertyChange(
                                timestamp,
                                user,
                                txId)
                )
                .collect(Can.toCan());

            }

        };

    }

    static HasEnlistedEntityPropertyChanges publishingPayloadForUpdate(
            final ManagedObject entity,
            final Can<PropertyChangeRecord> changeRecords) {

        return new HasEnlistedEntityPropertyChanges() {

            @Override
            public Can<EntityPropertyChange> getPropertyChanges(
                    final Timestamp timestamp,
                    final String user,
                    final TransactionId txId) {

                return changeRecords
                .map(changeRecord->
                    changeRecord
                    .toEntityPropertyChange(
                            timestamp,
                            user,
                            txId));
            }

        };

    }


}
