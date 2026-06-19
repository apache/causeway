package org.apache.causeway.extensions.commandlog.applib.dom.replay;

import lombok.RequiredArgsConstructor;

import org.apache.causeway.applib.annotation.*;

@Action(
        semantics = SemanticsOf.SAFE,
        commandPublishing = Publishing.DISABLED,
        domainEvent = CommandManager_previousPage.DomainEvent.class,
        executionPublishing = Publishing.DISABLED
)
@ActionLayout(
        associateWith = "limit", sequence = "1",
        named = "-1 page",
        position = ActionLayout.Position.BELOW,
        describedAs = "Move backwards to previous page of commands"
)
@RequiredArgsConstructor
public class CommandManager_previousPage {

    public static class DomainEvent extends CommandManager.ActionDomainEvent<CommandManager_previousPage> { }

    private final CommandManager commandManager;

    @MemberSupport
    public CommandManager act() {
        final var commands = commandManager.getCommandsForExport();   // returns descending, latest (youngest) first
        final var size = commands.size();
        if (size == 0) {
            return commandManager;
        }
        final var earliestReplayable = commands.get(size - 1);
        return commandManager(earliestReplayable);
    }

    @MemberSupport
    public String disableAct() {
        final var commands = commandManager.getCommandsForExport();
        final var size = commands.size();
        return size == 0 ? "No commands" : null;
    }

    private CommandManager commandManager(final ReplayableCommand replayableCommand) {
        final var timestamp = replayableCommand.getTimestamp().toInstant();
        final var baselineMinus5Millis = HasBaseline.addMillis(timestamp, -5);
        return commandManager.
                withBaseline(baselineMinus5Millis);
    }

}
