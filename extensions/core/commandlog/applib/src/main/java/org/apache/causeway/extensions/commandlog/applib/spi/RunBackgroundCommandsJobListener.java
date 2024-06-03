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
package org.apache.causeway.extensions.commandlog.applib.spi;

import java.util.List;

import org.apache.causeway.extensions.commandlog.applib.dom.CommandLogEntry;
import org.apache.causeway.schema.cmd.v2.CommandDto;

import org.springframework.stereotype.Component;

/**
 * Listens to the processing of the
 * {@link org.apache.causeway.extensions.commandlog.applib.job.RunBackgroundCommandsJob}.
 *
 * @since 2.1 {@index}
 */
public interface RunBackgroundCommandsJobListener {

    /**
     * The {@link CommandDto#getInteractionId() interactionId}s of the {@link CommandDto}s that were executed.
     *
     * <p>
     *     The commands thus identified may or may not have executed successfully; indeed if there was a deadlock then
     *     the transaction will have been rolled back and so the command may not even have been executed at all.
     * </p>
     *
     * <p>
     *     Implementation note: the {@link CommandDto}s are not passed in, instead only the command's
     *     {@link CommandDto#getInteractionId() interactionId}, to avoid issues and complications with the state of
     *     the in-memory {@link CommandDto}; is it in sync with the database if a deadlock occurred for example?
     *     Passing in just the identifier means that it's the responsibility of the listener to determine the state,
     *     typically by refetching the {@link org.apache.causeway.extensions.commandlog.applib.dom.CommandLogEntry}
     *     (that {@link CommandLogEntry#getCommandDto() contains} the {@link CommandDto}) in a separate transaction.
     * </p>
     * @param commandInteractionIds
     */
    void executed(List<String> commandInteractionIds);

    @Component
    public static class Noop implements RunBackgroundCommandsJobListener {
        @Override
        public void executed(List<String> commandInteractionIds) {
        }
    }
}
