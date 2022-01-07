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
package org.apache.isis.persistence.jdo.integration.changetracking;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.apache.isis.applib.annotations.EntityChangeKind;
import org.apache.isis.applib.jaxb.JavaSqlXMLGregorianCalendarMarshalling;
import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.applib.services.iactn.Interaction;
import org.apache.isis.applib.services.publishing.spi.EntityChanges;
import org.apache.isis.core.metamodel.execution.InteractionInternal;
import org.apache.isis.schema.chg.v2.ChangesDto;
import org.apache.isis.schema.chg.v2.ObjectsDto;
import org.apache.isis.schema.common.v2.OidsDto;

import lombok.val;

final class _ChangingEntitiesFactory {

    public static Optional<EntityChanges> createChangingEntities(
            final java.sql.Timestamp completedAt,
            final String userName,
            final EntityChangeTrackerJdo entityChangeTracker) {

        if(entityChangeTracker.getChangeKindByEnlistedAdapter().isEmpty()) {
            return Optional.empty();
        }

        // take a copy of enlisted adapters ... the JDO implementation of the PublishingService
        // creates further entities which would be enlisted;
        // taking copy of the map avoids ConcurrentModificationException
        val changeKindByEnlistedAdapter = new HashMap<>(
                entityChangeTracker.getChangeKindByEnlistedAdapter());

        val changingEntities = newChangingEntities(
                completedAt,
                userName,
                entityChangeTracker.currentInteraction(),
                entityChangeTracker.numberEntitiesLoaded(),
                // side-effect: it locks the result for this transaction,
                // such that cannot enlist on top of it
                entityChangeTracker.snapshotPropertyChangeRecords().size(),
                changeKindByEnlistedAdapter);

        return Optional.of(changingEntities);
    }

    // -- HELPER

    private static EntityChanges newChangingEntities(
            final java.sql.Timestamp completedAt,
            final String userName,
            final Interaction interaction,
            final int numberEntitiesLoaded,
            final int numberEntityPropertiesModified,
            final Map<Bookmark, EntityChangeKind> changeKindByEnlistedAdapter) {

        val interactionId = interaction.getInteractionId();
        final int nextEventSequence = ((InteractionInternal) interaction).getThenIncrementTransactionSequence();

        return new _SimpleChangingEntities(
                    interactionId, nextEventSequence,
                    userName, completedAt,
                    numberEntitiesLoaded,
                    numberEntityPropertiesModified,
                    ()->newDto(
                            interactionId, nextEventSequence,
                            userName, completedAt,
                            numberEntitiesLoaded,
                            numberEntityPropertiesModified,
                            changeKindByEnlistedAdapter));
    }

    private static ChangesDto newDto(
            final UUID interactionId, final int transactionSequenceNum,
            final String userName, final java.sql.Timestamp completedAt,
            final int numberEntitiesLoaded,
            final int numberEntityPropertiesModified,
            final Map<Bookmark, EntityChangeKind> changeKindByEnlistedEntity) {

        val objectsDto = new ObjectsDto();
        objectsDto.setCreated(new OidsDto());
        objectsDto.setUpdated(new OidsDto());
        objectsDto.setDeleted(new OidsDto());

        changeKindByEnlistedEntity.forEach((bookmark, kind)->{
            val oidDto = bookmark.toOidDto();
            if(oidDto==null) {
                return;
            }
            switch(kind) {
            case CREATE:
                objectsDto.getCreated().getOid().add(oidDto);
                return;
            case UPDATE:
                objectsDto.getUpdated().getOid().add(oidDto);
                return;
            case DELETE:
                objectsDto.getDeleted().getOid().add(oidDto);
                return;
            }
        });

        objectsDto.setLoaded(numberEntitiesLoaded);
        objectsDto.setPropertiesModified(numberEntityPropertiesModified);

        val changesDto = new ChangesDto();

        changesDto.setMajorVersion("2");
        changesDto.setMinorVersion("0");

        changesDto.setInteractionId(interactionId.toString());
        changesDto.setSequence(transactionSequenceNum);

        changesDto.setUser(userName);
        changesDto.setCompletedAt(JavaSqlXMLGregorianCalendarMarshalling.toXMLGregorianCalendar(completedAt));

        changesDto.setObjects(objectsDto);
        return changesDto;
    }


}
