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
package org.apache.isis.extensions.commandlog.jpa.entities;

import javax.persistence.Entity;

import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.Editing;
import org.apache.isis.applib.services.command.Command;
import org.apache.isis.extensions.commandlog.applib.command.CommandLog;
import org.apache.isis.extensions.commandlog.applib.command.ReplayState;
import org.apache.isis.extensions.commandlog.jpa.IsisModuleExtCommandLogJpa;
import org.apache.isis.schema.cmd.v2.CommandDto;

import lombok.NoArgsConstructor;

/**
 * @deprecated use {@link CommandLog} instead
 */
@Entity
@DomainObject(
        logicalTypeName = CommandJpa.LOGICAL_TYPE_NAME,
        editing = Editing.DISABLED
)
//@Log4j2
@Deprecated
@NoArgsConstructor
public class CommandJpa
extends CommandLog {

    public final static String LOGICAL_TYPE_NAME = IsisModuleExtCommandLogJpa.NAMESPACE + ".Command";
    protected final static String FQCN = "org.apache.isis.extensions.commandlog.jpa.entities.CommandJpa";

    /**
     * Intended for use on primary system.
     *
     * @param command
     */
    public CommandJpa(final Command command) {
        super(command);
    }


    /**
     * Intended for use on secondary (replay) system.
     *
     * @param commandDto - obtained from the primary system as a representation of a command invocation
     * @param replayState - controls whether this is to be replayed
     * @param targetIndex - if the command represents a bulk action, then it is flattened out when replayed; this indicates which target to execute against.
     */
    public CommandJpa(
            final CommandDto commandDto,
            final ReplayState replayState,
            final int targetIndex) {
        super(commandDto, replayState, targetIndex);
    }

}

