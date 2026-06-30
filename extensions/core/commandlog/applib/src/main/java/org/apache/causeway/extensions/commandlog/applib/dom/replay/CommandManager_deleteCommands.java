package org.apache.causeway.extensions.commandlog.applib.dom.replay;

import lombok.RequiredArgsConstructor;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.ActionLayout;
import org.apache.causeway.applib.annotation.MemberSupport;
import org.apache.causeway.applib.annotation.Publishing;
import org.apache.causeway.applib.annotation.RestrictTo;
import org.apache.causeway.applib.annotation.SemanticsOf;
import org.apache.causeway.applib.exceptions.RecoverableException;

@Action(
        restrictTo = RestrictTo.PROTOTYPING,
        choicesFrom = "excludedCommands",
        commandPublishing = Publishing.DISABLED,
        semantics = SemanticsOf.NON_IDEMPOTENT,
        domainEvent = CommandManager_deleteCommands.DomainEvent.class,
        executionPublishing = Publishing.DISABLED
)
@ActionLayout(
        associateWith = "excludedCommands", sequence = "1.2",
        cssClass = "btn-danger",
        describedAs = "Permanently deletes selected excluded Command Log Entries. This cannot be undone."
)
@RequiredArgsConstructor
public class CommandManager_deleteCommands {

    public static class DomainEvent extends CommandManager.ActionDomainEvent<CommandManager_deleteCommands> {
    }

    private final CommandManager commandManager;


    @MemberSupport
    public CommandManager act(final List<ReplayableCommand> selected) {
        final String validation = validateAct(selected);
        if (validation != null) {
            throw new RecoverableException(validation);
        }

        selected.forEach(ReplayableCommand::deleteObj);
        return commandManager;
    }

    @MemberSupport
    public String disableAct() {
        return commandManager.getExcluded().isEmpty()
                ? "No excluded commands in collection"
                : null;
    }

    @MemberSupport
    public String validateAct(final List<ReplayableCommand> selected) {
        final String selectedValidation = validateSelected(selected);
        if (selectedValidation != null) {
            return selectedValidation;
        }
        final Set<UUID> excludedIds = interactionIds(commandManager.getExcluded());
        final Set<UUID> selectedIds = interactionIds(selected);
        if (!excludedIds.containsAll(selectedIds)) {
            return "Selected commands must be excluded commands from the current baseline";
        }
        return null;
    }

    @MemberSupport
    public String validateSelected(final List<ReplayableCommand> selected) {
        return selected == null || selected.isEmpty() ? "Select at least one command to delete" : null;
    }

    // TODO: shouldn't be required because of 'choicesFrom', but in v2 there seems to be a MM validation error due to a missing choicesFacet
    @MemberSupport
    public List<ReplayableCommand> choicesSelected() {
        return commandManager.getExcluded();
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
