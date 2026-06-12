package org.apache.causeway.extensions.commandlog.applib.dom.replay;

import lombok.RequiredArgsConstructor;

import org.apache.causeway.applib.annotation.*;

@Action(
        semantics = SemanticsOf.SAFE,
        commandPublishing = Publishing.DISABLED,
        domainEvent = HasBaseline_nextHour.DomainEvent.class,
        executionPublishing = Publishing.DISABLED
)
@ActionLayout(
        associateWith = "baseline", sequence = "3",
        named = "+1 hour",
        position = ActionLayout.Position.PANEL,
        describedAs = "Move forward one hour"
)
@RequiredArgsConstructor
public class HasBaseline_nextHour {

    public static class DomainEvent extends HasBaseline.ActionDomainEvent<HasBaseline_nextHour> { }

    private final HasBaseline hasBaseline;

    @MemberSupport
    public HasBaseline act() {
        final var baseline = HasBaseline.addSeconds(hasBaseline.getBaseline(), +3600);
        return hasBaseline.withBaseline(baseline);
    }
}
