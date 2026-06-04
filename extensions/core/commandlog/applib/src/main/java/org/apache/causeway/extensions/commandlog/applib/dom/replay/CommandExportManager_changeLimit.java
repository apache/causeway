package org.apache.causeway.extensions.commandlog.applib.dom.replay;

import lombok.RequiredArgsConstructor;

import org.apache.causeway.applib.annotation.*;

@Action(
        semantics = SemanticsOf.SAFE,
        commandPublishing = Publishing.DISABLED,
        domainEvent = CommandExportManager_changeLimit.DomainEvent.class,
        executionPublishing = Publishing.DISABLED
)
@ActionLayout(
        associateWith = "limit", sequence = "2",
        promptStyle = PromptStyle.INLINE_AS_IF_EDIT
)
@RequiredArgsConstructor
public class CommandExportManager_changeLimit {

    public static int MAX_LIMIT = 100;

    public static class DomainEvent extends CommandExportManager.ActionDomainEvent<CommandExportManager_changeLimit> { }

    private final CommandExportManager commandExportManager;

    @MemberSupport
    public CommandExportManager act(int newLimit) {
        return commandExportManager.withLimit(newLimit);
    }


    @MemberSupport
    public int defaultNewLimit() {
        return commandExportManager.getLimit();
    }

    @MemberSupport
    public String validateNewLimit(int newLimit) {
        if(newLimit <= 0 || newLimit > MAX_LIMIT) {
            return "Limit must be a in range [1, " + MAX_LIMIT + "].";
        }
        return null;
    }
}
