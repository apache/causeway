package org.apache.isis.testing.fixtures.applib.personas.fixtures;

import org.apache.isis.testing.fixtures.applib.personas.dom.Person;
import org.apache.isis.testing.fixtures.applib.fixturescripts.FixtureScript;


public class ScenarioFixtureScript extends FixtureScript {

    @Override
    protected void execute(ExecutionContext executionContext) {

        // build it ..
        Person steve = Person_persona.SteveSingle.build(this, executionContext);

        // ... look it up
        Person steve2 = Person_persona.SteveSingle.findUsing(serviceRegistry);

    }
}
