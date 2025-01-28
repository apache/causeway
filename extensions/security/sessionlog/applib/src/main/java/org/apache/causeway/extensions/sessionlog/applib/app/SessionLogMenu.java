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

package org.apache.causeway.extensions.sessionlog.applib.app;

import java.time.LocalDate;
import java.util.List;

import jakarta.inject.Inject;
import jakarta.inject.Named;

import org.jspecify.annotations.Nullable;

import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.ActionLayout;
import org.apache.causeway.applib.annotation.DomainService;
import org.apache.causeway.applib.annotation.DomainServiceLayout;
import org.apache.causeway.applib.annotation.MemberSupport;
import org.apache.causeway.applib.annotation.Publishing;
import org.apache.causeway.applib.annotation.SemanticsOf;
import org.apache.causeway.extensions.sessionlog.applib.CausewayModuleExtSessionLogApplib;
import org.apache.causeway.extensions.sessionlog.applib.dom.SessionLogEntry;
import org.apache.causeway.extensions.sessionlog.applib.dom.SessionLogEntryRepository;

import lombok.RequiredArgsConstructor;

/**
 * This service exposes a set of menu actions to search and list {@link SessionLogEntry session}s, by default under
 * the &quot;Activity&quot; secondary menu.
 *
 * @since 2.0 {@index}
 */
@Named(SessionLogMenu.LOGICAL_TYPE_NAME)
@DomainService
@DomainServiceLayout(
        menuBar = DomainServiceLayout.MenuBar.SECONDARY,
        named = "Activity"
)
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class SessionLogMenu {

    static final String LOGICAL_TYPE_NAME = CausewayModuleExtSessionLogApplib.NAMESPACE + ".SessionLogMenu";

    final SessionLogEntryRepository sessionLogEntryRepository;

    public static abstract class ActionDomainEvent<T> extends CausewayModuleExtSessionLogApplib.ActionDomainEvent<T> { }

    @Action(
            commandPublishing = Publishing.DISABLED,
            domainEvent = activeSessions.ActionDomainEvent.class,
            executionPublishing = Publishing.DISABLED,
            semantics = SemanticsOf.SAFE
    )
    @ActionLayout(
            cssClassFa = "fa-bolt"
    )
    public class activeSessions {

        public class ActionDomainEvent extends SessionLogMenu.ActionDomainEvent<activeSessions> { }

        @MemberSupport public List<? extends SessionLogEntry> act() {
            return sessionLogEntryRepository.findActiveSessions();
        }
    }

    @Action(
            commandPublishing = Publishing.DISABLED,
            domainEvent = findSessions.ActionDomainEvent.class,
            executionPublishing = Publishing.DISABLED,
            semantics = SemanticsOf.SAFE
    )
    @ActionLayout(
            cssClassFa = "fa-search"
    )
    public class findSessions {

        public class ActionDomainEvent extends SessionLogMenu.ActionDomainEvent<findSessions> { }

        @MemberSupport public List<? extends SessionLogEntry> act(
                final @Nullable String user,
                final @Nullable LocalDate from,
                final @Nullable LocalDate to) {

            if(user == null) {
                return sessionLogEntryRepository.findByFromAndTo(from, to);
            } else {
                return sessionLogEntryRepository.findByUsernameAndFromAndTo(user, from, to);
            }
        }
    }

}
