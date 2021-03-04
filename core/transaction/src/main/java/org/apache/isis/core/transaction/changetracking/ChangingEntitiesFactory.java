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
package org.apache.isis.core.transaction.changetracking;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import javax.annotation.Nullable;

import org.apache.isis.applib.annotation.EntityChangeKind;
import org.apache.isis.applib.jaxb.JavaSqlXMLGregorianCalendarMarshalling;
import org.apache.isis.applib.services.iactn.Interaction;
import org.apache.isis.applib.services.iactn.SequenceType;
import org.apache.isis.applib.services.publishing.spi.EntityChanges;
import org.apache.isis.commons.internal.base._NullSafe;
import org.apache.isis.commons.internal.collections._Maps;
import org.apache.isis.commons.internal.collections._Multimaps.ListMultimap;
import org.apache.isis.core.metamodel.adapter.oid.RootOid;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ManagedObjects;
import org.apache.isis.schema.chg.v2.ChangesDto;
import org.apache.isis.schema.chg.v2.ObjectsDto;
import org.apache.isis.schema.common.v2.OidsDto;

import lombok.val;
import lombok.experimental.UtilityClass;

@UtilityClass
class ChangingEntitiesFactory {

    @Nullable
    public static EntityChanges createChangingEntities(
            final java.sql.Timestamp completedAt,
            final String userName,
            final EntityChangeTrackerDefault entityChangeTracker) {

        // take a copy of enlisted adapters ... the JDO implementation of the PublishingService
        // creates further entities which would be enlisted;
        // taking copy of the map avoids ConcurrentModificationException

        val changeKindByEnlistedAdapter = new HashMap<>(
                entityChangeTracker.getChangeKindByEnlistedAdapter());

        if(changeKindByEnlistedAdapter.isEmpty()) {
            return null;
        }

        val changingEntities = newChangingEntities(
                completedAt,
                userName,
                entityChangeTracker.currentInteraction(),
                entityChangeTracker.numberEntitiesLoaded(),
                entityChangeTracker.numberAuditedEntityPropertiesModified(),
                changeKindByEnlistedAdapter);

        return changingEntities;
    }

    // -- HELPER

    private static EntityChanges newChangingEntities(
            final java.sql.Timestamp completedAt,
            final String userName,
            final Interaction interaction,
            final int numberEntitiesLoaded,
            final int numberEntityPropertiesModified,
            final Map<ManagedObject, EntityChangeKind> changeKindByEnlistedAdapter) {

        val uniqueId = interaction.getInteractionId();
        final int nextEventSequence = interaction.next(SequenceType.TRANSACTION);

        return new SimpleChangingEntities(
                    uniqueId, nextEventSequence,
                    userName, completedAt,
                    numberEntitiesLoaded,
                    numberEntityPropertiesModified,
                    ()->newDto(
                            uniqueId, nextEventSequence,
                            userName, completedAt,
                            numberEntitiesLoaded,
                            numberEntityPropertiesModified,
                            changeKindByEnlistedAdapter));
    }

    private static ChangesDto newDto(
            final UUID uniqueId, final int nextEventSequence,
            final String userName, final java.sql.Timestamp completedAt,
            final int numberEntitiesLoaded,
            final int numberEntityPropertiesModified,
            final Map<ManagedObject, EntityChangeKind> changeKindByEnlistedAdapter) {

        // calculate the inverse of 'changesByAdapter'
        final ListMultimap<EntityChangeKind, ManagedObject> adaptersByChange =
            _Maps.invertToListMultimap(changeKindByEnlistedAdapter);

        val objectsDto = new ObjectsDto();

        objectsDto.setCreated(oidsDtoFor(adaptersByChange, EntityChangeKind.CREATE));
        objectsDto.setUpdated(oidsDtoFor(adaptersByChange, EntityChangeKind.UPDATE));
        objectsDto.setDeleted(oidsDtoFor(adaptersByChange, EntityChangeKind.DELETE));

        objectsDto.setLoaded(numberEntitiesLoaded);
        objectsDto.setPropertiesModified(numberEntityPropertiesModified);

        val changesDto = new ChangesDto();

        changesDto.setMajorVersion("2");
        changesDto.setMinorVersion("0");

        changesDto.setTransactionId(uniqueId.toString());
        changesDto.setSequence(nextEventSequence);

        changesDto.setUser(userName);
        changesDto.setCompletedAt(JavaSqlXMLGregorianCalendarMarshalling.toXMLGregorianCalendar(completedAt));

        changesDto.setObjects(objectsDto);
        return changesDto;
    }

    private static OidsDto oidsDtoFor(
            final ListMultimap<EntityChangeKind, ManagedObject> adaptersByChange,
            final EntityChangeKind kind) {
        val oidsDto = new OidsDto();

        _NullSafe.stream(adaptersByChange.get(kind))
        .map((final ManagedObject adapter) ->
            ManagedObjects.identify(adapter)
            .map(RootOid::asOidDto)
            .orElse(null)
        )
        .filter(Objects::nonNull)
        .forEach(oidsDto.getOid()::add);

        return oidsDto;
    }



}
