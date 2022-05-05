package org.apache.isis.testing.fixtures.applib.personas.fixtures;

import java.util.List;

import javax.inject.Inject;

import org.apache.isis.testing.fixtures.applib.fixturescripts.FixtureScript;
import org.apache.isis.testing.fixtures.applib.personas.BuilderScriptWithResult;
import org.apache.isis.testing.fixtures.applib.personas.dom.Customer;
import org.apache.isis.testing.fixtures.applib.personas.dom.CustomerRepository;
import org.apache.isis.testing.fixtures.applib.setup.PersonaEnumPersistAll;

import lombok.RequiredArgsConstructor;
import lombok.val;


public class PersistAllFixtureScript extends FixtureScript {

    @Override
    protected void execute(ExecutionContext executionContext) {

        val persistAll = new PersonaEnumPersistAll<>(Customer_persona.class);

        List<Customer> customers = executionContext.executeChildT(this, persistAll).getObjects();
    }
}
