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
package org.apache.isis.extensions.commandlog.jpa.dom;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import org.apache.isis.applib.annotation.PriorityPrecedence;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.jaxb.JavaSqlXMLGregorianCalendarMarshalling;
import org.apache.isis.applib.query.Query;
import org.apache.isis.applib.query.QueryRange;
import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.applib.services.command.Command;
import org.apache.isis.applib.services.iactn.InteractionProvider;
import org.apache.isis.applib.services.repository.RepositoryService;
import org.apache.isis.applib.util.schema.CommandDtoUtils;
import org.apache.isis.extensions.commandlog.applib.dom.ReplayState;
import org.apache.isis.extensions.commandlog.jpa.IsisModuleExtCommandLogJpa;
import org.apache.isis.schema.cmd.v2.CommandDto;
import org.apache.isis.schema.cmd.v2.CommandsDto;
import org.apache.isis.schema.cmd.v2.MapDto;
import org.apache.isis.schema.common.v2.InteractionType;
import org.apache.isis.schema.common.v2.OidDto;

import lombok.RequiredArgsConstructor;
import lombok.val;

/**
 * Provides supporting functionality for querying and persisting
 * {@link CommandLogEntry command} entities.
 */
@Service
@Named(CommandLogEntryRepository.LOGICAL_TYPE_NAME)
@javax.annotation.Priority(PriorityPrecedence.MIDPOINT)
@Qualifier("Jpa")
//@Log4j2
public class CommandLogEntryRepository
extends org.apache.isis.extensions.commandlog.applib.dom.CommandLogEntryRepository<CommandLogEntry> {

    public static final String LOGICAL_TYPE_NAME = IsisModuleExtCommandLogJpa.NAMESPACE + ".CommandLogEntryRepository";

    public CommandLogEntryRepository() {
        super(CommandLogEntry.class);
    }


}
