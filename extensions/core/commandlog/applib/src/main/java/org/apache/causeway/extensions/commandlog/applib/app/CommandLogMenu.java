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
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.ActionLayout;
import org.apache.causeway.applib.annotation.DomainService;
import org.apache.causeway.applib.annotation.DomainServiceLayout;
import org.apache.causeway.applib.annotation.MemberSupport;
import org.apache.causeway.applib.annotation.Optionality;
import org.apache.causeway.applib.annotation.Parameter;
import org.apache.causeway.applib.annotation.ParameterLayout;
import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.applib.annotation.Publishing;
import org.apache.causeway.applib.annotation.RestrictTo;
import org.apache.causeway.applib.annotation.SemanticsOf;
import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.applib.services.clock.ClockService;
import org.apache.causeway.applib.services.factory.FactoryService;
import org.apache.causeway.applib.services.message.MessageService;
import org.apache.causeway.applib.value.Blob;
import org.apache.causeway.extensions.commandlog.applib.CausewayModuleExtCommandLogApplib;
import org.apache.causeway.extensions.commandlog.applib.dom.CommandLogEntry;
import org.apache.causeway.extensions.commandlog.applib.dom.CommandLogEntryRepository;
import org.apache.causeway.extensions.commandlog.applib.dom.CommandReplayResultMapping;
import org.apache.causeway.extensions.commandlog.applib.dom.CommandReplayResultMappingRepository;
import org.apache.causeway.extensions.commandlog.applib.dom.replay.CommandManagerExport;
import org.apache.causeway.extensions.commandlog.applib.dom.replay.CommandManagerExport_changeLimit;
import org.apache.causeway.extensions.commandlog.applib.dom.replay.CommandManagerReplay;
import org.apache.causeway.extensions.commandlog.applib.dom.replay.CommandManagerReplay_importCommands;
import org.apache.causeway.extensions.commandlog.applib.dom.replay.ReplayContext;
import org.jspecify.annotations.NonNull;
import org.springframework.lang.Nullable;

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
@javax.annotation.Priority(PriorityPrecedence.EARLY)
@RequiredArgsConstructor(onConstructor_ = { @Inject })
public class CommandLogMenu {

    public static final String LOGICAL_TYPE_NAME =
            CausewayModuleExtCommandLogApplib.NAMESPACE + ".CommandLogMenu";

    public static abstract class ActionDomainEvent<T>
            extends CausewayModuleExtCommandLogApplib.ActionDomainEvent<T> { }


    final CommandLogEntryRepository commandLogEntryRepository;
    final Optional<CommandReplayResultMappingRepository> commandReplayResultMappingRepository;
    final ClockService clockService;
    final ReplayContext replayContext;
    final MessageService messageService;

    @Action(
            commandPublishing = Publishing.DISABLED,
            domainEvent = activeCommands.DomainEvent.class,
            executionPublishing = Publishing.DISABLED,
            semantics = SemanticsOf.SAFE,
            typeOf = CommandLogEntry.class
    )
    @ActionLayout(cssClassFa = "fa-bolt", sequence="10")
    public class activeCommands {
        public class DomainEvent extends ActionDomainEvent<activeCommands> {
            public DomainEvent() { }
        }

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
        public class DomainEvent extends ActionDomainEvent<findCommands> {
            public DomainEvent() { }
        }

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
        public class DomainEvent extends ActionDomainEvent<findAll> {
            public DomainEvent() { }
        }

        @MemberSupport public List<? extends CommandLogEntry> act() {
            return commandLogEntryRepository.findAll();
        }
    }

    @Action(
            commandPublishing = Publishing.DISABLED,
            domainEvent = findReplayResultMappings.DomainEvent.class,
            executionPublishing = Publishing.DISABLED,
            semantics = SemanticsOf.SAFE,
            typeOf = CommandReplayResultMapping.class
    )
    @ActionLayout(cssClassFa = "fa-search", sequence="52")
    public class findReplayResultMappings {
        public class DomainEvent extends ActionDomainEvent<findReplayResultMappings> {
            public DomainEvent() { }
        }

        @MemberSupport public List<? extends CommandReplayResultMapping> act() {
            return commandReplayResultMappingRepository
                    .map(CommandReplayResultMappingRepository::findAll)
                    .orElseGet(List::of);
        }

        @MemberSupport public boolean hideAct() {
            return commandReplayResultMappingRepository.isEmpty();
        }
    }

    @Action(
            commandPublishing = Publishing.DISABLED,
            domainEvent = findChangedReplayResultMappings.DomainEvent.class,
            executionPublishing = Publishing.DISABLED,
            semantics = SemanticsOf.SAFE,
            typeOf = CommandReplayResultMapping.class
    )
    @ActionLayout(cssClassFa = "fa-search", sequence="53")
    public class findChangedReplayResultMappings {
        public class DomainEvent extends ActionDomainEvent<findChangedReplayResultMappings> {
            public DomainEvent() { }
        }

        @MemberSupport public List<? extends CommandReplayResultMapping> act() {
            return commandReplayResultMappingRepository
                    .map(CommandReplayResultMappingRepository::findChanged)
                    .orElseGet(List::of);
        }

        @MemberSupport public boolean hideAct() {
            return commandReplayResultMappingRepository.isEmpty();
        }
    }

    @Action(
            commandPublishing = Publishing.DISABLED,
            domainEvent = findReplayResultMappingByRecordedBookmark.DomainEvent.class,
            executionPublishing = Publishing.DISABLED,
            semantics = SemanticsOf.SAFE,
            typeOf = CommandReplayResultMapping.class
    )
    @ActionLayout(cssClassFa = "fa-search", sequence="54")
    public class findReplayResultMappingByRecordedBookmark {
        public class DomainEvent extends ActionDomainEvent<findReplayResultMappingByRecordedBookmark> {
            public DomainEvent() { }
        }

        @MemberSupport public List<? extends CommandReplayResultMapping> act(
                @Parameter(optionality = Optionality.MANDATORY)
                final Bookmark recordedBookmark) {
            return commandReplayResultMappingRepository
                    .flatMap(repository -> repository.findByRecordedBookmark(recordedBookmark))
                    .map(List::of)
                    .orElseGet(List::of);
        }

        @MemberSupport public boolean hideAct() {
            return commandReplayResultMappingRepository.isEmpty();
        }
    }

    @Action(
            commandPublishing = Publishing.DISABLED,
            domainEvent = findReplayResultMappingsByActualBookmark.DomainEvent.class,
            executionPublishing = Publishing.DISABLED,
            semantics = SemanticsOf.SAFE,
            typeOf = CommandReplayResultMapping.class
    )
    @ActionLayout(cssClassFa = "fa-search", sequence="55")
    public class findReplayResultMappingsByActualBookmark {
        public class DomainEvent extends ActionDomainEvent<findReplayResultMappingsByActualBookmark> {
            public DomainEvent() { }
        }

        @MemberSupport public List<? extends CommandReplayResultMapping> act(
                @Parameter(optionality = Optionality.MANDATORY)
                final Bookmark actualBookmark) {
            return commandReplayResultMappingRepository
                    .map(repository -> repository.findByActualBookmark(actualBookmark))
                    .orElseGet(List::of);
        }

        @MemberSupport public boolean hideAct() {
            return commandReplayResultMappingRepository.isEmpty();
        }
    }

    @Action(
            commandPublishing = Publishing.DISABLED,
            domainEvent = deleteReplayResultMappings.DomainEvent.class,
            executionPublishing = Publishing.DISABLED,
            semantics = SemanticsOf.IDEMPOTENT_ARE_YOU_SURE
    )
    @ActionLayout(cssClassFa = "fa-trash", sequence="56")
    public class deleteReplayResultMappings {
        public class DomainEvent extends ActionDomainEvent<deleteReplayResultMappings> {
            public DomainEvent() { }
        }

        @MemberSupport public void act() {
            commandReplayResultMappingRepository.ifPresent(repository -> {
                final var count = repository.findAll().size();
                repository.removeAll();
                messageService.informUser(String.format("Deleted %d command replay result mapping%s", count, count == 1 ? "" : "s"));
            });
        }

        @MemberSupport public boolean hideAct() {
            return commandReplayResultMappingRepository.isEmpty();
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
        public class DomainEvent extends ActionDomainEvent<exportManager> {
            public DomainEvent() { }
        }

        @MemberSupport public CommandManagerExport act(
                @ParameterLayout(describedAs = "Limits the commands shown; only commands since this timestamp are available for export.  Set to a time immediately before the commands to be replayed.")
                final java.sql.Timestamp since
                ) {
            return new CommandManagerExport(new CommandManagerExport.State(since, CommandManagerExport_changeLimit.MAX_LIMIT), replayContext);
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
        public class DomainEvent extends ActionDomainEvent<replayManager> {
            public DomainEvent() { }
        }

        @MemberSupport public CommandManagerReplay act(
                @Parameter(
                        optionality = Optionality.OPTIONAL,
                        fileAccept = ".yml,.yaml"
                )
                final Blob commandsYaml
        ) {
            final var now = clockService.getClock().nowAsJavaSqlTimestamp();
            final var commandReplayManager = new CommandManagerReplay(now, replayContext);
            return Optional.ofNullable(commandsYaml)
                    .map(yaml -> importCommands(commandReplayManager).act(yaml, true))
                    .orElse(commandReplayManager);
        }

        private CommandManagerReplay_importCommands importCommands(CommandManagerReplay commandReplayManager) {
            return factoryService.mixin(CommandManagerReplay_importCommands.class, commandReplayManager);
        }
    }

    private static @NonNull Timestamp truncatedTo(Timestamp now, ChronoUnit chronoUnit) {
        return Timestamp.from(now.toInstant().truncatedTo(chronoUnit));
    }

    private LocalDate now() {
        return clockService.getClock().nowAsLocalDate(ZoneId.systemDefault());
    }

    @Inject private FactoryService factoryService;
}
