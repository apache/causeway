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
package org.apache.causeway.applib.services.publishing.spi;

import java.sql.Timestamp;
import java.util.UUID;

import org.apache.causeway.applib.services.bookmark.Bookmark;

import lombok.Value;

/**
 * Immutable data record for {@link EntityPropertyChangeSubscriber}s.
 *
 * @since 2.0 {@index}
 */
@Value(staticConstructor = "of")
public class EntityPropertyChange {

    private final UUID interactionId;
    private final int sequence;
    private final Bookmark target;
    private final String logicalMemberIdentifier;
    private final String propertyId;
    private final String preValue;
    private final String postValue;
    private final String username;
    private final Timestamp timestamp;

    @Override
    public String toString() {
        return String.format("%s,%d: %s by %s, %s: %s -> %s",
        getInteractionId(),
        getSequence(),
        getTarget().toString(),
        getUsername(),
        getPropertyId(),
        getPreValue(),
        getPostValue());
    }

}
