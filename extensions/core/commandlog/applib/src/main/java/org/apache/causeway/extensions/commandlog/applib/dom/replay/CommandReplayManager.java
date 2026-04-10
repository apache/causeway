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
import org.apache.causeway.applib.util.schema.CommandDtoUtils;
import org.apache.causeway.applib.value.Blob;
import org.apache.causeway.applib.value.NamedWithMimeType.CommonMimeType;
import org.apache.causeway.commons.io.JsonUtils;
import org.apache.causeway.commons.io.YamlUtils;
import org.apache.causeway.extensions.commandlog.applib.CausewayModuleExtCommandLogApplib;
import org.apache.causeway.extensions.commandlog.applib.dom.CommandLogEntry;
import org.apache.causeway.extensions.commandlog.applib.dom.CommandLogEntryRepository;
import org.apache.causeway.schema.cmd.v2.CommandDto;

@DomainObject(introspection = Introspection.ANNOTATION_REQUIRED)
@DomainObjectLayout(cssClassFa = "solid circle-play")
@Named(CommandReplayManager.LOGICAL_TYPE_NAME)
public record CommandReplayManager(
        BookmarkService bookmarkService,
        CommandLogEntryRepository commandLogEntryRepository) implements ViewModel {

    public static final String LOGICAL_TYPE_NAME = CausewayModuleExtCommandLogApplib.NAMESPACE + ".CommandReplayManager";

    @Inject
    public CommandReplayManager(final String memento,
            final BookmarkService bookmarkService,
            final CommandLogEntryRepository commandLogEntryRepository) {
        this(bookmarkService, commandLogEntryRepository);
    }

    @ObjectSupport public String title() {
        return "Command Replay Manager";
    }

    @Action
    @ActionLayout(describedAs = "Imports commands from a zipped yaml, then persists them with replayState=PENDING.")
    public String importCommands(
            @Parameter(fileAccept = ".zip")
            final Blob zippedCommandsYaml) {

        var yamlDs = zippedCommandsYaml.unZip(CommonMimeType.YAML).asDataSource();

        final List<CommandDto> commandDtos = CommandDtoUtils.fromYaml(yamlDs);
        commandDtos.forEach(commandLogEntryRepository::saveForReplay);

        var yaml = YamlUtils.toStringUtf8(commandDtos,
                JsonUtils::onlyIncludeNonNull);

        return yaml;
    }

    @Collection
    public List<CommandLogEntry> getNotYetReplayedRaw() {
        return commandLogEntryRepository.findNotYetReplayed();
    }

    @Collection
    public List<ReplayableCommand> getNotYetReplayed() {
        return commandLogEntryRepository.findNotYetReplayed().stream()
                .map(entry->new ReplayableCommand(bookmarkService.bookmarkFor(entry).get().identifier(), bookmarkService, commandLogEntryRepository))
                .toList();
    }

    @Override
    public String viewModelMemento() {
        // TODO Auto-generated method stub
        return null;
    }

}
