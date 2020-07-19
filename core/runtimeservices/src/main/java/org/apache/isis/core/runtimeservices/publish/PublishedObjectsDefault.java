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

package org.apache.isis.core.runtimeservices.publish;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.annotation.PublishingChangeKind;
import org.apache.isis.applib.jaxb.JavaSqlXMLGregorianCalendarMarshalling;
import org.apache.isis.applib.services.RepresentsInteractionMemberExecution;
import org.apache.isis.applib.services.publish.PublishedObjects;
import org.apache.isis.core.commons.internal.base._Lazy;
import org.apache.isis.core.commons.internal.base._NullSafe;
import org.apache.isis.core.commons.internal.collections._Maps;
import org.apache.isis.core.commons.internal.collections._Multimaps.ListMultimap;
import org.apache.isis.core.metamodel.adapter.oid.RootOid;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ManagedObjects;
import org.apache.isis.schema.chg.v2.ChangesDto;
import org.apache.isis.schema.chg.v2.ObjectsDto;
import org.apache.isis.schema.common.v2.OidsDto;

import lombok.ToString;
import lombok.val;

/**
 * Captures which objects were created, updated or deleted in the course of a transaction.
 */
@ToString
public class PublishedObjectsDefault implements PublishedObjects, RepresentsInteractionMemberExecution {

    // -- constructor, fields
    private UUID transactionUuid;
    private final int sequence;
    private final String userName;
    private final Timestamp completedAt;
    private final int numberLoaded;
    private final int numberObjectPropertiesModified;
    private final Map<ManagedObject, PublishingChangeKind> changesByAdapter;

    public PublishedObjectsDefault(
            final UUID transactionUuid,
            final int sequence,
            final String userName,
            final Timestamp completedAt,
            final int numberLoaded,
            final int numberObjectPropertiesModified,
            final Map<ManagedObject, PublishingChangeKind> changesByAdapter) {
        
        this.transactionUuid = transactionUuid;
        this.sequence = sequence;
        this.userName = userName;
        this.completedAt = completedAt;
        this.numberLoaded = numberLoaded;
        this.numberObjectPropertiesModified = numberObjectPropertiesModified;
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
        return numberLoaded;
    }

    @Override
    public int getNumberCreated() {
        return numAdaptersOfKind(PublishingChangeKind.CREATE);
    }

    @Override
    public int getNumberUpdated() {
        return numAdaptersOfKind(PublishingChangeKind.UPDATE);
    }

    @Override
    public int getNumberDeleted() {
        return numAdaptersOfKind(PublishingChangeKind.DELETE);
    }

    @Override
    public int getNumberPropertiesModified() {
        return numberObjectPropertiesModified;
    }

    private int numAdaptersOfKind(final PublishingChangeKind kind) {
        final Collection<ManagedObject> objectAdapters = adaptersByChange.get().get(kind);
        return objectAdapters != null ? objectAdapters.size() : 0;
    }


    /**
     * Lazily calculate the inverse of 'changesByAdapter'
     */
    private _Lazy<ListMultimap<PublishingChangeKind, ManagedObject>> adaptersByChange = 
            _Lazy.of(this::initAdaptersByChange);

    private ListMultimap<PublishingChangeKind, ManagedObject> initAdaptersByChange(){
        return _Maps.invertToListMultimap(changesByAdapter);
    }

    // -- newDto, newObjectsDto, newChangesDto

    private ChangesDto newDto() {
        final ObjectsDto objectsDto = newObjectsDto();
        return newChangesDto(objectsDto);
    }

    protected ObjectsDto newObjectsDto() {

        final ObjectsDto objectsDto = new ObjectsDto();

        objectsDto.setCreated(oidsDtoFor(PublishingChangeKind.CREATE));
        objectsDto.setUpdated(oidsDtoFor(PublishingChangeKind.UPDATE));
        objectsDto.setDeleted(oidsDtoFor(PublishingChangeKind.DELETE));

        objectsDto.setLoaded(getNumberLoaded());
        objectsDto.setPropertiesModified(getNumberPropertiesModified());

        return objectsDto;
    }

    private OidsDto oidsDtoFor(final PublishingChangeKind kind) {
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
        final ChangesDto changesDto = new ChangesDto();

        changesDto.setMajorVersion("1");
        changesDto.setMinorVersion("0");

        changesDto.setTransactionId(transactionUuid.toString());
        changesDto.setSequence(sequence);

        changesDto.setUser(userName);
        changesDto.setCompletedAt(JavaSqlXMLGregorianCalendarMarshalling.toXMLGregorianCalendar(completedAt));

        changesDto.setObjects(objectsDto);
        return changesDto;
    }





}
