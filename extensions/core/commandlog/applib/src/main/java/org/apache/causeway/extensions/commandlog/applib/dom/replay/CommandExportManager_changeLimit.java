package org.apache.causeway.extensions.commandlog.applib.dom.replay;

import lombok.RequiredArgsConstructor;

import org.apache.causeway.applib.annotation.*;

import static org.apache.causeway.extensions.commandlog.applib.dom.replay.CommandExportManager.Direction.NEXT;

@Action(
        semantics = SemanticsOf.SAFE,
        commandPublishing = Publishing.DISABLED,
        domainEvent = CommandExportManager_changeLimit.DomainEvent.class,
        executionPublishing = Publishing.DISABLED
)
@ActionLayout(
        associateWith = "limit", sequence = "1",
        position = ActionLayout.Position.PANEL,
        describedAs = "Change number of commands in page"
)
@RequiredArgsConstructor
public class CommandExportManager_changeLimit {

    public static int MAX_LIMIT = 100;

    public static class DomainEvent extends HasBaseline.ActionDomainEvent<CommandExportManager_changeLimit> { }

    private final CommandExportManager commandExportManager;

    @MemberSupport
    public CommandExportManager act(int newLimit) {
        return commandExportManager.withLimit(newLimit);
    }
    @MemberSupport
    public String validateNewLimit(int newLimit) {
        if(newLimit < 0) {
            return "Limit must be greater than or equal to 0.";
        }
        if(newLimit > MAX_LIMIT) {
            return "Limit cannot be greater than " + MAX_LIMIT + ".";
        }
        return null;
    }

    @MemberSupport
    public String disableAct() {
        final var commands = commandExportManager.commands(NEXT);
        final var size = commands.size();
        if (size == 0) {
            return "Empty";
        }
        final var lastReplayable = commands.get(size - 1);
        return commandExportManager(lastReplayable).commands(NEXT).isEmpty() ? "No more commands" : null;
    }

    private CommandExportManager commandExportManager(ReplayableCommand lastReplayable) {
        final var timestamp = lastReplayable.getTimestamp().toInstant();
        final var baselinePlus5Millis = HasBaseline.addMillis(timestamp, 5);
        return commandExportManager.withBaseline(baselinePlus5Millis);
    }
}
