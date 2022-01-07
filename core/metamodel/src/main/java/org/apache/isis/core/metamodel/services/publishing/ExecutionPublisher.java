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
package org.apache.isis.core.metamodel.services.publishing;

import java.util.function.Supplier;

import org.apache.isis.applib.annotations.Action;
import org.apache.isis.applib.annotations.Property;
import org.apache.isis.applib.services.iactn.Execution;
import org.apache.isis.applib.services.publishing.spi.ExecutionSubscriber;

/**
 * Notifies {@link ExecutionSubscriber}s that an action has been executed
 * or a property edited.
 *
 * @since 1.x but renamed/refactored for v2 {@index}
 *
 * @see ExecutionSubscriber
 */
public interface ExecutionPublisher {

    /**
     * Notifies {@link ExecutionSubscriber}s of an action invocation through
     * the {@link ExecutionSubscriber#onExecution(Execution)} callback.
     *
     * @see Action#executionPublishing()
     */
    void publishActionInvocation(Execution<?,?> execution);

    /**
     * Notifies {@link ExecutionSubscriber}s of a property edit through
     * the {@link ExecutionSubscriber#onExecution(Execution)} callback.
     *
     * @see Property#executionPublishing()
     */
    void publishPropertyEdit(Execution<?,?> execution);

    /**
     * Slightly hokey wormhole (anti)pattern to disable publishing for mixin associations.
     */
    <T> T withPublishingSuppressed(Supplier<T> block);

}
