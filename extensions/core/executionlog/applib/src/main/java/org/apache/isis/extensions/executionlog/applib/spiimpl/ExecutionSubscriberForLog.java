package org.apache.isis.extensions.executionlog.applib.spiimpl;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import org.apache.isis.applib.services.iactn.Execution;
import org.apache.isis.applib.services.publishing.spi.ExecutionSubscriber;
import org.apache.isis.extensions.executionlog.applib.dom.ExecutionLogEntryRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ExecutionSubscriberForLog implements ExecutionSubscriber {

    final @Inject ExecutionLogEntryRepository repository;

    @Override
    public void onExecution(Execution<?, ?> execution) {
        repository.createEntryAndPersist(execution);
    }

}
