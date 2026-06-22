package org.apache.causeway.extensions.commandlog.applib.dom.replay;


import java.time.Instant;
import java.time.chrono.ChronoZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;

import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.ActionLayout;
import org.apache.causeway.applib.annotation.MemberSupport;
import org.apache.causeway.applib.annotation.ParameterLayout;
import org.apache.causeway.applib.annotation.Publishing;
import org.apache.causeway.applib.annotation.RestrictTo;
import org.apache.causeway.applib.annotation.SemanticsOf;
import org.apache.causeway.applib.util.schema.CommandDtoUtils;
import org.apache.causeway.applib.value.Clob;
import org.apache.causeway.applib.value.NamedWithMimeType;
import org.apache.causeway.extensions.commandlog.applib.dom.CommandLogEntry;

import lombok.RequiredArgsConstructor;

@Action(
        restrictTo = RestrictTo.PROTOTYPING,
        semantics = SemanticsOf.NON_IDEMPOTENT,
        commandPublishing = Publishing.DISABLED,
        domainEvent = ReplayableCommand_export.DomainEvent.class,
        executionPublishing = Publishing.DISABLED
)
@ActionLayout(
        associateWith = "dto", sequence = "0.1",
        cssClassFa = "solid share-from-square",
        cssClass = "btn-primary",
        describedAs = "Exports the commands, optionally applying result remappings."
)
@RequiredArgsConstructor
public class ReplayableCommand_export {

    public static class DomainEvent extends ReplayableCommand.ActionDomainEvent<ReplayableCommand_export> {
    }

    private final ReplayableCommand replayableCommand;

    @MemberSupport
    public Clob act(
            @ParameterLayout(describedAs = "File name for the exported file.") final String filenamePrefix,
            @ParameterLayout(describedAs = "Whether to add a timestamp suffix to the exported file's name.") final boolean filenameTimestamp,
            @ParameterLayout(describedAs = "Whether to remap recording results with actuals.") final boolean remapResults) {

        var yaml = CommandDtoUtils.toYamlExport(
                Stream.of(replayableCommand.commandLogEntry().orElseThrow())
                        .map(entry -> CommandDtoUtils.CommandExportDto.of(
                                entry.getCommandDto(),
                                entry.getResult()))
                        .map(commandExportDto -> remapResults ? remapResults(commandExportDto) : commandExportDto)
                        .collect(Collectors.toList()));

        final var timestamp = filenameTimestamp
                ? replayableCommand.getTimestampIfAny()
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
        if (replayableCommand.commandLogEntry().isEmpty()) {
            return "Underlying CommandLogEntry not found.";
        }
        return null;
    }

    @MemberSupport
    public String defaultFilenamePrefix() {
        return "command";
    }

    @MemberSupport
    public boolean defaultFilenameTimestamp() {
        return true;
    }

    @MemberSupport
    public boolean defaultRemapResults() {
        return true;
    }

    private CommandDtoUtils.CommandExportDto remapResults(CommandDtoUtils.CommandExportDto commandExportDto) {
        return resultRemappingService.remapped(commandExportDto);
    }

    @Inject private ResultRemappingService resultRemappingService;

}
