package org.apache.isis.extensions.executionlog.applib.contributions;

import java.util.List;

import javax.inject.Inject;

import org.apache.isis.applib.annotation.Collection;
import org.apache.isis.applib.mixins.system.HasInteractionId;
import org.apache.isis.extensions.executionlog.applib.IsisModuleExtExecutionLogApplib;
import org.apache.isis.extensions.executionlog.applib.dom.ExecutionLogEntry;
import org.apache.isis.extensions.executionlog.applib.dom.ExecutionLogEntryRepository;

import lombok.RequiredArgsConstructor;

@Collection(
        domainEvent = HasInteractionId_executionLogEntries.CollectionDomainEvent.class
)
@RequiredArgsConstructor
public class HasInteractionId_executionLogEntries {

    private final HasInteractionId hasInteractionId;

    public static class CollectionDomainEvent
            extends IsisModuleExtExecutionLogApplib.CollectionDomainEvent<HasInteractionId_executionLogEntries, ExecutionLogEntry> { }


    public List<ExecutionLogEntry> act() {
        return executionLogEntryRepository.findByInteractionId(hasInteractionId.getInteractionId());
    }

    @Inject ExecutionLogEntryRepository<ExecutionLogEntry> executionLogEntryRepository;

}
