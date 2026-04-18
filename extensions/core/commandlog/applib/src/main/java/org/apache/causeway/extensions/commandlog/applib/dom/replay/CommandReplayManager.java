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

import java.util.List;

import jakarta.inject.Inject;
import jakarta.inject.Named;

import org.apache.causeway.applib.ViewModel;
import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.ActionLayout;
import org.apache.causeway.applib.annotation.Collection;
import org.apache.causeway.applib.annotation.CollectionLayout;
import org.apache.causeway.applib.annotation.DomainObject;
import org.apache.causeway.applib.annotation.DomainObjectLayout;
import org.apache.causeway.applib.annotation.Introspection;
import org.apache.causeway.applib.annotation.ObjectSupport;
import org.apache.causeway.applib.annotation.Parameter;
import org.apache.causeway.applib.annotation.SemanticsOf;
import org.apache.causeway.applib.util.schema.CommandDtoUtils;
import org.apache.causeway.applib.value.Blob;
import org.apache.causeway.applib.value.NamedWithMimeType.CommonMimeType;
import org.apache.causeway.extensions.commandlog.applib.CausewayModuleExtCommandLogApplib;
import org.apache.causeway.extensions.commandlog.applib.dom.CommandLogEntryRepository;
import org.apache.causeway.schema.cmd.v2.CommandDto;

@DomainObject(introspection = Introspection.ANNOTATION_REQUIRED)
@DomainObjectLayout(cssClassFa = "solid circle-play")
@Named(CommandReplayManager.LOGICAL_TYPE_NAME)
public record CommandReplayManager(
        ReplayContext replayContext) implements ViewModel {

    public static final String LOGICAL_TYPE_NAME = CausewayModuleExtCommandLogApplib.NAMESPACE + ".CommandReplayManager";

    @Inject
    public CommandReplayManager(
            final String memento,
            final ReplayContext replayContext) {
        this(replayContext);
    }

    @ObjectSupport public String title() {
        return "Command Replay Manager";
    }

    @Action
    @ActionLayout(
            sequence = "0.1",
            cssClass = "btn-primary",
            describedAs = "Imports commands from a zipped yaml, then persists them with replayState=PENDING.")
    public CommandReplayManager importCommands(
            @Parameter(fileAccept = ".zip")
            final Blob zippedCommandsYaml) {

        var yamlDs = zippedCommandsYaml.unZip(CommonMimeType.YAML).asDataSource();

        final List<CommandDto> commandDtos = CommandDtoUtils.fromYaml(yamlDs);
        commandDtos.forEach(commandLogEntryRepository()::saveForReplay);

        return this;
    }

    @Action(semantics = SemanticsOf.IDEMPOTENT_ARE_YOU_SURE)
    @ActionLayout(
            sequence = "0.2",
            describedAs = "Deletes all commands, regardless of state (cannot be undone)")
    public CommandReplayManager deleteAll() {
        commandLogEntryRepository().removeAll();
        return this;
    }

    // -- PENDING OR FAILED

    @Collection
    @CollectionLayout(
            describedAs = "Imported Commands that can be either replayed (replayState=PENDING) or retried (when replayState=FAILED)")
    public List<ReplayableCommand> getPendingOrFailed() {
        return commandLogEntryRepository().findReplayPendingOrFailed().stream()
            .map(entry->new ReplayableCommand(
                    entry.getInteractionId(),
                    replayContext))
            .toList();
    }

    @Action(choicesFrom = "pendingOrFailed")
    @ActionLayout(associateWith = "pendingOrFailed",
        sequence = "1.1",
        cssClassFa = "solid circle-play",
        cssClass = "btn-primary",
        describedAs = "Executes the list of commands in sequence, after having sorted them by their timestamp. "
                + "If any of the given commands fails, "
                + "the surrounding transaction is rolled back and any successful commands are undone). "
                + "The command, that caused the failure, gets marked as FAILED.")
    public CommandReplayManager replayOrRetrySelected(final List<ReplayableCommand> selected) {
        var replayables = selected.stream()
            .sorted()
            .toList();
        for(var replayableCommand : replayables) {
            var tryReplayOrRetry = replayableCommand.tryReplayOrRetry(); // filtered on its own responsibility
            if(tryReplayOrRetry.isFailure())
                return this; // stop further execution
        }
        return this;
    }

    @Action(choicesFrom = "pendingOrFailed")
    @ActionLayout(associateWith = "pendingOrFailed", sequence = "1.2",
            describedAs = "Marks selected Commands to be EXCLUDED from replay")
    public CommandReplayManager excludeSelectedFromReplay(final List<ReplayableCommand> selected) {
        selected.stream()
            .forEach(ReplayableCommand::excludeFromReplay); // filtered on its own responsibility
        return this;
    }

    @Action(choicesFrom = "pendingOrFailed")
    @ActionLayout(associateWith = "pendingOrFailed", sequence = "1.3",
            describedAs = "Deletes selected Commands (cannot be undone)")
    public CommandReplayManager deleteSelected(final List<ReplayableCommand> selected) {
        selected.stream()
            .forEach(ReplayableCommand::delete); // filtered on its own responsibility
        return this;
    }

    // -- OK OR EXCLUDE

    @Collection
    @CollectionLayout(
            describedAs = "Imported Commands that were either replayed with success (replayState=OK) "
                    + "or marked to be excluded from replay (replayState=EXCLUDE)")
    public List<ReplayableCommand> getSucceededOrExcluded() {
        return commandLogEntryRepository().findReplaySucceededOrExcluded().stream()
            .map(entry->new ReplayableCommand(
                    entry.getInteractionId(),
                    replayContext))
            .toList();
    }

    @Action(choicesFrom = "succeededOrExcluded")
    @ActionLayout(associateWith = "succeededOrExcluded",
            named = "Delete Selected",
            describedAs = "Deletes selected Commands (cannot be undone)")
    public CommandReplayManager deleteSelected2(final List<ReplayableCommand> selected) {
        selected.stream()
            .forEach(ReplayableCommand::delete); // filtered on its own responsibility
        return this;
    }

    // -- VM STATE

    @Override
    public String viewModelMemento() {
        // TODO could use to store filter state
        return null;
    }

    // -- HELPER

    private CommandLogEntryRepository commandLogEntryRepository() {
        return replayContext.commandLogEntryRepository();
    }

}
