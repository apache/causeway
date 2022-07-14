package org.apache.isis.extensions.commandlog.applib.integtest.model;

import java.util.List;

import javax.inject.Inject;

import org.apache.isis.applib.services.repository.RepositoryService;

public abstract class CounterRepository<X extends Counter> {

    private final Class<X> counterClass;

    public CounterRepository(Class<X> counterClass) {
        this.counterClass = counterClass;
    }

    public List<X> find() {
        return repositoryService.allInstances(counterClass);
    }

    public X persist(X counter) {
        return repositoryService.persistAndFlush(counter);
    }

    public void removeAll() {
        repositoryService.removeAll(counterClass);
    }

    @Inject RepositoryService repositoryService;

}
