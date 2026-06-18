package org.apache.causeway.extensions.commandlog.applib.dom.replay;

import lombok.RequiredArgsConstructor;

import org.apache.causeway.applib.annotation.*;

@Action(
        restrictTo = RestrictTo.PROTOTYPING,
        choicesFrom = "pendingOrFailed",
        semantics = SemanticsOf.NON_IDEMPOTENT,
        commandPublishing = Publishing.DISABLED,
        domainEvent = CommandManagerReplay_replayOrRetrySelected.DomainEvent.class,
        executionPublishing = Publishing.DISABLED
)
@ActionLayout(
        associateWith = "pendingOrFailed", sequence = "1.1",
        cssClassFa = "solid circle-play",
        cssClass = "btn-primary",
        describedAs = "Executes the oldest command.")
@RequiredArgsConstructor
public class CommandManagerReplay_replayOrRetryNext {

    public static class DomainEvent extends CommandManagerReplay.ActionDomainEvent<CommandManagerReplay_replayOrRetrySelected> { }

    private final CommandManagerReplay commandReplayManager;

    @MemberSupport
    public CommandManagerReplay act() {
        if (ReplayPendingBackgroundCommands.hasPendingBackgroundCommands(commandReplayManager.replayContext())) {
            return commandReplayManager;
        }

        var nextIfAny = commandReplayManager.streamPendingOrFailed().findFirst();
        // should always be present, due to our guard
        nextIfAny.ifPresent(ReplayableCommand::tryReplayOrRetry);
        return commandReplayManager;
    }

    @MemberSupport
    public String disableAct() {
        var pendingBackgroundCommandsReason = ReplayPendingBackgroundCommands.disableReason(commandReplayManager.replayContext());
        if (pendingBackgroundCommandsReason != null) {
            return pendingBackgroundCommandsReason;
        }
        return commandReplayManager.sizePendingOrFailed() == 0 ? "No commands in collection" : null;
    }
}
