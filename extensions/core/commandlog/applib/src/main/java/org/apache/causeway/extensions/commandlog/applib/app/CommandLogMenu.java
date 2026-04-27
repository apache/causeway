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

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;

import jakarta.inject.Inject;
import jakarta.inject.Named;

import org.jspecify.annotations.Nullable;

import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.ActionLayout;
import org.apache.causeway.applib.annotation.DomainService;
import org.apache.causeway.applib.annotation.DomainServiceLayout;
import org.apache.causeway.applib.annotation.MemberSupport;
import org.apache.causeway.applib.annotation.ParameterLayout;
import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.applib.annotation.Publishing;
import org.apache.causeway.applib.annotation.RestrictTo;
import org.apache.causeway.applib.annotation.SemanticsOf;
import org.apache.causeway.applib.services.clock.ClockService;
import org.apache.causeway.extensions.commandlog.applib.CausewayModuleExtCommandLogApplib;
import org.apache.causeway.extensions.commandlog.applib.dom.CommandLogEntry;
import org.apache.causeway.extensions.commandlog.applib.dom.CommandLogEntryRepository;
import org.apache.causeway.extensions.commandlog.applib.dom.replay.CommandExportManager;
import org.apache.causeway.extensions.commandlog.applib.dom.replay.CommandReplayManager;
import org.apache.causeway.extensions.commandlog.applib.dom.replay.ReplayContext;

import lombok.RequiredArgsConstructor;

/**
 * This service exposes a set of menu actions to search and list {@link CommandLogEntry command}s, by default under
 * the &quot;Activity&quot; secondary menu.
 *
 * @since 2.0 {@index}
 */
@Named(CommandLogMenu.LOGICAL_TYPE_NAME)
@DomainService
@DomainServiceLayout(
    menuBar = DomainServiceLayout.MenuBar.SECONDARY,
    named = "Activity"
)
@jakarta.annotation.Priority(PriorityPrecedence.EARLY)
@RequiredArgsConstructor(onConstructor_ = { @Inject })
public class CommandLogMenu {

    public static final String LOGICAL_TYPE_NAME =
            CausewayModuleExtCommandLogApplib.NAMESPACE + ".CommandLogMenu";

    public static abstract class ActionDomainEvent<T>
            extends CausewayModuleExtCommandLogApplib.ActionDomainEvent<T> { }

    private final CommandLogEntryRepository commandLogEntryRepository;
    private final ClockService clockService;
    private final ReplayContext replayContext;

    @Action(
            commandPublishing = Publishing.DISABLED,
            domainEvent = activeCommands.DomainEvent.class,
            executionPublishing = Publishing.DISABLED,
            semantics = SemanticsOf.SAFE,
            typeOf = CommandLogEntry.class
    )
    @ActionLayout(cssClassFa = "fa-bolt", sequence="10")
    public class activeCommands {
        public class DomainEvent extends ActionDomainEvent<activeCommands> { }

        @MemberSupport public List<? extends CommandLogEntry> act() {
            return commandLogEntryRepository.findCurrent();
        }
    }

    @Action(
            commandPublishing = Publishing.DISABLED,
            domainEvent = findMostRecent.DomainEvent.class,
            executionPublishing = Publishing.DISABLED,
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
            commandPublishing = Publishing.DISABLED,
            domainEvent = findCommands.DomainEvent.class,
            executionPublishing = Publishing.DISABLED,
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
            commandPublishing = Publishing.DISABLED,
            domainEvent = findAll.DomainEvent.class,
            executionPublishing = Publishing.DISABLED,
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

    @Action(
            commandPublishing = Publishing.DISABLED,
            domainEvent = exportManager.DomainEvent.class,
            executionPublishing = Publishing.DISABLED,
            restrictTo = RestrictTo.PROTOTYPING,
            semantics = SemanticsOf.SAFE
    )
    @ActionLayout(cssClassFa = "solid share-from-square", sequence="50")
    public class exportManager {
        public class DomainEvent extends ActionDomainEvent<exportManager> { }

        @MemberSupport public CommandExportManager act(
                @ParameterLayout(
                        describedAs = "Limits the commands shown; "
                            + "only commands since this timestamp are available for export. "
                            + "Set to a time immediately before the commands to be replayed.")
                final java.sql.Timestamp since
        ) {
            return new CommandExportManager(since, replayContext);
        }

        @MemberSupport public java.sql.Timestamp defaultSince() {
            final var now = clockService.getClock().nowAsJavaSqlTimestamp();
            return truncatedTo(now, ChronoUnit.HOURS);
        }
    }

    @Action(
            commandPublishing = Publishing.DISABLED,
            domainEvent = replayManager.DomainEvent.class,
            executionPublishing = Publishing.DISABLED,
            restrictTo = RestrictTo.PROTOTYPING,
            semantics = SemanticsOf.SAFE
    )
    @ActionLayout(cssClassFa = "solid circle-play", sequence="51")
    public class replayManager {
        public class DomainEvent extends ActionDomainEvent<replayManager> { }

        @MemberSupport public CommandReplayManager act(
                @ParameterLayout(
                        describedAs = "Limits the commands shown; "
                            + "only commands since this timestamp are available for replay. "
                            + "Set to a time immediately before the commands to be replayed.")
                final java.sql.Timestamp since
        ) {
            return new CommandReplayManager(since, replayContext);
        }

        @MemberSupport public java.sql.Timestamp defaultSince() {
            final var now = clockService.getClock().nowAsJavaSqlTimestamp();
            return truncatedTo(now, ChronoUnit.HOURS);
        }
    }

    private static Timestamp truncatedTo(final Timestamp now, final ChronoUnit chronoUnit) {
        return Timestamp.from(now.toInstant().truncatedTo(chronoUnit));
    }

    private LocalDate now() {
        return clockService.getClock().nowAsLocalDate(ZoneId.systemDefault());
    }
}
