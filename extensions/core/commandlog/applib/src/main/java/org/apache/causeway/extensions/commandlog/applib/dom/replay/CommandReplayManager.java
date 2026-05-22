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
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;
import javax.inject.Named;
import javax.xml.datatype.XMLGregorianCalendar;

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
import org.apache.causeway.applib.annotation.Parameter;
import org.apache.causeway.applib.annotation.ParameterLayout;
import org.apache.causeway.applib.annotation.Property;
import org.apache.causeway.applib.annotation.PropertyLayout;
import org.apache.causeway.applib.annotation.Publishing;
import org.apache.causeway.applib.annotation.RestrictTo;
import org.apache.causeway.applib.annotation.SemanticsOf;
import org.apache.causeway.applib.util.schema.CommandDtoUtils;
import org.apache.causeway.applib.value.Blob;
import org.apache.causeway.extensions.commandlog.applib.CausewayModuleExtCommandLogApplib;
import org.apache.causeway.extensions.commandlog.applib.dom.CommandLogEntryRepository;
import org.apache.causeway.schema.cmd.v2.CommandDto;

import org.jspecify.annotations.NonNull;

import lombok.Getter;

@DomainObject(introspection = Introspection.ANNOTATION_REQUIRED)
@DomainObjectLayout(cssClassFa = "solid circle-play")
@Named(CommandReplayManager.LOGICAL_TYPE_NAME)
public final class CommandReplayManager implements ViewModel {

    public static final String LOGICAL_TYPE_NAME = CausewayModuleExtCommandLogApplib.NAMESPACE + ".CommandReplayManager";

    public static abstract class ActionDomainEvent<T>
            extends CausewayModuleExtCommandLogApplib.ActionDomainEvent<T> { }

    private ReplayContext replayContext;

    @Inject
    public CommandReplayManager(
            final String memento,
            final ReplayContext replayContext) {
        this(fromString(memento, replayContext.clockService().getClock().nowAsJavaSqlTimestamp()),  replayContext);
    }

    public CommandReplayManager(
            final java.sql.Timestamp baseline,
            final ReplayContext replayContext) {
        this.baseline = baseline;
        this.replayContext = replayContext;
    }

    @ObjectSupport public String title() {
        return "Command Replay Manager";
    }


    @Property
    @PropertyLayout(describedAs = "Only commands after this baseline are listed for replay")
    @Getter
    private java.sql.Timestamp baseline;

    @Action(
            semantics = SemanticsOf.SAFE,
            commandPublishing = Publishing.DISABLED,
            domainEvent = previousHour.DomainEvent.class,
            executionPublishing = Publishing.DISABLED
    )
    @ActionLayout(
            associateWith = "baseline", sequence = "1",
            named = "Previous",
            position = ActionLayout.Position.PANEL,
            describedAs = "Move back one hour"
    )
    public class previousHour {
        public class DomainEvent extends ActionDomainEvent<previousHour> { }

        @MemberSupport public CommandReplayManager act() {
            return new CommandReplayManager(addSeconds(baseline, -3600), replayContext);
        }
    }

    @Action(
            semantics = SemanticsOf.SAFE,
            commandPublishing = Publishing.DISABLED,
            domainEvent = nextHour.DomainEvent.class,
            executionPublishing = Publishing.DISABLED
    )
    @ActionLayout(
            associateWith = "baseline", sequence = "3",
            named = "Next",
            position = ActionLayout.Position.PANEL,
            describedAs = "Move forward one hour"
    )
    public class nextHour {
        public class DomainEvent extends ActionDomainEvent<nextHour> { }
        @MemberSupport public CommandReplayManager act() {
            return new CommandReplayManager(addSeconds(baseline, +3600), replayContext);
        }
    }

    @Action(
            restrictTo = RestrictTo.PROTOTYPING,
            semantics = SemanticsOf.SAFE,
            commandPublishing = Publishing.DISABLED,
            domainEvent = changeBaseline.DomainEvent.class,
            executionPublishing = Publishing.DISABLED
    )
    @ActionLayout(
            associateWith = "baseline", sequence = "2",
            named = "Change",
            position = ActionLayout.Position.PANEL
    )
    public class changeBaseline {
        public class DomainEvent extends ActionDomainEvent<nextHour> { }
        @MemberSupport public CommandReplayManager act(final java.sql.Timestamp baseline) {
            return new CommandReplayManager(baseline, replayContext);
        }
        @MemberSupport public java.sql.Timestamp defaultBaseline() {
            return CommandReplayManager.this.baseline;
        }
    }

    private static Timestamp addSeconds(Timestamp ts, int secondsToAdd) {
        return Timestamp.from(ts.toInstant().plusSeconds(secondsToAdd));
    }

    @Action(
            restrictTo = RestrictTo.PROTOTYPING,
            semantics = SemanticsOf.IDEMPOTENT,
            commandPublishing = Publishing.DISABLED,
            domainEvent = importCommands.DomainEvent.class,
            executionPublishing = Publishing.DISABLED
    )
    @ActionLayout(
            sequence = "1.1",
            cssClass = "btn-secondary",
            describedAs = "Imports commands from yaml format, then persists them with a replayState of PENDING."
    )
    public class importCommands {
        public class DomainEvent extends ActionDomainEvent<importCommands> { }
        public CommandReplayManager act(
                @Parameter(fileAccept = ".yml,.yaml")
                final Blob commandsYaml,
                @ParameterLayout(describedAs = "Change the baseline to the timestamp of the oldest, so that they are listed at top")
                final boolean moveBaselineToOldest) {
            var yamlDs = commandsYaml.asDataSource();

            final List<CommandDto> commandDtos = CommandDtoUtils.fromYaml(yamlDs);
            commandDtos.forEach(commandLogEntryRepository()::saveForReplay);

            return commandDtos.stream()
                    .filter(x -> moveBaselineToOldest)
                    .map(CommandDto::getTimestamp)
                    .map(CommandReplayManager::toJavaSqlTimestamp)
                    .sorted()
                    .findFirst()
                    .map(timestamp -> new CommandReplayManager(timestamp, replayContext))
                    .orElse(CommandReplayManager.this);
        }

        @MemberSupport public boolean defaultMoveBaselineToOldest() {
            return true;
        }

    }

    private static Timestamp toJavaSqlTimestamp(XMLGregorianCalendar xgc) {
        if (xgc == null) return null;
        Instant instant = xgc.toGregorianCalendar().toZonedDateTime().toInstant();
        return Timestamp.from(instant);
    }


    // -- PENDING OR FAILED

    @Collection
    @CollectionLayout(
            describedAs = "Imported Commands that can be either replayed (if PENDING) or retried (if FAILED)"
    )
    public List<ReplayableCommand> getPendingOrFailed() {
        return streamPendingOrFailed()
            .collect(Collectors.toList());
    }

    private @NonNull Stream<ReplayableCommand> streamPendingOrFailed() {
        return commandLogEntryRepository().findForegroundSinceTimestampAndWithReplayPendingOrFailed(baseline).stream()
                .map(entry -> new ReplayableCommand(
                        entry.getInteractionId(),
                        replayContext));
    }

    private long sizePendingOrFailed() {
        return streamPendingOrFailed().count();
    }


    @Action(
            restrictTo = RestrictTo.PROTOTYPING,
            choicesFrom = "pendingOrFailed",
            semantics = SemanticsOf.NON_IDEMPOTENT,
            commandPublishing = Publishing.DISABLED,
            domainEvent = replayOrRetrySelected.DomainEvent.class,
            executionPublishing = Publishing.DISABLED
    )
    @ActionLayout(
            associateWith = "pendingOrFailed", sequence = "1.2",
            cssClass = "btn-secondary",
            cssClassFa = "solid forward",
            describedAs = "Executes the list of commands in sequence, after having sorted them by their timestamp. "
                    + "If any of the given commands fails, "
                    + "its surrounding transaction is rolled back, but any successful commands so far are marked OK). "
                    + "The command, that caused the failure, gets marked FAILED.")
    public class replayOrRetrySelected {
        public class DomainEvent extends ActionDomainEvent<replayOrRetrySelected> { }
        @MemberSupport public CommandReplayManager act(final List<ReplayableCommand> selected) {
            var replayables = selected.stream()
                .sorted()
                .collect(Collectors.toList());
            for(var replayableCommand : replayables) {
                var tryReplayOrRetry = replayableCommand.tryReplayOrRetry(); // filtered on its own responsibility
                if(tryReplayOrRetry.isFailure()) {
                    return CommandReplayManager.this; // stop further execution
                }
            }
            return CommandReplayManager.this;
        }


        @MemberSupport
        public String disableAct() {
            return getPendingOrFailed().isEmpty() ? "No commands in collection" : null;
        }

        @MemberSupport
        public String validateSelected(final List<ReplayableCommand> selected) {
            return selected != null && selected.isEmpty() ? "Select at least one command" : null;
        }

        // TODO: shouldn't be required because of 'choicesFrom', but in v2 there seems to be a MM validation error due to a missing choicesFacet
        @MemberSupport
        public List<ReplayableCommand> choicesSelected() {
            return getPendingOrFailed();
        }
    }



    @Action(
            restrictTo = RestrictTo.PROTOTYPING,
            choicesFrom = "pendingOrFailed",
            semantics = SemanticsOf.NON_IDEMPOTENT,
            commandPublishing = Publishing.DISABLED,
            domainEvent = replayOrRetrySelected.DomainEvent.class,
            executionPublishing = Publishing.DISABLED
    )
    @ActionLayout(
            associateWith = "pendingOrFailed", sequence = "1.1",
            cssClassFa = "solid circle-play",
            cssClass = "btn-primary",
            describedAs = "Executes the oldest command.")
    public class replayOrRetryNext {
        public class DomainEvent extends ActionDomainEvent<replayOrRetrySelected> { }
        @MemberSupport public CommandReplayManager act() {
            var nextIfAny = streamPendingOrFailed().findFirst();
            // should always be present, due to our guard
            nextIfAny.ifPresent(ReplayableCommand::tryReplayOrRetry);
            return CommandReplayManager.this;
        }

        @MemberSupport
        public String disableAct() {
            return sizePendingOrFailed() == 0 ? "No commands in collection" : null;
        }
    }



    @Action(
            restrictTo = RestrictTo.PROTOTYPING,
            choicesFrom = "pendingOrFailed",
            semantics = SemanticsOf.NON_IDEMPOTENT,
            commandPublishing = Publishing.DISABLED,
            domainEvent = excludeSelectedFromReplay.DomainEvent.class,
            executionPublishing = Publishing.DISABLED
    )
    @ActionLayout(
            associateWith = "pendingOrFailed", sequence = "1.3",
            cssClass = "btn-secondary",
            describedAs = "Marks selected Commands to be EXCLUDED from replay"
    )
    public class excludeSelectedFromReplay {
        public class DomainEvent extends ActionDomainEvent<excludeSelectedFromReplay> { }
        @MemberSupport
        public CommandReplayManager act(final List<ReplayableCommand> selected) {
            selected.stream()
                .forEach(ReplayableCommand::excludeFromReplay); // filtered on its own responsibility
            return CommandReplayManager.this;
        }

        @MemberSupport
        public String disableAct() {
            return sizePendingOrFailed() == 0 ? "No commands in collection" : null;
        }

        @MemberSupport
        public String validateSelected(final List<ReplayableCommand> selected) {
            return selected != null && selected.isEmpty() ? "Select at least one command" : null;
        }

        // TODO: shouldn't be required because of 'choicesFrom', but in v2 there seems to be a MM validation error due to a missing choicesFacet
        @MemberSupport
        public List<ReplayableCommand> choicesSelected() {
            return getPendingOrFailed();
        }

    }



    @Action(
            restrictTo = RestrictTo.PROTOTYPING,
            choicesFrom = "pendingOrFailed",
            semantics = SemanticsOf.NON_IDEMPOTENT,
            commandPublishing = Publishing.DISABLED,
            domainEvent = deleteSelectedPendingOrFailed.DomainEvent.class,
            executionPublishing = Publishing.DISABLED
    )
    @ActionLayout(
            associateWith = "pendingOrFailed", sequence = "1.4",
            cssClass = "btn-danger",
            describedAs = "Deletes selected Commands (cannot be undone)"
    )
    public class deleteSelectedPendingOrFailed {
        public class DomainEvent extends ActionDomainEvent<deleteSelectedPendingOrFailed> { }
        public CommandReplayManager act(final List<ReplayableCommand> selected) {
            selected.stream()
                .forEach(ReplayableCommand::deleteObj); // filtered on its own responsibility
            return CommandReplayManager.this;
        }

        @MemberSupport
        public String disableAct() {
            return getPendingOrFailed().isEmpty() ? "No commands in collection" : null;
        }

        @MemberSupport
        public String validateSelected(final List<ReplayableCommand> selected) {
            return selected != null && selected.isEmpty() ? "Select at least one command" : null;
        }

        // TODO: shouldn't be required because of 'choicesFrom', but in v2 there seems to be a MM validation error due to a missing choicesFacet
        @MemberSupport
        public List<ReplayableCommand> choicesSelected() {
            return getPendingOrFailed();
        }

    }



    // -- OK OR EXCLUDE

    @Collection
    @CollectionLayout(
            describedAs = "Imported Commands that were either replayed with success (replayState=OK) "
                    + "or marked to be excluded from replay (replayState=EXCLUDE)"
    )
    public List<ReplayableCommand> getSucceededOrExcluded() {
        return commandLogEntryRepository().findSinceAndWithReplayOkOrExcluded(baseline).stream()
            .map(entry->new ReplayableCommand(
                    entry.getInteractionId(),
                    replayContext))
            .collect(Collectors.toList());
    }


    @Action(
            restrictTo = RestrictTo.PROTOTYPING,
            choicesFrom = "succeededOrExcluded",
            semantics = SemanticsOf.IDEMPOTENT,
            domainEvent = deleteSelectedSucceededOrExcluded.DomainEvent.class,
            executionPublishing = Publishing.DISABLED
    )
    @ActionLayout(
            associateWith = "succeededOrExcluded",
            named = "Delete Selected",
            describedAs = "Deletes selected Commands (cannot be undone)"
    )
    public class deleteSelectedSucceededOrExcluded {
        public class DomainEvent extends ActionDomainEvent<deleteSelectedSucceededOrExcluded> { }
        public CommandReplayManager act(final List<ReplayableCommand> selected) {
            selected.stream()
                .forEach(ReplayableCommand::deleteObj); // filtered on its own responsibility
            return CommandReplayManager.this;
        }

        @MemberSupport
        public String disableAct() {
            return getSucceededOrExcluded().isEmpty() ? "No commands in collection" : null;
        }

        @MemberSupport
        public String validateSelected(final List<ReplayableCommand> selected) {
            return selected != null && selected.isEmpty() ? "Select at least one command" : null;
        }

        // TODO: shouldn't be required because of 'choicesFrom', but in v2 there seems to be a MM validation error due to a missing choicesFacet
        @MemberSupport
        public List<ReplayableCommand> choicesSelected() {
            return getSucceededOrExcluded();
        }
    }


    // -- VM STATE

    @Override
    public String viewModelMemento() {
        return TimestampMarshallUtil.toString(this.baseline);
    }

    // -- HELPER
    private CommandLogEntryRepository commandLogEntryRepository() {
        return replayContext.commandLogEntryRepository();
    }
}
