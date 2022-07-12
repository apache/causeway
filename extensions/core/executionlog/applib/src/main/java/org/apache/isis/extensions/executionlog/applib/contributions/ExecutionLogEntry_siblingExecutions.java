package org.apache.isis.extensions.executionlog.applib.contributions;

import java.util.List;

import javax.inject.Inject;


import org.apache.isis.applib.annotation.Collection;
import org.apache.isis.extensions.executionlog.applib.IsisModuleExtExecutionLogApplib;
import org.apache.isis.extensions.executionlog.applib.dom.ExecutionLogEntry;
import org.apache.isis.extensions.executionlog.applib.dom.ExecutionLogEntryRepository;

import lombok.RequiredArgsConstructor;
import lombok.val;

@Collection(
        domainEvent = ExecutionLogEntry_siblingExecutions.CollectionDomainEvent.class
)
@RequiredArgsConstructor
public class ExecutionLogEntry_siblingExecutions {

    private final ExecutionLogEntry executionLogEntry;

    public static class CollectionDomainEvent
            extends IsisModuleExtExecutionLogApplib.CollectionDomainEvent<ExecutionLogEntry_siblingExecutions, ExecutionLogEntry> { }


    public List<ExecutionLogEntry> coll() {
        val entries = executionLogEntryRepository.findByInteractionId(executionLogEntry.getInteractionId());
        entries.remove(executionLogEntry);
        return entries;
    }

    @Inject ExecutionLogEntryRepository<ExecutionLogEntry> executionLogEntryRepository;

}
