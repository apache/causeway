package org.apache.isis.extensions.executionlog.applib.dom;

import java.util.List;
import java.util.UUID;

import org.apache.isis.applib.services.iactn.Execution;
import org.apache.isis.applib.services.publishing.spi.ExecutionSubscriber;

public abstract class ExecutionLogEntryRepository<E extends ExecutionLogEntry> implements ExecutionSubscriber {

    public abstract E createEntryAndPersist(Execution<?, ?> execution);

    public abstract List<E> findByInteractionId(UUID interactionId);
}
