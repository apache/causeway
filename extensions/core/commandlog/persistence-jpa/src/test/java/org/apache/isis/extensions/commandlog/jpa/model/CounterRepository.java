package org.apache.isis.extensions.commandlog.jpa.model;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CounterRepository
        extends JpaRepository<Counter, Integer>,
        org.apache.isis.extensions.commandlog.applib.integtest.model.CounterRepository<Counter> {

    @Override
    default List<Counter> find() {
        return findAll();
    }

    @Override
    default Counter persist(Counter counter) {
        return save(counter);
    }

    @Override
    default void remove(Counter counter) {
        delete(counter);
    }
}
