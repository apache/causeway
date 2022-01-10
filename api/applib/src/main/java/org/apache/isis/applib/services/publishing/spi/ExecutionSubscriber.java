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

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.Property;
import org.apache.isis.applib.services.iactn.Execution;
import org.apache.isis.applib.util.schema.InteractionDtoUtils;
import org.apache.isis.commons.having.HasEnabling;

/**
 * SPI that allows the execution of individual interactions (action invocations
 * or property edits) to be subscribed to.
 *
 * <p>
 *  The typical use case is to facilitate coarse-grained messaging for
 *  system-to-system interactions, that is from an Apache Isis application to
 *  some other system.  This could be done using a pub/sub bus such as
 *  <a href="http://activemq.apache.org">Apache ActiveMQ</a> with
 *  <a href="http://camel.apache.org">Apache Camel</a>.
 * </p>
 *
 * <p>
 *     Only actions/properties annotated for publishing (using
 *     {@link Action#executionPublishing()} or
 *     {@link Property#executionPublishing()} are published.
 * </p>
 *
 * @since 2.0 {@index}
 */
public interface ExecutionSubscriber extends HasEnabling {

    /**
     * Callback to notify that an interaction (an action invocation or property
     * edit, as represented by
     * {@link Execution}) has
     * completed.
     *
     * <p>
     *     This callback method is called by the framework immediately after
     *     the interaction (not at the end of the transaction, unlike some of
     *     the other subscribers).
     * </p>
     *
     * <p>
     * Most implementations are expected to use
     * {@link Execution#getDto()}
     * to create a serializable XML representation of the execution.
     * The easiest way to do this is using
     * {@link InteractionDtoUtils#newInteractionDto(Execution)}.
     * </p>
     */
    void onExecution(Execution<?, ?> execution);

}
