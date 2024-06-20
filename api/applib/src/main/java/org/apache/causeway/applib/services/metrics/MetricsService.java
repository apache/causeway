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
package org.apache.causeway.applib.services.metrics;

import java.util.Collections;
import java.util.List;

import org.apache.causeway.applib.annotation.DomainObject;
import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.applib.services.iactn.InteractionProvider;
import org.apache.causeway.schema.ixn.v2.MemberExecutionDto;

/**
 * Hooks into the transaction nechanism to provide a counters relating to
 * numbers of object loaded, dirtied etc.
 *
 * <p>
 *     Only entities with {@link DomainObject#entityChangePublishing()} enabled
 *     are counted.
 * </p>
 *
 * @since 1.x {@index}
 */
public interface MetricsService {

    /**
     * The number of entities that have, so far in this request, been loaded from the database.
     * <p>
     * Corresponds to the number of times that <code>javax.jdo.listener.LoadLifecycleListener#postLoad(InstanceLifecycleEvent)</code> (or equivalent) is fired.
     * <p>
     * Is captured within {@link MemberExecutionDto#getMetrics()} (accessible from {@link InteractionProvider#currentInteraction()}).
     */
    int numberEntitiesLoaded();

    /**
     * Returns the bookmarks of the entities loaded within the transaction.
     *
     * <p>
     *     Only returns a value if the service is configured to return detailed metrics.
     * </p>
     */
    default List<Bookmark> entitiesLoaded() {
        return Collections.emptyList();
    }

    /**
     * The number of objects that have, so far in this request, been dirtied/will need updating in the database); a
     * good measure of the footprint of the interaction.
     * <p>
     * Corresponds to the number of times that <code>javax.jdo.listener.DirtyLifecycleListener#preDirty(InstanceLifecycleEvent)</code> (or equivalent) callback is fired.
     * <p>
     * Is captured within {@link MemberExecutionDto#getMetrics()} (accessible from {@link InteractionProvider#currentInteraction()}.
     */
    int numberEntitiesDirtied();

    /**
     * Returns the bookmarks of the entities that were modified within the transaction.
     *
     * <p>
     *     Only returns a value if the service is configured to return detailed metrics.
     * </p>
     */
    default List<Bookmark> entitiesDirtied() {
        return Collections.emptyList();
    }


}


