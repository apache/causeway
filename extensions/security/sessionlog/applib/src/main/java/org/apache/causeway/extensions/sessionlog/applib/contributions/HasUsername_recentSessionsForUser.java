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

package org.apache.causeway.extensions.sessionlog.applib.contributions;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.ActionLayout;
import org.apache.causeway.applib.annotation.MemberSupport;
import org.apache.causeway.applib.annotation.SemanticsOf;
import org.apache.causeway.applib.mixins.security.HasUsername;
import org.apache.causeway.extensions.sessionlog.applib.CausewayModuleExtSessionLogApplib;
import org.apache.causeway.extensions.sessionlog.applib.dom.SessionLogEntry;
import org.apache.causeway.extensions.sessionlog.applib.dom.SessionLogEntryRepository;

import lombok.RequiredArgsConstructor;


/**
 * Contributes the `recentSessionsForUser` collection to any domain object implementing {@link HasUsername}.
 *
 * @since 2.0 {@index}
 */
@Action(
        semantics = SemanticsOf.SAFE,
        domainEvent = HasUsername_recentSessionsForUser.ActionDomainEvent.class
)
@ActionLayout(
        fieldSetId = "username"
)
@RequiredArgsConstructor
public class HasUsername_recentSessionsForUser {

    public static class ActionDomainEvent
            extends CausewayModuleExtSessionLogApplib.ActionDomainEvent<HasUsername_recentSessionsForUser> { }

    private final HasUsername hasUsername;

    @MemberSupport public List<? extends SessionLogEntry> act() {
        if(hasUsername == null || hasUsername.getUsername() == null) {
            return Collections.emptyList();
        }
        return sessionLogEntryRepository.findRecentByUsername(hasUsername.getUsername());
    }
    @MemberSupport public boolean hideAct() {
        return hasUsername == null || hasUsername.getUsername() == null;
    }

    @Inject SessionLogEntryRepository<? extends SessionLogEntry> sessionLogEntryRepository;

}
