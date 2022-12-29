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
package org.apache.causeway.extensions.commandlog.applib.contributions;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import org.apache.causeway.applib.annotation.Collection;
import org.apache.causeway.applib.annotation.CollectionLayout;
import org.apache.causeway.applib.annotation.MemberSupport;
import org.apache.causeway.applib.mixins.security.HasUsername;
import org.apache.causeway.extensions.commandlog.applib.CausewayModuleExtCommandLogApplib;
import org.apache.causeway.extensions.commandlog.applib.dom.CommandLogEntry;
import org.apache.causeway.extensions.commandlog.applib.dom.CommandLogEntryRepository;

import lombok.val;


/**
 * Contributes the <code>recentCommandsByUser</code> collection to any domain object implementing {@link HasUsername}.
 *
 * <p>
 *     For example the <i>secman</i> extension's <code>ApplicationUser</code> entity implements this interface.
 * </p>
 *
 * @since 2.0 {@index}
 */
@Collection(
    domainEvent = HasUsername_recentCommandsByUser.CollectionDomainEvent.class
)
@CollectionLayout(
    defaultView = "table",
    paged = 5,
    sequence = "3"
)
public class HasUsername_recentCommandsByUser {

    public static class CollectionDomainEvent
            extends CausewayModuleExtCommandLogApplib.CollectionDomainEvent<HasUsername_recentCommandsByUser, CommandLogEntry> { }

    private final HasUsername hasUsername;
    public HasUsername_recentCommandsByUser(final HasUsername hasUsername) {
        this.hasUsername = hasUsername;
    }

    @MemberSupport public List<? extends CommandLogEntry> coll() {
        val username = hasUsername.getUsername();
        return username != null
                ? commandLogEntryRepository.findRecentByUsername(username)
                : Collections.emptyList();
    }
    @MemberSupport public boolean hideColl() {
        return hasUsername.getUsername() == null;
    }

    @Inject CommandLogEntryRepository<? extends CommandLogEntry> commandLogEntryRepository;
}
