package org.apache.isis.extensions.commandlog.applib.integtest.model;

import java.util.List;

import org.springframework.stereotype.Repository;

@Repository
public interface CounterRepository<X extends Counter> {

    List<X> find();

    X persist(X e1);

    void remove(X e1);
}
