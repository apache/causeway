/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package org.apache.isis.extensions.commandlog.applib.contributions;

import javax.inject.Inject;

import org.apache.isis.applib.annotation.Property;
import org.apache.isis.applib.mixins.system.HasInteractionId;
import org.apache.isis.applib.services.command.Command;
import org.apache.isis.applib.services.queryresultscache.QueryResultsCache;
import org.apache.isis.extensions.commandlog.applib.IsisModuleExtCommandLogApplib;
import org.apache.isis.extensions.commandlog.applib.dom.CommandLogEntry;
import org.apache.isis.extensions.commandlog.applib.dom.CommandLogEntryRepository;

import lombok.RequiredArgsConstructor;

/**
 * @since 2.x {@index}
 */
@Property(
        domainEvent = HasInteractionId_commandLogEntry.PropertyDomainEvent.class
)
@RequiredArgsConstructor
public class HasInteractionId_commandLogEntry {

    private final HasInteractionId hasInteractionId;

    public static class PropertyDomainEvent
            extends IsisModuleExtCommandLogApplib.PropertyDomainEvent<HasInteractionId_commandLogEntry, CommandLogEntry> { }


    public CommandLogEntry prop() {
        return queryResultsCache.execute(this::doProp, getClass(), "prop");
    }

    private CommandLogEntry doProp() {
        return commandLogEntryRepository.findByInteractionId(hasInteractionId.getInteractionId()).orElse(null);
    }

    /**
     * Hide if the contributee is a {@link CommandLogEntry}, because we don't want to navigate to ourselves, and there
     * are other ways to navigate to the parent or child commands.
     */
    public boolean hideProp() {
        return (hasInteractionId instanceof CommandLogEntry);
    }

    @Inject CommandLogEntryRepository<? extends CommandLogEntry> commandLogEntryRepository;
    @Inject QueryResultsCache queryResultsCache;

}
