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
package org.apache.isis.applib.services.publishing.spi;

import java.sql.Timestamp;
import java.util.UUID;

import org.apache.isis.applib.mixins.system.HasInteractionId;
import org.apache.isis.applib.mixins.security.HasUsername;
import org.apache.isis.schema.chg.v2.ChangesDto;

/**
 * As used by {@link EntityChangesSubscriber}, provides metrics on the
 * &quot;footprint&quot; of an interaction, in other words the number of
 * objects accessed or changed.
 *
 * <p>
 *  The numbers of objects loaded, created, updated or deleted and the number
 *  of object properties modified (in other words the "size" or "weight" of the transaction).
 * </p>
 * @since 2.0 {@index}
 */
public interface EntityChanges
        extends HasInteractionId,
                HasUsername {

    /**
     * inherited from {@link HasInteractionId}, correlates back to the unique
     * identifier of the transaction in which these objects were changed.
     */
    @Override
    UUID getInteractionId();

    /**
     * Inherited from {@link HasUsername}, is the user that initiated the
     * transaction causing these objects to change.
     * @return
     */
    @Override
    String getUsername();

    /**
     * Time that the interaction execution completed
     * @return
     */
    Timestamp getCompletedAt();

    /**
     * Number of domain objects loaded in this interaction
     * @return
     */
    int getNumberLoaded();

    /**
     * Number of domain objects created in this interaction
     * @return
     */
    int getNumberCreated();

    /**
     * Number of domain objects updated in this interaction
     * @return
     */
    int getNumberUpdated();

    /**
     * Number of domain objects deleted in this interaction
     * @return
     */
    int getNumberDeleted();

    /**
     * Number of domain objects properties that were changed in this interaction
     * @return
     */
    int getNumberPropertiesModified();

    /**
     * Same details, but as an an instance of {@link ChangesDto}.
     *
     * <p>
     * This can be converted into a serializable XML representation using the
     * {@link org.apache.isis.applib.util.schema.ChangesDtoUtils} utility class.
     * </p>
     *
     * @return
     */
    ChangesDto getDto();
}
