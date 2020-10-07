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
package org.apache.isis.applib.services.publish;

import org.apache.isis.applib.services.iactn.Interaction;
import org.apache.isis.applib.util.schema.InteractionDtoUtils;

/**
 * SPI that allows individual interactions (action invocations or property edits) to be
 * {@link #publish(Interaction.Execution) published}.
 * Note that re-publishing is not part of this SPI.
 */
// tag::refguide[]
public interface PublisherService {

    // end::refguide[]
    /**
     * Publish each {@link Interaction.Execution} immediately after it completes.
     * <p>
     * Most implementations are expected to use {@link Interaction.Execution#getDto()} to create a serializable
     * XML representation of the execution.  The easiest way to do this is using {@link InteractionDtoUtils#newInteractionDto(Interaction.Execution)}.  There is
     * some flexibility here, though.
     */
    // tag::refguide[]
    void publish(final Interaction.Execution<?, ?> execution);  // <.>

    // end::refguide[]
    /**
     * Publish all changed entities at end of transaction (during pre-commit phase).
     */
    // tag::refguide[]
    void publish(final PublishedObjects publishedObjects);      // <.>
}
// end::refguide[]
