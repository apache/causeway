package org.apache.isis.applib.fixturescripts.personas.fixtures;

import java.util.List;

import org.apache.isis.applib.fixturescripts.personas.dom.Person;
import org.apache.isis.testing.fixtures.applib.fixturescripts.FixtureScript;
import org.apache.isis.testing.fixtures.applib.setup.PersonaEnumPersistAll;

import lombok.val;


public class PersistAllFixtureScript extends FixtureScript {

    @Override
    protected void execute(ExecutionContext executionContext) {

        // create them all.
        val persistAll = new PersonaEnumPersistAll<Person_persona, Person>(Person_persona.class);

        List<Person> people = executionContext.executeChildT(this, persistAll).getObjects();
    }
}
