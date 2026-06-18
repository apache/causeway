package org.apache.causeway.extensions.commandlog.applib.dom.replay;

import lombok.RequiredArgsConstructor;

import org.apache.causeway.applib.annotation.*;

import static org.apache.causeway.extensions.commandlog.applib.dom.replay.CommandManagerExport.Direction.*;

@Action(
        semantics = SemanticsOf.SAFE,
        commandPublishing = Publishing.DISABLED,
        domainEvent = CommandManagerExport_nextPage.DomainEvent.class,
        executionPublishing = Publishing.DISABLED
)
@ActionLayout(
        associateWith = "limit", sequence = "2",
        named = "+1 page",
        position = ActionLayout.Position.BELOW,
        describedAs = "Move forward to next page of commands"
)
@RequiredArgsConstructor
public class CommandManagerExport_nextPage {

    public static class DomainEvent extends CommandManagerExport.ActionDomainEvent<CommandManagerExport_nextPage> { }

    private final CommandManagerExport commandExportManager;

    @MemberSupport
    public CommandManagerExport act() {
        final var commands = commandExportManager.commands(NEXT);
        final var size = commands.size();
        if (size == 0) {
            return commandExportManager;
        }
        final var lastReplayable = commands.get(size - 1);
        return commandExportManager(lastReplayable);
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

    private CommandManagerExport commandExportManager(final ReplayableCommand replayableCommand) {
        final var timestamp = replayableCommand.getTimestamp().toInstant();
        final var baselinePlus5Millis = HasBaseline.addMillis(timestamp, 5);
        return commandExportManager.withBaseline(baselinePlus5Millis);
    }
}
