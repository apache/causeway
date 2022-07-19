package org.apache.isis.extensions.executionoutbox.jpa.integtests.model;

import org.springframework.stereotype.Repository;

@Repository
public class CounterRepository
        extends org.apache.isis.extensions.executionoutbox.applib.integtest.model.CounterRepository<Counter> {

    public CounterRepository() {
        super(Counter.class);
    }

}
