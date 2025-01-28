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
package org.apache.causeway.persistence.commons.integration.changetracking;

import java.sql.Timestamp;
import java.util.UUID;
import java.util.function.Supplier;

import org.apache.causeway.applib.services.publishing.spi.EntityChanges;
import org.apache.causeway.schema.chg.v2.ChangesDto;

import org.jspecify.annotations.NonNull;
import lombok.ToString;

/**
 * Captures which objects were created, updated or deleted in the course of a transaction.
 */
@ToString
final class _SimpleChangingEntities implements EntityChanges {

    private UUID transactionUuid;
    private final int sequence;
    private final String userName;
    private final Timestamp completedAt;
    private final int numberEntitiesLoaded;
    private final int numberEntityPropertiesModified;

    @ToString.Exclude
    private final Supplier<ChangesDto> changesDtoSupplier;

    public _SimpleChangingEntities(
            final @NonNull UUID transactionUuid,
            final int sequence,
            final @NonNull String userName,
            final @NonNull Timestamp completedAt,
            final int numberEntitiesLoaded,
            final int numberEntityPropertiesModified,
            final @NonNull Supplier<ChangesDto> changesDtoSupplier) {

        this.transactionUuid = transactionUuid;
        this.sequence = sequence;
        this.userName = userName;
        this.completedAt = completedAt;
        this.numberEntitiesLoaded = numberEntitiesLoaded;
        this.numberEntityPropertiesModified = numberEntityPropertiesModified;
        this.changesDtoSupplier = changesDtoSupplier;
    }

    @Override
    public UUID getInteractionId() {
        return transactionUuid;
    }

    @Override
    public int getSequence() {
        return sequence;
    }

    /**
     * The date/time at which this set of enlisted objects was created
     * (approx the completion time of the transaction).
     */
    @Override
    public Timestamp getCompletedAt() {
        return completedAt;
    }

    @Override
    public String getUsername() {
        return userName;
    }

    private ChangesDto dto;

    @Override
    public ChangesDto getDto() {
        return dto != null ? dto : (dto = changesDtoSupplier.get());
    }

    @Override
    public int getNumberLoaded() {
        return numberEntitiesLoaded;
    }

    @Override
    public int getNumberCreated() {
        return getDto().getObjects().getCreated().getOid().size();
    }

    @Override
    public int getNumberUpdated() {
        return getDto().getObjects().getUpdated().getOid().size();
    }

    @Override
    public int getNumberDeleted() {
        return getDto().getObjects().getDeleted().getOid().size();
    }

    @Override
    public int getNumberPropertiesModified() {
        return numberEntityPropertiesModified;
    }

}
