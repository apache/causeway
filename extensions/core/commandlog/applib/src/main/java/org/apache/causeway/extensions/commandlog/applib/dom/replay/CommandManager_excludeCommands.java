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
import org.apache.causeway.extensions.commandlog.applib.dom.ReplayState;

@Action(
        restrictTo = RestrictTo.PROTOTYPING,
        choicesFrom = "commandsInSequence",
        commandPublishing = Publishing.DISABLED,
        semantics = SemanticsOf.NON_IDEMPOTENT,
        domainEvent = CommandManager_excludeCommands.DomainEvent.class,
        executionPublishing = Publishing.DISABLED
)
@ActionLayout(
        associateWith = "commandsInSequence", sequence = "1.3",
        cssClass = "btn-secondary",
        describedAs = "Marks selected Commands as EXCLUDED from either export or replay."
)
public class CommandManager_excludeCommands {

    public static class DomainEvent extends CommandManager.ActionDomainEvent<CommandManager_excludeCommands> {
    }

    private final CommandManager commandManager;

    @Inject ReplayContext replayContext;

    public CommandManager_excludeCommands(final CommandManager commandManager) {
        this.commandManager = commandManager;
    }

    @MemberSupport
    public CommandManager act(final List<ReplayableCommand> selected) {
        final String validation = validateAct(selected);
        if (validation != null) {
            throw new RecoverableException(validation);
        }

        selected.stream()
                .map(ReplayableCommand::commandLogEntry)
                .flatMap(java.util.Optional::stream)
                .forEach(commandLogEntry -> commandLogEntry.setReplayState(ReplayState.EXCLUDED));
        return commandManager;
    }

    @MemberSupport
    public String disableAct() {
        if (!replayContext.isRecordingSupportEnabled()) {
            return "Command exclusion requires command-log recording support to be enabled";
        }
        return commandManager.getCommandsInSequence().isEmpty() ? "No commands in collection" : null;
    }

    @MemberSupport
    public String validateAct(final List<ReplayableCommand> selected) {
        if (!replayContext.isRecordingSupportEnabled()) {
            return "Command exclusion requires command-log recording support to be enabled";
        }
        final String selectedValidation = validateSelected(selected);
        if (selectedValidation != null) {
            return selectedValidation;
        }
        final Set<UUID> activeIds = interactionIds(commandManager.getCommandsInSequence());
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

    // TODO: shouldn't be required because of 'choicesFrom', but in v2 there seems to be a MM validation error due to a missing choicesFacet
    @MemberSupport
    public List<ReplayableCommand> choicesSelected() {
        return commandManager.getCommandsInSequence();
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
