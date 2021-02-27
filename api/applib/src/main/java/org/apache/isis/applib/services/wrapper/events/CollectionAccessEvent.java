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

package org.apache.isis.applib.services.wrapper.events;

import org.apache.isis.applib.id.Identifier;

/**
 * <i>Supported only by {@link org.apache.isis.applib.services.wrapper.WrapperFactory} service, </i> represents an access (reading) of a collection.
 *
 * <p>
 * Analogous to {@link CollectionAddToEvent} or
 * {@link CollectionRemoveFromEvent}, however the {@link #getReason()} will
 * always be <tt>null</tt>. (If access is not allowed then a vetoing
 * {@link CollectionVisibilityEvent} would have been fired).
 *
 * @since 1.x {@index}
 */
public class CollectionAccessEvent extends AccessEvent {

    public CollectionAccessEvent(final Object source, final Identifier collectionIdentifier) {
        super(source, collectionIdentifier);
    }

}
