package org.apache.causeway.extensions.commandlog.applib.dom.replay;

import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.time.chrono.ChronoZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.causeway.applib.annotation.*;
import org.apache.causeway.applib.util.schema.CommandDtoUtils;
import org.apache.causeway.applib.value.Clob;
import org.apache.causeway.applib.value.NamedWithMimeType;
import org.apache.causeway.extensions.commandlog.applib.dom.CommandLogEntry;
import org.apache.causeway.extensions.commandlog.applib.dom.ReplayState;

@Action(
        restrictTo = RestrictTo.PROTOTYPING,
        choicesFrom = "notYetExported",
        semantics = SemanticsOf.NON_IDEMPOTENT,
        commandPublishing = Publishing.DISABLED,
        domainEvent = CommandExportManager_exportSelected.DomainEvent.class,
        executionPublishing = Publishing.DISABLED
)
@ActionLayout(
        associateWith = "notYetExported", sequence = "1.1",
        cssClassFa = "solid share-from-square",
        cssClass = "btn-primary",
        describedAs = "Exports selected Commands as zipped DTOs for import later. "
                + "Refresh the page to see changed states."
)
@RequiredArgsConstructor
public class CommandExportManager_exportSelected {

    public static class DomainEvent extends CommandExportManager.ActionDomainEvent<CommandExportManager_exportSelected> { }

    private final CommandExportManager commandExportManager;

    @MemberSupport
    public Clob act(
            final List<ReplayableCommand> selected,
            @ParameterLayout(describedAs = "File name for the exported file.") final String filenamePrefix,
            @ParameterLayout(describedAs = "Whether to add a timestamp suffix to the exported file's name.") final boolean filenameTimestamp) {

        var selectedCommandLogEntries = selected.stream()
                .map(ReplayableCommand::commandLogEntry)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .filter(entry -> !ReplayState.isExported(entry.getReplayState())) // shouldn't be necessary unless a race condition
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

        var clob = Clob.of(filename, NamedWithMimeType.CommonMimeType.YAML, yaml);

        // do this last once we have successfully created the Clob
        selectedCommandLogEntries.forEach(c -> c.setReplayState(ReplayState.EXPORTED));

        return clob;
    }

    @MemberSupport
    public String disableAct() {
        return commandExportManager.getNotYetExported().isEmpty() ? "No commands in collection" : null;
    }

    @MemberSupport
    public String defaultFilenamePrefix() {
        return "commands";
    }

    @MemberSupport
    public boolean defaultFilenameTimestamp() {
        return true;
    }

    @MemberSupport
    public String validateSelected(final List<ReplayableCommand> selected) {
        return selected != null && selected.isEmpty() ? "Select at least one command to export" : null;
    }

    // TODO: shouldn't be required because of 'choicesFrom', but in v2 there seems to be a MM validation error due to a missing choicesFacet
    @MemberSupport
    public List<ReplayableCommand> choicesSelected() {
        return commandExportManager.getNotYetExported();
    }
}
