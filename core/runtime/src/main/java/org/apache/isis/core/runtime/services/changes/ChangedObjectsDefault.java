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

package org.apache.isis.core.runtime.services.changes;

import java.sql.Timestamp;
import java.util.Map;
import java.util.UUID;

import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.annotation.PublishedObject;
import org.apache.isis.applib.services.changes.ChangedObjects;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.oid.RootOid;
import org.apache.isis.core.metamodel.facets.object.publishedobject.PublishedObjectFacet;
import org.apache.isis.schema.chg.v1.ChangesDto;
import org.apache.isis.schema.chg.v1.ObjectsDto;
import org.apache.isis.schema.common.v1.OidDto;
import org.apache.isis.schema.common.v1.OidsDto;
import org.apache.isis.schema.utils.jaxbadapters.JavaSqlTimestampXmlGregorianCalendarAdapter;

/**
 * Captures which objects were created, updated or deleted in the course of a transaction.
 */
public class ChangedObjectsDefault implements ChangedObjects {

    private UUID transactionUuid;
    private final String userName;
    private final Timestamp completedAt;
    private final Map<ObjectAdapter, PublishedObject.ChangeKind> changesByAdapter;

    public ChangedObjectsDefault(
            final UUID transactionUuid,
            final String userName,
            final Timestamp completedAt,
            final Map<ObjectAdapter, PublishedObject.ChangeKind> changesByAdapter) {
        this.transactionUuid = transactionUuid;
        this.userName = userName;
        this.completedAt = completedAt;
        this.changesByAdapter = changesByAdapter;
    }

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
    public String getUser() {
        return userName;
    }

    @Programmatic
    @Override
    public String getUsername() {
        return getUser();
    }
    //endregion

    private ChangesDto dto;

    @Override
    public ChangesDto getDto() {
        return dto != null ? dto : (dto = newDto());
    }


    //region > newDto, newObjectsDto, newChangesDto

    private ChangesDto newDto() {
        final ObjectsDto objectsDto = newObjectsDto(changesByAdapter);
        return newChangesDto(objectsDto, transactionUuid, userName, completedAt);
    }

    protected ObjectsDto newObjectsDto(
            final Map<ObjectAdapter, PublishedObject.ChangeKind> changeKindByEnlistedAdapter) {
        final ObjectsDto objectsDto = new ObjectsDto();

        final OidsDto createdOids = new OidsDto();
        final OidsDto updatedOids = new OidsDto();
        final OidsDto deletedOids = new OidsDto();

        for (final Map.Entry<ObjectAdapter, PublishedObject.ChangeKind> adapterAndChange : changeKindByEnlistedAdapter.entrySet()) {
            final ObjectAdapter enlistedAdapter = adapterAndChange.getKey();

            final PublishedObjectFacet publishedObjectFacet =
                    enlistedAdapter.getSpecification().getFacet(PublishedObjectFacet.class);

            if(publishedObjectFacet == null) {
                continue;
            }

            final RootOid rootOid = (RootOid) enlistedAdapter.getOid();
            final OidDto oidDto = rootOid.asOidDto();

            final PublishedObject.ChangeKind changeKind = adapterAndChange.getValue();
            switch (changeKind) {
            case CREATE:
                createdOids.getOid().add(oidDto);
                break;
            case UPDATE:
                updatedOids.getOid().add(oidDto);
                break;
            case DELETE:
                deletedOids.getOid().add(oidDto);
                break;
            default:
                // shouldn't happen
                throw new RuntimeException("ChangeKind '" + changeKind + "' not recognized");
            }
        }

        objectsDto.setCreated(createdOids);
        objectsDto.setUpdated(updatedOids);
        objectsDto.setDeleted(deletedOids);
        return objectsDto;
    }

    protected ChangesDto newChangesDto(
            final ObjectsDto objectsDto,
            final UUID transactionUuid,
            final String userName,
            final Timestamp timestamp) {
        final String transactionId = transactionUuid.toString();
        final ChangesDto changesDto = new ChangesDto();

        changesDto.setMajorVersion("1");
        changesDto.setMinorVersion("0");

        changesDto.setTransactionId(transactionId);
        changesDto.setUser(userName);
        changesDto.setCompletedAt(
                JavaSqlTimestampXmlGregorianCalendarAdapter.print(timestamp));
        changesDto.setObjects(objectsDto);
        return changesDto;
    }



    //endregion



}
