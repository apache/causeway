package org.apache.isis.extensions.commandlog.jdo.integtests.model;

import org.springframework.stereotype.Repository;

@Repository
public class CounterRepository
        extends org.apache.isis.extensions.commandlog.applib.integtest.model.CounterRepository<Counter> {

    public CounterRepository() {
        super(Counter.class);
    }
}
