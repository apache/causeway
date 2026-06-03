package org.apache.causeway.extensions.commandlog.applib.dom.replay;

import lombok.RequiredArgsConstructor;

import org.apache.causeway.applib.annotation.*;

@Action(
        semantics = SemanticsOf.SAFE,
        commandPublishing = Publishing.DISABLED,
        domainEvent = HasBaseline_previousHour.DomainEvent.class,
        executionPublishing = Publishing.DISABLED
)
@ActionLayout(
        associateWith = "baseline", sequence = "1",
        named = "Previous",
        position = ActionLayout.Position.PANEL,
        describedAs = "Move back one hour"
)
@RequiredArgsConstructor
public class HasBaseline_previousHour {
    public static class DomainEvent extends HasBaseline.ActionDomainEvent<HasBaseline_previousHour> {
    }

    private final HasBaseline hasBaseline;

    @MemberSupport
    public HasBaseline act() {
        final var baseline = HasBaseline.Util.addSeconds(hasBaseline.getBaseline(), -3600);
        return hasBaseline.withBaseline(baseline);
    }
}
