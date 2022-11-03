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
package org.apache.causeway.extensions.commandlog.applib.app;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.lang.Nullable;

import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.ActionLayout;
import org.apache.causeway.applib.annotation.BookmarkPolicy;
import org.apache.causeway.applib.annotation.DomainService;
import org.apache.causeway.applib.annotation.DomainServiceLayout;
import org.apache.causeway.applib.annotation.MemberSupport;
import org.apache.causeway.applib.annotation.NatureOfService;
import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.applib.annotation.RestrictTo;
import org.apache.causeway.applib.annotation.SemanticsOf;
import org.apache.causeway.applib.services.clock.ClockService;
import org.apache.causeway.extensions.commandlog.applib.CausewayModuleExtCommandLogApplib;
import org.apache.causeway.extensions.commandlog.applib.dom.CommandLogEntry;
import org.apache.causeway.extensions.commandlog.applib.dom.CommandLogEntryRepository;

import lombok.RequiredArgsConstructor;

/**
 * This service exposes a set of menu actions to search and list {@link CommandLogEntry command}s, by default under
 * the &quot;Activity&quot; secondary menu.
 *
 * @since 2.0 {@index}
 */
@Named(CommandLogMenu.LOGICAL_TYPE_NAME)
@DomainService(nature = NatureOfService.VIEW)
@DomainServiceLayout(
    menuBar = DomainServiceLayout.MenuBar.SECONDARY,
    named = "Activity"
)
@javax.annotation.Priority(PriorityPrecedence.EARLY)
@RequiredArgsConstructor(onConstructor_ = { @Inject })
public class CommandLogMenu {

    public static final String LOGICAL_TYPE_NAME =
            CausewayModuleExtCommandLogApplib.NAMESPACE + ".CommandLogMenu";

    public static abstract class ActionDomainEvent<T>
            extends CausewayModuleExtCommandLogApplib.ActionDomainEvent<T> { }


    final CommandLogEntryRepository<? extends CommandLogEntry> commandLogEntryRepository;
    final ClockService clockService;


    @Action(
            domainEvent = activeCommands.DomainEvent.class,
            semantics = SemanticsOf.SAFE,
            typeOf = CommandLogEntry.class
    )
    @ActionLayout(bookmarking = BookmarkPolicy.AS_ROOT, cssClassFa = "fa-bolt", sequence="10")
    public class activeCommands {
        public class DomainEvent extends ActionDomainEvent<activeCommands> { }

        @MemberSupport public List<? extends CommandLogEntry> act() {
            return commandLogEntryRepository.findCurrent();
        }
    }


    @Action(
            domainEvent = findMostRecent.DomainEvent.class,
            semantics = SemanticsOf.SAFE,
            typeOf = CommandLogEntry.class
    )
    @ActionLayout(cssClassFa = "fa-search", sequence="10")
    public class findMostRecent {
        public class DomainEvent extends ActionDomainEvent<findMostRecent> { }

        @MemberSupport public List<? extends CommandLogEntry> act() {
            return commandLogEntryRepository.findMostRecent();
        }
    }


    @Action(
            domainEvent = findCommands.DomainEvent.class,
            semantics = SemanticsOf.SAFE,
            typeOf = CommandLogEntry.class
    )
    @ActionLayout(cssClassFa = "fa-search", sequence="30")
    public class findCommands {
        public class DomainEvent extends ActionDomainEvent<findCommands> { }

        @MemberSupport public List<? extends CommandLogEntry> act(
                final @Nullable LocalDate from,
                final @Nullable LocalDate to) {
            return commandLogEntryRepository.findByFromAndTo(from, to);
        }
        @MemberSupport public LocalDate default0Act() {
            return now().minusDays(7);
        }
        @MemberSupport public LocalDate default1Act() {
            return now();
        }
    }



    @Action(
            domainEvent = findAll.DomainEvent.class,
            restrictTo = RestrictTo.PROTOTYPING,
            semantics = SemanticsOf.SAFE,
            typeOf = CommandLogEntry.class
    )
    @ActionLayout(cssClassFa = "fa-search", sequence="40")
    public class findAll {
        public class DomainEvent extends ActionDomainEvent<findAll> { }

        @MemberSupport public List<? extends CommandLogEntry> act() {
            return commandLogEntryRepository.findAll();
        }
    }




    private LocalDate now() {
        return clockService.getClock().nowAsLocalDate(ZoneId.systemDefault());
    }
}
