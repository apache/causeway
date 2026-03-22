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
package org.apache.causeway.applib.annotation;

import org.apache.causeway.applib.services.command.Command;
import org.apache.causeway.applib.services.iactn.Execution;
import org.apache.causeway.applib.services.publishing.spi.CommandSubscriber;
import org.apache.causeway.applib.services.publishing.spi.EntityChanges;
import org.apache.causeway.applib.services.publishing.spi.EntityChangesSubscriber;
import org.apache.causeway.applib.services.publishing.spi.EntityPropertyChange;
import org.apache.causeway.applib.services.publishing.spi.EntityPropertyChangeSubscriber;
import org.apache.causeway.applib.services.publishing.spi.ExecutionSubscriber;

/**
 * The available policies as to whether data should be published to
 * corresponding subscribers. The framework supports several kinds of data
 * that are available for publishing:
 * <ul>
 * <li><b>{@link EntityChanges} ... subscribed to via {@link EntityChangesSubscriber}</li>
 * <li><b>{@link EntityPropertyChange} ... subscribed to via {@link EntityPropertyChangeSubscriber}</li>
 * <li><b>{@link Command} ... subscribed to via {@link CommandSubscriber}</li>
 * <li><b>{@link Execution} ... subscribed to via {@link ExecutionSubscriber}</li>
 * </ul>
 * @since 1.x {@index}
 */
public enum Publishing {

    /**
     * Publishing of data triggered by interaction with this object
     * should be handled as per the default publishing policy
     * configured in <tt>application.properties</tt>.
     * <p>
     * If no publishing policy is configured, then publishing is disabled.
     */
    AS_CONFIGURED,

    /**
     * Do publish data triggered by interaction with this object.
     */
    ENABLED,

    /**
     * Applies only to {@link EntityPropertyChangeSubscriber}, whereby events are published for modifications to the
     * object, but no events are published for the initial creation of an object.
     *
     * <p>
     * In the case of audit trail extension,
     * this effectively suppresses all of the "[NEW] -> value" entries that are created for every property of the
     * entity when it is being created, and also all of the "value -> [DELETED]" entries that are created for every property of the
     * entity when it is being deleted.
     * </p>
     *
     * <p>
     *     This variant is intended only where the application code has enough traceability built into the domain
     *     (perhaps to provide visibility to the end-users) that the technical auditing is overkill.  It will also
     *     of course reduce the volume of auditing, so improves performance (likely both response times and throughput).
     * </p>
     *
     * <p>
     * For other subscribers, behaviour is the same as {@link #ENABLED}.
     * </p>
     */
    ENABLED_FOR_UPDATES_ONLY,

    /**
     * Do <b>not</b> publish data triggered by interaction with this object
     * (even if otherwise configured to enable publishing).
     */
    DISABLED,

    /**
     * Ignore the value provided by this annotation (meaning that the framework will keep searching, in meta
     * annotations or super-classes/interfaces).
     */
    NOT_SPECIFIED

}
