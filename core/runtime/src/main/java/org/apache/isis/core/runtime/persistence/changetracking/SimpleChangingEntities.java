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

package org.apache.isis.core.runtime.persistence.changetracking;

import java.sql.Timestamp;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import org.apache.isis.applib.annotation.EntityChangeKind;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.jaxb.JavaSqlXMLGregorianCalendarMarshalling;
import org.apache.isis.applib.services.RepresentsInteractionMemberExecution;
import org.apache.isis.applib.services.publish.ChangingEntities;
import org.apache.isis.commons.internal.base._Lazy;
import org.apache.isis.commons.internal.base._NullSafe;
import org.apache.isis.commons.internal.collections._Maps;
import org.apache.isis.commons.internal.collections._Multimaps.ListMultimap;
import org.apache.isis.core.metamodel.adapter.oid.RootOid;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ManagedObjects;
import org.apache.isis.schema.chg.v2.ChangesDto;
import org.apache.isis.schema.chg.v2.ObjectsDto;
import org.apache.isis.schema.common.v2.OidsDto;

import lombok.NonNull;
import lombok.ToString;
import lombok.val;

/**
 * Captures which objects were created, updated or deleted in the course of a transaction.
 */
@ToString
class SimpleChangingEntities implements ChangingEntities, RepresentsInteractionMemberExecution {

    // -- constructor, fields
    private UUID transactionUuid;
    private final int sequence;
    private final String userName;
    private final Timestamp completedAt;
    private final int numberEntitiesLoaded;
    private final int numberEntityPropertiesModified;
    private final Map<ManagedObject, EntityChangeKind> changesByAdapter;

    public SimpleChangingEntities(
            final @NonNull UUID transactionUuid,
            final int sequence,
            final @NonNull String userName,
            final @NonNull Timestamp completedAt,
            final int numberEntitiesLoaded,
            final int numberEntityPropertiesModified,
            final @NonNull Map<ManagedObject, EntityChangeKind> changesByAdapter) {

        this.transactionUuid = transactionUuid;
        this.sequence = sequence;
        this.userName = userName;
        this.completedAt = completedAt;
        this.numberEntitiesLoaded = numberEntitiesLoaded;
        this.numberEntityPropertiesModified = numberEntityPropertiesModified;
        this.changesByAdapter = changesByAdapter;
    }

    // -- transactionId, sequence completedAt, user
    @Programmatic
    public UUID getTransactionId() {
        return getUniqueId();
    }

    @Programmatic
    @Override
    public UUID getUniqueId() {
        return transactionUuid;
    }

    @Programmatic
    @Override
    public int getSequence() {
        return sequence;
    }

    /**
     * The date/time at which this set of enlisted objects was created (approx the completion time of the transaction).
     */
    @Override
    public Timestamp getCompletedAt() {
        return completedAt;
    }

    @Override
    public String getUsername() {
        return userName;
    }


    // -- dto
    /**
     * lazily computed
     */
    private ChangesDto dto;

    @Override
    public ChangesDto getDto() {
        return dto != null ? dto : (dto = newDto());
    }


    // -- numberLoaded, numberCreated, numberUpdated, numberDeleted, numberObjectPropertiesModified

    @Override
    public int getNumberLoaded() {
        return numberEntitiesLoaded;
    }

    @Override
    public int getNumberCreated() {
        return numAdaptersOfKind(EntityChangeKind.CREATE);
    }

    @Override
    public int getNumberUpdated() {
        return numAdaptersOfKind(EntityChangeKind.UPDATE);
    }

    @Override
    public int getNumberDeleted() {
        return numAdaptersOfKind(EntityChangeKind.DELETE);
    }

    @Override
    public int getNumberPropertiesModified() {
        return numberEntityPropertiesModified;
    }

    private int numAdaptersOfKind(final EntityChangeKind kind) {
        return _NullSafe.size(adaptersByChange.get().get(kind));
    }


    /**
     * Lazily calculate the inverse of 'changesByAdapter'
     */
    private _Lazy<ListMultimap<EntityChangeKind, ManagedObject>> adaptersByChange = 
            _Lazy.of(this::initAdaptersByChange);

    private ListMultimap<EntityChangeKind, ManagedObject> initAdaptersByChange(){
        return _Maps.invertToListMultimap(changesByAdapter);
    }

    // -- newDto, newObjectsDto, newChangesDto

    private ChangesDto newDto() {
        final ObjectsDto objectsDto = newObjectsDto();
        return newChangesDto(objectsDto);
    }

    protected ObjectsDto newObjectsDto() {

        final ObjectsDto objectsDto = new ObjectsDto();

        objectsDto.setCreated(oidsDtoFor(EntityChangeKind.CREATE));
        objectsDto.setUpdated(oidsDtoFor(EntityChangeKind.UPDATE));
        objectsDto.setDeleted(oidsDtoFor(EntityChangeKind.DELETE));

        objectsDto.setLoaded(getNumberLoaded());
        objectsDto.setPropertiesModified(getNumberPropertiesModified());

        return objectsDto;
    }

    private OidsDto oidsDtoFor(final EntityChangeKind kind) {
        val oidsDto = new OidsDto();

        _NullSafe.stream(adaptersByChange.get().get(kind))
        .map((final ManagedObject adapter) -> 
            ManagedObjects.identify(adapter)
            .map(RootOid::asOidDto)
            .orElse(null)
        )
        .filter(Objects::nonNull)
        .forEach(oidsDto.getOid()::add);
        
        return oidsDto;
    }

    protected ChangesDto newChangesDto(final ObjectsDto objectsDto) {
        val changesDto = new ChangesDto();

        changesDto.setMajorVersion("2");
        changesDto.setMinorVersion("0");

        changesDto.setTransactionId(transactionUuid.toString());
        changesDto.setSequence(sequence);

        changesDto.setUser(userName);
        changesDto.setCompletedAt(JavaSqlXMLGregorianCalendarMarshalling.toXMLGregorianCalendar(completedAt));

        changesDto.setObjects(objectsDto);
        return changesDto;
    }





}
