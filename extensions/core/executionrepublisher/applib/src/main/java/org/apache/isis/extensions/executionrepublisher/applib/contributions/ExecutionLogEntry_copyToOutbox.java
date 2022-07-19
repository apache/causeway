package org.apache.isis.extensions.executionrepublisher.applib.contributions;

import javax.inject.Inject;


import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.extensions.executionlog.applib.dom.ExecutionLogEntry;
import org.apache.isis.extensions.executionlog.applib.dom.ExecutionLogEntryType;
import org.apache.isis.extensions.executionoutbox.applib.dom.ExecutionOutboxEntry;
import org.apache.isis.extensions.executionoutbox.applib.dom.ExecutionOutboxEntryRepository;
import org.apache.isis.extensions.executionoutbox.applib.dom.ExecutionOutboxEntryType;
import org.apache.isis.extensions.executionrepublisher.applib.IsisModuleExtExecutionRepublisherApplib;

import lombok.RequiredArgsConstructor;

@Action(
        domainEvent = ExecutionLogEntry_copyToOutbox.ActionDomainEvent.class,
        semantics = SemanticsOf.NON_IDEMPOTENT_ARE_YOU_SURE
)
@ActionLayout(
        position = ActionLayout.Position.PANEL,
        cssClassFa = "share-alt",
        cssClass = "btn-warning"
)
@RequiredArgsConstructor
public class ExecutionLogEntry_copyToOutbox {

    private final ExecutionLogEntry executionLogEntry;


    public static class ActionDomainEvent extends IsisModuleExtExecutionRepublisherApplib.ActionDomainEvent<ExecutionLogEntry_copyToOutbox> { }

    public ExecutionLogEntry act() {

        outboxRepository.upsert(
                executionLogEntry.getInteractionId(),
                executionLogEntry.getSequence(),
                map(executionLogEntry.getExecutionType()),
                executionLogEntry.getTimestamp(),
                executionLogEntry.getUsername(),
                executionLogEntry.getTarget(),
                executionLogEntry.getLogicalMemberIdentifier(),
                executionLogEntry.getInteractionDto()
        );

        return executionLogEntry;
    }

    static ExecutionOutboxEntryType map(ExecutionLogEntryType executionType) {
        return executionType == ExecutionLogEntryType.ACTION_INVOCATION
                ? ExecutionOutboxEntryType.ACTION_INVOCATION
                : ExecutionOutboxEntryType.PROPERTY_EDIT;
    }

    @Inject ExecutionOutboxEntryRepository<? extends ExecutionOutboxEntry> outboxRepository;

}
