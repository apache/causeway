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
import java.util.Optional;

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
import org.apache.causeway.applib.annotation.SemanticsOf;
import org.apache.causeway.applib.util.schema.CommandDtoUtils;
import org.apache.causeway.applib.value.Blob;
import org.apache.causeway.applib.value.Clob;
import org.apache.causeway.applib.value.NamedWithMimeType.CommonMimeType;
import org.apache.causeway.extensions.commandlog.applib.CausewayModuleExtCommandLogApplib;
import org.apache.causeway.extensions.commandlog.applib.dom.CommandLogEntry;
import org.apache.causeway.extensions.commandlog.applib.dom.CommandLogEntryRepository;
import org.apache.causeway.extensions.commandlog.applib.dom.ReplayState;

@DomainObject(introspection = Introspection.ANNOTATION_REQUIRED)
@DomainObjectLayout(cssClassFa = "solid share-from-square")
@Named(CommandExportManager.LOGICAL_TYPE_NAME)
public record CommandExportManager(
        ReplayContext replayContext) implements ViewModel {

    public static final String LOGICAL_TYPE_NAME = CausewayModuleExtCommandLogApplib.NAMESPACE + ".CommandExportManager";

    @Inject
    public CommandExportManager(
            final String memento,
            final ReplayContext replayContext) {
        this(replayContext);
    }

    @ObjectSupport public String title() {
        return "Command Export Manager";
    }

    @Action(semantics = SemanticsOf.IDEMPOTENT_ARE_YOU_SURE)
    @ActionLayout(
            sequence = "0.1",
            describedAs = "Deletes all commands, regardless of state (cannot be undone)")
    public CommandExportManager deleteAll() {
        commandLogEntryRepository().removeAll();
        return this;
    }

    // -- NOT YET EXPORTED

    @Collection
    @CollectionLayout(
            describedAs = "Commands that can be exported")
    public List<ReplayableCommand> getNotYetExported() {
        return commandLogEntryRepository().findAll().stream()
            .filter(entry->ReplayState.canExport(entry.getReplayState()))
            .map(entry->new ReplayableCommand(
                    replayContext.bookmarkService().bookmarkFor(entry).get().identifier(),
                    replayContext))
            .toList();
    }

    @Action(choicesFrom = "notYetExported", semantics = SemanticsOf.NON_IDEMPOTENT)
    @ActionLayout(associateWith = "notYetExported",
        sequence = "1.1",
        cssClassFa = "solid share-from-square",
        cssClass = "btn-primary",
        describedAs = "Exports selected Commands as zipped DTOs for import later. "
                + "(You need to refresh the page to see changed states.)")
    public Blob exportSelected(
            final List<ReplayableCommand> selected) {

        var selectedCommandLogEntries = selected.stream()
            .map(ReplayableCommand::commandLogEntry)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .sorted()
            .toList();

        var yaml = CommandDtoUtils.toYaml(
            selectedCommandLogEntries.stream()
                .filter(entry->!ReplayState.isExported(entry.getReplayState()))
                .map(CommandLogEntry::getCommandDto)
                .toList());

        var blob = Clob.of("commands.yaml", CommonMimeType.YAML, yaml)
                .toBlobUtf8()
                .zip();

        // do this last once we have successfully created the Clob
        selectedCommandLogEntries.forEach(c->c.setReplayState(ReplayState.EXPORTED));

        return blob;
    }

    @Action(choicesFrom = "notYetExported")
    @ActionLayout(associateWith = "notYetExported", sequence = "1.2",
            describedAs = "Deletes selected Commands (cannot be undone)")
    public CommandExportManager deleteSelected(final List<ReplayableCommand> selected) {
        selected.stream()
            .forEach(ReplayableCommand::delete); // filtered on its own responsibility
        return this;
    }

    // -- EXPORTED

    @Collection
    @CollectionLayout(
            describedAs = "Commands that were exported")
    public List<ReplayableCommand> getExported() {
        return commandLogEntryRepository().findAll().stream()
            .filter(entry->ReplayState.isExported(entry.getReplayState()))
            .map(entry->new ReplayableCommand(
                    replayContext.bookmarkService().bookmarkFor(entry).get().identifier(),
                    replayContext))
            .toList();
    }

    @Action(choicesFrom = "exported")
    @ActionLayout(associateWith = "exported", sequence = "2.1",
            describedAs = "Makes selected Commands exportable (again)")
    public CommandExportManager makeSelectedExportable(final List<ReplayableCommand> selected) {
        selected.stream()
            .forEach(ReplayableCommand::makeExportable); // filtered on its own responsibility
        return this;
    }

    @Action(choicesFrom = "exported")
    @ActionLayout(associateWith = "exported", sequence = "2.2",
            named = "Delete Selected",
            describedAs = "Deletes selected Commands (cannot be undone)")
    public CommandExportManager deleteSelected2(final List<ReplayableCommand> selected) {
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
