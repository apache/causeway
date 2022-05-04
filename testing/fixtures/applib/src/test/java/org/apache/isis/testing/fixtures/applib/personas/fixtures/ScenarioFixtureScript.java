package org.apache.isis.testing.fixtures.applib.personas.fixtures;

import javax.inject.Inject;

import org.apache.isis.testing.fixtures.applib.fixturescripts.FixtureScript;
import org.apache.isis.testing.fixtures.applib.personas.BuilderScriptWithResult;
import org.apache.isis.testing.fixtures.applib.personas.dom.Customer;
import org.apache.isis.testing.fixtures.applib.personas.dom.CustomerRepository;

import lombok.RequiredArgsConstructor;


public class ScenarioFixtureScript extends FixtureScript {

    @Override
    protected void execute(ExecutionContext executionContext) {

        // build it ..
        Customer steve = Customer_persona.SteveSingle.build(this, executionContext);

        // ... look it up
        Customer steve2 = Customer_persona.SteveSingle.findUsing(serviceRegistry);
    }
}
