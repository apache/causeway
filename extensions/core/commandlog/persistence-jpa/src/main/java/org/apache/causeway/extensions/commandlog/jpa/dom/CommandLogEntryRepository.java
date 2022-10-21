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
package org.apache.causeway.extensions.commandlog.jpa.dom;

import javax.inject.Named;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.extensions.commandlog.jpa.CausewayModuleExtCommandLogPersistenceJpa;

/**
 * Provides supporting functionality for querying and persisting
 * {@link CommandLogEntry command} entities.
 */
@Service
@Named(CommandLogEntryRepository.LOGICAL_TYPE_NAME)
@javax.annotation.Priority(PriorityPrecedence.MIDPOINT)
@Qualifier("Jpa")
public class CommandLogEntryRepository
extends org.apache.causeway.extensions.commandlog.applib.dom.CommandLogEntryRepository<CommandLogEntry> {

    public static final String LOGICAL_TYPE_NAME = CausewayModuleExtCommandLogPersistenceJpa.NAMESPACE + ".CommandLogEntryRepository";

    public CommandLogEntryRepository() {
        super(CommandLogEntry.class);
    }

}
