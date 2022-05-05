package org.apache.isis.applib.fixturescripts.personas.fixtures;

import java.util.List;

import org.apache.isis.applib.fixturescripts.personas.dom.Customer;
import org.apache.isis.testing.fixtures.applib.fixturescripts.FixtureScript;
import org.apache.isis.testing.fixtures.applib.setup.PersonaEnumPersistAll;

import lombok.val;


public class Scenario2FixtureScript extends FixtureScript {

    @Override
    protected void execute(ExecutionContext executionContext) {

        // create them all.
        val persistAll = new PersonaEnumPersistAll<Customer_persona, Customer>(Customer_persona.class);

        List<Customer> customers = executionContext.executeChildT(this, persistAll).getObjects();
    }
}
