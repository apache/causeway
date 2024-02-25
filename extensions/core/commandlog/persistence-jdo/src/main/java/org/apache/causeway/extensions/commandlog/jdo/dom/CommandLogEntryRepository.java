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
package org.apache.causeway.extensions.commandlog.jdo.dom;

import javax.inject.Named;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.extensions.commandlog.applib.dom.CommandLogEntryRepositoryAbstract;
import org.apache.causeway.extensions.commandlog.jdo.CausewayModuleExtCommandLogPersistenceJdo;

/**
 * Provides supporting functionality for querying and persisting
 * {@link CommandLogEntry command} entities.
 */
@Service
@Named(CommandLogEntryRepository.LOGICAL_TYPE_NAME)
@javax.annotation.Priority(PriorityPrecedence.MIDPOINT)
@Qualifier("Jdo")
public class CommandLogEntryRepository
extends CommandLogEntryRepositoryAbstract<CommandLogEntry> {

    public static final String LOGICAL_TYPE_NAME = CausewayModuleExtCommandLogPersistenceJdo.NAMESPACE + ".CommandLogEntryRepository";

    public CommandLogEntryRepository() {
        super(CommandLogEntry.class);
    }

    /**
     * The DN annotation processor (from artifact {@literal org.datanucleus:datanucleus-jdo-query})
     * should  generate Q classes under 'target/generated-sources/annotations'.
     * @see "https://www.datanucleus.org/products/accessplatform_6_0/jdo/query.html#jdoql"
     */
    @SuppressWarnings("unused")
    private void ensureWeHaveQClasses() {
        // ensures at compile time, that Q classes are generated
        org.apache.causeway.extensions.commandlog.jdo.dom.QCommandLogEntry q;
    }

}
