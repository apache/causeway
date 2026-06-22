package org.apache.causeway.extensions.commandlog.applib.dom.replay;


import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.time.chrono.ChronoZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.apache.causeway.applib.annotation.*;
import org.apache.causeway.applib.util.schema.CommandDtoUtils;
import org.apache.causeway.applib.value.Clob;
import org.apache.causeway.applib.value.NamedWithMimeType;
import org.apache.causeway.extensions.commandlog.applib.dom.CommandLogEntry;

@Action(
        restrictTo = RestrictTo.PROTOTYPING,
        choicesFrom = "commandsInSequence",
        semantics = SemanticsOf.NON_IDEMPOTENT,
        commandPublishing = Publishing.DISABLED,
        domainEvent = CommandManager_exportSequence.DomainEvent.class,
        executionPublishing = Publishing.DISABLED
)
@ActionLayout(
        associateWith = "commandsToExport", sequence = "1.1",
        cssClassFa = "solid share-from-square",
        cssClass = "btn-primary",
        describedAs = "Exports all Commands in the sequence, applying result remappings if available. "
)
@RequiredArgsConstructor
public class CommandManager_exportSequence {

    public static class DomainEvent extends CommandManager.ActionDomainEvent<CommandManager_exportSequence> {
    }

    private final CommandManager commandManager;

    @MemberSupport
    public Clob act(
            @ParameterLayout(describedAs = "File name for the exported file.") final String filenamePrefix,
            @ParameterLayout(describedAs = "Whether to add a timestamp suffix to the exported file's name.") final boolean filenameTimestamp) {

        List<CommandLogEntry> selectedCommandLogEntries =
                commandManager.streamCommandsInSequence()
                .filter(ReplayableCommand::isKnownParticipants)
                .map(ReplayableCommand::commandLogEntry)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .sorted()
                .collect(Collectors.toList());

        var yaml = CommandDtoUtils.toYamlExport(
                selectedCommandLogEntries.stream()
                        .map(entry -> CommandDtoUtils.CommandExportDto.of(
                                entry.getCommandDto(),
                                entry.getResult()))
                        .map(this::remapResults)
                        .collect(Collectors.toList()));

        final var firstReplayableCommand =
                commandManager.streamCommandsInSequence()
                .filter(ReplayableCommand::isKnownParticipants)
                .findFirst().orElseThrow();  // disable guard ensures there is at least one command
        final var timestamp = filenameTimestamp
                ? firstReplayableCommand.getTimestampIfAny()
                .map(ChronoZonedDateTime::toInstant)
                .map(Instant::toString)
                .map(x -> "." + x.replaceAll("[^A-Za-z0-9._-]", "_"))   // make safe within filename
                .orElse("")
                : "";
        final var filename = filenamePrefix + timestamp;

        return Clob.of(filename, NamedWithMimeType.CommonMimeType.YAML, yaml);
    }

    @MemberSupport
    public String disableAct() {
        return commandManager.getCommandsInSequence().stream().noneMatch(ReplayableCommand::isKnownParticipants)
                ? "No commands (with known participants) in this sequence."
                : null;
    }

    @MemberSupport
    public String defaultFilenamePrefix() {
        return "commands";
    }

    @MemberSupport
    public boolean defaultFilenameTimestamp() {
        return true;
    }

    private CommandDtoUtils.CommandExportDto remapResults(CommandDtoUtils.CommandExportDto commandExportDto) {
        return resultRemappingService.remapped(commandExportDto);
    }

    @Inject private ResultRemappingService resultRemappingService;

}
