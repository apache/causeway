package org.apache.causeway.extensions.commandlog.applib.dom.replay;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.ActionLayout;
import org.apache.causeway.applib.annotation.MemberSupport;
import org.apache.causeway.applib.annotation.Publishing;
import org.apache.causeway.applib.annotation.RestrictTo;
import org.apache.causeway.applib.annotation.SemanticsOf;
import org.apache.causeway.applib.exceptions.RecoverableException;
import org.apache.causeway.core.config.CausewayConfiguration;
import org.apache.causeway.extensions.commandlog.applib.dom.ReplayState;

@Action(
        restrictTo = RestrictTo.PROTOTYPING,
        choicesFrom = "commands",
        commandPublishing = Publishing.DISABLED,
        semantics = SemanticsOf.NON_IDEMPOTENT,
        domainEvent = CommandManagerExport_excludeCommands.DomainEvent.class,
        executionPublishing = Publishing.DISABLED
)
@ActionLayout(
        associateWith = "commands", sequence = "1.3",
        cssClass = "btn-secondary",
        describedAs = "Marks selected active Commands as EXCLUDED so they are omitted from export, exportability, and movement. "
                + "Non-exportable commands are selected by default."
)
public class CommandManagerExport_excludeCommands {

    public static class DomainEvent extends CommandManagerExport.ActionDomainEvent<CommandManagerExport_excludeCommands> {
    }

    private final CommandManagerExport commandExportManager;

    @Inject CausewayConfiguration causewayConfiguration;

    public CommandManagerExport_excludeCommands(final CommandManagerExport commandExportManager) {
        this.commandExportManager = commandExportManager;
    }

    @MemberSupport
    public CommandManagerExport act(final List<ReplayableCommand> selected) {
        final String validation = validateAct(selected);
        if (validation != null) {
            throw new RecoverableException(validation);
        }

        selected.stream()
                .map(ReplayableCommand::commandLogEntry)
                .flatMap(java.util.Optional::stream)
                .forEach(commandLogEntry -> commandLogEntry.setReplayState(ReplayState.EXCLUDED));
        return commandExportManager;
    }

    @MemberSupport
    public String disableAct() {
        if (!isRecordingSupportEnabled()) {
            return "Command exclusion requires command-log recording support to be enabled";
        }
        return commandExportManager.getCommands().isEmpty() ? "No commands in collection" : null;
    }

    @MemberSupport
    public String validateAct(final List<ReplayableCommand> selected) {
        if (!isRecordingSupportEnabled()) {
            return "Command exclusion requires command-log recording support to be enabled";
        }
        final String selectedValidation = validateSelected(selected);
        if (selectedValidation != null) {
            return selectedValidation;
        }
        final Set<UUID> activeIds = interactionIds(commandExportManager.getCommands());
        final Set<UUID> selectedIds = interactionIds(selected);
        if (!activeIds.containsAll(selectedIds)) {
            return "Selected commands must be active commands from the current baseline";
        }
        return null;
    }

    @MemberSupport
    public String validateSelected(final List<ReplayableCommand> selected) {
        return selected == null || selected.isEmpty() ? "Select at least one command to exclude" : null;
    }

    @MemberSupport
    public List<ReplayableCommand> defaultSelected() {
        return commandExportManager.getCommands().stream()
                .filter(command -> Boolean.FALSE.equals(command.getExportable()))
                .collect(Collectors.toList());
    }

    // TODO: shouldn't be required because of 'choicesFrom', but in v2 there seems to be a MM validation error due to a missing choicesFacet
    @MemberSupport
    public List<ReplayableCommand> choicesSelected() {
        return commandExportManager.getCommands();
    }

    private boolean isRecordingSupportEnabled() {
        return causewayConfiguration != null
                && causewayConfiguration.getExtensions().getCommandLog().getRecordingSupport().isEnabled();
    }

    private static Set<UUID> interactionIds(final List<ReplayableCommand> commands) {
        if (commands == null) {
            return Set.of();
        }
        return commands.stream()
                .map(ReplayableCommand::interactionId)
                .collect(Collectors.toCollection(HashSet::new));
    }
}
