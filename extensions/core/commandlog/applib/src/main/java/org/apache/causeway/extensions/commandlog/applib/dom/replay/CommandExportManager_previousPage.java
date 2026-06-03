package org.apache.causeway.extensions.commandlog.applib.dom.replay;

import lombok.RequiredArgsConstructor;

import org.apache.causeway.applib.annotation.*;

import static org.apache.causeway.extensions.commandlog.applib.dom.replay.CommandExportManager.Direction.*;

@Action(
        semantics = SemanticsOf.SAFE,
        commandPublishing = Publishing.DISABLED,
        domainEvent = CommandExportManager_previousPage.DomainEvent.class,
        executionPublishing = Publishing.DISABLED
)
@ActionLayout(
        associateWith = "baseline", sequence = "4",
        named = "Next Page",
        position = ActionLayout.Position.PANEL,
        describedAs = "Move backwards to previous page of commands"
)
@RequiredArgsConstructor
public class CommandExportManager_previousPage {

    public static class DomainEvent extends HasBaseline.ActionDomainEvent<CommandExportManager_previousPage> { }

    private final CommandExportManager commandExportManager;

    @MemberSupport
    public CommandExportManager act() {
        final var commands = commandExportManager.commands(NEXT);
        final var size = commands.size();
        if (size == 0) {
            return commandExportManager;
        }
        final var firstReplayable = commands.get(0);
        return commandExportManager(firstReplayable);
    }

    @MemberSupport
    public String disableAct() {
        final var commands = commandExportManager.commands(NEXT);
        final var size = commands.size();
        if (size == 0) {
            return "No commands";
        }
        final var firstReplayable = commands.get(0);
        return commandExportManager(firstReplayable).commands(PREVIOUS).isEmpty() ? "No previous commands" : null;
    }

    private CommandExportManager commandExportManager(final ReplayableCommand replayableCommand) {
        final var timestamp = replayableCommand.getTimestamp().toInstant();
        final var baselineMinus5Millis = HasBaseline.addMillis(timestamp, -5);
        return commandExportManager.withBaseline(baselineMinus5Millis);
    }

}
