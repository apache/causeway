package org.apache.causeway.extensions.commandlog.applib.dom.replay;

import lombok.RequiredArgsConstructor;

import org.apache.causeway.applib.annotation.*;

import static org.apache.causeway.extensions.commandlog.applib.dom.replay.CommandManagerExport.Direction.*;

@Action(
        semantics = SemanticsOf.SAFE,
        commandPublishing = Publishing.DISABLED,
        domainEvent = CommandManagerExport_previousPage.DomainEvent.class,
        executionPublishing = Publishing.DISABLED
)
@ActionLayout(
        associateWith = "limit", sequence = "1",
        named = "-1 page",
        position = ActionLayout.Position.BELOW,
        describedAs = "Move backwards to previous page of commands"
)
@RequiredArgsConstructor
public class CommandManagerExport_previousPage {

    public static class DomainEvent extends CommandManagerExport.ActionDomainEvent<CommandManagerExport_previousPage> { }

    private final CommandManagerExport commandExportManager;

    @MemberSupport
    public CommandManagerExport act() {
        final var commands = commandExportManager.commands(PREVIOUS);   // returns descending, latest (youngest) first
        final var size = commands.size();
        if (size == 0) {
            return commandExportManager;
        }
        final var earliestReplayable = commands.get(size - 1);
        return commandExportManager(earliestReplayable);
    }

    @MemberSupport
    public String disableAct() {
        final var commands = commandExportManager.commands(PREVIOUS);
        final var size = commands.size();
        return size == 0 ? "No commands" : null;
    }

    private CommandManagerExport commandExportManager(final ReplayableCommand replayableCommand) {
        final var timestamp = replayableCommand.getTimestamp().toInstant();
        final var baselineMinus5Millis = HasBaseline.addMillis(timestamp, -5);
        return commandExportManager.withBaseline(baselineMinus5Millis);
    }

}
