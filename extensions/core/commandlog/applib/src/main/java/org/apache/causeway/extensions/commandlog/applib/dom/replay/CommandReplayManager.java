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
import org.apache.causeway.applib.annotation.DomainObject;
import org.apache.causeway.applib.annotation.DomainObjectLayout;
import org.apache.causeway.applib.annotation.Introspection;
import org.apache.causeway.applib.annotation.ObjectSupport;
import org.apache.causeway.applib.annotation.Parameter;
import org.apache.causeway.applib.services.bookmark.BookmarkService;
import org.apache.causeway.applib.services.command.CommandExecutorService;
import org.apache.causeway.applib.util.schema.CommandDtoUtils;
import org.apache.causeway.applib.value.Blob;
import org.apache.causeway.applib.value.NamedWithMimeType.CommonMimeType;
import org.apache.causeway.extensions.commandlog.applib.CausewayModuleExtCommandLogApplib;
import org.apache.causeway.extensions.commandlog.applib.dom.CommandLogEntryRepository;
import org.apache.causeway.extensions.commandlog.applib.dom.ReplayState;
import org.apache.causeway.schema.cmd.v2.CommandDto;

@DomainObject(introspection = Introspection.ANNOTATION_REQUIRED)
@DomainObjectLayout(cssClassFa = "solid circle-play")
@Named(CommandReplayManager.LOGICAL_TYPE_NAME)
public record CommandReplayManager(
        ReplayContext replayContext) implements ViewModel {

    public static final String LOGICAL_TYPE_NAME = CausewayModuleExtCommandLogApplib.NAMESPACE + ".CommandReplayManager";

    @Inject
    public CommandReplayManager(final String memento,
            final BookmarkService bookmarkService,
            final CommandLogEntryRepository commandLogEntryRepository,
            final CommandExecutorService commandExecutorService) {
        this(new ReplayContext(bookmarkService, commandLogEntryRepository, commandExecutorService));
    }

    @ObjectSupport public String title() {
        return "Command Replay Manager";
    }

    @Action
    @ActionLayout(describedAs = "Imports commands from a zipped yaml, then persists them with replayState=PENDING.")
    public CommandReplayManager importCommands(
            @Parameter(fileAccept = ".zip")
            final Blob zippedCommandsYaml) {

        var yamlDs = zippedCommandsYaml.unZip(CommonMimeType.YAML).asDataSource();

        final List<CommandDto> commandDtos = CommandDtoUtils.fromYaml(yamlDs);
        commandDtos.forEach(commandLogEntryRepository()::saveForReplay);

        return this;
    }

    // -- NOT YET REPLAYED

    @Collection
    public List<ReplayableCommand> getNotYetReplayed() {
        return commandLogEntryRepository().findNotYetReplayed().stream()
            .map(entry->new ReplayableCommand(
                    replayContext.bookmarkService().bookmarkFor(entry).get().identifier(),
                    replayContext))
            .toList();
    }

    @Action(choicesFrom = "notYetReplayed")
    @ActionLayout(associateWith = "notYetReplayed")
    public CommandReplayManager replaySelected(final List<ReplayableCommand> selected) {
        selected.stream()
            .filter(c->c.getReplayState() == ReplayState.PENDING)
            .forEach(ReplayableCommand::replay);
        return this;
    }

    @Action(choicesFrom = "notYetReplayed")
    @ActionLayout(associateWith = "notYetReplayed",
            describedAs = "Marks selected Commands to be EXCLUDED from replay.")
    public CommandReplayManager excludeSelectedFromReplay(final List<ReplayableCommand> selected) {
        selected.stream()
            .filter(c->c.getReplayState() == ReplayState.PENDING)
            .forEach(ReplayableCommand::excludeFromReplay);
        return this;
    }

    // -- FAILED //TODO

    // -- OK //TODO

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
