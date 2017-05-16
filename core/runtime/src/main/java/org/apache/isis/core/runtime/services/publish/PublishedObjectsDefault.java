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

package org.apache.isis.core.runtime.services.publish;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

import com.google.common.base.Function;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multimaps;

import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.annotation.PublishedObject;
import org.apache.isis.applib.services.publish.PublishedObjects;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.oid.RootOid;
import org.apache.isis.schema.chg.v1.ChangesDto;
import org.apache.isis.schema.chg.v1.ObjectsDto;
import org.apache.isis.schema.common.v1.OidDto;
import org.apache.isis.schema.common.v1.OidsDto;
import org.apache.isis.schema.utils.jaxbadapters.JavaSqlTimestampXmlGregorianCalendarAdapter;

/**
 * Captures which objects were created, updated or deleted in the course of a transaction.
 */
public class PublishedObjectsDefault implements PublishedObjects {

    //region > constructor, fields
    private UUID transactionUuid;
    private final int sequence;
    private final String userName;
    private final Timestamp completedAt;
    private final int numberLoaded;
    private final int numberObjectPropertiesModified;
    private final Map<ObjectAdapter, PublishedObject.ChangeKind> changesByAdapter;

    public PublishedObjectsDefault(
            final UUID transactionUuid,
            final int sequence,
            final String userName,
            final Timestamp completedAt,
            final int numberLoaded,
            final int numberObjectPropertiesModified,
            final Map<ObjectAdapter, PublishedObject.ChangeKind> changesByAdapter) {
        this.transactionUuid = transactionUuid;
        this.sequence = sequence;
        this.userName = userName;
        this.completedAt = completedAt;
        this.numberLoaded = numberLoaded;
        this.numberObjectPropertiesModified = numberObjectPropertiesModified;
        this.changesByAdapter = changesByAdapter;
    }
    //endregion


    //region > transactionId, completedAt, user
    @Programmatic
    @Override
    public UUID getTransactionId() {
        return transactionUuid;
    }

    /**
     * Unused; the {@link #getTransactionId()} is set in the constructor.
     */
    @Override
    public void setTransactionId(final UUID transactionId) {
        this.transactionUuid = transactionId;
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
    //endregion

    //region > dto
    /**
     * lazily computed
     */
    private ChangesDto dto;

    @Override
    public ChangesDto getDto() {
        return dto != null ? dto : (dto = newDto());
    }
    //endregion

    //region > numberLoaded, numberCreated, numberUpdated, numberDeleted, numberObjectPropertiesModified

    @Override
    public int getNumberLoaded() {
        return numberLoaded;
    }

    @Override
    public int getNumberCreated() {
        return numAdaptersOfKind(PublishedObject.ChangeKind.CREATE);
    }

    @Override
    public int getNumberUpdated() {
        return numAdaptersOfKind(PublishedObject.ChangeKind.UPDATE);
    }

    @Override
    public int getNumberDeleted() {
        return numAdaptersOfKind(PublishedObject.ChangeKind.DELETE);
    }

    @Override
    public int getNumberPropertiesModified() {
        return numberObjectPropertiesModified;
    }

    private int numAdaptersOfKind(final PublishedObject.ChangeKind kind) {
        final Collection<ObjectAdapter> objectAdapters = adaptersByChange().get(kind);
        return objectAdapters != null ? objectAdapters.size() : 0;
    }


    /**
     * Lazily populated
     */
    private Map<PublishedObject.ChangeKind, Collection<ObjectAdapter>> adaptersByChange;

    private Map<PublishedObject.ChangeKind, Collection<ObjectAdapter>> adaptersByChange() {
        return adaptersByChange != null? adaptersByChange : (adaptersByChange = invert(changesByAdapter));
    }

    private static <T, S> Map<T, Collection<S>> invert(final Map<S, T> valueByKey) {
        return new TreeMap<>(
                Multimaps.invertFrom(
                        Multimaps.forMap(valueByKey),
                        ArrayListMultimap.<T, S>create()
                ).asMap()
        );
    }

    //endregion


    //region > newDto, newObjectsDto, newChangesDto

    private ChangesDto newDto() {
        final ObjectsDto objectsDto = newObjectsDto();
        return newChangesDto(objectsDto);
    }

    protected ObjectsDto newObjectsDto() {

        final ObjectsDto objectsDto = new ObjectsDto();

        objectsDto.setCreated(oidsDtoFor(PublishedObject.ChangeKind.CREATE));
        objectsDto.setUpdated(oidsDtoFor(PublishedObject.ChangeKind.UPDATE));
        objectsDto.setDeleted(oidsDtoFor(PublishedObject.ChangeKind.DELETE));

        objectsDto.setLoaded(getNumberLoaded());
        objectsDto.setPropertiesModified(getNumberPropertiesModified());

        return objectsDto;
    }

    private OidsDto oidsDtoFor(final PublishedObject.ChangeKind kind) {
        final OidsDto oidsDto = new OidsDto();

        final Map<PublishedObject.ChangeKind, Collection<ObjectAdapter>> adaptersByChange = adaptersByChange();

        final Collection<ObjectAdapter> adapters = adaptersByChange.get(kind);
        if(adapters != null) {
            final ImmutableList<OidDto> oidDtos = FluentIterable.from(adapters)
                    .transform(new Function<ObjectAdapter, OidDto>() {
                        @Override
                        public OidDto apply(final ObjectAdapter objectAdapter) {
                            final RootOid rootOid = (RootOid) objectAdapter.getOid();
                            return rootOid.asOidDto();
                        }
                    })
                    .toList();
            oidsDto.getOid().addAll(oidDtos);
        }
        return oidsDto;
    }

    protected ChangesDto newChangesDto(final ObjectsDto objectsDto) {
        final ChangesDto changesDto = new ChangesDto();

        changesDto.setMajorVersion("1");
        changesDto.setMinorVersion("0");

        changesDto.setTransactionId(transactionUuid.toString());
        changesDto.setSequence(sequence);

        changesDto.setUser(userName);
        changesDto.setCompletedAt(JavaSqlTimestampXmlGregorianCalendarAdapter.print(completedAt));

        changesDto.setObjects(objectsDto);
        return changesDto;
    }


    //endregion


}
