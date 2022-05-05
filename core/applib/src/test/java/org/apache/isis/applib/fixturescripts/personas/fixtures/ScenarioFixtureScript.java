package org.apache.isis.applib.fixturescripts.personas.fixtures;

import java.util.List;

import org.apache.isis.applib.fixturescripts.personas.dom.Customer;
import org.apache.isis.testing.fixtures.applib.fixturescripts.FixtureScript;
import org.apache.isis.testing.fixtures.applib.setup.PersonaEnumPersistAll;

import lombok.val;


public class ScenarioFixtureScript extends FixtureScript {

    @Override
    protected void execute(ExecutionContext executionContext) {

        // build it ..
        Customer steve = Customer_persona.SteveSingle.build(this, executionContext);

        // ... look it up
        Customer steve2 = Customer_persona.SteveSingle.findUsing(serviceRegistry);

    }
}
