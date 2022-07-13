package org.apache.isis.extensions.commandlog.jdo.model;

import java.util.List;

import javax.inject.Inject;

import org.springframework.stereotype.Repository;

import org.apache.isis.applib.services.repository.RepositoryService;

@Repository
public class CounterRepository
        implements org.apache.isis.extensions.commandlog.applib.integtest.model.CounterRepository<Counter> {

    final RepositoryService repositoryService;

    @Inject
    public CounterRepository( RepositoryService repositoryService) {
        this.repositoryService = repositoryService;
    }

    @Override
    public List<Counter> find() {
        return repositoryService.allInstances(Counter.class);
    }

    @Override
    public Counter persist(Counter counter) {
        return repositoryService.persist(counter);
    }

    @Override
    public void remove(Counter counter) {
        repositoryService.remove(counter);
    }
}
