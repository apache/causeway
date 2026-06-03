package org.apache.causeway.extensions.commandlog.applib.dom.replay;

import org.apache.causeway.applib.annotation.*;

@Action(
        restrictTo = RestrictTo.PROTOTYPING,
        choicesFrom = "pendingOrFailed",
        semantics = SemanticsOf.NON_IDEMPOTENT,
        commandPublishing = Publishing.DISABLED,
        domainEvent = CommandReplayManager_replayOrRetrySelected.DomainEvent.class,
        executionPublishing = Publishing.DISABLED
)
@ActionLayout(
        associateWith = "pendingOrFailed", sequence = "1.1",
        cssClassFa = "solid circle-play",
        cssClass = "btn-primary",
        describedAs = "Executes the oldest command.")
public class CommandReplayManager_replayOrRetryNext {
    private final CommandReplayManager commandReplayManager;

    public CommandReplayManager_replayOrRetryNext(CommandReplayManager commandReplayManager) {
        this.commandReplayManager = commandReplayManager;
    }

    public class DomainEvent extends CommandReplayManager.ActionDomainEvent<CommandReplayManager_replayOrRetrySelected> {
    }

    @MemberSupport
    public CommandReplayManager act() {
        var nextIfAny = commandReplayManager.streamPendingOrFailed().findFirst();
        // should always be present, due to our guard
        nextIfAny.ifPresent(ReplayableCommand::tryReplayOrRetry);
        return commandReplayManager;
    }

    @MemberSupport
    public String disableAct() {
        return commandReplayManager.sizePendingOrFailed() == 0 ? "No commands in collection" : null;
    }
}
