package org.apache.causeway.extensions.commandlog.applib.dom.replay;

import lombok.RequiredArgsConstructor;

import org.apache.causeway.applib.annotation.*;

@Action(
        restrictTo = RestrictTo.PROTOTYPING,
        choicesFrom = "pendingOrFailed",
        semantics = SemanticsOf.NON_IDEMPOTENT,
        commandPublishing = Publishing.DISABLED,
        domainEvent = CommandManager_replayOrRetrySelected.DomainEvent.class,
        executionPublishing = Publishing.DISABLED
)
@ActionLayout(
        associateWith = "pendingOrFailed", sequence = "1.1",
        cssClassFa = "solid circle-play",
        cssClass = "btn-primary",
        describedAs = "Executes the oldest command.")
@RequiredArgsConstructor
public class CommandManager_replayOrRetryNext {

    public static class DomainEvent extends CommandManager.ActionDomainEvent<CommandManager_replayOrRetrySelected> { }

    private final CommandManager commandManager;

    @MemberSupport
    public CommandManager act() {
        if (ReplayPendingBackgroundCommands.hasPendingBackgroundCommands(commandManager.replayContext())) {
            return commandManager;
        }

        var nextIfAny = commandManager.streamPendingOrFailed().findFirst();
        // should always be present, due to our guard
        nextIfAny.ifPresent(ReplayableCommand::tryReplayOrRetry);
        return commandManager;
    }

    @MemberSupport
    public String disableAct() {
        var pendingBackgroundCommandsReason = ReplayPendingBackgroundCommands.disableReason(commandManager.replayContext());
        if (pendingBackgroundCommandsReason != null) {
            return pendingBackgroundCommandsReason;
        }
        return commandManager.sizePendingOrFailed() == 0 ? "No commands in collection" : null;
    }
}
