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
package org.apache.causeway.extensions.commandlog.applib.dom.replay;

import static org.apache.causeway.extensions.commandlog.applib.dom.replay.TimestampMarshallUtil.fromString;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.chrono.ChronoZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.causeway.applib.ViewModel;
import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.ActionLayout;
import org.apache.causeway.applib.annotation.Collection;
import org.apache.causeway.applib.annotation.CollectionLayout;
import org.apache.causeway.applib.annotation.DomainObject;
import org.apache.causeway.applib.annotation.DomainObjectLayout;
import org.apache.causeway.applib.annotation.Introspection;
import org.apache.causeway.applib.annotation.MemberSupport;
import org.apache.causeway.applib.annotation.ObjectSupport;
import org.apache.causeway.applib.annotation.ParameterLayout;
import org.apache.causeway.applib.annotation.Property;
import org.apache.causeway.applib.annotation.PropertyLayout;
import org.apache.causeway.applib.annotation.Publishing;
import org.apache.causeway.applib.annotation.RestrictTo;
import org.apache.causeway.applib.annotation.SemanticsOf;
import org.apache.causeway.applib.util.schema.CommandDtoUtils;
import org.apache.causeway.applib.value.Clob;
import org.apache.causeway.applib.value.NamedWithMimeType.CommonMimeType;
import org.apache.causeway.extensions.commandlog.applib.CausewayModuleExtCommandLogApplib;
import org.apache.causeway.extensions.commandlog.applib.dom.CommandLogEntry;
import org.apache.causeway.extensions.commandlog.applib.dom.CommandLogEntryRepository;
import org.apache.causeway.extensions.commandlog.applib.dom.ReplayState;

import lombok.Getter;

@DomainObject(introspection = Introspection.ANNOTATION_REQUIRED)
@DomainObjectLayout(cssClassFa = "solid share-from-square")
@Named(CommandExportManager.LOGICAL_TYPE_NAME)
public final class CommandExportManager implements ViewModel {

	public static final String LOGICAL_TYPE_NAME = CausewayModuleExtCommandLogApplib.NAMESPACE + ".CommandExportManager";

    public static abstract class ActionDomainEvent<T>
            extends CausewayModuleExtCommandLogApplib.ActionDomainEvent<T> { }

    private ReplayContext replayContext;

    @Inject
    public CommandExportManager(
            final String memento,
            final ReplayContext replayContext) {
        this(fromString(memento, replayContext.clockService().getClock().nowAsJavaSqlTimestamp()),  replayContext);
    }

    public CommandExportManager(
            final java.sql.Timestamp since,
            final ReplayContext replayContext) {
        this.since = since;
        this.replayContext = replayContext;
    }

    @ObjectSupport public String title() {
        return "Command Export Manager";
    }


    @Property
    @PropertyLayout(describedAs = "Only commands since this timestamp are available for export")
    @Getter
    private java.sql.Timestamp since;

    @Action(
            semantics = SemanticsOf.SAFE,
            commandPublishing = Publishing.DISABLED,
            domainEvent = previousHour.DomainEvent.class,
            executionPublishing = Publishing.DISABLED
    )
    @ActionLayout(
            associateWith = "since", sequence = "1",
            named = "Previous",
            position = ActionLayout.Position.PANEL,
            describedAs = "Move back one hour"
    )
    public class previousHour {
        public class DomainEvent extends ActionDomainEvent<previousHour> { }

        @MemberSupport public CommandExportManager act() {
            return new CommandExportManager(addSeconds(since, -3600), replayContext);
        }
    }

    @Action(
            semantics = SemanticsOf.SAFE,
            commandPublishing = Publishing.DISABLED,
            domainEvent = nextHour.DomainEvent.class,
            executionPublishing = Publishing.DISABLED
    )
    @ActionLayout(
            associateWith = "since", sequence = "3",
            named = "Next",
            position = ActionLayout.Position.PANEL,
            describedAs = "Move forward one hour"
    )
    public class nextHour {
        public class DomainEvent extends ActionDomainEvent<nextHour> { }
        @MemberSupport public CommandExportManager act() {
            return new CommandExportManager(addSeconds(since, +3600), replayContext);
        }
    }

    @Action(
            restrictTo = RestrictTo.PROTOTYPING,
            semantics = SemanticsOf.SAFE,
            commandPublishing = Publishing.DISABLED,
            domainEvent = changeSince.DomainEvent.class,
            executionPublishing = Publishing.DISABLED
    )
    @ActionLayout(
            associateWith = "since", sequence = "2",
            named = "Change",
            position = ActionLayout.Position.PANEL
    )
    public class changeSince {
        public class DomainEvent extends ActionDomainEvent<nextHour> { }
        @MemberSupport public CommandExportManager act(final java.sql.Timestamp since) {
            return new CommandExportManager(since, replayContext);
        }
        @MemberSupport public java.sql.Timestamp defaultSince() {
            return CommandExportManager.this.since;
        }
    }

    private static Timestamp addSeconds(Timestamp since, int secondsToAdd) {
        return Timestamp.from(since.toInstant().plusSeconds(secondsToAdd));
    }


    // -- NOT YET EXPORTED

    @Collection
    @CollectionLayout(
            describedAs = "Commands that can be exported"
    )
    public List<ReplayableCommand> getNotYetExported() {
        return commandLogEntryRepository().findForegroundSinceTimestampAndCanBeExported(since).stream()
            .map(entry->new ReplayableCommand(
                    entry.getInteractionId(),
                    replayContext))
            .collect(Collectors.toList());
    }

    @Action(
            restrictTo = RestrictTo.PROTOTYPING,
            choicesFrom = "notYetExported",
            semantics = SemanticsOf.NON_IDEMPOTENT,
            commandPublishing = Publishing.DISABLED,
            domainEvent = exportSelected.DomainEvent.class,
            executionPublishing = Publishing.DISABLED
    )
    @ActionLayout(
            associateWith = "notYetExported", sequence = "1.1",
            cssClassFa = "solid share-from-square",
            cssClass = "btn-primary",
            describedAs = "Exports selected Commands as zipped DTOs for import later. "
                        + "Refresh the page to see changed states."
    )
    public class exportSelected {
        public class DomainEvent extends ActionDomainEvent<exportSelected> { }

        @MemberSupport public Clob act(
                final List<ReplayableCommand> selected,
                @ParameterLayout(describedAs = "File name for the exported file." )
                final String filenamePrefix,
                @ParameterLayout(describedAs = "Whether to add a timestamp suffix to the exported file's name." )
                final boolean filenameTimestamp) {

            var selectedCommandLogEntries = selected.stream()
                .map(ReplayableCommand::commandLogEntry)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .filter(entry->!ReplayState.isExported(entry.getReplayState())) // shouldn't be necessary unless a race condition
                .sorted()
                .collect(Collectors.toList());

            var yaml = CommandDtoUtils.toYaml(
                selectedCommandLogEntries.stream()
                    .map(CommandLogEntry::getCommandDto)
                    .collect(Collectors.toList()));

            final var replayableCommand = selected.get(0);  // validate ensures there is at least one command
            final var timestamp = filenameTimestamp
                    ? replayableCommand.getTimestampIfAny()
                        .map(ChronoZonedDateTime::toInstant)
                        .map(Instant::toString)
                        .map(x -> "." + x.replaceAll("[^A-Za-z0-9._-]", "_"))   // make safe within filename
                        .orElse("")
                    : "";
            final var filename = filenamePrefix + timestamp;

            var clob = Clob.of(filename, CommonMimeType.YAML, yaml);

            // do this last once we have successfully created the Clob
            selectedCommandLogEntries.forEach(c->c.setReplayState(ReplayState.EXPORTED));

            return clob;
        }

        @MemberSupport public String disableAct() {
            return getNotYetExported().isEmpty() ? "No commands in collection" : null;
        }

        @MemberSupport public String defaultFilenamePrefix() {
            return "commands";
        }

        @MemberSupport public boolean defaultFilenameTimestamp() {
            return true;
        }

        @MemberSupport public String validateSelected(final List<ReplayableCommand> selected) {
            return selected != null && selected.isEmpty() ? "Select at least one command to export" : null;
        }

        // TODO: shouldn't be required because of 'choicesFrom', but in v2 there seems to be a MM validation error due to a missing choicesFacet
        @MemberSupport
        public List<ReplayableCommand> choicesSelected() {
            return getNotYetExported();
        }
    }


    // -- EXPORTED

    @Collection
    @CollectionLayout(describedAs = "Commands that have been exported")
    public List<ReplayableCommand> getExported() {
        return commandLogEntryRepository().findForegroundSinceTimestampAndHasBeenExported(since).stream()
            .map(entry->new ReplayableCommand(
                    entry.getInteractionId(),
                    replayContext))
            .collect(Collectors.toList());
    }


    @Action(
            restrictTo = RestrictTo.PROTOTYPING,
            choicesFrom = "exported",
            commandPublishing = Publishing.DISABLED,
            semantics =  SemanticsOf.IDEMPOTENT,
            domainEvent = makeSelectedExportable.DomainEvent.class,
            executionPublishing = Publishing.DISABLED
    )
    @ActionLayout(
            associateWith = "exported", sequence = "2.1",
            describedAs = "Makes selected Commands exportable (again)"
    )
    public class makeSelectedExportable {
        public class DomainEvent extends ActionDomainEvent<makeSelectedExportable> { }

        @MemberSupport
        public CommandExportManager act(final List<ReplayableCommand> selected) {
            selected.forEach(ReplayableCommand::makeExportable); // filtered on its own responsibility
            return CommandExportManager.this;
        }

        @MemberSupport
        public String disableAct() {
            return getExported().isEmpty() ? "No commands in collection" : null;
        }

        @MemberSupport
        public String validateSelected(final List<ReplayableCommand> selected) {
            return selected != null && selected.isEmpty() ? "Select at least one command" : null;
        }

        // TODO: shouldn't be required because of 'choicesFrom', but in v2 there seems to be a MM validation error due to a missing choicesFacet
        @MemberSupport
        public List<ReplayableCommand> choicesSelected() {
            return getExported();
        }
    }


    // -- VM STATE

    @Override
    public String viewModelMemento() {
        return TimestampMarshallUtil.toString(this.since);
    }

    // -- HELPER
    private CommandLogEntryRepository commandLogEntryRepository() {
        return replayContext.commandLogEntryRepository();
    }
}
