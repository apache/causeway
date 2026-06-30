package org.apache.causeway.extensions.commandlog.applib.dom.replay;

import lombok.RequiredArgsConstructor;

import java.util.stream.Collectors;

import org.apache.causeway.applib.annotation.*;

@Action(
        restrictTo = RestrictTo.PROTOTYPING,
        choicesFrom = "pendingOrFailed",
        semantics = SemanticsOf.NON_IDEMPOTENT,
        commandPublishing = Publishing.DISABLED,
        domainEvent = CommandManager_replayOrRetryMultiple.DomainEvent.class,
        executionPublishing = Publishing.DISABLED
)
@ActionLayout(
        associateWith = "pendingOrFailed", sequence = "1.2",
        cssClass = "btn-secondary",
        cssClassFa = "solid forward",
        describedAs = "Executes multiple commands in sequence, each in their own transaction.  Note that 'knownParticipants' is not checked, so review first."
)
@RequiredArgsConstructor
public class CommandManager_replayOrRetryMultiple {

    public static class DomainEvent extends CommandManager.ActionDomainEvent<CommandManager_replayOrRetryMultiple> { }

    private final CommandManager commandManager;


    public enum Limit {
        FIVE(5),
        TEN(10),
        TWENTY(20),
        FORTY(40),
        ALL(Integer.MAX_VALUE),
        ;
        private final int limit;
        Limit(int limit) {
            this.limit = limit;
        }
        public String title() {
            return this == ALL ? "All" : ("" + limit);
        }

        public long limit() {
            return limit;
        }
    }

    @MemberSupport
    public CommandManager act(final Limit limit) {
        if (ReplayPendingBackgroundCommands.hasPendingBackgroundCommands(commandManager.replayContext())) {
            return commandManager;
        }

        var replayables = commandManager.getPendingOrFailed().stream()
                .sorted()
                .limit(limit.limit())
                .collect(Collectors.toList());
        for (var replayableCommand : replayables) {
            var tryReplayOrRetry = replayableCommand.tryReplayOrRetry(); // filtered on its own responsibility
            if (tryReplayOrRetry.isFailure()
                    || ReplayPendingBackgroundCommands.hasPendingBackgroundCommands(commandManager.replayContext())) {
                return commandManager; // stop further execution
            }
        }
        return commandManager;
    }

    @MemberSupport
    public String disableAct() {
        var pendingBackgroundCommandsReason = ReplayPendingBackgroundCommands.disableReason(commandManager.replayContext());
        if (pendingBackgroundCommandsReason != null) {
            return pendingBackgroundCommandsReason;
        }
        return commandManager.getPendingOrFailed().isEmpty() ? "No commands in collection" : null;
    }

    @MemberSupport
    public Limit defaultLimit() {
        return Limit.TEN;
    }
}
