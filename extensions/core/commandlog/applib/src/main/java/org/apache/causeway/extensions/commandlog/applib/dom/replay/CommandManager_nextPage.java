package org.apache.causeway.extensions.commandlog.applib.dom.replay;

import lombok.RequiredArgsConstructor;

import org.apache.causeway.applib.annotation.*;

@Action(
        semantics = SemanticsOf.SAFE,
        commandPublishing = Publishing.DISABLED,
        domainEvent = CommandManager_nextPage.DomainEvent.class,
        executionPublishing = Publishing.DISABLED
)
@ActionLayout(
        associateWith = "limit", sequence = "2",
        named = "+1 page",
        position = ActionLayout.Position.BELOW,
        describedAs = "Move forward to next page of commands"
)
@RequiredArgsConstructor
public class CommandManager_nextPage {

    public static class DomainEvent extends CommandManager.ActionDomainEvent<CommandManager_nextPage> { }

    private final CommandManager commandManager;

    @MemberSupport
    public CommandManager act() {
        final var commands = commandManager.getCommandsInSequence();
        final var size = commands.size();
        if (size == 0) {
            return commandManager;
        }
        final var lastReplayable = commands.get(size - 1);
        return commandManager(lastReplayable);
    }

    @MemberSupport
    public String disableAct() {
        final var commands = commandManager.getCommandsInSequence();
        final var size = commands.size();
        if (size == 0) {
            return "Empty";
        }
        final var lastReplayable = commands.get(size - 1);
        return commandManager(lastReplayable).getCommandsInSequence().isEmpty() ? "No more commands" : null;
    }

    private CommandManager commandManager(final ReplayableCommand replayableCommand) {
        final var timestamp = replayableCommand.getTimestamp().toInstant();
        final var baselinePlus5Millis = HasBaseline.addMillis(timestamp, 5);
        return commandManager.withBaseline(baselinePlus5Millis);
    }
}
