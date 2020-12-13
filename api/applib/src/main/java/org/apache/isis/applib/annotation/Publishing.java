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
package org.apache.isis.applib.annotation;

import org.apache.isis.applib.services.command.Command;
import org.apache.isis.applib.services.iactn.Interaction;
import org.apache.isis.applib.services.publishing.spi.CommandSubscriber;
import org.apache.isis.applib.services.publishing.spi.EntityChanges;
import org.apache.isis.applib.services.publishing.spi.EntityChangesSubscriber;
import org.apache.isis.applib.services.publishing.spi.EntityPropertyChange;
import org.apache.isis.applib.services.publishing.spi.EntityPropertyChangeSubscriber;
import org.apache.isis.applib.services.publishing.spi.ExecutionSubscriber;

/**
 * The available policies as to whether data should be published to  
 * corresponding subscribers. The framework supports several kinds of data 
 * that are available for publishing:
 * <ul>
 * <li><b>{@link EntityChanges} ... subscribed to via {@link EntityChangesSubscriber}</li>
 * <li><b>{@link EntityPropertyChange} ... subscribed to via {@link EntityPropertyChangeSubscriber}</li>
 * <li><b>{@link Command} ... subscribed to via {@link CommandSubscriber}</li>
 * <li><b>{@link Interaction.Execution} ... subscribed to via {@link ExecutionSubscriber}</li>
 * </ul>
 * @since 1.x {@index}
 */
// tag::refguide[]
public enum Publishing {

    // end::refguide[]
    /**
     * Publishing of data triggered by interaction with this object 
     * should be handled as per the default publishing policy 
     * configured in <tt>application.properties</tt>.
     * <p>
     * If no publishing policy is configured, then publishing is disabled.
     */
    // tag::refguide[]
    AS_CONFIGURED,

    // end::refguide[]
    /**
     * Do publish data triggered by interaction with this object.
     */
    // tag::refguide[]
    ENABLED,

    // end::refguide[]
    /**
     * Do <b>not</b> publish data triggered by interaction with this object
     * (even if otherwise configured to enable publishing).
     */
    // tag::refguide[]
    DISABLED,

    // end::refguide[]
    /**
     * Ignore the value provided by this annotation (meaning that the framework will keep searching, in meta
     * annotations or super-classes/interfaces).
     */
    // tag::refguide[]
    NOT_SPECIFIED

}
// end::refguide[]
