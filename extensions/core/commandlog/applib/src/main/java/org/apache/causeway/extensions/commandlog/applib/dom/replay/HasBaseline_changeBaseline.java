package org.apache.causeway.extensions.commandlog.applib.dom.replay;

import lombok.RequiredArgsConstructor;

import org.apache.causeway.applib.annotation.*;

@Action(
        semantics = SemanticsOf.SAFE,
        commandPublishing = Publishing.DISABLED,
        domainEvent = HasBaseline_changeBaseline.DomainEvent.class,
        executionPublishing = Publishing.DISABLED
)
@ActionLayout(
        associateWith = "baseline", sequence = "2",
        promptStyle = PromptStyle.INLINE_AS_IF_EDIT
)
@RequiredArgsConstructor
public class HasBaseline_changeBaseline {

    public static class DomainEvent extends HasBaseline.ActionDomainEvent<HasBaseline_changeBaseline> { }

    private final HasBaseline hasBaseline;

    @MemberSupport
    public HasBaseline act(final java.sql.Timestamp baseline) {
        return hasBaseline.withBaseline(baseline);
    }

    @MemberSupport
    public java.sql.Timestamp defaultBaseline() {
        return hasBaseline.getBaseline();
    }
}
