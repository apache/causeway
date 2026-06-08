package org.apache.causeway.extensions.commandlog.applib.dom.replay;


import java.time.Instant;
import java.time.chrono.ChronoZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.apache.causeway.applib.annotation.*;
import org.apache.causeway.applib.exceptions.RecoverableException;
import org.apache.causeway.applib.services.metamodel.MetaModelService;
import org.apache.causeway.applib.util.schema.CommandDtoUtils;
import org.apache.causeway.applib.value.Clob;
import org.apache.causeway.applib.value.NamedWithMimeType;
import org.apache.causeway.core.config.CausewayConfiguration;
import org.apache.causeway.extensions.commandlog.applib.dom.CommandLogEntry;
import org.apache.causeway.extensions.commandlog.applib.dom.ReplayState;

@Action(
        restrictTo = RestrictTo.PROTOTYPING,
        choicesFrom = "commands",
        semantics = SemanticsOf.NON_IDEMPOTENT,
        commandPublishing = Publishing.DISABLED,
        domainEvent = CommandExportManager_exportSelected.DomainEvent.class,
        executionPublishing = Publishing.DISABLED
)
@ActionLayout(
        associateWith = "commands", sequence = "1.1",
        cssClassFa = "solid share-from-square",
        cssClass = "btn-primary",
        describedAs = "Exports selected Commands as zipped DTOs for import later. "
                + "Refresh the page to see changed states."
)
public class CommandExportManager_exportSelected {

    public static class DomainEvent extends CommandExportManager.ActionDomainEvent<CommandExportManager_exportSelected> {
    }

    private final CommandExportManager commandExportManager;

    @Inject MetaModelService metaModelService;
    @Inject CausewayConfiguration causewayConfiguration;

    @Inject
    public CommandExportManager_exportSelected(final CommandExportManager commandExportManager) {
        this.commandExportManager = commandExportManager;
    }

    @MemberSupport
    public Clob act(
            final List<ReplayableCommand> selected,
            @ParameterLayout(describedAs = "File name for the exported file.") final String filenamePrefix,
            @ParameterLayout(describedAs = "Whether to add a timestamp suffix to the exported file's name.") final boolean filenameTimestamp) {

        var selectedCommandLogEntries = selectedCommandLogEntries(selected);

        validateKnownTargets(selectedCommandLogEntries)
                .ifPresent(failure -> {
                    throw new RecoverableException(failure.message());
                });

        var yaml = CommandDtoUtils.toYamlExport(
                selectedCommandLogEntries.stream()
                        .map(entry -> CommandDtoUtils.CommandExportDto.of(
                                entry.getCommandDto(),
                                entry.getResult()))
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
        return commandExportManager.getCommands().isEmpty() ? "No commands in collection" : null;
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
    public List<ReplayableCommand> defaultSelected() {
        syncInjectedServicesToManager();
        return commandExportManager.getCommands().stream()
                .filter(command -> Boolean.TRUE.equals(command.getExportable()))
                .collect(Collectors.toList());
    }

    @MemberSupport
    public String validateSelected(final List<ReplayableCommand> selected) {
        if (selected != null && selected.isEmpty()) {
            return "Select at least one command to export";
        }
        return validateKnownTargets(selectedCommandLogEntries(selected))
                .map(CommandExportKnownTargetValidator.Failure::message)
                .orElse(null);
    }

    private List<CommandLogEntry> selectedCommandLogEntries(final List<ReplayableCommand> selected) {
        if (selected == null) {
            return List.of();
        }
        return selected.stream()
                .map(ReplayableCommand::commandLogEntry)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .sorted()
                .collect(Collectors.toList());
    }

    private Optional<CommandExportKnownTargetValidator.Failure> validateKnownTargets(
            final List<CommandLogEntry> selectedCommandLogEntries) {
        syncInjectedServicesToManager();
        return commandExportManager.validateKnownTargets(selectedCommandLogEntries);
    }

    private void syncInjectedServicesToManager() {
        commandExportManager.metaModelService = metaModelService;
        commandExportManager.causewayConfiguration = causewayConfiguration;
    }

    // TODO: shouldn't be required because of 'choicesFrom', but in v2 there seems to be a MM validation error due to a missing choicesFacet
    @MemberSupport
    public List<ReplayableCommand> choicesSelected() {
        return commandExportManager.getCommands();
    }
}
