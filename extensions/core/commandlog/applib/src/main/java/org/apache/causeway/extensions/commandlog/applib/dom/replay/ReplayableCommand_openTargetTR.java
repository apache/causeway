package org.apache.causeway.extensions.commandlog.applib.dom.replay;

import javax.inject.Inject;

import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.ActionLayout;
import org.apache.causeway.applib.annotation.MemberSupport;
import org.apache.causeway.applib.annotation.Publishing;
import org.apache.causeway.applib.annotation.SemanticsOf;
import org.apache.causeway.applib.annotation.Where;
import org.apache.causeway.applib.services.factory.FactoryService;

import lombok.RequiredArgsConstructor;

@Action(
        semantics = SemanticsOf.IDEMPOTENT,
        commandPublishing = Publishing.DISABLED,
        domainEvent = ReplayableCommand_export.DomainEvent.class,
        executionPublishing = Publishing.DISABLED
)
@ActionLayout(
        named = "Open Target",
        hidden = Where.OBJECT_FORMS
)
@RequiredArgsConstructor
public class ReplayableCommand_openTargetTR {

    public static class DomainEvent extends ReplayableCommand.ActionDomainEvent<ReplayableCommand_export> {
    }

    private final ReplayableCommand replayableCommand;

    @MemberSupport
    public Object act(boolean areYouSure) {
        return mixin().act();
    }

    @MemberSupport
    public boolean defaultAreYouSure() {
        return true;
    }

    @MemberSupport
    public String disableAct() {
        return mixin().disableAct();
    }

    private ReplayableCommand_openTarget mixin() {
        return factoryService.mixin(ReplayableCommand_openTarget.class, replayableCommand);
    }

    @Inject private FactoryService factoryService;

}
